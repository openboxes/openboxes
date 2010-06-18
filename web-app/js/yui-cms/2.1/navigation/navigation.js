/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.0
*/
(function() {
  var $B = YAHOO.Bubbling,
  	  $H = YAHOO.util.History,
  	  $L = YAHOO.lang,
	  $E = YAHOO.util.Event;

	/**
	* @singleton Navigation - Use this object to control the YUI History Manager...
	* Add your custom navigation entries to create the corresponding browser actions
	* @constructor
	*/
    YAHOO.plugin.Navigation = function() {
		var obj = {},
		    _id = 'q',
			_pages = {},
			_state = $H.getBookmarkedState(_id) || 'default', // the default state...
			_defaultBehavior = null;
		
		$B.on('newRestorationEntry', function(layer, args) {
			if (args[1].state) {
				obj.add (args[1].state, args[1]);
			}
		});
		var b64="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		function base64encode(str){var out,i,len;var c1,c2,c3;len=str.length;i=0;out="";while(i<len){c1=str.charCodeAt(i++)&0xff;if(i==len){out+=b64.charAt(c1>>2);out+=b64.charAt((c1&0x3)<<4);out+="==";break;}c2=str.charCodeAt(i++);if(i==len){out+=b64.charAt(c1>>2);out+=b64.charAt(((c1&0x3)<<4)|((c2&0xF0)>>4));out+=b64.charAt((c2&0xF)<<2);out+="=";break;}c3=str.charCodeAt(i++);out+=b64.charAt(c1>>2);out+=b64.charAt(((c1&0x3)<<4)|((c2&0xF0)>>4));out+=b64.charAt(((c2&0xF)<<2)|((c3&0xC0)>>6));out+=b64.charAt(c3&0x3F);}return out;}
		function utf16to8(str){var out,i,len,c;out="";len=str.length;for(i=0;i<len;i++){c=str.charCodeAt(i);if((c>=0x0001)&&(c<=0x007F)){out+=str.charAt(i);}else if(c>0x07FF){out+=String.fromCharCode(0xE0|((c>>12)&0x0F));out+=String.fromCharCode(0x80|((c>>6)&0x3F));out+=String.fromCharCode(0x80|((c>>0)&0x3F));}else{out+=String.fromCharCode(0xC0|((c>>6)&0x1F));out+=String.fromCharCode(0x80|((c>>0)&0x3F));}}return out;}
		// public vars
		obj.debug = false,
		// public methods
		/**
		* * configure the history manager package
		* @publicf
		* @return void
		*/
		obj.init = function () {
			// Initialize the browser history management library.
			try {
				YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
			} catch (e) {
				// The only exception that gets thrown here is when the browser is
				// not supported (Opera, or not A-grade) Degrade gracefully.
				return false;
			}
			$H.register(_id, _state, obj.restore);
			// defining the listeners
			$B.on('onNavigate', function(layer, args) {
				if (args[1].state) {
					obj.navigate (args[1].state, args[1]);
				}
			});
			// restoring the first entry
			// Use the Browser History Manager onReady method to restore the entry state.
			$H.onReady(function () {
				var currentState = $H.getCurrentState(_id);
				// This is the tricky part... The onLoad event is fired when the user
				// comes back to the page using the back button. 
				obj.restore(currentState);
			});
		};
		obj.restore = function (state) {
			// This is called after calling YAHOO.util.History.navigate, or after the user
			// has trigerred the back/forward button. We cannot discrminate between
			// these two situations.
			if (state && _pages.hasOwnProperty(state) && $L.isFunction(_pages[state].restore)) {
				_state = state; // keeping the track
				if (!_pages[state].active) {
if (obj.debug) console.log ('restore', state);
				  _pages[state].restore.call (_pages[state], state);
				}
				_pages[state].active = false;
			}
		};
		/**
		* * add a new state to the history stack and navigate
		* @public
		* @param {string} state
		* @param {object} userConfig
		* @return boolean
		*/
		obj.navigate = function ( state, userConfig ) {
			// encripting the state
			state = base64encode(utf16to8(state));
			var c = this.add(state, userConfig);
			var currentState = $H.getCurrentState(_id);
            // The following test is crucial. Otherwise, we end up circling forever.
            // Indeed, YAHOO.util.History.navigate will call the module onStateChange
            // callback
			
			// saving the state...
			if (state && (currentState != state)) {
if (obj.debug) console.log ('Navigate', state);
			  _pages[state].active = true;
			  $H.navigate(_id, state);
			  return true;
			} 
			return false;
		};
		/**
		* * add a new callback method for a certain state
		* @public
		* @param {string} state
		* @param {object} userConfig
		* @return Object
		*/
		obj.add = function ( state, userConfig ) {
			var c = { state: state };
            if (state && userConfig) {
			  $L.augmentObject(c, userConfig);
			  _pages[state] = c;
if (obj.debug) console.log ('Add', state);
			} 
			return c;
		};
		/**
		* * registering a tabview object in the navigation routine
		* @public
		* @param {function} default functionality for the default state...
		* @
		*/
		obj.setDefaultState = function ( f ) {
			if ($L.isFunction(f)) {
				this.add ('default', {
					state: 'default',
					restore: f
				});
				_defaultBehavior = f;				   
			}
		} 
		/**
		* * registering a tabview object in the navigation routine
		* @public
		* @param {object} oTabview
		* @
		*/
		obj.tabView = function ( oTabview ) {
			// creating the tab entries for restoration
			var tabs = oTabview.get('tabs'), i, len, oTab, s;
            for (i = 0, len = tabs.length; i < len; i++) {
                oTab = tabs[i];
				s = 'tabview-'+oTabview.get('element').id+'-tab-'+i;
				this.add (s, {
					state: s,
					tab: i,
					restore: function() { 
					   oTabView.set("activeIndex", i);
					}
				});
			}
			// creating the tabview callback to register all the navigation clicks on the tabs
			oTabview.addListener("activeTabChange", function (e) {
				var iTab = this.getTabIndex(e.newValue);
				YAHOO.plugin.Navigation.navigate ('tabview-'+oTabview.get('element').id+'-tab-'+iTab, {
					restore: function() { 
					   oTabview.set("activeIndex", iTab);
					},
					tab: iTab,
					tabview: oTabview
				});			
			});
		};		
		$E.onDOMReady (obj.init, obj, true);
		return obj;
    }();
})();
YAHOO.register("navigation", YAHOO.plugin.Navigation, {version: "2.0", build: "206"});