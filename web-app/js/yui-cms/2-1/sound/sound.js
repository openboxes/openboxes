/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.0
*/
(function() {

    var $B = YAHOO.Bubbling,
		$E = YAHOO.util.Event,
		$D = YAHOO.util.Dom,
		$L = YAHOO.lang;

	/**
	* @singleton Selector - The Selector Manager will monitoring the areas using the className value and will highlighting the "yui-cms-item" areas on mouseover.
	* Apply visual enhanced to an area
	* @constructor
	*/
	YAHOO.plugin.SoundManager = function() {
		var obj = {},
		    _isReady = false,
		    _sounds = {},
		    _id = 'soundManagerObject';
		    _handle = null,
			_defConfing   = {
							repeating: false,
							volume: 80,
							onLoad: null,
							onPlay: null,
							onStop: null
						 };

		function action (el, track) {
			if (el && (el.getAttribute('rel'))) {
    		    obj.play(el.getAttribute('rel'), track);
    		}
		}
		
		
		// todo: default behaviors...
		// actionPlaySound
		// actionStopSound
		// actionLoadSound

		// rollover behavior...
	    $B.on('rollover', function (layer, args) {
		  $B.processingAction (layer, args, {
    			/**
    			* Playing sound when you rollover
    			* @public
    			* @return boolean
    			*/
    			soundOnRollover: function(layer, args) {
				  action(args[1].el);
				  return false;
    			}
    	  }, true);
	    });

	    $B.on('navigate', function (layer, args) {
    	  $B.processingAction (layer, args, {
    			/**
    			* Playing sound when you rollover
    			* @public
    			* @return boolean
    			*/
    			soundOnClick: function(layer, args) {
				  action(args[1].el);
				  return false;
    			},
    			soundMasterTrack: function(layer, args) {
				  action(args[1].el, 'master');
				  return false;
    			},
				soundOnMute: function(layer, args) {
				  var c = null;
				  if (args[1].target && (c = $D.getAncestorByClassName(args[1].target, 'yui-cms-sound-control'))) {
					$D.addClass (c, 'mute');
				  }
				  obj.mute = true;
				  return true;
    			},
    			soundOnPlay: function(layer, args) {
				  var c = null;
				  if (args[1].target && (c = $D.getAncestorByClassName(args[1].target, 'yui-cms-sound-control'))) {
					$D.removeClass (c, 'mute');
				  }
				  obj.mute = false;
				  return true;
    			}
    	  }, true);
        });

	    // creating the most common message (behavior layer)
        $B.addLayer (['newSoundReady', 'soundManagerAvailable'], obj);

	    function _createFlashWrapper (uri) {
	        var content = '',
	            wrapper = "yui-cms-sm-wrapper",
	            dom = document.createElement('div');
            var el = new YAHOO.util.Element(dom, {
                id: wrapper,
                innerHTML: ''
            });
            el.appendTo(document.body);
            wrapper = $D.get (wrapper);
            if ($E.isIE) {
                content = '<object id="'+_id+'" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="1" height="1"><param name="movie" value="'+uri+'" /><param name="allowScriptAccess" value="sameDomain" /></object>';
            } else {
                content = '<embed src="'+uri+'" width="1" height="1" name="'+_id+'" allowScriptAccess="sameDomain" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" />';
            }
            wrapper.innerHTML = content;
            el.setStyle('visibility', 'hidden');
	    }
	    function _play (id) {
	        _handle.playsound(id);
	    }
	    function _stop (id) {
	        _handle.stopsound(id);
	    }
	    function _load (id) {
	        _handle.addsound(id, _sounds[id].uri);
	    }

		// public vars
		obj.mute = false;
		obj.tracks = {};

		// public methods
		/**
		* * load new sound to the available list...
		* @public
		* @param {string} id      ID for the sound
		* @param {object} conf    default configuration for the sound
		* @return void
		*/
		obj.load = function ( id, conf ) {
			if ($L.isString(id) && (id !== '') && !_sounds.hasOwnProperty(id)) {
			  _sounds[id] = conf || _defConfing;
			  // default values
			  _sounds[id].isReady = !conf.cache;// must be false at the begining
			  _sounds[id].id = id;
			  // hack for the cache issue
			  if (conf.cache) {
			    _sounds[id].uri += (_sounds[id].uri.indexOf('?')>=0?'&':'?')+'_cache='+(new Date()).getTime();
			  }
			  // flash integration:
			  if (_isReady) {
			    _load (id);
			  }
			}
		};
		/**
		* * the flash wrapper is ready (start loading sounds)...
		* @public
		* @return void
		*/
		obj.init = function() {
            // getting the Flash Object Reference
		    _handle = ($E.isIE?window[_id]:document[_id]);
		    if (!_isReady && _handle) {
    		    _isReady = true;
    		    $B.fire ('soundManagerAvailable', {wrapper: obj});
    		    // loading all the pended sounds...
    		    for (id in _sounds) {
    		        if (_sounds.hasOwnProperty(id)) { // match with a certain area
    		            _load (id);
    		        }
    		    }
    		}
		};
		/**
		* * the flash wrapper is ready (start loading sounds)...
		* @public
		* @param {string} uri      URL for the SWF wrapper...
		* @return void
		*/
		obj.create = function( uri ) {
	       $E.onDOMReady(function(){
	          if (!$D.inDocument(_id)) {
	            _createFlashWrapper(uri);
	          }
	       });
		};
		/**
		* * notificate that the sound is ready to play
		* @public
		* @param {string} id      ID for the sound
		* @return void
		*/
		obj.ready = function (id) {
		    if (_sounds.hasOwnProperty(id)) {
		        _sounds[id].isReady = true;
		        // notify that the sound is ready to the whole app
		        $B.fire ('newSoundReady', _sounds[id]);
		    }
		};
		// sound control public methods
		/**
		* * Playing an available sound...
		* @public
		* @param {string} id      ID for the sound
		* @return boolean
		*/
		obj.play = function (id, track) {
		    if (_sounds.hasOwnProperty(id) && _sounds[id].isReady && !this.mute) {
		        // checking for track
		        if ($L.isString(track)) {
		            if (obj.tracks.hasOwnProperty(track) && _sounds.hasOwnProperty(obj.tracks[track])) {
		                _stop (obj.tracks[track]);
		            }
		            obj.tracks[track] = id;
		        }
		        _play (id);
		        return true;
		    } else {
		        return false;
		    }
		};
		/**
		* * Stoping an available sound...
		* @public
		* @param {string} id      ID for the sound
		* @return boolean
		*/
		obj.stop = function (id) {
		    if (_sounds.hasOwnProperty(id) && (_sounds[id].isReady)) {
		        _stop (id);
		        return true;
		    } else {
		        return false;
		    }
		};
		/**
		* * Checking the state of a sound...
		* @public
		* @param {string} id      ID for the sound
		* @return integer   (0:not available, 1:not ready, 2: ready)
		*/
		obj.state = function (id) {
		    if (_sounds.hasOwnProperty(id)) {
		        return (_sounds[id].isReady?2:1);
		    } else {
		        return 0;
		    }
		};
		return obj;
	}();

	// flash and JS integration...
	window.soundManagerInit = function() {
        YAHOO.plugin.SoundManager.init();
    };
	window.soundManagerReady = function(id) {
        YAHOO.plugin.SoundManager.ready(id);
    };

})();
YAHOO.register("soundmanager", YAHOO.plugin.SoundManager, {version: "2.0", build: "206"});