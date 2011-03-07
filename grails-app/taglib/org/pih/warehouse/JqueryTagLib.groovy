package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class JqueryTagLib {
		
	def autoSuggest = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name	
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;						
					}				
				</style>
				
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<span id="${id}-span" style="text-align: left; display: ${spanDisplay};">${valueName}</span>
				<input id="${id}-suggest" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};"> 	
				
				
				<script>
					\$(document).ready(function() {
						// Captures 'Enter' key presses
						//\$(window).keydown(function(event){
						//	if(event.keyCode == 13) {
						//		event.preventDefault();
						//		return false;
						//	}
						//});
						
						\$("#${id}-suggest").click(function() {
						    \$("#${id}-suggest").trigger("focus"); 
                        });
                        
						\$("#${id}-suggest").blur(function() { 							
							var text = \$('#${id}-suggest').val();
							//\$('#${id}-suggest').hide();					
							//\$('#${id}-span').html(text?text:'<b>empty</b> &nbsp; click to change');
							//\$('#${id}-span').show();						
						});                        
						\$("#${id}-span").click(function() {
							//\$('#${id}-span').hide();							
							//\$('#${id}-suggest').show();
							//\$('#${id}-suggest').val('');
							//\$('#${id}-span').html('');
							//\$('#${id}-id').val('');
						});
				      	\$("#${id}-suggest").autocomplete({
				            width: ${width},
				            minLength: ${minLength},
				            dataType: 'json',
				            highlight: true,
				            //selectFirst: true,
				            scroll: true,
				            autoFill: true,
				            //scrollHeight: 300,
							//define callback to format results
							source: function(req, add){
								\$.getJSON('${jsonUrl}', req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
					      	},
					        focus: function(event, ui) {			
					      		//\$('#${id}-suggest').val(ui.item.valueText);					
					      		//return false;
					        },	
					        change: function(event, ui) { 
								//alert("changed " + ui.item)
								//\$('#${id}-id').val(0);
								//\$('#${id}-suggest').val(ui.item.valueText);
					        },
							select: function(event, ui) {
								//alert("selected " + ui.item.value + " " + ui.item.valueText);								
								\$('#${id}-id').val(ui.item.value);
								\$('#${id}-suggest').val(ui.item.valueText);
								\$('#${id}-span').html(ui.item.valueText);
								//\$('#${id}-suggest').hide();
								//\$('#${id}-span').show();
								return false;
							}
						});
					});
					
				</script>
			</div>		
		""";
			
		
		out << html; 
	}

	
	def autoComplete = { attrs, body ->
		def id = attrs.id
		def name = attrs.name
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;
					}
				</style>
				
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${id}-suggest" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};">
				<span id="${id}-span" style="text-align: left; display: ${spanDisplay};">${valueName}</span>
				
				<script>
					\$(document).ready(function() {
							\$("#${id}-suggest").autocomplete( { 							
								source: function(req, add){
									\$.getJSON('${jsonUrl}', req, function(data) {
										var items = [];
										\$.each(data, function(i, item) {
											items.push(item);
										});
										add(items);
									});
					      		}							
							
							});
					});
					
				</script>
			</div>
		""";
			
		
		out << html;
	}
	
	
	def autoSuggestEditable = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def valueType = (attrs.valueType)?attrs.valueType:"";
		def width = (attrs.width) ? attrs.width : 200;
		def size = (attrs.size) ? attrs.size : 20;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/zoom.png'); background-repeat: no-repeat; background-position: center left;
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
				<div id="${id}-span" style="text-align: left; display: ${spanDisplay}; width: ${width}px;">
					<span id="${id}-span-value">${valueName?:'&nbsp;'}</span>
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
								if (console.log)
								  console.log("changed value " + ui.item.valueText);
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
		""";
			
		
		out << html;
	}

	
	def autoSuggestSearchable = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name	
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;						
					}				
					.ui-autocomplete-term { font-weight: bold; color: #DDD; }
				</style>
				
				<input id="${id}-suggest" type="text" name="${name}.name" 
					value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};"> 	
				
				<script>
					\$(document).ready(function() {
						
                        
				      	\$("#${id}-suggest").autocomplete({
				            width: ${width},
				            minLength: ${minLength},
				            dataType: 'json',
				            highlight: true,
				            //selectFirst: true,
				            scroll: true,
				            autoFill: true,
				            //scrollHeight: 300,
							//define callback to format results
							source: function(req, add){								
								\$.getJSON('${jsonUrl}', req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
					      	},
					        focus: function(event, ui) {			
					        },	
					        change: function(event, ui) { 
					        },
							select: function(event, ui) {
								// text display
								\$("#lotNumber-text").html(ui.item.lotNumber);
								\$("#product-text").html(ui.item.productName);
								\$("#quantity-text").html(ui.item.quantity);
								
								// product hidden values
								\$("#product-id").val(ui.item.productId);
								\$("#lotNumber").val(ui.item.lotNumber);
								\$("#quantity").val(ui.item.quantity);
								
								//
								\$("#itemFoundForm").show();
								\$("#itemSearchForm").hide();
								
							}
						});
					});
					
				</script>
			</div>		
		""";
			
		
		out << html; 
	}

	
	
	def jqueryDatePicker = {attrs, body ->
		
		def id = attrs.id ? attrs.id : attrs.name;
		def name = attrs.name;
		def autoSize = attrs.autoSize ?: "true";
		def showOn = attrs.showOn ?: "both";
		def showTrigger = Boolean.valueOf(attrs.showTrigger ?: "true");
				
		def value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""

		if (name == null) { 
			throw new IllegalArgumentException("name parameter must be specified")			
		}
		
		def html = """

		<span>
			<input id='${id}' name='${name}' type='hidden'/> 
			<input id='${id}-datepicker' name='${name}-datepicker' type='text' class='date' /> 
			<script type=\'text/javascript\'> 
				jQuery(document).ready(function() {
					jQuery('#${id}-datepicker').datepicker({
						altField: '#${id}',
						altFormat: 'mm/dd/yy',
						dateFormat: 'dd/M/yy',
						autoSize: ${autoSize},
						showOn: '${showOn}',
						buttonImageOnly: true, 
						buttonImage: '/warehouse/images/icons/silk/calendar.png',
						//buttonText: '...',
						//showButtonPanel: true,
						//showOtherMonths: true,
						//selectOtherMonths: true
					});					
					var dateValue = '${value}';					 
					if (dateValue) { 
						jQuery('#${name}-datepicker').datepicker('setDate', new Date('${value}'));
					}
				}); 
			</script> 
		</span>
		""";

		if (showTrigger) { 
			html += """
			<style>
			.ui-datepicker-trigger {
				position: relative; left: -20px; top: -2px;
			}
			</style>
			""";
		}
		
				
		out << html;
				
	}
	
	
	
	def autoSuggestString = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def value = (attrs.value)?attrs.value:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (value) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;
					}
				</style>
				
				<input id="${id}" type="hidden" name="${name}" value="${value}"/>
				<input id="${id}-suggest" type="text" name="${name}.autoSuggest" value="${value}" style="width: ${width}px; display: ${suggestDisplay};">
				
				
				<script>
					\$(document).ready(function() {
						
						\$("#${id}-suggest").autocomplete({
							width: ${width},
							minLength: ${minLength},
							dataType: 'json',
							highlight: true,
							//selectFirst: true,
							scroll: true,
							autoFill: true,
							//scrollHeight: 300,
							//define callback to format results
							source: function(req, add){
							
								var url = '${jsonUrl}';
								\$.getJSON(url, req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
							  },
							focus: function(event, ui) {

							},
							change: function(event, ui) {
							},
							select: function(event, ui) {
								//alert("selected " + ui.item.value + " " + ui.item.valueText);
								\$('#${id}').val(ui.item.value);
								\$('#${id}-suggest').val(ui.item.valueText);
								return false;
							}
						});
					});
					
				</script>
			</div>
		""";
			
		
		out << html;
	}

	
	
	
}
