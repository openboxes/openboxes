(function ($){
/*!
 * mcDropdown jQuery Plug-in
 *
 * Copyright 2011 Giva, Inc. (http://www.givainc.com/labs/) 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Date: 2011-07-25
 * Rev:  1.3.1
 */
	$.fn.mcDropdown = function(list, options) {
		// track the dropdown object
		var dd;

		// create a dropdown for each match
		this.each(function() {
			dd = $.data(this, "mcDropdown");
			
			// we're already a dropdown, return a reference to myself
			if( dd ) return false;

			new $.mcDropDownMenu(this, list, options);
		});
		
		// return either the dropdown object or the jQuery object reference
		return dd || this;
	};

	// set default options
	$.mcDropdown = {
		version: "1.3.1",
		setDefaults: function(options){
			$.extend(defaults, options);
		}
	};

	// set the defaults
	var defaults = {
		  minRows: 8                   // specify the minimum rows before creating a new column
		, maxRows: 25                  // specify the maximum rows in a column
		, targetColumnSize: 2          // specify the default target column size (it'll attempt to create this many columns by default, unless the min/max row rules are not being met)
		, openFx: "slideDown"          // the fx to use for showing the root menu
		, openSpeed: 250               // the speed of the openFx
		, closeFx: "slideUp"           // the fx to use for hiding the root menu
		, closeSpeed: 250              // the speed of the closeFx
		, hoverOverDelay: 200          // the delay before opening a submenu
		, hoverOutDelay: 0             // the delay before closing a submenu
		, showFx: "show"               // the fx to use when showing a submenu
		, showSpeed: 0                 // the speed of the showFx
		, hideFx: "hide"               // the fx to use when closing a submenu
		, hideSpeed: 0                 // the speed of the hideFx
		, dropShadow: true             // determine whether drop shadows should be shown on the submenus
		, autoHeight: true             // always uses the lineHeight options (much faster than calculating height)
		, lineHeight: 19               // the base height of each list item (li) this is normally calculated automatically, but in some cases the value can't be determined and you'll need to set it manually
		, screenPadding: 10            // the padding to use around the border of the screen -- this is used to make sure items stay on the screen
		, allowParentSelect: false     // determines if parent items are allowed to be selected (by default only end nodes can be selected)
		, delim: ":"                   // the delimited to use when showing the display string
		, showACOnEmptyFocus: false    // show the autocomplete box on focus when input is empty
		, valueAttr: "rel"             // the attribute that contains the value to use in the hidden field
		, click: null                  // callback that occurs when the user clicks on a menu item
		, select: null                 // callback that occurs when a value is selected
		, init: null                   // callback that occurs when the control is fully initialized
	};

	// check to see if the browser is IE6	
	var isIE6 = ($.browser.version && $.browser.version <= 6);

	$.mcDropDownMenu = function(el, list, options){
		var $self, thismenu = this, $list, $divInput, settings, typedText = "", matchesCache, oldCache, $keylist, $keylistiframe, bInput, bDisabled = false;
		
		// create a reference to the dropdown
		$self = $(el);
		
		// is the field and input element
		bInput = $self.is(":input");
		
		// get the settings for this instance
		settings = $.extend({}, defaults, options);
		
		// set the default click behavior
		if( settings.click == null ) {
			settings.click = function (e, dropdown, settings){
				if( this.attr(settings.valueAttr) ){
					dropdown.setValue(this.attr(settings.valueAttr));
				} else {
					dropdown.setValue($(this.parents("li")[0]).attr(settings.valueAttr));
				}
			};
		}
	
		// attach window behaviors
		$(document)
			// Bind a click event to hide all visible menus when the document is clicked
			.bind("click", function(e){
				// get the target that was clicked
				var $target = $(e.target);
				var $ul = $target.parents().filter(function (){ return this === $list[0] || (!!$keylist && $keylist[0] === this); });
				// check to make sure the clicked element was inside the list
				if( $ul.length ){
					var bIsParent = $target.is(".mc_parent");
					
					// if we've clicked a parent item in the autocomplete box, we must adjust the current value
					if( bIsParent && $keylist && $ul[0] === $keylist[0] ){
						updateValue($target.find("> ul > li:first"), false);
						e.stopPropagation();
						return false;
					}
					// check to see if the user can click on parent items
					else if( !settings.allowParentSelect && bIsParent ) return false;

					// make sure to hide the parent branch if we're not the root
					if( $target.not(".mc_root") ) hideBranch.apply($target.parent().parent()[0], [e]);
					
					if( settings.click != null && settings.click.apply($target, [e, thismenu, settings]) == false ){
						return false;
					}
				}
				
				// close the menu
				thismenu.closeMenu();
			});
			
		// store a reference to the list, if it's not already a jQuery object make it one
		$list = (((typeof list == "object") && !!list.jquery)) ? list : $(list);
		
		// we need to calculate the visual width for each nested list
		$list
			// move list to body -- this allows us to always calculate the correct position & width of the elements
			.appendTo("body")
			// move the list way off screen
			.css({position: "absolute", top: -10000, left: -10000})
			// find all the ul tags
			.find("ul")
			// add the root ul tag to the array
			.andSelf()
			// make all the nodes visible
			.css("display", "block")
			// loop through each node
			.each(function (){
				var $el = $(this);
				// calculate the width of the element -- using clientWidth is 2x as fast as width()
				$el.data("width", $el[0].clientWidth);
			})
			// now that we've gotten the widths, hide all the lists and move them to x:0, y:0
			.css({top: 0, left: 0, display: "none"});
			
		// mark the root children items
		$list.find("> li").addClass("mc_root");
		// add parent class
		$("li > ul", $list).parent().addClass("mc_parent");

		// create the div to wrap everything in
		$divInput = $('<div class="mcdropdown"><a href="#" tabindex="-1"></a><input type="hidden" name="' + (el.name || el.id) + '" id="' + (el.id || el.name) + '" /></div>')
			.appendTo($('<div style="position: relative;"></div>'))
			.parent();
			
		// get a reference to the input element and remove it from the DOM				
		var $input = $self.replaceWith($divInput).attr({id: "", name: ""});
		// get a reference to the hidden form field
		var $hidden = $divInput.find(":input");
		
		// put the input element back in the div.mcdropdown layer
		$divInput = $divInput.find(".mcdropdown").prepend($input);
		
		// make a visible copy of the element so we can get the correct sizes, then delete it
		var $divInputClone = $divInput.clone().css({position: "absolute", top: -9999999, left: -999999, visibility: "visible"}).show().appendTo("body");
		var di = {width: $divInputClone.width() - $("a", $divInputClone).width(), height: $divInputClone.outerHeight()}
		$divInputClone.remove();
		
		// store a reference to this link select
		$.data($hidden[0], "mcDropdown", thismenu);

		// update the height of the outer relative div, this allows us to
		// correctly anchor the dropdown
		$divInput.parent().height(di.height);

		// safari will not get the correct width until after everything has rendered		
		if( $.browser.safari ){
			setTimeout(function (){
				$self
				.width($divInput.width() - $("a", $divInput).width());
			}, 100);
		}
		
		// adjust the width of the new input element
		$self
			.width(di.width)
			// make sure we only attach the next events if we're in input element
			.filter(":input")
			// turn autocomplete off
			.attr("autocomplete", "off")
			// add key stroke bindings (IE6 requires keydown)
			.bind("keypress", checkKeypress)
			// prevent user from selecting text
			.bind("mousedown", function (e){ $(this).triggerHandler("focus"); e.stopPropagation(); return false; })
			// disable context menu
			.bind("contextmenu", function (){ return false; })
			// select the text when the cursor is placed in the field
			.bind("focus", onFocus)
			// when the user leaves the text field
			.bind("blur", onBlur);
			
		// IE6 doesn't register certain keypress events, so we must catch them during the keydown event
		if( $.browser.msie || $.browser.safari) $self.bind("keydown", function (e){
			// check to see if a key was pressed that IE6 doesn't trigger a keypress event for
			if( ",8,9,37,38,39,40,".indexOf("," + e.keyCode + ",") > -1 ) return checkKeypress(e);
		});
		
		// attach a click event to the anchor
		$("a", $divInput).bind("click", function (e){
			// if disabled, skip processing
			if( bDisabled ) return false;
			thismenu.openMenu(e);
			return false;
		});
		
		// set the value of the field
		this.setValue = function (value, skipCallback){
			// update the hidden value
			$hidden.val(value);
			// get the display name
			var name = displayString(value);
			
			// run the select callback (some keyboard entry methods will manage this callback manually)
			if( settings.select != null && skipCallback != true ) settings.select.apply(thismenu, [value, name]);
			
			// update the display value and return the jQuery object
			return $self[bInput ? "val" : "text"](name);
		};
		
		// set the default value (but don't run callback)
		if( bInput ) this.setValue($self[0].defaultValue, true);
	
		// get the value of the field (returns array)
		this.getValue = function (value){
			return [$hidden.val(), $self[bInput ? "val" : "text"]()];
		};
		
		// open the menu programmatically
		this.openMenu = function (e){
			// if the menu is open, kill processing
			if( $list.is(":visible") ){
				// on a mouse click, close the menu, otherwise just cancel
				return (!!e) ? thismenu.closeMenu() : false;
			}
			
			function open(){
				// columnize the root list
				columnizeList($list).hide();
				// add the bindings to the menu
				addBindings($list);
				
				// anchor the menu relative parent
				anchorTo($divInput.parent(), $list, true);
				
				// remove existing hover classes, which might exist from keyboard entry
				$list.find(".mc_hover").removeClass("mc_hover");

				// show the menu
				$list[settings.openFx](settings.openSpeed, function (){
					// scroll the list into view
					scrollToView($list);
				});

				// if the bgIframe exists, use the plug-in
				if( isIE6 && !!$.fn.bgIframe ) $list.bgIframe();
			}
			
			// if this is triggered via an event, just open the menu
			if( e ) open();
			// otherwise we need to open the menu asynchronously to avoid collision with $(document).click event
			else setTimeout(open, 1);
		};
		
		// close the menu programmatically
		this.closeMenu = function (e){
			// hide any open menus
			$list.find("ul:visible").parent().each(function (){
				hideBranch.apply(this);
			});
			
			// remove the bindings
			removeBindings($list);
	
			// close the menu
			$list[settings.closeFx](settings.closeSpeed);
		};
		
		// place focus in the input box
		this.focus = function (){
			$self.focus();
		};
	
		// disable the element
		this.disable = function (status){
			// change the disabled status
			bDisabled = !!status;
			
			$divInput[bDisabled ? "addClass" : "removeClass"]("mcdropdownDisabled");
			$input.attr("disabled", bDisabled);
		};
		
		function getNodeText($el){
			var nodeContent;
			var nContents = $el.contents().filter(function() {
				// remove empty text nodes and comments
				return (this.nodeType == 1) || (this.nodeType == 3 && $.trim(this.nodeValue).length>0);
			});
			// return either an empty string or the node's value
			if (nContents[0] && nContents[0].nodeType == 3) {
				// Text node : take it's value
				nodeContent = nContents[0].nodeValue; 
			} else if (nContents[0] && nContents[0].nodeType == 1) {
				// Element node : take the contents
				nodeContent = $(nContents[0]).text();
			} else {
				nodeContent = "";
			}
			return $.trim(nodeContent);
		};
		
		function getTreePath($li){
			if( $li.length == 0 ) return [];
	
			var name = [getNodeText($li)];
			// loop through the parents and get the value
			$li.parents().each(function (){
				var $el = $(this);
				// break when we get to the main list element
				if( this === $list[0] ) return false;
				else if( $el.is("li") ) name.push(getNodeText($el));
			});
	
			// return the display name
			return name.reverse();
		};
		
		function displayValue(value){
			// return the path as an array
			return getTreePath(getListItem(value));
		};
		
		function displayString(value){
			// return the display name
			return displayValue(value).join(settings.delim);
		};
		
		function parseTree($selector){
			var s = [], level = (arguments.length > 1) ? ++arguments[1] : 1;
	
			// loop through all the children and store information about the tree
			$("> li", $selector).each(
				function (){
					// get a reference to the current object
					var $self = $(this);
	
					// look for a ul tag as a direct child
					var $ul = $("> ul", this);
					
					// push a reference to the element to the tree array
					s.push({
						// get the name of the node
						  name:     getNodeText($self)
						// store a reference to the current element
						, element:  this
						// parse and store any children items
						, children: ($ul.length) ? parseTree($ul, level) : []
					});
	
				}
			);
			
			return s;
		};
		
		function addBindings(el){
			removeBindings(el);
			$("> li", el)
				.bind("mouseover", hoverOver)
				.bind("mouseout", hoverOut);
		};
		
		function removeBindings(el){
			$("> li", el)
				.unbind("mouseover", hoverOver)
				.unbind("mouseout", hoverOut);
		};
		
		// scroll the current element into view
		function scrollToView($el){
			// get the current position
			var p = position($el, true);
			// get the screen dimensions
			var sd = getScreenDimensions();
			
			// if we're hidden off the bottom of the page, move up
			if( p.bottom > sd.y ){
				$("html,body").animate({"scrollTop": "+=" + ((p.bottom - sd.y) + settings.screenPadding) + "px" })
			}
		};
		
		function hoverOver(e){
			var self = this;
			var timer = $.data(self, "timer");
			
			// if the timer exists, clear it
			if( !isNaN(timer) ) clearTimeout(timer);
	
			// if IE6, add the hover class
			$(this).addClass("mc_hover");

			// show the branch
			$.data(self, "timer", setTimeout(function(){
					showBranch.apply(self);
				}, settings.hoverOverDelay)
			);
	  };
		
		function hoverOut(e){
			var self = this;
			var timer = $.data(self, "timer");
			
			// if the timer exists, clear it
			if( !isNaN(timer) ) clearTimeout(timer);
	
			// if IE6, remove the hover class
			$(this).removeClass("mc_hover");
			
			// hide the branch
			$.data(self, "timer", setTimeout(function(){
					var $li = $(self);
					setTimeout(function (){
						// if no children selected, we must close the parent menus
						if( $li.parent().find("> li.mc_hover").length == 0 ){
							$li.parents("li").each(function (){
								var self = this;
								clearTimeout($.data(self, "timer"));
								hideBranch.apply(self);
								// check to see if we've hovered over a parent item
								if( $(this).siblings().filter(".mc_hover").length > 0 ) return false;
							});
					}
						
					}, settings.hoverOverDelay);

					hideBranch.apply(self);
				}, settings.hoverOutDelay)
			);
			
			// this will stop flickering in IE6, but it leaves mc_hover classes behind
			if( isIE6 ) e.stopPropagation();
	  };
		
		function getShadow(depth){
			var shadows = $self.data("shadows");
			
			// if the shadows don't exist, create an object to track them
			if( !shadows ) 
				shadows = {};
			
			// if the shadow doesn't exist, create it
			if( !shadows[depth] ){
				// create shadow
				shadows[depth] = $('<div class="mcdropdown_shadow"></div>').appendTo('body');
				// if the bgIframe exists, use the plug-in
				if( !!$.fn.bgIframe ) shadows[depth].bgIframe();
				// update the shadows cache
				$self.data("shadows", shadows);
			}
			
			return shadows[depth];
		};
		
		function showBranch(){
			var self = this;
			// the child menu
			var $ul = $("> ul", this);
			
			// if the menu is already visible or there is no submenu, cancel
			if( $ul.is(":visible") || ($ul.length == 0) ) return false;
			
			// hide any visible sibling menus
			$(this).parent().find('> li ul:visible').not($ul).parent().each(function(){
				hideBranch.apply(this);
			});
			
			// columnize the list
			columnizeList($ul);
			
			// add new bindings
			addBindings($ul);
	
			var depth = $ul.parents("ul").length;
			
			// get the screen dimensions
			var sd = getScreenDimensions();
			
			// get the coordinates for the menu item
			var li_coords = position($(this));
	
			// move the menu to the correct position and show the menu || ((depth)*2)
			$ul.css({top: li_coords.bottom, left: li_coords.marginLeft/*, zIndex: settings.baseZIndex + ((depth)*2)*/}).show();
	
			// get the bottom of the menu
			var menuBottom = $ul.outerHeight() + $ul.offset().top;
	
			// if we're hidden off the bottom of the page, move up
			if( menuBottom > sd.y ){
				// adjust the menu by subtracting the bottom edge by the screen offset
				$ul.css("top", li_coords.bottom - (menuBottom - sd.y) - settings.screenPadding);
			}
			
			var showShadow = function (){
				// if using drop shadows, then show them		
				if( settings.dropShadow ){
					// get a reference to the current shadow
					var $shadow = getShadow(depth);
					// get the position of the parent element
					var pos = position($ul);
					
					// move the shadow to the correct visual & DOM position
					$shadow.css({
						  top: pos.top + pos.marginTop
						, left: pos.left + pos.marginLeft
						, width: pos.width
						, height: pos.height
						/*, zIndex: settings.baseZIndex + ((2*depth)-1)*/
					}).insertAfter($ul).show();
			
					// store a reference to the shadow so we can hide it		
					$.data(self, "shadow", $shadow);
				}
			}
			
			// columnize the list and then show it using the defined effect
			// if the menu has a zero delay, just open it and then draw the
			// shadow, otherwise show the effect and the draw the shadow
			// after you're done.
			if( settings.showSpeed <= 0 ){
				showShadow();
			} else {
				$ul.hide()[settings.showFx](settings.showSpeed, showShadow);
			}
		};
		
		function hideBranch(){
			var $ul = $("> ul", this);
			// if the menu is already visible or there is no submenu, cancel
			if( $ul.is(":hidden") || ($ul.length == 0) ) return false;
			
			// if using drop shadows, then hide
			if( settings.dropShadow && $.data(this, "shadow") ) $.data(this, "shadow").hide();
	
			// if we're IE6, we need to set the visiblity to "hidden" so child
			// menus are correctly hidden and remove the .mc_hover class due to
			// the e.stopPropagation() call in the hoverOut() call
			if( isIE6 )
				$ul.css("visibility", "hidden").parent().removeClass("mc_hover");
	
			// hide the menu
			$ul.stop()[settings.hideFx](settings.hideSpeed);
		};
	
		function position($el, bUseOffset){
			var bHidden = false;
			// if the element is hidden we must make it visible to the DOM to get
			if ($el.is(":hidden")) {
				bHidden = !!$el.css("visibility", "hidden").show();
			}
			
			var pos = $.extend($el[bUseOffset === true ? "offset" : "position"](),{
				  width: $el.outerWidth()
				, height: $el.outerHeight()
				, marginLeft: parseInt($.curCSS($el[0], "marginLeft", true), 10) || 0
				, marginRight: parseInt($.curCSS($el[0], "marginRight", true), 10) || 0
				, marginTop: parseInt($.curCSS($el[0], "marginTop", true), 10) || 0
				, marginBottom: parseInt($.curCSS($el[0], "marginBottom", true), 10) || 0
			});
			
			if( pos.marginTop < 0 ) pos.top += pos.marginTop;
			if( pos.marginLeft < 0 ) pos.left += pos.marginLeft;
			
			pos["bottom"] = pos.top + pos.height;
			pos["right"] = pos.left + pos.width;
			
			// hide the element again
			if( bHidden ) $el.hide().css("visibility", "visible");
	
			return pos;
		};
		
		function anchorTo($anchor, $target, bUseOffset){
			var pos = position($anchor, bUseOffset);
			
			$target.css({
				  position: "absolute"
				, top: pos.bottom
				, left: pos.left
			});
			
			/*
			 * we need to return the top edge of the core drop down menu, because
			 * the top:0 starts at this point when repositioning items absolutely
			 * this means we have to offset everything by the offset of the top menu
			 */ 
			
			return pos.bottom;
		};
		
		function getScreenDimensions(){
			var d = {
				  scrollLeft: $(window).scrollLeft()
				, scrollTop:  $(window).scrollTop()
				, width:      $(window).width()     // changed from innerWidth
				, height:     $(window).height()    // changed from innerHeight			
			};
			
			// calculate the correct x/y positions
			d.x = d.scrollLeft + d.width;
			d.y = d.scrollTop + d.height;
			
			return d;
		};
		
		function getPadding(el, name){
			var torl = name == 'height' ? 'Top'    : 'Left',  // top or left
			    borr = name == 'height' ? 'Bottom' : 'Right'; // bottom or right
			
			return (
				// we add "0" to each string to make sure parseInt() returns a number
			    parseInt("0"+$.curCSS(el, "border"+torl+"Width", true), 10)
				+ parseInt("0"+$.curCSS(el, "border"+borr+"Width", true), 10)
				+ parseInt("0"+$.curCSS(el, "padding"+torl, true), 10)
				+ parseInt("0"+$.curCSS(el, "padding"+borr, true), 10)
				+ parseInt("0"+$.curCSS(el, "margin"+torl, true), 10)
				+ parseInt("0"+$.curCSS(el, "margin"+borr, true), 10)
			);
		};
		
		function getListDimensions($el, cols){
			if( !$el.data("dimensions") ){
				// get the width of the dropdown menu
				var ddWidth = $divInput.outerWidth();
				// if showing the root item, then try to make sure the width of the menu is sized to the drop down menu
				var width = ( ($el === $list) && ($el.data("width") * cols < ddWidth) ) ? Math.floor(ddWidth/cols) : $el.data("width");
		
				$el.data("dimensions", {
					// get the original width of the list item
					column: width
					// subtract the padding from the first list item from the width to get the width of the items
					, item: width - getPadding($el.children().eq(0)[0], "width")
					// get the original height
					, height: $el.height()
				});
			}
			
			return $el.data("dimensions");
		};
		
		function getHeight($el){
			// skip height calculation and use lineHeight
			if( settings.autoHeight === false ) return settings.lineHeight;
			// if we haven't cached our height, do so now
			if( !$el.data("height") ) $el.data("height", $el.outerHeight());
	
			// return the cached value
			return $el.data("height");
		};
		
		function columnizeList($el){
			// get the children items
			var $children = $el.find("> li");
			// get the total number of items
			var items = $children.length;
			
			// calculate how many columns we think we should have based on the max rows
			var calculatedCols = Math.ceil(items/settings.maxRows);
			// get the number of columns, don't columnize if we don't have enough rows
			// if the height of the column is bigger than the screen, we automatically try 
			// moving to a new column
			var cols = !!arguments[1] ? arguments[1] : ( items <= settings.minRows ) ? 1 : (calculatedCols > settings.targetColumnSize) ? calculatedCols : settings.targetColumnSize;
			// get the dimension of this element
			var widths = getListDimensions($el, cols);
			var prevColumn = 0;
			var columnHeight = 0;
			var maxColumnHeight = 0;
			var maxRows = Math.ceil(items/cols);
	
			// get the width of the parent item
			var parentLIWidth = $el.parent("li").width();
			
			// we need to draw the list element, but hide it so we can correctly calculate it's information
			$el.css({"visibility": "hidden", "display": "block"});
			
			// loop through each child item
			$children.each(function (i){
				var currentItem = i+1;
				var nextItemColumn = Math.floor((currentItem/items) * cols);
				// calculate the column we're in
				var column = Math.floor((i/items) * cols);
				// reference the current item
				var $li = $(this);
				// variable to track margin-top
				var marginTop;
	
				// if we're in the same column
				if( prevColumn != column ){
					// move to the top of the next column
					marginTop = (columnHeight+1) * -1;
					// reset column height
					columnHeight = 0;
				// if we're in a new column
				} else {
					marginTop = 0;
				}
				
				// increase the column height based on it's current height (calculate this before adding classes)
				columnHeight += (getHeight($li) || settings.lineHeight);
				
				// update the css settings
				$li.css({
		  		"marginLeft": (widths.column * column)
					, "marginTop": marginTop
					, "width": widths.item
		  	})
					[((nextItemColumn > column) || (currentItem == items)) ? "addClass" : "removeClass"]("mc_endcol")
					[(marginTop != 0) ? "addClass" : "removeClass"]("mc_firstrow")
					;
				// get the height of the longest column			
				if( columnHeight > maxColumnHeight ) maxColumnHeight = columnHeight;
	
				// update the previous column
				prevColumn = column;
			});
	
			// if the menu is too tall to fit on the screen, try adding another column
			if( ($el !== $list) && (maxColumnHeight + (settings.screenPadding*2) >= getScreenDimensions().height) ){
				return columnizeList($el, cols+1);
			}
	
			/*
			 * set the height of the list to the max column height. this fixes
			 * display problems in FF when the last column is not full.	
			 * 
			 * we also need to set the visiblity to "visible" to make sure that
			 * the element will show up	
			 */ 
			$el.css("visibility", "visible").height(maxColumnHeight);
			
			return $el;
		};
		
		function getListItem(value){
			return $list.find("li[" + settings.valueAttr + "='"+ value +"']");
		};
		
		function getCurrentListItem(){
			return getListItem($hidden.val());
		};
		
		function onFocus(e){
			var $current = getCurrentListItem();
			var value = $self.val().toLowerCase();
			var treePath = value.toLowerCase().split(settings.delim);
			var currentNode = treePath.pop();
			var lastDelim = value.lastIndexOf(settings.delim) + 1;
			
			// reset the typed text
			typedText = treePath.join(settings.delim) + (treePath.length > 0 ? settings.delim : "");
	
			// we need to set the selection asynchronously so that when user TABs to field the pre-select isn't overwritten
			setTimeout(function (){
				// preselect the last child node
				setSelection($self[0], lastDelim, lastDelim+currentNode.length);
			}, 0);
			
			// create the keyboard hint list
			if( !$keylist ){
				$keylist = $('<ul class="mcdropdown_autocomplete"></ul>').appendTo("body");
				// if IE6 we need an iframe to hide the scrolling list
				if( isIE6 && !!$.fn.bgIframe ) $keylistiframe = $('<div></div>').bgIframe().appendTo("body");
			} 
			
			// should we show matches?
			var hideResults = !(settings.showACOnEmptyFocus && (typedText.length == 0));

			// get the siblings for the current item
			var $siblings = ($current.length == 0 || $current.hasClass("mc_root")) ? $list.find("> li") : $current.parent().find("> li");
			// show all matches
			showMatches($siblings, hideResults);
		};
		
		var iBlurTimeout = null;
		function onBlur(e){
			// only run the last blur event
			if( iBlurTimeout ) clearTimeout(iBlurTimeout);
			// we may need to cancel this blur event, so we run it asynchronously
			iBlurTimeout = setTimeout(function (){
				// get the current item
				var $current = getCurrentListItem();
				
				// if we must select a child item, then update to the first child we can find
				if( !settings.allowParentSelect && $current.is(".mc_parent") ){
					// grab the first end child item we can find for the current path
					var value = $current.find("li:not('.mc_parent'):first").attr(settings.valueAttr);
					// update the value
					thismenu.setValue(value, true);
				}
				
				// run the select callback
				if( settings.select != null ) settings.select.apply(thismenu, thismenu.getValue());
				
				// hide matches
				hideMatches();
				
				// mark event as having run
				iBlurTimeout = null;
			}, 200);
		};
		
		function showMatches($li, hideResults){
			var bCached = ($li === oldCache), $items = bCached ? $keylist.find("> li").removeClass("mc_hover mc_hover_parent mc_firstrow") : $li.clone().removeAttr("style").removeClass("mc_hover mc_hover_parent mc_firstrow mc_endcol").filter(":last").addClass("mc_endcol").end();
	
			// only do the following if we've updated the cache or the list is hidden
			if( !bCached || $keylist.is(":hidden") ){
				// update the matches
				$keylist.empty().append($items).width($divInput.outerWidth() - getPadding($keylist[0], "width")).css("height", "auto");
				
				// anchor the menu relative parent
				anchorTo($divInput.parent(), $keylist, true);

				// show hover on mouseover				
				$items.hover(function (){$keylist.find("> li").removeClass("mc_hover_parent mc_hover"); $(this).addClass("mc_hover")}, function (){$(this).removeClass("mc_hover")});

				// make sure the the ul's are hidden (so the li's are sized correctly)				
				$items.find("> ul").css("display", "none");
		
				// show the list
				$keylist.show().css("visibility", (hideResults === true) ? "hidden" : "visible");
	
				// if we're IE6, ensure we enforce the "max-height" CSS property			
				if( isIE6 ){
					var maxHeight = parseInt($keylist.css("max-height"), 10) || 0;
					if( (maxHeight > 0) && (maxHeight < $keylist.outerHeight()) ) $keylist.height(maxHeight);
					
					// anchor the iframe behind the scrollable list
					if( !!$.fn.bgIframe ) anchorTo($divInput.parent(), $keylistiframe.css({height: $keylist.outerHeight(), width: $keylist.width()}, true).show())
				}
	
				// scroll the list into view
				if( hideResults !== true ) scrollToView($keylist);
			}
			
			// do not show the list on screen
			if( hideResults === true ){
				// hide the results and move them offscreen (so it doesn't hide the cursor in FF2)
				$keylist.css({top: "-10000px", left: "-10000px"});
				// hiden the iframe overlay
				if( isIE6 && !!$.fn.bgIframe ) $keylistiframe.css("display", "none");
			}

			// get the currently selected item
			var $current = $keylist.find("li[" + settings.valueAttr + "='"+ $hidden.val() +"']");
			
			// make sure the last match is still highlighted
			$current.addClass("mc_hover" + ($current.is(".mc_parent")? "_parent" : ""));
			
			// scroll the item into view
			if( $current.length > 0 && (hideResults != true) ) scrollIntoView($current);
			
			// update the cache
			oldCache = matchesCache = $li;
		};
		
		function hideMatches(){
			// hide the bgiframe
			if( isIE6 && !!$.fn.bgIframe && $keylistiframe ) $keylistiframe.hide();
			if( $keylist ) $keylist.hide();
		};
		
		// check the user's keypress
		function checkKeypress(e){
			var key = String.fromCharCode(e.keyCode || e.charCode).toLowerCase();
			var $current = getCurrentListItem();
			var $lis = ($current.length == 0 || $current.hasClass("mc_root")) ? $list.find("> li") : $current.parent().find("> li");
			var treePath = typedText.split(settings.delim);
			var currentNode = treePath.pop();
			var compare = currentNode + key;
			var selectedText = getSelection($self[0]).toLowerCase();
			var value = $self.val().toLowerCase();
			
			// if the up arrow was pressed
			if( e.keyCode == 38 ){
				moveMatch(-1);
				return false;
	
			// if the down arrow was pressed
			} else if( e.keyCode == 40 ){
				moveMatch(1);
				return false;
	
			// if the [ESC] was pressed
			} else if( e.keyCode == 27 ){
				// clear typedText
				typedText = "";
				// clear the value
				thismenu.setValue("");
				// show the root level
				showMatches($list.find("> li"));
			
				return false;
	
			// if user pressed [DEL] or [LEFT ARROW], go remove last typed character		
			} else if( e.keyCode == 8 || e.keyCode == 37 ){
				// if left arrow, go back to previous parent
				compare = (e.keyCode == 37) ? "" : currentNode.substring(0, currentNode.length - 1);
				
				// if all the text is highlighted we just came from a delete
				if( selectedText == currentNode ){
					currentNode = "";
				}
				// we're going backwards to the last parent, move backwards
				if( treePath.length > 0 && currentNode.length == 0){
					updateValue($current.parent().parent());
					return false;
				// if all the text is selected, remove everything
				} else if( selectedText == value ){
					typedText = "";
					thismenu.setValue("");
					return false;
				}
			// if the user pressed [ENTER], [TAB], [RIGHT ARROW] or the delimiter--go to next level
			} else if( e.keyCode == 9 || e.keyCode == 13 || e.keyCode == 39 || key == settings.delim ){
				// get the first child item if there is one
				var $first = $current.find("> ul > li:first");
	
				// update with the next child branch
				if( $first.length > 0 ){
					updateValue($first);
				// leave the field
				} else {
					// if IE6, we must deselect the selection
					if( $.browser.msie ) setSelection($self[0], 0, 0);
					if( e.keyCode == 9 ){
						// blur out of the field
						$self.triggerHandler("blur");
						// hide the matches
						hideMatches();
						// allow the tab
						return true;
					} else {
						// blur out of the field
						$self.trigger("blur");
						// hide the matches
						hideMatches();
					}
				}
	
				return false;
			// if all the text is highlighted then we need to delete everything
			} else if( selectedText == value ){
				typedText = "";
				compare = key;
			}
	
			// update the match cache with all the matches
			matchesCache = findMatches($lis, compare);
			
			// if we have some matches, populate autofill and show matches
			if( matchesCache.length > 0 ){
				// update the a reference to what the user's typed
				typedText = treePath.join(settings.delim) + (treePath.length > 0 ? settings.delim : "") + compare;
				updateValue(matchesCache.eq(0), true);
			} else {
				// find the previous compare string
				compare = compare.length ? compare.substring(0, compare.length-1) : "";
			
				// since we have no matches, get the previous matches
				matchesCache = findMatches($lis, compare);
	
				// if we have some matches, show them
				if( matchesCache.length > 0 )
					showMatches(matchesCache);
				// hide the matches
				else
					hideMatches();
			}

			// stop default behavior
			e.preventDefault();
			
			return false;
		};
		
		function moveMatch(step){
			// find the current item in the matches cache
			var $current = getCurrentListItem(), $next, pos = 0;
			
			// if nothing selected, look for the item with the hover class
			if( $current.length == 0 ) $current = matchesCache.filter(".mc_hover, .mc_hover_parent");
			// if still nothing, grab the first item in the cache
			if( $current.length == 0 || $keylist.is(":hidden") ){
				// grab the first item
				$current = matchesCache.eq(0);
				// since nothing is selected, don't step forward/back
				step = 0;
			} 
	
			// find the current position of the element		
			matchesCache.each(function (i){ 
				if( this === $current[0]){
					pos = i;
					return false;
				}
			});
	
			// if no matches, cancel
			if( !matchesCache || matchesCache.length == 0 || $current.length == 0 ) return false;
			
			// adjust by the step count
			pos = pos + step;
	
			// make sure pos is in valid bounds		
			if( pos < 0 ) pos = matchesCache.length-1;
			else if( pos >= matchesCache.length ) pos = 0;
			
			// get the next item
			$next = matchesCache.eq(pos);
			
			updateValue($next, true);
		};
		
		function findMatches($lis, compare){
			var matches = $([]); // $([]) = empty jquery object
			
			$lis.each(function (){
				// get the current list item and it's label
				var $li = $(this), label = getNodeText($li);
	
				// label matches what the user typed, add it to the queue
				if( label.substring(0, compare.length).toLowerCase() == compare ){
					// store a copy to this jQuery item
					matches = matches.add($li);
				}
			});
	
			// return the matches found		
			return matches;
		};
		
		function updateValue($li, keepTypedText){
			// grab all direct children items
			var $siblings = keepTypedText ? matchesCache : ($li.length == 0 || $li.hasClass("mc_root")) ? $list.find("> li") : $li.parent().find("> li");
			var treePath = getTreePath($li);
			var currentNode = treePath.pop().toLowerCase();
	
			// update the a reference to what the user's typed
			if( !keepTypedText ) typedText = treePath.join(settings.delim).toLowerCase() + (treePath.length > 0 ? settings.delim : "");
	
			// update form field and display with the updated value
			thismenu.setValue($li.attr(settings.valueAttr), true);
			
			// pre-select the last node
			setSelection($self[0], typedText.length, currentNode.length+typedText.length);
			
			// remove any currently selected items
			$siblings.filter(".mc_hover,.mc_hover_parent").removeClass("mc_hover mc_hover_parent");
			// add the hover class
			$li.addClass("mc_hover" + ($li.is(".mc_parent")? "_parent" : ""));
			
			// show all the matches
			showMatches($siblings);
		};
	
		// get the text currently selected by the user in a text field
		function getSelection(field){
			var text = "";
			if( field.setSelectionRange ){
				text = field.value.substring(field.selectionStart, field.selectionEnd);
			} else if( document.selection ){
				var range = document.selection.createRange();
				if( range.parentElement() == field ){
					text = range.text;
				}
			}
			return text;
		};
	
		// set the text selected in a text field
		function setSelection(field, start, end) {
			if( field.createTextRange ){
				var selRange = field.createTextRange();
				selRange.collapse(true);
				selRange.moveStart("character", start);
				selRange.moveEnd("character", end);
				selRange.select();
			} else if( field.setSelectionRange ){
				field.setSelectionRange(start, end);
			} else {
				if( field.selectionStart ){
					field.selectionStart = start;
					field.selectionEnd = end;
				}
			}
			field.focus();
		};
	
		function scrollIntoView($el, center){
			var el = $el[0];
			var scrollable = $keylist[0];
			// get the padding which is need to adjust the scrollTop
			var s = {pTop: parseInt($keylist.css("paddingTop"), 10)||0, pBottom: parseInt($keylist.css("paddingBottom"), 10)||0, bTop: parseInt($keylist.css("borderTopWidth"), 10)||0, bBottom: parseInt($keylist.css("borderBottomWidth"), 10)||0};
	
			// scrolling down
			if( (el.offsetTop + el.offsetHeight) > (scrollable.scrollTop + scrollable.clientHeight) ){
				scrollable.scrollTop = $el.offset().top + (scrollable.scrollTop - $keylist.offset().top) - ((scrollable.clientHeight/((center == true) ? 2 : 1)) - ($el.outerHeight() + s.pBottom));
			// scrolling up
			} else if( el.offsetTop - s.bTop - s.bBottom <= (scrollable.scrollTop + s.pTop + s.pBottom) ){
				scrollable.scrollTop = $el.offset().top + (scrollable.scrollTop - $keylist.offset().top) - s.pTop;
			}
		};
		
		// run the init callback (some keyboard entry methods will manage this callback manually)
		if( settings.init != null ) settings.init.apply(thismenu, [$input, $hidden, $list]);

  };
	
})(jQuery);
