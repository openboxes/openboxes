/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse


class AutoSuggestEditableTagLib {


    def autoSuggestEditable = { attrs, body ->
        def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
        def name = attrs.name
        def valueId = (attrs.valueId) ? attrs.valueId : ""
        def valueName = (attrs.valueName) ? attrs.valueName : ""
        def valueType = (attrs.valueType) ? attrs.valueType : ""
        def width = (attrs.width) ? attrs.width : 200
        def size = (attrs.size) ? attrs.size : 20
        def minLength = (attrs.minLength) ? attrs.minLength : 1
        def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : ""

        def spanDisplay = "none"
        def suggestDisplay = "inline"
        def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('${request.contextPath}/images/icons/silk/zoom.png'); background-repeat: no-repeat; background-position: center left;
						padding: 5px; padding-left: 25px;
					}
					#${id}-span { padding: 5px; border: 1px solid lightgrey; background-color: #eee;}
				</style>

				<!-- Hidden fields used to pass the ID and type for the selected option -->
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${id}-type" type="hidden" name="${name}.type" value="${valueType}"/>

				<!-- Auto suggest textbox -->
				<input id="${id}-suggest" type="text" name="${name}.name" value="${valueName}" 
					size="${size}" style="width: ${width}px; display: ${suggestDisplay};">
				
				<!-- DIV used to display the selected value -->
				<div id="${id}-span" style="text-align: left; display: ${spanDisplay}; width: ${
            width
        }px;">
					<span id="${id}-span-value">${valueName ?: '&nbsp;'}</span>
					<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}" style="float: right;"/>
				</div>
				
				
				<script>
					\$(document).ready(function() {
					
						/* Suppresses the ENTER key for autocomplete fields */
						\$('#${id}-suggest').keydown(function(event){
							if(event.keyCode == 13) {
								event.preventDefault();
								\$(this).blur();
								//\$(this).trigger("focus");
								return false;
							}
						});
						
						
						/* Triggered when user clicks on display DIV */
						\$("#${id}-span").click(function() {
							\$('#${id}-span').hide();
							\$('#${id}-suggest').show();
							//\$('#${id}-suggest').val('');
							//\$('#${id}-suggest').select();
							
							\$('#${id}-suggest').focus();
							//\$('#${id}-span-value').html('');
							\$('#${id}-id').val('');
						});
						
						/* Triggered when user clicks on auto suggest box */
						\$("#${id}-suggest").click(function() {
							\$("#${id}-suggest").trigger("focus");
						});
						
						
						/* Triggered when the user tabs out of the auto suggest box */
						\$("#${id}-suggest").blur(function() {
							var valueText = \$('#${id}-suggest').val();		
							
							// When 
							
							\$('#${id}-id').val(valueText);
							\$('#${id}-type').val(valueText);
							\$('#${id}-suggest').val(valueText);
							\$('#${id}-span-value').html((valueText)?valueText:'&nbsp');								
							\$('#${id}-suggest').hide();
							\$('#${id}-span').show();
						});
						
						/* Attaches autosuggest box to the textbox */
						\$("#${id}-suggest").autocomplete({
							width: ${width},
							minLength: ${minLength},
							dataType: 'json',
							highlight: true,
							//selectFirst: true,
							scroll: true,
							autoFill: true,
							//scrollHeight: 300,
							/* callback that pulls value from the server based on the given params.term */
							source: function(req, add){
								\$.getJSON('${jsonUrl}', req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
							},
							/* Callback that is triggered when the autosuggest textbox gets the focus */
							focus: function(event, ui) {
								if (console.log)
									console.log("changed value " + ui.item.valueText);
								  //\$('#${id}-suggest').val(ui.item.valueText);
								  //return false;
							},
							/* Callback triggered when the autosuggest box value is changed */
							change: function(event, ui) {
								//if (console.log)
								//  console.log("changed value " + ui.item.valueText);
								//console.log("changed " + ui.item)
								//\$('#${id}-id').val(0);
								//\$('#${id}-suggest').val(ui.item.valueText);
								var valueText = \$('#${id}-suggest').val();
								\$('#${id}-id').val(valueText);
								\$('#${id}-name').val(valueText);
								\$('#${id}-suggest').val(valueText);
								\$('#${id}-span-value').html(valueText);
								\$('#${id}-suggest').hide();
								\$('#${id}-span').show();
							},
							/* Callback triggered when a value is selected */
							select: function(event, ui) {
								if (console.log)
									console.log("selected " + ui.item.value + " " + ui.item.valueText);
								\$('#${id}-id').val(ui.item.value);
								\$('#${id}-type').val(ui.item.type);
								\$('#${id}-suggest').val(ui.item.valueText);
								\$('#${id}-span-value').html(ui.item.valueText);
								//return false;
							}
						});
						
						/* On load, we should hide the auto suggest and show the display DIV */
						\$('#${id}-suggest').hide();
						\$('#${id}-span').show();
						
						
					});
					
				</script>
			</div>
		"""


        out << html
    }

}