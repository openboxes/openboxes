/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.1
*/
YAHOO.namespace("plugin");
(function() {
  var $C = YAHOO.util.Connect,
	  $L = YAHOO.lang,
	  $E = YAHOO.util.Event,
	  $D = YAHOO.util.Dom,
	  $  = YAHOO.util.Dom.get;

  /**
  * Constants
  */
  var constants = { LOADING: 1, DISPATCHED: 2, ERROR: 3, EMPTY: 4, proxy: '/dispatcher.php?uri=', CSSNODE: 1, JSNODE: 2 };

  var reScriptTag = /<script([^>]*)>([\s\S]*?)<\/script>/igm,
  	  reScriptTagSrc = /src=(['"]?)([^"']*)\1/i,
  	  reScriptTagRel = /rel=(['"]?)([^"']*)\1/i,
  	  reLinkTag = /<link([^>]*)(>[\s]*<\/link>|>)/igm,
  	  reLinkTagSrc = /href=(['"]?)([^"']*)\1/i,
	  reStyleTag = /<style([^>]*)>([\s\S]*?)<\/style>/igm,
	  reTagParams = new RegExp('([\\w-\.]+)\\s*=\\s*(".*?"|\'.*?\'|\\w+)*', 'im');

  var reCSS3rdFile    = new RegExp('url\\s*\\(([^\\)]*)', 'igm');                // [url(image.gif] - also can include quotes

  var reURI = new RegExp('^((?:http|https)://)((?:\\w+[\.|-]?)*\\w+)(/.*)$', 'i');  // full url: [http://www.domain.com/path/file.html]

  /**
  * @class Dispatcher
  */
  YAHOO.plugin.Dispatcher = function () {
  	var obj = {},
		_threads = {}, // each thread represent an area...
		_hashtable = [],
		_oDefaultConfig = {relative: false, baseURI:document.location.toString()},
		_loadingClass = 'loading',
		_classname = 'yui-dispatchable',
		_reURI = {
				key: ["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],
				q:   {
					name:   "queryKey",
					parser: /(?:^|&)([^&=]*)=?([^&]*)/g
				},
				parser: {
					strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
					loose:  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
				}
		},
		_reTrim = /^\s+|\s+$/g;

	/*
		parseUri 1.2.1
		(c) 2007 Steven Levithan <stevenlevithan.com>
		MIT License
	*/
	function _parseUri (str, strictMode) {
		var	o   = _reURI,
			m   = o.parser[strictMode?"strict":"loose"].exec(str),
			uri = {},
			i   = 14;
	
		while (i--) uri[o.key[i]] = m[i] || "";
		uri[o.q.name] = {};
		uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
			if ($1) uri[o.q.name][$1] = $2;
		});
		return uri;
	}; 

	function _globalEval (scriptString) {
		var h = document.getElementsByTagName('head')[0] || document.documentElement,
			s = document.createElement('script');
		scriptString = scriptString.replace (_reTrim, '');
		if (scriptString) {
			s.type = "text/javascript";
			/* hack: IE doesn't support appendChild+document.createTextNode, using .text instead */
			if (YAHOO.env.ua.ie) {
				s.text = scriptString;
			} else {
				s.appendChild( document.createTextNode( scriptString ) );
			}
			h.insertBefore(s, h.firstChild);
			h.removeChild(s);
		}
	}

	// utilities
	function _eraseQuotes( str ) {
	  if ($L.isString(str)) {
	  	str = str.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");  // trim
		str = str.replace(/^(['|"])*(\S*(?:\s+\S+)*)\1$/, "$2"); // un-quotes
	  }
	  return str;
	}
	function _getParams ( str, validator ) {
		var p = null, r = {};
		validator = validator || {};
		// capturing the params into an object literal (r)
		if($L.isString(str)){
		  while(p = reTagParams.exec(str)){
		  	// apply validation if exists, if the value is null will be discarted
			p[2] = (validator.hasOwnProperty(p[1])?validator[p[1]]:p[2]);
			if (p[2]) {
			   r[p[1]] = _eraseQuotes(p[2]);
			}
			str = str.replace(reTagParams, '');
		  }
		}
		return r;
	}
	function _baseURI ( uri ) {
		uri = (($L.isString(uri) && (uri.indexOf('/') > -1))?uri:_oDefaultConfig.baseURI) + ''; // the default base is the current document uri (hack to convert to string)
		return uri.substr (0, uri.lastIndexOf('/')+1); // forget the file name
	}
	function _relativeURI( base, uri ) {
	  	// is the url is relative (not http..., not / at the begining)
		if(uri && !reURI.test(uri) && (uri.indexOf('/') !== 0)){
		  uri = base+uri;
		}
	    return uri;
	}
	function _onStart (config) {
	 	 /* ARIA: set as a live area */
		 if (config.element && config.element.setAttribute) {
			 config.element.setAttribute ('aria-live', 'polite');
			 config.element.setAttribute ('aria-atomic', 'true'); // the whole area will be updated
			 config.element.setAttribute ('aria-relevant', 'all'); // the whole area will be updated
			 config.element.setAttribute ('aria-busy', 'true'); // starting the loading process
		 }
		 // onStart (before the loading)
		 config.onStart = config.before || config.onStart;
		 if ($L.isFunction(config.onStart)) {
		   config.onStart.apply ( config, [config.element] );
		   config.onStart = null; // because the process method will try to executed again...
		 }
		 // broadcasting the message
		 if (!config.underground && YAHOO.Bubbling) {
			 YAHOO.Bubbling.fire ('onAsyncRequestStart', {
				element: config.element
			 });
		 }
	}
	function _onChange (config, el) {
		 if ($L.isFunction(config.onChange)) {
		   el = el || config.element;
		   config.onChange.apply ( config, [el] );
		 }
	}
	function _onLoad (config) {
		 // onLoad (after the execution)
		 config.onLoad = config.after || config.onLoad;
		 if ($L.isFunction(config.onLoad)) {
		   config.onLoad.apply ( config, [config.element] );
		 }
		 //console.log ("config: ", config);
		 if (!config.underground && YAHOO.Bubbling) {
		 //console.log ('el:', (config.tab || config.element));
			 YAHOO.Bubbling.fire ('onAsyncRequestEnd', {
				element: config.element
			 });
		 }
		 /* ARIA: set as a live area */
		 if (config.element && config.element.setAttribute) {
		 	config.element.setAttribute ('aria-busy', 'false'); // ending the loading process
		 }
	}
	// private stuff
	/**
	* Dispatching the next node of the handle
	* @public
	* @param {Object} hd     		Thread's handle
	* @param {Object} config    Used to pass the user configuration thru the chain of execution
	* @return boolean
	*/
	function dispatch( hd, config ) {
	  var callback = null, flag = true, node = null, uri = '', i = 0;
	  config = config || {};
	  if (obj.isAlive(hd)) {
	  	node = _threads[hd].chunks.shift ();
	  	if ($L.isObject(node) && node.src) {
		  // cheching the uri in the hashtable
		  config.hash = _hashtable.length; // default hash (at the end of the table)
		  for (i=0; i<_hashtable.length; i++) {
		  	if (_hashtable[i].uri == node.src) {
				if ((_hashtable[i].status == constants.DISPATCHED) && !config.override) {
					// this uri was already dispatched by this plugin and will be discarted...
					flag = false;
				} else {
					// the uri already exists in the table
				}
		  		config.hash = i;
				break;
			}
		  }
		  if (flag) {
		  	// fetching the remote script using the YUI Get Utility
		    uri = obj.firewall (node.src, config, true);
			if ($L.isString(uri) && (uri !== '')) {
				_hashtable[config.hash] = {uri: node.src, proxy: uri, status: constants.LOADING};
				if (node.type === constants.JSNODE) {
					obj.area = hd;
					obj.destroyer = _threads[hd].destroyer;
					config.handle = YAHOO.util.Get.script(uri, { 
						onSuccess: function() {
						    _hashtable[config.hash].status = constants.DISPATCHED;
							// broadcasting the message
							if (config.rel && YAHOO.Bubbling) {
								 YAHOO.Bubbling.fire ('onScriptReady', {
									module: node.rel,
									src: node.src,
									uri: uri,
									hash: config.hash
								 });
							}
							config.hash = null; // resetting the config.hash for the next iteration...
							// continue with the thread
							dispatch( hd, config );
						},
						onFailure: function() {
							// continue with the thread
							dispatch( hd, config );
						},
						scope: obj,
						data: config
					});
				} else if (node.type === constants.CSSNODE) {
					YAHOO.util.Get.css(uri, {});
					_hashtable[config.hash].status = constants.DISPATCHED;
					// continue with the thread
					dispatch( hd, config );
				}
			}
		  } else {
		  	// continue with the thread execution
			dispatch( hd, config );
		  }
		}
		else {
		  // the node represent an inline script (don't have hash value)
		  config.hash = null;
		  exec (hd, node.content, config);
		}
	  } else {
	  	// ending the execution thread
	  	obj.kill(hd);
		_onLoad (config);
	  }
	}

	/**
	* Executing a javascript script segment
	* @public
	* @param {Object} hd     		Thread's handle
	* @param {string} c     		Content to execute
	* @param {Object} config    User configuration (useful for future implementations)
	* @return boolean
	*/
	function exec( hd, c, config ) {
	  var status = constants.EMPTY;
	  if (c && (c !== '')) {
		try{
		  status = constants.DISPATCHED;
		  // instead send in a variable that points to this object...
		  if (!config.hash || (_hashtable[config.hash].status != constants.DISPATCHED)) {
			 obj.area = hd;
			 obj.destroyer = _threads[hd].destroyer;
			 // you can define your own evaluation routine for inline scripts
			 if ($L.isFunction(config.evalRoutine)) {
				 config.evalRoutine(c, config);
			 } else {
			 	 /* using the new routine to evaluate inline script, no longer using eval */
				 _globalEval (c);
			 }			 
		  }
		}catch(e){
		  status = constants.ERROR;
		  if ($L.isFunction(config.error)) {
			  config.error.apply ( config, [hd, c, _hashtable, e] );
		  } else {
			  throw new
			    Error ("Dispacher: Script Execution Error ("+e+")");
		  }
		}
	  }
	  // updating the status of the remote script in the hashtable
	  if ($L.isNumber(config.hash)) {
		_hashtable[config.hash].status = status;
		config.hash = null; // resetting the config.hash for the next iteration...
	  }
	  dispatch( hd, config );
	}

	/**
	* Display the content inside the element
	* @public
	* @param {Object} el    		Element reference
	* @param {string} c     		Content to display
	* @param {Object} config    User configuration (useful for future implementations)
	* @return boolean
	*/
	function display( el, c, config ) {
		config.action = (config.action?config.action:'replace');
		switch (config.action)
		{
			case 'tabview':
				// onDestroy event... used to release the memory inside the tab...
				destroy (el.get('contentEl'), config);
				try { el.set('content', c); } catch (e1) {return false;}
				_onChange (config, el.get('contentEl'));
				break;
			case 'update':
				c = el.innerHTML + c;
				try { el.innerHTML = c; } catch (e2) {return false;}
				_onChange (config, el);
				break;
			case 'replace':
			case 'layout':
			default:
				// onDestroy event... used to release the memory inside the element...
			    destroy (el, config);
				// changing the content
				try { el.innerHTML = c; } catch (e3) {return false;}
				_onChange (config, el);
				break;
		}
		return true;
	}
	/**
	* "Destroy Custom Event" will be fired before remove the innerHTML in the displaying process
	* @public
	* @param {Object} el    	DOM Element reference
	* @param {Object} config    User configuration (useful for future implementations)
	* @return void
	*/
    function destroy( el, config ) {
		var hd = config.guid, i = 0;
		if ($L.isObject(_threads[hd].destroyer)) {
			_threads[hd].destroyer.fire (el, config);
		}
		if ($D.inDocument(el)) {
			// purge the child elements and the events attached...
		  	for (i=0;i<el.childNodes.length;i++) {
			  $E.purgeElement ( el.childNodes[i], true );
			}
			$D.addClass (el, _classname);
		}
		_threads[hd].destroyer = new YAHOO.util.CustomEvent('destroyer');
		// checking for default onDestroy (usually passed to the methods fetch, delegate or process)
		if ($L.isFunction(config.onDestroy)) {
			_threads[hd].destroyer.subscribe (config.onDestroy);
		}
		// other subscribers can be add using: YAHOO.plugin.Dispatcher.destroyer.subscribe ( mySubscriber );
	}
	/**
	* Parse the string, remove the script tags, and create the execution thread...
	* @public
	* @param {Object} hd    		Element reference
	* @param {string} s     		String with the script tags inside...
	* @return string
	*/
    function parse( hd, s, config ) {
		config = config || {};
		config.uri = config.uri || null;
		config.relative = config.relative || _oDefaultConfig.relative;
		var m = true, attr = false,
		    base = _baseURI(config.uri); // calculation of the base path...
		// searching and cut out all style tags, push them into thread
		// ej. <style title="" type="text/css"></style>
		// not supported yet. <!--@import url("http://us.js2.yimg.com/us.js.yimg.com/lib/hdr/ygma_2.19.css");body{margin:0px 4px;}-->
		s = s.replace(reStyleTag,
		  function (str,p1,p2,offset,s) {
			// apply the inline style automatically
			if (p2) {
			    obj.applyCSS(p2, _getParams(p1), config);
			}
		    return "";
	      }
		);

		// searching and cut out all Link tags, push them into thread
		// ej. <link rel="stylesheet" type="text/css" href='/themes/bubbling/css/common.css' />
		s = s.replace(reLinkTag,
		  function (str,p1,p2,offset,s) {
			// add a remote style to buffer
			if(p1){
			  attr = p1.match(reLinkTagSrc);
			  if(attr) {
			    if (config.relative) {
					attr[2] = _relativeURI(base, attr[2]); // path correction process...
				}
			    _threads[hd].chunks.push ({
					src: attr[2],
					content: '',
					type: constants.CSSNODE,
					params: _getParams(p1)
				});
			  }
			}
		    return "";
	      }
		);

		// searching and cut out all script tags, push them into thread
		s = s.replace(reScriptTag,
		  function (str,p1,p2,offset,s) {
			// add a remote script to buffer
			if(p1){
			  attr = p1.match(reScriptTagSrc);
			  if(attr) {
				var rel = p1.match(reScriptTagRel);
				rel = (rel?rel[2]:null);
			    if (config.relative) {
					attr[2] = _relativeURI(base, attr[2]); // path correction process...
				}
			    _threads[hd].chunks.push ({
					src: attr[2],
					content: '',
					type: constants.JSNODE,
					rel: rel,
					params: _getParams(p1)
				});
			  }
			}
			// add a inline script to buffer
			if (p2) {
			    _threads[hd].chunks.push ({
					src: null,
					content: p2,
					type: constants.JSNODE,
					params: _getParams(p1)
				});
			}
		    return "";
	      }
		);
		return s;
    }
	
	// is the bubbling is available, we will define the cache listener...
	if (YAHOO.Bubbling) {
		YAHOO.Bubbling.on('onScriptReady', function() {
			if (this.src && !this.hash) {
				_hashtable[this.hash].status = constants.DISPATCHED;
			}
		});		
	}

	// public vars
	obj.area = null; // current thread, useful to get the current DOM element...
	obj.strictMode = true;
	obj.destroyer = null; // current destroyer, useful to add new subscriber during the execution...
	// public methods
	/**
	* * Fetching a remote file that will be processed thru this object...
	* @public
	* @param {object} el     	{HTMLElement | String | Object} The html element that represents the dynamic area.
	* @param {object} uri       Remote file that will be loaded using AJAX
	* @param {object} config    Literal object with the user configuration vars
	* @return object  Reference to the connection handle
	*/
	obj.fetch = function( el, uri, config ){
	   config = config || {};
	   config.uri = uri;
	   var id = null,
	       u = null,
	       callback = {
			success: function (o) {
				if (o.responseText != 'undefined') {
					obj.process( el, o.responseText, config, true );
				}
				$D.removeClass(el, _loadingClass);
			},
			failure:function (o) {
				if ($L.isFunction(config.onError)) {
        		   config.onError.apply ( config, [config.element, o] );
        		}
				$D.removeClass(el, _loadingClass);
			}
	   };
	   if (($L.isObject(el) || (el = $( el ))) && uri) {
		 u = obj.firewall (uri, config);
		 $D.addClass(el, _loadingClass);
		 config.handle = $C.asyncRequest('GET', u, callback);
		 config.element = el;
		 _onStart (config);
		 // broadcasting the message to log the navigation action into the history manager
		 if (YAHOO.Bubbling) {
			id = config.guid || $E.generateId (el);
			YAHOO.Bubbling.fire ('onNavigate', {
				state: id+escape(uri),				
				control: 'dispatcher',
				element: el,
				uri: uri,
				config: config,
				restore: function() { 
				   obj.fetch (el, uri, config);
				}
			});
		 }
		 return config.handle;
	   }
	   return null;
	};
	/**
	* * Starting the process for a content...
	* @public
	* @param {object} el     		{HTMLElement | String | Object} The html element that represents the dynamic area.
	* @param {string} content       Content to be processed
	* @param {object} config    	Literal object with the user configuration vars
	* @param {boolean} flag    	    If the call was internal or external
	* @return object  reference to the thread handle...
	*/
	obj.process = function( el, content, config, flag ){
		var hd = null;
		config = config || {};
		if ($L.isObject(el) || (el = $( el ))) {
			hd = config.guid || $E.generateId (el); // by default, one thread by element, use the GUID to discard this rule...
			this.kill(hd); // kill the previous process for this handle...
			config.element = el;
			config.content = content;
			config.guid = hd;
			if (!flag) {
				_onStart(config);
			}
			// processing
			if (display(el, parse (hd, content, config), config)) {
				dispatch (hd, config); // starting the execution chain
			}
		}
		return hd;
	};
	/**
	* * TABVIEW: delegate the set content method...
	* @public
	* @param {object} tab		reference to the tab...
	* @param {object} tabview   reference to the tabview...
	* @param {object} config    Literal object with the user configuration vars
	* @return void
	*/
	obj.delegate = function ( tab, tabview, config ) {
		config = config || {};
		config.action = 'tabview';
		config.uri = tab.get ('dataSrc') || null; // getting the base url for the execution...
		config.tab = tab;
		tab.loadHandler.success = function(o) {
			var el = tab.get('contentEl');
			config.tab = el;
			config.underground = true;
			obj.process( tab, o.responseText, config );
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
	* * addUnit: delegate the addUnit layout method...
	* @public
	* @param {object} unit         reference to the unit...
	* @param {object} layout       reference to the layout...
	* @param {object} config       Literal object with the user configuration vars
	* @return object 	reference to the unit
	*/
	obj.addUnit = function ( unit, layout, config ) {
		var c = config || {}, el;
		c.action = 'layout';
		if (!unit || !layout) {
			return false;
		}
		if ($L.isString(unit)) {
			unit = layout.getUnitByPosition (unit);
		} else if (unit.position && !layout.getUnitByPosition (unit.position)) {
			// creating the new unit if the object is a simple literal with the position and 
			// the position is not filled yet in the layout manager...
			unit = layout.addUnit(unit);
		}
		if (c.uri) {
			unit.set('dataSrc', c.uri);
		}
		if ((c.unit = unit) && (c.uri = c.unit.get ('dataSrc')) && (el = c.unit.body)) {
			c.underground = true;
			c._dispatcherConfig = c;
			c.unit.loadHandler.success = function(o) {
				obj.process( this.body, o.responseText, this._dispatcherConfig );
				// broadcasting the message
				if (YAHOO.Bubbling) {
					 YAHOO.Bubbling.fire ('onAsyncRequestEnd', {
						element: this.body
					 });
				}
			};
			/* starting the loading process */
			if (c.unit.loadContent() && YAHOO.Bubbling) {
				 // broadcasting the message
				 YAHOO.Bubbling.fire ('onAsyncRequestStart', {
					element: el
				 });
			}
		}
		return c.unit;
	};
	/**
	* * Injecting CSS in the current page
	* @public
	* @param {string} cssCode   CSS content that will be injected
	* @param {object} params    Literal object with the tag configuration params
	* @param {object} config    Literal object with the dispatcher configuration values
	* @return boolean  the operation result
	*/
	obj.applyCSS = function( cssCode, params, config ) {
		params = params || {};
		var styleElement = document.createElement("style"), base = params.href || '';
		// calculation of the base path...
		config = config || {};
		var uri = config.uri || _oDefaultConfig.baseURI;
		config.relative = config.relative || _oDefaultConfig.relative;
		if (config.relative) {
			// applying the firewall routine to get the real path and load the css internal resources correctly.
		    uri = obj.firewall (uri, config, true);
			base = _baseURI(uri); // calculation of the base path...
			base = _relativeURI(base, params.href); // path correction process...
		}
		base = _baseURI(base);
		// path correction process...
		// ej: (image.gif) or ("image.gif") or ('image.gif') or (http://fullpath/image.gif)
		/*  In the case of DXImageTransform.Microsoft.AlphaImageLoader(src='image.png',sizingMethod='scale');
		    The use of the AlphaImageLoader ultimately proved to be
			an unreliable solution for IE 6 as it never handled relative paths well.
			It seems like the AlphaImageLoader always expects the paths to be relative
			to the page the CSS is included in, rather than relative to the CSS file itself. */
  		cssCode = cssCode.replace(reCSS3rdFile,
		  function (str,p1,offset,s) {
			p1 = _eraseQuotes (p1);
			p1 = 'url('+_relativeURI(base, p1);
			return p1;
		  }
		);

		// the CSS is ready...
	    styleElement.type = "text/css";
	    if ($L.isObject(styleElement.styleSheet)) {
	      styleElement.styleSheet.cssText = cssCode;
	    } else {
	      styleElement.appendChild(document.createTextNode(cssCode));
	    }
	    try{
	      document.getElementsByTagName("head")[0].appendChild(styleElement);
	    }catch(e){
		  throw new
		    Error ("Dispacher: CSS Processing Error ("+e+")");
		  return false;
	    }
	    return true;
	};
	/**
	* * Fetching a remote javascript file that will be processed thru this object...
	* @public
	* @param {object} uri       Remote js file that will be loaded using AJAX
	* @param {object} config    Literal object with the user configuration vars
	* @return string  ID reference to the new dispatcher's thread
	*/
	obj.jsLoader = function( uri, config ){
	   if ($L.isString(uri) && (uri !== '')) {
	   	 config = config || {};
		 $E.generateId ( config ); // generating an unique ID for the thread (config.id)
		 obj.kill (config.id);
	   	 // add a remote script to buffer
 	     _threads[config.id].chunks = [{
			src: uri,
			content: '',
			type: constants.JSNODE,
			params: {href: uri}
		 }];
		 config.underground = true;
		 _onStart(config);
		 dispatch (config.id, config); // starting the execution chain
		 return config.id;
	   }
	   return null;
	};
	/**
	* * Fetching a remote CSS file that will be processed thru this object...
	* @public
	* @param {object} uri       Remote CSS file that will be loaded using AJAX
	* @param {object} config    Literal object with the user configuration vars
	* @return string  ID reference to the new dispatcher's thread
	*/
	obj.cssLoader = function( uri, config ){
	   if ($L.isString(uri) && (uri !== '')) {
	   	 config = config || {};
		 $E.generateId ( config ); // generating an unique ID for the thread (config.id)
		 obj.kill (config.id);
	   	 // add a remote script to buffer
	     _threads[config.id].chunks = [{
			src: uri,
			content: '',
			type: constants.CSSNODE,
			params: {href: uri}
		 }];
		 config.underground = true;
		 _onStart(config);
		 dispatch (config.id, config); // starting the execution chain
		 return config.id;
	   }
	   return null;
	};
	/**
	* * Verify if the a process is still alive
	* @public
	* @param {object} hd   Process handle
	* @return boolean
	*/
	obj.isAlive = function ( hd ) {
		return (hd && $L.isObject(_threads[hd]) && (_threads[hd].chunks.length > 0));
	};
	/**
	* * Kill a process...
	* @public
	* @param {object} handle   Process handle
	* @return void
	*/
	obj.kill = function ( hd ) {
		if (hd && !$L.isObject(_threads[hd])) {
			_threads[hd] = {chunks: [], destroyer: null};
		} else if (this.isAlive (hd)) {
			_threads[hd].chunks = []; // discarding the handle...
		}
	};
	/**
	* * destroy an area...
	* @public
	* @param {object} handle   Process handle
	* @return void
	*/
	obj.destroy = function ( hd ) {
		this.kill(hd);
		if (hd && !$L.isObject(_threads[hd])) {
			_threads[hd].destroyer.fire( $(hd), {} );
		}
	};
	/* * onDestroy - subscribe a new destroyer for a certain area...
	* @public
	* @param {object} handle   Process handle
	* @return boolean
	*/
	obj.onDestroy = function ( hd, bh, scope ) {
        var params = (scope?[bh, scope, true]:[bh]); // params for scope corrections
        if ($L.isObject(_threads[hd]) && $L.isObject(_threads[hd].destroyer)) {
			if ($L.isObject(scope)) {
			  _threads[hd].destroyer.subscribe(bh, scope, true);  // correcting the default scope
			} else {
			  _threads[hd].destroyer.subscribe(bh);  // use the default scope
			}
			return true;
		}
		return false;
	};
	obj.init = function (c) {
		c = c || {};
		c.relative = c.relative || false;
		_oDefaultConfig = c;
	};
	/**
	* Analyze the uri before start the downloading process (if the uri isn't in over the current domain name, the dispatcher can use a proxy)
	* @public
	* @param {String} uri     	  Remote URL
	* @param {Object} config      Used to pass the user configuration thru the chain of execution
	* @param {Boolean} monolitic  Do not use proxy
	* @return String
	*/
	obj.firewall = function( uri, config, monolitic ) {
		var sDomain = null,
			sProtocol = null,
			m = null;
		// AJAX url don't work with &amp;
		while (uri.indexOf ( '&amp;' ) > -1) {
		    uri = uri.replace ( '&amp;', '&' );
		}
		// defining the proxy for cross-domain scripting...
		config.proxy = config.proxy || constants.proxy;
		if ($L.isFunction(config.firewall)) {
		  // external verification only...
		  uri = config.firewall.apply ( config, [uri] );
		} else {
			// internal verification only...
			// monolithic execution discard the cross-domain capabilities
			if (!config.monolithic && !monolitic && config.proxy) {
				m = uri.match(reURI); // checking the RE to verify if the url is external...
				if (m && (m[2] !== document.domain)) {
					// the uri is external, escaping especial chars and use a proxy
					uri = config.proxy + escape(uri);
				}
			}
		}
		return uri;
	};
  /**
    * converting a literal object into a query string
    * @public
    * @param {object} params       Literal object to create the url querystring {'param1':'value1','param2':'value2'}
    * @return string
    */
    obj.obj2query = function( params ) {
		var u = '', indx;
		if ($L.isObject(params)) {
			for (key in params) {
				if (params.hasOwnProperty(key)) {
					u += (u==''?'':'&');
					u += key+'='+params[key];
				}
			}
		}
		return u;
    };
  /**
    * augment an url with more parameters, overriding...
    * @public
    * @param {string} url 
    * @param {string|array} m   MoreParams: string: param1=value1&param2=value2 or array: {'param1':'value1','param2':'value2'}
    * @return string
    */
    obj.augmentURI = function( url, m ) {
		m = m || {};
	    var o = _parseUri(url, this.strictMode),
	        u = '';
		o.queryKey = o.queryKey || {};
		$L.augmentObject(o.queryKey, m, true);
		if (o.protocol) u += o.protocol + ':';
		if (this.strictMode) {
			if (/^(?:[^:\/?#]+:)?\/\//.test(o.source)) u += '//';
		} else {
			if (/^(?:(?![^:@]+:[^:@\/]*@)[^:\/?#.]+:)?\/\//.test(o.source)) u += '//';
		}
		if (o.authority) {
			if (o.userInfo) {
				if (o.user) u += o.user;
				if (o.userInfo.indexOf(':') > -1) u += ':';
				if (o.password) u += o.password;
				u += '@';
			}
			if (o.host) u += o.host;
			if (o.port) u += ':' + o.port;
		}
		if (o.relative) {
			if (o.path) {
				if (o.directory) u += o.directory;
				if (o.file) u += o.file;
			}
			u += '?';// + o.query;
			for (sName in o.queryKey) {
				if (o.queryKey.hasOwnProperty(sName)) {
					u += sName+'='+o.queryKey[sName]+'&';
				}
			}
			if (o.anchor) u += '#' + o.anchor;
		}
		return u;
    };
	/**
	* @method toString
	* @description Returns a string representing the dispatcher plugin.
	* @return {String}
	*/
	obj.toString = function() {
	    return ("YUI Dispatcher Plugin");
	};
	return obj;
  }();
})();
YAHOO.util.Dispatcher = YAHOO.plugin.Dispatcher; // deprecated: backward compatibility issue...
YAHOO.register("dispatcher", YAHOO.plugin.Dispatcher, {version: "2.1", build: "237"});