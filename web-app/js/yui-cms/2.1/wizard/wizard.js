/*
Copyright (c) 2008, Caridy Patino. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.1
*/
(function() {
  var $L = YAHOO.lang,
	  $C = YAHOO.util.Connect,
	  $E = YAHOO.util.Event,
	  $D = YAHOO.util.Dom,
	  $ =  YAHOO.util.Dom.get,
	  $DP = YAHOO.plugin.Dispatcher;

	/**
	* @singleton WizardManager - Use this object to management your wizards...
	* Apply dynamic functionality to a wizard widget
	* @constructor
	*/
    YAHOO.plugin.WizardManager = function() {
		var obj = {},
			_area = null,
			_areas = {},
			_className = 'yui-cms-wizard',
			_formClass = 'yui-cms-form',
			_dynamic   = 'dynamic',
			_loadingClass = 'loading',
			_form_control = 'yuicmswizard',
			_defConf   = {
							status: false,
							handle: null,
							done: false,
							uri: null,
							moreGetData: {},
							morePostData: {},
							onCancel: null,
							onReady: null,
							onFinish: null,
							onSubmit: null,
							values: {}
						 };

		var callback = {
			success: function(o) {
				var area = obj._area = o.argument.area;
				// process the response here & compiling the result using the dispatcher...
				$DP.process ( area.element, o.responseText, {
					onStart: function(){
					},
					error: function() {
						onError ( area );
				    },
					onLoad: function(){
						// process the area
						$D.removeClass(area.element, _loadingClass);
						// looking for important moments in the execution thread
						if (area.values._wizardfinish || area.done) {
							onFinish(area);
						}
						else if (area.values._wizardcancel) {
							onCancel(area);
						}
						check ( area );
					},
					firewall: area.firewall
				}, true);
				obj._area = null;
			},
			failure: function(o) {
				onError ( o.argument.area );
			}
		};
		function fetch( config, m, uri, c, data ) {
			// set the loading class to the config...
			$D.addClass(config.element, _loadingClass);
			uri = $DP.firewall (uri, config);
			if ($L.isFunction(config.onStart)) {
				config.onStart.apply ( config, [config.values] );
			    config.onStart = null; // because the process method will try to executed again...
			}
			// broadcasting the message
			if (YAHOO.Bubbling) {
				 YAHOO.Bubbling.fire ('onAsyncRequestStart', {
					element: config.element
				 });
			}
			config.handle = $C.asyncRequest(m, uri, c, data);
		}
		function _getFormElements(el, type) {
			var items = [],
				els = el.getElementsByTagName(type);
			for(var k = 0, len = els.length; k < len; k++) {
				items.push(els[k]);
			}
			return items;
		}
		function check (area) {
			var items = [], sName, bt, label, oForm, c;
			if ($L.isObject(area) && $L.isObject(area.element)) {
			    if (!area.overheat) {
				  // analysing the content of the area...
			  	  // searching for buttons elements
				  items = _getFormElements(area.element, 'button');
				  // Loop through each result
				  for(var k = 0, len = items.length; k < len; k++) {
					if (!$D.getAncestorByClassName(items[k], 'yui-button')) {
	                    // YUI buttons are incompletes, that's why I introduce a new level with class="i" to include icons in radios and checkbox
	                    // Create a Button using an existing <input> element as a data source
						bt = new YAHOO.widget.Button(items[k], {className: items[k].className});
					}
				  }
				  // searching submit buttons
				  items = _getFormElements(area.element, 'input');
				  // Loop through each result
				  for(var k = 0, len = items.length; k < len; k++) {
					bt = items[k].getAttribute('type').toLowerCase();
	                label = items[k].getAttribute('value');
					if ($L.isObject(YAHOO.widget.Button) && ((bt == 'submit') || (bt == 'reset') || (bt == 'button'))) {
	                    // YUI buttons are incompletes, that's why I introduce a new level with class="i" to include icons in radios and checkbox
		                label = '<div class="i">'+label+'</div>';
	                    // Create a Button using an existing <input> element as a data source
						bt = new YAHOO.widget.Button(items[k], {label: label, className: items[k].className});
					}
				  }
				}
			  	// searching forms
				items = _getFormElements(area.element, 'form');
	            // Loop through each result
				for(var k = 0, len = items.length; k < len; k++) {
					oForm = items[k];
					// if the form was already manipulated, just let it pass
					if (!$D.hasClass(oForm, _formClass)) {
						$D.addClass(oForm, _formClass); // flagging the form...
						area.morePostData[_form_control] = area.id;
						var cc = {
							upload: callback.success,
							success: callback.success,
							failure: callback.failure,
							argument: { area:area }
						};
						// applying the correct event for each form...
						$E.on(oForm, "submit", function (e) {
							var f = this,
								sName = null, uri = f.getAttribute("action",2) || area.uri,
								m = (f.getAttribute('method').toLowerCase() == 'post'?'POST':'GET');
							// preparing the form...
							$C._formNode = f;
							// adding the post params
							for (sName in area.morePostData) {
								if (area.morePostData.hasOwnProperty(sName)) {
									$C.appendPostData (sName+'='+area.morePostData[sName]);
								}
							}
							onSubmit (area);
							// hacking the YUI Button Submit - To include the submit button values (deprecated after the 2.3.1)
							if ($L.isObject(YAHOO.widget.Button)) {
							  YAHOO.widget.Button.addHiddenFieldsToForm(f);
							}
							// workaround to save the editor instances before the submit process
							if ($L.isObject(YAHOO.widget.EditorInfo) && $L.isObject(YAHOO.widget.EditorInfo._instances)) {
								for (j in YAHOO.widget.EditorInfo._instances) {
									if (YAHOO.widget.EditorInfo._instances.hasOwnProperty(j)) {
										YAHOO.widget.EditorInfo._instances[j].saveHTML();
									}
								}
							}
							
							if (!$D.hasClass(f, 'yui-cms-simpleform')) {
								// adding the get params...
								uri = $DP.augmentURI (uri, area.moreGetData);
								// adding the form params
								$C.setForm(f, $D.hasClass(f, 'yui-cms-upload'));
								fetch (area, m, uri, cc);
								// Hack: clear the YUI Connection Manager...
								// if we leave the form linked to the connection manager object, all the futher call will have all this values...
								$C.resetFormState();
								$E.stopEvent(e);
							}
						});
					}
				}
				// everything was fine here...
				area.status = true;
	            onReady(area);
			}
		}
		function reset (area, uri) {
			var i, m = '', data;
			uri = new String($L.isString(uri)?uri:area.uri);
			uri += ((uri.indexOf('?') == -1)?'?':'&')+_form_control+'='+area.id;
			uri = $DP.augmentURI (uri, area.moreGetData);
			data = $DP.obj2query (area.morePostData);
			if ($L.isObject(area) && $L.isObject(area.element = $(area.id)) && uri) {
			  // fetching the remote uri...
			  var cc = {
				success: callback.success,
				failure: callback.failure,
				argument: { area:area }
			  };
			  fetch (area, area.loadMethod, uri, cc, data);
			  // broadcasting the message to log the navigation action into the history manager
			  if (YAHOO.Bubbling) {
				YAHOO.Bubbling.fire ('onNavigate', {
					state: area.id+escape(uri),
					control: 'wizard',
					uri: uri,
					area: area,
					restore: function() { 
					   reset (area, uri);
					}
				});
			  }			  
			}
		}

	    var onSubmit = function ( area ) {
			if ($L.isFunction(area.onSubmit)) {
				area.onSubmit.apply ( area, [area.values] );
			}
	    };
	    var onFinish = function ( area ) {
			if ($L.isFunction(area.onFinish)) {
				area.onFinish.apply ( area, [area.values] );
			}
	    };
		var onCancel = function ( area ) {
			if ($L.isFunction(area.onCancel)) {
				area.onCancel.apply ( area, [area.values] );
			}
		};
		var onError = function ( area ) {
			if ($L.isFunction(area.onError)) {
				area.onError.apply ( area, [area.values] );
			}
		};
		var onReady = function ( area ) {
			if ($L.isFunction(area.onReady)) {
				area.onReady.apply ( area, [area.values] );
			}
		};
		// public vars
		// public methods
		/**
		* * get the current status of the wizard
		* @public
		* @param {string} id   area Id
		* @return string
		*/
		obj.getStatus = function ( id ) {
			if ($L.isObject(_areas[id])) {
				return _areas[id].status;
			}
			return false;
		};
		/**
		* * get the values for the current wizard
		* @public
		* @param {string} id   area Id
		* @return boolean
		*/
		obj.getValues = function ( id ) {
			if (this.getStatus(id)) {
				return _areas[id].values;
			}
			return false;
		};
		/**
		* * set the values for the current wizard
		* @public
		* @param {string} id   area Id
		* @param {string} enc  json encoded string
		* @return Object
		*/
		obj.setValues = function ( id, enc ) {
			if (this.getStatus(id)) {
				var area = _areas[id];
				try {
                  area.values = $L.JSON.parse(enc);
                }
                catch (e) {
                  onError ( area );
                }
				return area.values;
			}
			return null;
		};
		/**
		* * add more values for the current area
		* @public
		* @param {string|object} values  json encoded string or a literal object
		* @return Object
		*/
		obj.valuer = function (values) {
			var area = this._area;
			if ($L.isString(values) && $L.JSON) {
				try {
                  values = $L.JSON.parse(values);
                }
                catch (e) {
				  values = {};
                  onError ( area );
                }
			}
			values = ($L.isString(values)||!$L.isObject(values)?{}:values);
			$L.augmentObject(area.values, values, true);
			return area.values;
		};
		/**
		* * mark the current wizard as finished
		* @public
		* @return void
		*/
		obj.finisher = function () {
			if (this._area) {
				this._area.done = true;/* marking the area as DONE */
			}
		};
		/**
		* * use the jump method to manually jump to new uri
		* @public
		* @param {string} id   area Id
		* @param {string} uri  url for the next step
		* @return boolean
		*/
		obj.jump = function ( id, uri ) {
			if (this.getStatus(id)) {
				reset( _areas[id], uri );
				return true;
			}
			return false;
		};
		/**
		* * configure the area properties
		* @publicf
		* @param {string} id
		* @param {object} userConfig
		* @return Object
		*/
		obj.init = function (id, userConfig) {
			var c = userConfig || _defConf;
			if ($L.isObject(id)) {
				id = $E.generateId(id);
			}
			// recompiling the properties of the current area...
			if ($L.isObject(_areas[id])) {
				$L.augmentObject(c, _areas[id], true);
			}
			// filtering the masking parameters...
			c.morePostData = c.morePostData || {};
			c.moreGetData = c.moreGetData || {};
			if ($L.isObject(c.dataMask)) {
				$L.augmentObject(c.morePostData, c.dataMask, true);
				$L.augmentObject(c.moreGetData, c.dataMask, true);
			}
			c.loadMethod = (($L.isString(c.loadMethod) && c.loadMethod.toLowerCase()=='post')?'POST':'GET');
			// checking for dynamic areas...
			if (c.dynamic) {
				$D.addClass(c.id, _dynamic);
			}
			$D.addClass(c.id, _className);
			_areas[c.id] = c;
			return c;
		};
		/**
		* * add a new area to the list
		* @public
		* @param {string} id
		* @param {object} userConfig
		* @return Object
		*/
		obj.add = function ( id, userConfig ) {
			var c = userConfig || _defConf,
				el = c.id || id ||  c.element;
			if ($L.isString(el) && (el !== '')) {
				el = $(el);
			}
			if ($L.isObject(el) && $L.isString(c.uri) && (c.uri !== '')) {
				c.id = $E.generateId(el);
				c.element = el;
				c.handle = null;
				c.status = false;
				c.values = {};
				this.remove(c.id);
				this.init(id, c);
				reset ( _areas[id] );
			}
			return _areas[id];
		};
		/**
		* * adopt an area with the content inside and add to the list
		* @public
		* @param {string} id
		* @param {object} userConfig
		* @return Object
		*/
		obj.adopt = function ( id, userConfig ) {
			var c = userConfig || _defConf,
				el = c.id || id ||  c.element;
			if ($L.isString(el) && (el !== '')) {
				el = $(el);
			}
			if ($L.isObject(el)) {
				c.id = $E.generateId(el);
				c.element = el;
				c.handle = null;
				c.status = false;
				c.values = {};
				this.remove(c.id);
				this.init(id, c);
				// process the current area's content...
				check ( _areas[c.id] );
			}
			return _areas[id];
		};
		/**
		* * TABVIEW: delegate the set content method...
		* @public
		* @param {object} tab		reference to the tab...
		* @param {object} tabview   reference to the tabview (optional)...
		* @param {object} config    Literal object with the user configuration vars
		* @return void
		*/
		obj.delegate = function ( tab, tabview, config ) {
			config = config || {};
			config.action = 'tabview';
			config.uri = tab.get ('dataSrc') || null; // getting the base url for the execution...
			config.tab = tab;
			// expanding the URI based on the current GET and MASK params
			config.moreGetData = config.moreGetData || {};
			if ($L.isObject(config.dataMask)) {
				$L.augmentObject(config.moreGetData, config.dataMask, true);
			}
			tab.set ('dataSrc', $DP.augmentURI (config.uri, config.moreGetData));
			tab.loadHandler.success = function(o) {
				var el = tab.get('contentEl');
				config.tab = el;
				config.underground = true;
				// processing the response with the dispatcher
				$DP.process( tab, o.responseText, config );
				// adopting the area... 
				$D.addClass(el, _dynamic);
				obj.adopt(el, config);
				// broadcasting the message
				if (YAHOO.Bubbling) {
				 	 YAHOO.Bubbling.fire ('onAsyncRequestEnd', {
						element: el
					 });
				 }
			};
			tab.on("activeChange", function() {
				// broadcasting the message
				if (YAHOO.Bubbling && this.get('active') && tab.get ('dataSrc') && !this.get('cacheData')) {
					 YAHOO.Bubbling.fire ('onAsyncRequestStart', {
						element: this.get('contentEl')
					 });
				}											
			});
			if ($L.isObject(tabview)) {
				tabview.addTab(tab);
			}
		};
		/**
		* * Remove an area from the stock...
		* @public
		* @param {object} id	className
		* @return void
		*/
		obj.remove = function ( id ) {
			if ($L.isObject(id)) {
				id = $E.generateId(id);
			}
			if (id && (_areas[id])) {
				_areas[id].handle = null; // resetting the handle
				_areas[id] = null; // discarding the area...
			}
			_areas[id] = [];
		};
		/**
		* * Reload an area...
		* @public
		* @param {object} id	className
		* @return void
		*/
		obj.reload = function ( id, uri ) {
			if ($L.isObject(id)) {
				id = $E.generateId(id);
			}
			if (id && (_areas[id])) {
				reset (_areas[id], uri);
			}
		};
		return obj;
    }();

})();
YAHOO.util.WizardManager = YAHOO.plugin.WizardManager; // deprecated: backward compatibility issue...
YAHOO.register("wizard", YAHOO.plugin.WizardManager, {version: "2.1", build: "242"});