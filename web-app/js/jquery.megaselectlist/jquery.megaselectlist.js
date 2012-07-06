(function($)
{
	// This script was written by Steve Fenton
	// http://www.stevefenton.co.uk/Content/Jquery-Mega-Select-List/
	// Feel free to use this jQuery Plugin
	// Version: 3.0.4
    // Contributions by: 
	
	var nextModifierNumber = 0;

	$.fn.megaselectlist = function (settings) {
	
		var config = {
			classmodifier: "megaselectlist",
			headers: "rel",
			animate: false,
			animateevent: "mouseover",
			multiple: false,
			maximumitems: 0,
			warningmessage: "You can only select {0} items",
			itemseparator: ", ",
			hideloading: false,
			isxhtml: false
		};
	
		if (settings) {
			$.extend(config, settings);
		}
		
		function IsSelected(compareList, compareValue) {
			var found = false;
			for (var i = 0; i < compareList.length; i++) {
				if ($(compareList[i]).val() == compareValue) {
					found = true;
				}
			}
			return found;
		}
		
		return this.each(function () {
			var classModifier = config.classmodifier;
			nextModifierNumber++;
			
			var $This = $(this);
			
			var selectedAttribute = " selected=\"true\"";
			if (config.isxhtml) {
				selectedAttribute = " selected=\"selected\"";
			}
			
			var originalId = $This.attr("id");
			var originalName = $This.attr("name");
			var label = $("label[for='" + originalId + "']");
			var labelText = $(label).text();
			var selectedValue = $This.val();
			var selectedOptionValues = $This.find("option:selected");

			if (labelText == "") {
				label = $This.parents("label");
				labelText = $(label).clone().children().remove().end().text();
			}
			
			var replacementHtml = '<div id="' + classModifier + nextModifierNumber +'" class="' + classModifier + '">' +
				'<p tabindex="0">' + labelText + ': <span></span></p>' +
				'<div class="' + classModifier + 'options">';
			
			var currentHeader = "";
			var isHeaderOpen = false;
			var header = "";
			var options;
			var i;
			
			var optgroups = $This.children("optgroup");
			
			// If optgroups exist, use them rather than attributes
			if (optgroups.length > 0) {
				for (var og = 0; og < optgroups.length; og++) {
					header = $(optgroups[og]).attr("label");
					options = $(optgroups[og]).children("option");
					replacementHtml += '<div class="' + classModifier + 'column"><h2>' + header + '</h2><ul>';
					var tabindex = "0";
					if (config.animate) {
						tabindex = "-1";
					}
				
					for (i = 0; i < options.length; i++) {
						var x = IsSelected(selectedOptionValues, $(options[i]).val());
						var cnn = "";
						if (x) {
							cnn = ' class="currentitem"';
						}
						replacementHtml += '<li rel="' + $(options[i]).val() + '" tabindex="' + tabindex + '"' + cnn + '>' + $(options[i]).text() + '</li>';
					}
					
					replacementHtml += '</ul></div>';
				}
				
			} else {
			
				options = $This.children("option");
				for (i = 0; i < options.length; i++) {
					header = $(options[i]).attr(config.headers);
					
					if (header != currentHeader) {
						currentHeader = header;
						if (isHeaderOpen) {
							replacementHtml += '</ul></div>';
						}
						isHeaderOpen = true;
						replacementHtml += '<div class="' + classModifier + 'column"><h2>' + header + '</h2><ul>';
					}

					var x = IsSelected(selectedOptionValues, $(options[i]).val());
					var cnn = "";
					if (x) {
						cnn = ' class="currentitem"';
					}
					replacementHtml += '<li rel="' + $(options[i]).val() + '"' + cnn + '>' + $(options[i]).text() + '</li>';
				}
				if (isHeaderOpen) {
					replacementHtml += '</ul></div>';
				}
			}
			
			// The form element to contain the selected value
			replacementHtml += '<div style="clear: both;">&nbsp;</div></div>' +
				'<div style="display: none;"><select name="' + originalName + '" id="' + originalId + '" multiple="true" size="5"><option value="' + selectedValue + '"' + selectedAttribute + '>Selected</option></div>';

			$This.remove();
			$(label).hide().after(replacementHtml);
			$(label).remove();
			
			// Set span to show current selection
			var spanText = "";
			$("#" + classModifier + nextModifierNumber + " li.currentitem").each( function () {
				$("#" + originalId).append('<option value="' + $(this).attr("rel") + '"' + selectedAttribute + '>' + $(this).text() + '</option>');
				spanText += $(this).text() + config.itemseparator;
			});
			spanText = spanText.substring(0, spanText.length - config.itemseparator.length);
			
			$("#" + classModifier + nextModifierNumber + " span").text(spanText);
			
			// For keyboard use, trigger the click event when "enter" or "space" is pressed
			$("#" + classModifier + nextModifierNumber + " li").keypress( function (e) {
				if (e.keyCode == "13" || e.keyCode == "0") {
					e.preventDefault();
					$(this).trigger("click");
				} else if (e.keyCode == "33") {
					e.preventDefault();
					$("#" + classModifier + nextModifierNumber + " li:first")[0].focus();
				} else if (e.keyCode == "34") {
					e.preventDefault();
					$("#" + classModifier + nextModifierNumber + " li:last")[0].focus();
				} else if (e.keyCode == "27" && config.animate) {
					e.preventDefault();
					$("#" + classModifier + nextModifierNumber + " .megaselectlistoptions").animate({ height: "0px" });
					$("#" + classModifier + nextModifierNumber + " p:first")[0].focus();
					$("#" + classModifier + nextModifierNumber + " li").attr("tabindex", "-1");
				} else {
					//alert(e.keyCode);
				}
			});
			
			// Event handler for selection click
			$("#" + classModifier + nextModifierNumber + " li").click(function () {
				var item = $(this);
				var thisValue = $(item).attr("rel");
				
				if (!config.multiple) {
					// If this isn't a multi select, remove any other current item
					$(item).parents("." + classModifier).find(".currentitem").removeClass("currentitem");
				}
				
				if ($(item).hasClass("currentitem") && config.multiple) {
					// This is a de-select on a multiple select
					$(item).removeClass("currentitem");
				} else {
					// Set selected class on item
					$(item).addClass("currentitem");
				}
				
				if (config.animate && !config.multiple) {
					// If it is a single select, collapse the list on selection
					$(item).parent().parent().parent().parent().find(".megaselectlistoptions").animate({ height: "0px" });
					$(item).parent().parent().parent().parent().find("p:first")[0].focus();
					$(item).parent().parent().parent().parent().find("li").attr("tabindex", "-1");
				}
				
				// Remove all previous selections as we will re-add the current selections
				$("#" + originalId).children("option").remove();
				spanText = "";
				
				// Check number of selections
				if (config.multiple && config.maximumitems > 0) {
					var selectedItems = $(item).parent().parent().parent().find(".currentitem");
					if (selectedItems.length > config.maximumitems) {
						$(this).removeClass("currentitem");
						alert(config.warningmessage.replace("{0}", config.maximumitems));
					}
				}
				
				// Add all current selections
				$(item).parent().parent().parent().find(".currentitem").each( function () {
					$("#" + originalId).append('<option value="' + $(this).attr("rel") + '"' + selectedAttribute + '>' + $(this).text() + '</option>');
					spanText += $(this).text() + config.itemseparator;
				});
				spanText = spanText.substring(0, spanText.length - config.itemseparator.length);
				
				// Set span to show current selections
				$(item).parents("." + classModifier).find("span").text(spanText);
				
				return false;
			});
			
			if (config.animate) {
				var optionHeight = $("#" + classModifier + nextModifierNumber + " .megaselectlistoptions").height();
				
				if (config.hideloading) {
					$("#" + classModifier + nextModifierNumber + " .megaselectlistoptions").attr("rel", optionHeight).css({ position: "relative", overflow: "hidden" , height: "0px" });
				} else {
					$("#" + classModifier + nextModifierNumber + " .megaselectlistoptions").attr("rel", optionHeight).css({ overflow: "hidden" }).animate({ height: "0px" });
				}
			
				$("#" + classModifier + nextModifierNumber + " p:first").keypress( function (e) {
					if (e.keyCode == "13" || e.keyCode == "0") {
						e.preventDefault();
						$(this).trigger(config.animateevent);
					}
				});
			
				$("#" + classModifier + nextModifierNumber + " p:first").css({ cursor: "pointer" }).bind(config.animateevent, function () {
					var optionList = $(this).parent().find(".megaselectlistoptions");
					if ($(optionList).height() > 0) {
						var animateHeight = 0;
						$(this).parent().find("li").attr("tabindex", "-1");
					} else {
						var animateHeight = $(optionList).attr("rel");
						$(this).parent().find("li").attr("tabindex", "0");
					}
					$(optionList).animate({ height: animateHeight + "px" });
				});
			}
		});
	};
})(jQuery);