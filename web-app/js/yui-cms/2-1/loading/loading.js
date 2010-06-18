/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.0
*/
(function() {

    var $B = YAHOO.Bubbling,
  	    $L = YAHOO.lang,
	    $E = YAHOO.util.Event,
	    $D = YAHOO.util.Dom,
	    $ =  YAHOO.util.Dom.get;

	/**
	* @class Loading
	* Loading mask...
	* @constructor
	*/
	YAHOO.widget.Loading = function() {
		var obj = {},
			_handle = 'yui-cms-loading',
			_content = 'yui-cms-float',
			_visible = false,
			_ready = false,
			_first = true,
			_timer = null,
			_backup = {},
			_defStyle = {
							zIndex: 10000,
							left: 0,
							top: 0,
							margin: 0,
							padding: 0,
							opacity: 0,
							overflow: 'hidden',
							visibility: 'visible',
							position: 'absolute',
							display: 'block'
						 };
			_defConf = {
							autodismissdelay: 0,
							opacity: 1,
							closeOnDOMReady: false,
							closeOnLoad: true,
							close: false,
							effect: false,
							simple: false,
							fullscreen: false
						 };
		// private stuff
		function _onHide () {
		  if ($L.isObject(obj.element)) {
		    $D.setStyle(obj.element, 'opacity', 0);
		    $D.setStyle(obj.element, 'display', 'none');
		  }
		 // obj.counter = 0;
		}
		function _onShow () {
		  if ($L.isObject(obj.element)) {
		    $D.setStyle(obj.element, 'opacity', _defConf.opacity);
		  }
		}
		// pasive behavior...
	    var actionControl = function (layer, args) {
	      if (_visible && $L.isObject(obj.element) && ((obj.element === args[1].target) || $D.isAncestor (obj.element, args[1].target))) {
	      	// the user try to do something...
	      	// alert...
	      	if (window.confirm ('Do you want to hide the loading mask?')) {
	      	    // closing the mask...
	      	    obj.hide ();
	      	}
		  }
	    };
	    $B.on('navigate', actionControl);
	    $B.on('property', actionControl);
		// public vars
		obj.counter = 0;
		obj.element = null;
		obj.content = null;
		obj.proxy = null;
		obj.anim = null;
		// public methods
		obj.config = function ( userConfig ) {
			userConfig = userConfig || {};
			$L.augmentObject(_defConf, userConfig, true);
			// adjusting the mask...
			if (this.element && _visible) {
				_onShow ();
			}
		};
		obj.backup = function () {
		    var el = document.body;
		    _backup.padding = $D.getStyle(el, 'padding');
		    _backup.margin = $D.getStyle(el, 'margin');
		    _backup.overflow = $D.getStyle(el, 'overflow');
		};
		obj.restore = function () {
	       var el = document.body, 
			   p = document.documentElement;
		   // the restoration trick: from overflow hidden to overflow auto, to height auto and to height body.size
	       $D.setStyle(el, 'padding', _backup.padding);
	       $D.setStyle(el, 'padding', _backup.padding);
	       $D.setStyle(el, 'overflow', _backup.overflow);
		   $D.setStyle(el, 'height', 'auto');
		   $D.setStyle(el, 'width',  'auto');
	       $D.setStyle(p,  'overflow', 'auto');
		   $D.setStyle(p,  'height', 'auto');
		   $D.setStyle(p,  'width',  'auto');
		   var h = Math.max($D.getViewportHeight (), el.offsetHeight)+'px';
		   $D.setStyle(el, 'height', h);
		   $D.setStyle(p,  'height', h);
		   // using setTimeout to restore the body height to auto, the trick to avoid the flash issue
		   window.setTimeout(function(){ 
                $D.setStyle(el, 'height', 'auto');
		  		$D.setStyle(p,  'height', 'auto');
           }, 1);
		};
		obj.init = function () {
		    var item;
		    this.element = $(_handle);
		    this.content = $(_content);
			if (!_ready && ($L.isObject(this.element))) {
				_ready = true;
        		for (item in _defStyle) {
        			if (_defStyle.hasOwnProperty(item)) {
        				$D.setStyle(this.element, item, _defStyle[item]);
        			}
        		}
        		obj.show ();
			}
		};
		obj.adjust = function () {
			var vp;
			if (!_defConf.fullscreen && this.proxy && $D.inDocument(this.proxy)) {
				// cover a certain element in the DOM
				vp = $D.getRegion (this.proxy);
				vp.height = vp.bottom-vp.top;vp.width = vp.right-vp.left;
			} else {
				// cover the full viewport
				// getting the current viewport position and dimentions
				vp = {top: $D.getDocumentScrollTop(), left: $D.getDocumentScrollLeft(), width:$D.getViewportWidth (), height: $D.getViewportHeight ()};
			}
			if (_visible) {
			   $D.setStyle(this.element, 'height', vp.height+'px');
	           $D.setStyle(this.element, 'width', vp.width+'px');
		       $D.setXY(this.element, [vp.left, vp.top]);
	           // adjust the content...
	           if (this.content) {
	              // applying the corresponding position: centering the content...
	              var size = $D.getRegion(this.content);
                  var oHeight = size.bottom - size.top;
                  var oWidth = size.right - size.left;
		          $D.setXY(this.content, [vp.left + ((vp.width - oWidth) / 2), vp.top + ((vp.height - oHeight) / 2)]);
	           }
		    }
		};
		obj.show = function (el) {
		  if (this.element && !_visible) {
		    _visible = true;
			this.backup ();
			this.proxy = el; // use the mask over a certain element
			if (!this.proxy || _defConf.fullscreen) {
				// cover the full viewport
		        $D.setStyle(document.documentElement, 'overflow', 'hidden');
				$D.setStyle(document.body, 'overflow', 'hidden'); // contrain the viewport to the actual size (prevent the scrolling)...
			}
			$D.setStyle(this.element, 'display', 'block');
			if (_first) {
			  $B.on('repaint', obj.adjust, obj, true);
		    }
			obj.adjust ();
			// applying the effects if needed...
			if (_defConf.effect && !_first) {
                // stopping the current animation
                if ((this.anim) && (this.anim.isAnimated())) { this.anim.stop(); }
    	        // starting a new animation
        		this.anim = new YAHOO.util.Anim(this.element, {opacity: { to: _defConf.opacity} }, 1.5, YAHOO.util.Easing.easeIn);
        		this.anim.onComplete.subscribe(_onShow);
        		this.anim.animate();
		    } else {
			    _onShow();
		    }
		    if (_defConf.closeOnDOMReady) {
    		    $E.onDOMReady ( obj.hide, obj, true );
		    }
		    if (_defConf.closeOnLoad) {
    		    $E.on(window, 'load', obj.hide, obj, true );
		    }
			window.clearTimeout(_timer);_timer=null;
			if ($L.isNumber(_defConf.autodismissdelay) && (_defConf.autodismissdelay > 0)) {
			  _timer = window.setTimeout(function() {
				obj.hide();
			  }, Math.abs(_defConf.autodismissdelay));
			}
		  }
		};
		obj.hide = function () {
		  if (this.element && _visible) {
			// applying the effects if needed...
			_visible = false;
			if (_defConf.effect) {
                // stopping the current animation
                if ((this.anim) && (this.anim.isAnimated())) { this.anim.stop(); }
    	         // starting a new animation
        		this.anim = new YAHOO.util.Anim(this.element, { opacity: { to: 0} }, 1.5, YAHOO.util.Easing.easeOut);
        		this.anim.onComplete.subscribe(_onHide);
        		this.anim.animate();
		    } else {
			    _onHide();
		    }
			obj.counter = 0;
			// restoring everything...
			obj.restore();
			// only the first time
			if (_first) {
				_first = false;
				YAHOO.Bubbling.fire ('onMaskReady', {
					element: obj.element,
					content: obj.content,
					config: _defConf
				});
			}
		  }
		};
		if ($D.inDocument(_handle)) {
            // this script was loaded after the element
            obj.init();
        } else {
    		$E.onContentReady ( _handle, obj.init, obj, true );
    	}
		// getting the default configuration...
		if ($L.isObject(YAHOO.widget._cLoading)) {
			obj.config(YAHOO.widget._cLoading);
		}
		if (!_defConf.simple) {
			$B.on('onAsyncRequestStart', function(layer, args) {
				obj.counter++;
				obj.show(args[1].element);
			});
			$B.on('onAsyncRequestEnd', function(layer, args) {
				obj.counter--;
				if (obj.counter <= 0) {
					obj.hide();
					obj.counter = 0;
				}
			});
		}
		return obj;
	}();
})();
YAHOO.register("loading", YAHOO.widget.Loading, {version: "2.0", build: "218"});