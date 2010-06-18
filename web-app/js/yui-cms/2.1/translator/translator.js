/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.0
*/
YAHOO.namespace("plugin");
(function() {
  var $L = YAHOO.lang,
	  $C = YAHOO.util.Connect,
	  $E = YAHOO.util.Event;

	/**
	* @singleton Translator - Use this object to management your languages...
	* Load current language sentences
	* @constructor
	*/
    YAHOO.plugin.Translator = function() {
		var obj = {},
			_modules = {},
			_uri = '/translator.php?',
			_lang = 'eng',
			_charset = 'UTF-8';
		var callback = {
			success: function(o) {
				var values = null,
				    m = _modules[o.argument.id].modConfig;
				// process the json response here...
				try {
                  values = $L.JSON.parse(o.responseText);
                }
                catch (e) {
				  m.onError.fire();
                }
                if (values) {
                    obj.set (o.argument.id, values);
                }
			},
			failure: function(o) {
				var m = _modules[o.argument.id].modConfig;
				m.onError.fire();
			}
		};
		// public vars
		// public methods
		/**
		* Load a remote language module...
		* @public
		* @param {string} id   Module Name...
		* @return void
		*/
		obj.load = function ( id, modConfig ) {
			var uri, cb, cs;
			if ($L.isString(id) && (id !== '')) {
				// reseting certain module
				this.reset ( id, modConfig );
				_modules[id].modConfig.ready = false;
				uri = new String((_modules[id].modConfig.uri)?_modules[id].modConfig.uri:_uri);
				cs = ((_modules[id].modConfig.charset)?_modules[id].modConfig.charset:_charset);
				if (!uri.indexOf ('?')) {
					uri += '?';
				} else if (uri.indexOf ('?') !== uri.length - 1) {
					uri += '&';
				}
				uri += 'lang='+_lang+'&module='+id+'&charset='+cs;
				cb = {
					success: callback.success,
					failure: callback.failure,
					argument: { id:id, module:modConfig }
				};
				_modules[id].handle = $C.asyncRequest('GET', uri, cb);
			}
		};
		/**
		* Set the languages values for a module...
		* @public
		* @param {string} id        -  module id
		* @param {Object} values    -  Literal object with language constants...
		* @return Object
		*/
		obj.set = function ( id, values ) {
			if (!$L.isObject(_modules[id])) {
				this.reset (id);
				_modules[id].modConfig.name = id;
			}
			_modules[id].values = values;
			_modules[id].modConfig.ready = true;
			_modules[id].modConfig.onReady.fire();
			return _modules[id].values;
		};
		/**
		* Translate a certain language sentence...
		* @public
		* @param {string} id     -  module id
		* @param {string} c      -  the language sentence to be translated
		* @param {object} config -  the configuration object in case the module is not loaded yet or an error ocurred
		* @return String
		*/
		obj.get = function ( id, c, config ) {
			config = config || {};
			config.autoloader = config.autoloader || true;
			if ($L.isObject(_modules[id])) {
				if (this.isReady(id)) {
					if ($L.isString(_modules[id].values[c])) {
						// returning the correct translation
						return _modules[id].values[c];
					} else {
						// this language constant don't exists, returning the original value
						if ($L.isFunction(config.onNull)) {
							config.onNull.apply ( _modules[id], [id, c] );
						}
						return c;
					}
				}
				else {
					// the module is already set, but not ready: adding the callback to the module list...
					if ($L.isFunction(config.onReady)) {
					  _modules[id].modConfig.onReady.subscribe ( config.onReady );
					}
					if ($L.isFunction(config.onError)) {
					  _modules[id].modConfig.onError.subscribe ( config.onError );
					}
				}
			} else if (config.autoloader) {
				// loading the new module and setting the callback on ready...
				this.load ( id, config );
			}
			return null;
		};
		/**
		* find a certain language constant thru the full list of modules...
		* @public
		* @param {string} c          -  language string for translation...
		* @return string
		*/
		obj.find = function ( c ) {
			var r;
			for (id in _modules) {
			  if (_modules.hasOwnProperty(id)) {
				r = this.get ( id, c, false );
				if (r) {
					return r;
				}
			  }
			}
			return null;
		};
		/**
		* Add a module's data...
		* @public
		* @param {string} id        -  module id
		* @param {Object} values    -  Literal object with language constants...
		* @param {object} modConfig -  Initial module configuration
		* @return Object
		*/
		obj.add = function ( id, values, modConfig ) {
			modConfig = ($L.isObject(modConfig)?modConfig:{});
			this.reset (id, modConfig);
			_modules[id].modConfig.ready = false;
			_modules[id].modConfig.name = id;
			return this.set ( id, values );
		};
		/**
		* Reload new languages...
		* @public
		* @return void
		*/
		obj.reload = function () {
			for (id in _modules) {
			  if (_modules.hasOwnProperty(id)) {
				this.reset ( id, _modules[id].modConfig );
				this.load ( id, _modules[id].modConfig );
			  }
			}
		};
		/**
		* Reset a module or reset all...
		* @public
		* @param {string} id   Module Name...
		* @param {object} modConfig -  Initial module configuration
		* @return void
		*/
		obj.reset = function ( id, modConfig ) {
			var onReady, onError;
			if ($L.isString(id) && (id !== '')) {
				// reseting certain module
				modConfig = modConfig || {};
				if ($L.isObject(_modules[id])) {
					// if the module exists, we use the old modConfig obj
					modConfig = _modules[id].modConfig;
				} else {
					// if the module is new, setting the custon events...
					onReady = new YAHOO.util.CustomEvent('onReady');
					onError = new YAHOO.util.CustomEvent('onError');
					// adding the callback to the module list...
					if ($L.isFunction(modConfig.onReady)) {
					  onReady.subscribe ( modConfig.onReady );
					}
					if ($L.isFunction(modConfig.onError)) {
					  onError.subscribe ( modConfig.onError );
					}
					modConfig.onReady = onReady;
					modConfig.onError = onError;
				}
				_modules[id] = { modConfig : modConfig };
			} else {
				// reset all languages...
				for (id in _modules) {
				  if (_modules.hasOwnProperty(id)) {
			  		this.reset ( id, modConfig );
				  }
				}
			}
		};
		/**
		* Set a new language, translator gateway, charset, and reload all the modules...
		* @public
		* @param {object} userConfig   literal with the configuration... (userConfig.silence set the values and don't reload the modules...)
		* @return void
		*/
		obj.init = function ( userConfig ) {
			_lang = userConfig.lang || _lang;
			_uri = userConfig.uri || _uri;
			_charset = userConfig.charset || _charset;
			if (!userConfig.silence) {
				this.reload ();
			}
		};
		/**
		* Active language...
		* @public
		* @return string
		*/
		obj.getLang = function () {
			return _lang;
		};
		/**
		* Active language...
		* @public
		* @param {string} id   modules id...
		* @return boolean
		*/
		obj.isReady = function ( id ) {
			if ($L.isObject(_modules[id])) {
				return _modules[id].modConfig.ready;
			}
			return false;
		};
		return obj;
    }();

})();
/* String prototype, translate the current string using the module param, and return a new string (translated)
 * If the mod isn't specify, we will try to find a translation, else return the original string...
 */
String.prototype.translate = function ( mod ) {
	if (YAHOO.lang.isString(mod) && (mod !== '')) {
		return YAHOO.util.Translator.get ( mod, this );
	} else {
		return YAHOO.util.Translator.find ( this ) || this;
	}
};
YAHOO.util.Translator = YAHOO.plugin.Translator; // deprecated: backward compatibility issue...
YAHOO.register("translator", YAHOO.plugin.Translator, {version: "2.0", build: "208"});