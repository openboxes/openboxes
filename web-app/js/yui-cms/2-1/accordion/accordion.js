/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.1
*/
(function() {

    var $B = YAHOO.Bubbling,
		$L = YAHOO.lang,
        $E = YAHOO.util.Event,
	    $D = YAHOO.util.Dom;

	/**
	* @singleton Accordion Manager - Creating accordion controls based on the markup.
	* Apply visual enhanced to an area
	* @constructor
	*/
	YAHOO.widget.AccordionManager = function() {
		var obj = {},
		    _selector = 'selected',
		    _sliding = 'sliding',
		    _anims = {};

		// on click action behaviors...
		
		$B.on('navigate', function (layer, args) {
			var el = $B.getOwnerByClassName (args[1].target, 'trigger');
            if (!args[1].flagged && el) {
                // switching the slidable area and reclaiming the behavior
                if (obj.toggle(args[1].target)) {
					args[1].stop = true;
					args[1].flagged = true;
				}
            }
        });
		
		$B.addDefaultAction('accordionToggleItem', function (layer, args) {
            if (!args[1].flagged) {
                // switching the slidable area and reclaiming the behavior
                return obj.toggle(args[1].target);
            }
        });
		$B.addDefaultAction('accordionRemoveItem', function (layer, args) {
            if (!args[1].flagged) {
                // removing an item from the accordion and reclaiming the behavior
                return obj.remove(args[1].target);
            }
        });

        // on event arrive
        // behaviors for the second accordion (ADVANCED AJAX APPLICATION)
        // Tagline: One behavior that will rule them all (based on the slide's rel attribute)
        $B.on('accordionOpenItem', function (layer, args) {
          var reLink = /.*#/;
          // if can be loaded thru AJAX
          if ($D.hasClass(args[1].slide, 'ajax') && $L.isObject(YAHOO.plugin.Dispatcher)) {
            var trigger = $D.getElementsByClassName('accordionToggleItem','*',args[1].el);
            trigger = (trigger.length>0?trigger[0]:null);
            if (trigger && (trigger = trigger.getAttribute('href',2))) {
                YAHOO.plugin.Dispatcher.fetch ( args[1].slide, trigger.replace(reLink,''), {
                    onLoad: function (el) {
						// forcing the accordion item to expand to the correct height when the content is loaded using ajax
						$D.setStyle(el,"height",el.scrollHeight+"px");
						
						
						// TODO: apply the animation instead to apply the height/width directly
						
						
						// each accordion item can have his own cache definition, applying the class "nocache" to the BD element,
						// the accordion will reload the content everytime the accordion is opened...
						if (!$D.hasClass(el, 'nocache')) {
                        	$D.removeClass(el, 'ajax');
						}						
                    }
                });
            }
          }
        });

        // on keyboad action behavior... (only works if the target element is within a trigger/class)
        $B.on('key', function (layer, args) {
    	  var o = args[1], item = null, result = false, el;
    	  if (!o.flagged && (o.type == 'keyup') && (el = $B.getOwnerByClassName (o.target, 'trigger'))) {
			  if (((o.keyCode === 39) && obj.open (el)) ||
    	          ((o.keyCode === 37) && obj.close (el))) { // Shortcut: cursor -> or cursor <-
        		  // reclaiming the event & stoping the event propagation
        	      o.flagged = true;
        	      o.stop = true;
        	  }
    	  }
        });

        // on rollover action behavior...
		$B.on('rollover', function (layer, args) {
		  var list, item, onWayOut;
		  if (item = _getItem(args[1].target)) {
		    if ((list = _getList(item)) && list.rollover) {
				if (!list.selected) {
		            $D.addClass(list.el, _selector);
					onWayOut = function (e) {
					    var l = _getList ({el:$E.getTarget(e)});
					    if (l && !$B.virtualTarget(e, l.el) && !l.persistent) {
					      _reset(l, {force:true});
					    }
					};
					if (!list.persistent) {
					    $E.removeListener ( list.el, 'mouseout', onWayOut );
						$E.addListener ( list.el, 'mouseout', onWayOut, obj, true );
					}
				}
				if (!item.selected) {
					// is over a new item...
					_openItem(item, list);
				}
			}
		  }
	    });

        // creating the most common message (behavior layer)
        $B.addLayer (['accordionOpenItem', 'accordionCloseItem', 'accordionRemoveItem'], obj);

        function _getEffect ( el ) {
            var effect = el.getAttribute('rel') || null;
            if (effect) {
              effect = YAHOO.util.Easing[effect] || null;
            }
            return effect;
        }
        function _getTimer ( el ) {
            var t = ($D.hasClass(el, 'fast')?0.1:null) || ($D.hasClass(el, 'slow')?0.6:null) || 0.4;
            return t;
        }
        function _getItem ( elem ) {
          if (elem && ($L.isObject(elem) || (elem = $D.get (elem)))) {
            var item, el = $B.getOwnerByClassName (elem, 'yui-cms-item');
            if ($L.isObject(el)) {
                item = {
                    el: el,
                    triger: elem,
                    selected: $D.hasClass(el, _selector),
                    sliding: $D.hasClass(el, _sliding),
                    size: {width:0, Height: 0}
                };
                // getting the slidable element
                var slide = $D.getElementsByClassName('bd','*',el);
				// if there is no Body element within the item, we should try to find the next BD: Semantic Markups DL\DT\DD
                slide = (slide.length>0?slide[0]:$D.getNextSiblingBy(el, function(node){return $D.hasClass(node, 'bd');}));
                if (item.slide = slide) {
	                var h = parseInt($D.getStyle(slide, 'height'), 10);
	                var w = parseInt($D.getStyle(slide, 'width'), 10);
	                // forcing to number... to avoid misbehavior on "auto" height/width...
	                if (!$L.isNumber(h)) {
	                    $D.setStyle(slide, 'height', slide.scrollHeight+'px');
	                }
	                if (!$L.isNumber(w)) {
	                    $D.setStyle(slide, 'width', slide.scrollWidth+'px');
	                }
	                item.size.height = slide.scrollHeight;
	                item.size.width = slide.scrollWidth;
				} else {
					return false;
				}
            }
            return item;
          }
        }
        function _getList ( item ) {
          var list = null, el = item.el;
          if (el && ($L.isObject(el) || (el = $D.get (el)))) {
            if (el = $B.getOwnerByClassName (el, 'yui-cms-accordion')) {
                // creating the list literal based on the classnames defined for the accordion wrapper
                list = {
                    el: el,
                    effect: _getEffect(el),
                    orientation: ($D.hasClass(el, 'vertical')?'width':'height'),
                    selected: $D.hasClass(el, _selector),
                    fade: $D.hasClass(el, 'fade'),
                    manually: $D.hasClass(el, 'manually'),
                    fixIE: ($E.isIE && $D.hasClass(el, 'fixIE')), // hack for IE and quirk mode...
                    multiple: $D.hasClass(el, 'multiple'),
                    rollover: $D.hasClass(el, 'rollover'),
                    persistent: $D.hasClass(el, 'persistent'),
                    dispatcher: $D.hasClass(el, 'dispatcher'),
                    wizard: $D.hasClass(el, 'wizard'),
                    timer: _getTimer(el),
                    items: []
                };
                // searching for items childs...
                $D.batch ($D.getElementsByClassName('bd','*',el), function(elem){
                    // adding an item to the list
                    list.items.push (_getItem(elem));
                });
            }
            return list;
          }
        }
        function _reset ( list, params ) {
            params = params || {};
            var conf = [], i,
                force = params.force || false,
                item = params.item || null;
            if (list) {
              if (!list.multiple || force) {
	            // closing all the selected items
		        for (i=0; i<list.items.length; i++) {
		          // is the element is not equal to item, or if the item is under an animation...
				  if ((!item || (list.items[i].el !== item.el)) && (list.items[i].selected || list.items[i].sliding || params.expand)) {
				     if (params.expand) {
				       _openItem (list.items[i], list, params.grouping);
				     } else {
				       _closeItem (list.items[i], list, params.grouping);
				       if (params.mirror) {
				          // hack for get the mirror element in persistent and !mutiples accordions
				          params.mirror.push(list.items[i]);
				       }
				     }
			      }
	            }
	          }
	        }
        }
        function _openItem ( item, list ) {
            var conf = [], anim, i, g = [], m = [], fs, onFinish;
            if (list || (list = _getList (item))) {
              // if the item is not already opened
              if (!item.selected) {
                  // closing all the selected items if neccesary
                  if (!list.multiple) {
                    _reset ( list, {item: item, grouping: g, mirror: m} );
                  }
    	          // if the animation is underway: we need to stop it...
                  anim = _anims[$E.generateId(item.slide)];
                  if ((anim) && (anim.isAnimated())) {anim.stop();}
    	          // opening the selected element, based on the list's orientation, timer and effect attribute...
    	          conf[list.orientation] = {to: item.size[list.orientation]};
    	          // scrolling effect
    	          if (!list.manually) {
    	            conf['scroll'] = {from: (list.orientation=='width'?[item.size[list.orientation],0]:[0,item.size[list.orientation]]), to: [0,0]};
    	          }
    	          if (list.fade) { // appliying fadeIn
    	            conf['opacity'] = {to: 1};
    	          }
    	          anim = new YAHOO.util.Scroll(item.slide, conf, list.timer, list.effect);
            	  $D.addClass(item.el, _sliding);
            	  onFinish = function() {
            		  $D.removeClass(item.el, _sliding);
            		  $D.addClass(item.el, _selector);
            		  // broadcasting the corresponding event (open)...
            		  $B.fire ('accordionOpenItem', item);
            	  };
            	  anim.onComplete.subscribe(onFinish);
            	  _anims[$E.generateId(item.slide)] = anim;
            	  if (list.manually) {
            	    // manually animation...
            	    m = m[0] || null;
            	    // getting the desired dimension from the mirror or from the current item
            	    fs = (m?m.size[list.orientation]:item.size[list.orientation]);
            	    for (i=1;i<=fs;i++){
            	        if (m) {
            	          $D.setStyle (m.slide, list.orientation, (fs-i)+'px');
            	        }
            	        $D.setStyle (item.slide, list.orientation, i+'px');
            	    }
            	    onFinish();
            	  } else {
            	    // creating an animation thread
            	    for (i=0; i<g.length; i++) {
            	      YAHOO.util.AnimMgr.registerElement(g[i]);
            	    }
            	    YAHOO.util.AnimMgr.registerElement(anim);
            	  }
              }
        	  return true;
	        }
	        return false;
        }
        function _closeItem ( item, list, grouping ) {
            var conf = [], anim, fs;
            if (item && (list || (list = _getList (item)))) {
	            // closing the item, based on the list's orientation, timer and effect attribute...
	            conf[list.orientation] = {to: ((list.orientation=='width'||list.fixIE)?1:0)}; // hack for vertical accordion issue on Safari and Opera
	            if (list.fade) { // appliying fadeIn
	              conf['opacity'] = {to: 0};
	            }
    	        // scrolling effect
    	        if (!list.manually) {
    	          conf['scroll'] = {to: (list.orientation=='width'?[item.size[list.orientation],0]:[0,item.size[list.orientation]])};
    	        }
                // if the animation is underway: we need to stop it...
                anim = _anims[$E.generateId(item.slide)];
                if ((anim) && (anim.isAnimated())) {anim.stop();}
        	    anim = new YAHOO.util.Scroll(item.slide, conf, list.timer, list.effect);
        		$D.addClass(item.el, _sliding);
            	onFinish = function() {
                    $D.removeClass(item.el, _sliding);
        		    $D.removeClass(item.el, _selector);
            		// broadcasting the corresponding event (close)...
            		$B.fire ('accordionCloseItem', item);
            	};
        		anim.onComplete.subscribe(onFinish);
        		if ($L.isArray(grouping)) {
        		    grouping.push(anim);
        	    } else {
        		    anim.animate();
        		}
        		if (list.manually) {
        	        // animation manually
        	        fs = item.size[list.orientation];
            	    for (i=fs;i>=conf[list.orientation].to;i--){
            	        $D.setStyle (item.slide, list.orientation, i+'px');
            	    }
            	    onFinish();
        		}
        		_anims[$E.generateId(item.slide)] = anim;
        		return true;
	        }
	        return false;
        }
        function _removeItem ( item, list ) {
            if (item && (list || (list = _getList (item)))) {
                // closing element
                _closeItem (item, list);
                // removing listeners...
    			$E.purgeElement ( item.el, true );
    			// hack, removing the element after close it...
    			window.setTimeout (function(){
    			      item.el.parentNode.removeChild(item.el);
    			      $B.fire ('accordionRemoveItem', item);
    			   }, list.timer+0.1);
        		return true;
	        }
	        return false;
        }

		// public vars
		// public methods
		/**
		* * Expanding all the elements in the accordion...
		* @public
		* @param {object} el   DOM reference
		* @return boolean
		*/
		obj.expand = function ( el ) {
		    var list;
		    if (list = _getList ({el:el})) {
		        return _reset (list, {force:true, expand:true});
		    }
		};
		/**
		* * Collapsing all the elements in the accordion...
		* @public
		* @param {object} el   DOM reference
		* @return boolean
		*/
		obj.collapse = function ( el ) {
		    var list;
		    if (list = _getList ({el:el})) {
		        return _reset (list, {force:true});
		    }
		};
		/**
		* * Open a certain item inside an area...
		* @public
		* @param {object} el   DOM reference
		* @return boolean
		*/
		obj.open = function ( el ) {
		    var item;
		    if (item = _getItem(el)) {
		        return _openItem (item);
		    }
		};
		/**
		* * Close a certain item inside an area...
		* @public
		* @param {object} el   DOM reference
		* @return boolean
		*/
		obj.close = function ( el ) {
		    var item, list;
		    if (item = _getItem(el)) {
		        if (list = _getList (item)) {
		          // if the item is already opened, and is multiple and not persistent
		          return ((item.selected && (list.multiple || !list.persistent))?_closeItem (item, list):false);
		        }
		    }
		};
		/**
		* * toggle a certain item inside an area...
		* @public
		* @param {object} el   DOM reference
		* @return boolean
		*/
		obj.toggle = function ( el ) {
		    var item, list;
		    if (item = _getItem(el)) {
				if (list = _getList (item)) {
				  // if the item is already opened, and is multiple and not persistent
		          return ((item.selected && (list.multiple || !list.persistent))?_closeItem (item, list):_openItem (item, list));
		        }
		    }
		};
		/**
		* * remove a certain item from the area...
		* @public
		* @param {object} el   DOM reference
		* @return boolean
		*/
		obj.remove = function ( el ) {
		    var item, list;
		    if (item = _getItem(el)) {
		        if (list = _getList (item)) {
		          return _removeItem (item, list);
		        }
		    }
		};
		return obj;
	}();
})();
YAHOO.register("accordion", YAHOO.widget.AccordionManager, {version: "2.1", build: "211"});