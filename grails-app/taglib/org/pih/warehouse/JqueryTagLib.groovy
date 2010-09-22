package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class JqueryTagLib {
		
	def autoSuggest = { attrs, body ->
		def id = attrs.id
		def name = attrs.name	
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 0;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		def spanDisplay = (showValue) ? "inline" : "none";
		def suggestDisplay = (showValue) ? "none" : "inline";
		
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
							\$('#${id}-suggest').hide();					
							\$('#${id}-span').html(text?text:'<b>empty</b> &nbsp; click to change');
							\$('#${id}-span').show();						
						});                        
						\$("#${id}-span").click(function() {
							\$('#${id}-span').hide();							
							\$('#${id}-suggest').show();
							\$('#${id}-suggest').val('');
							\$('#${id}-span').html('');
							\$('#${id}-id').val('');
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
					      		\$('#${id}-suggest').val(ui.item.valueText);					
					      		return false;
					        },	
							select: function(event, ui) {
								\$('#${id}-id').val(ui.item.value);
								\$('#${id}-suggest').val(ui.item.valueText);
								\$('#${id}-span').html(ui.item.valueText);
								\$('#${id}-suggest').hide();
								\$('#${id}-span').show();
								return false;
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
		
		def value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""

		if (name == null) { 
			throw new IllegalArgumentException("name parameter must be specified")			
		}
		def html = """

		<div>
			<style>
				.ui-datepicker-trigger { 
					position: relative; left: -20px; top: 2px; 
				}
			</style>
			<input id='${id}' name='${name}' type='hidden'/> 
			<input id='${id}-datepicker' name='${name}-datepicker' type='text' class='date' /> 
			<script type=\'text/javascript\'> 
				jQuery(function() {
					var dateValue = new Date('${value}'); 	
					jQuery('#${id}-datepicker').datepicker({
						showOn: 'both',
						altField: '#${name}',
						altFormat: 'mm/dd/yy',
						dateFormat: 'MM dd yy',
						//autoSize: true,
						//closeText: 'Done',
						buttonImageOnly: true, 
						buttonImage: '/warehouse/images/icons/silk/calendar.png',
						//buttonText: '...',
						//showButtonPanel: true,
						//showOtherMonths: true,
						//selectOtherMonths: true
					});
					jQuery('#${name}-datepicker').datepicker('setDate', dateValue);
				}); 
			</script> 
		</div>
		""";
		
		out << html;
				
	}
	
	
	def jqueryComboBox = { attrs, body ->
		
		
		
		def html = """
		
				<style>
					.ui-button { margin-left: -1px; }
					.ui-button-icon-only .ui-button-text { padding: 0.35em; }
					.ui-autocomplete-input { margin: 0; padding: 0.48em 0 0.47em 0.45em; }
				</style>
				<script>
					(function( \$ ) {
						\$.widget( "ui.combobox", {
							_create: function() {
								var self = this,
									select = this.element.hide(),
									selected = select.children( ":selected" ),
									value = selected.val() ? selected.text() : "";
								var input = \$( "<input>" )
									.insertAfter( select )
									.val( value )
									.autocomplete({
										delay: 0,
										minLength: 0,
										source: function( request, response ) {
											var matcher = new RegExp( \$.ui.autocomplete.escapeRegex(request.term), "i" );
											response( select.children( "option" ).map(function() {
												var text = \$( this ).text();
												if ( this.value && ( !request.term || matcher.test(text) ) )
													return {
														label: text.replace(
															new RegExp(
																"(?![^&;]+;)(?!<[^<>]*)(" +
																\$.ui.autocomplete.escapeRegex(request.term) +
																")(?![^<>]*>)(?![^&;]+;)", "gi"
															), "<strong>\$1</strong>" ),
														value: text,
														option: this
													};
											}) );
										},
										select: function( event, ui ) {
											ui.item.option.selected = true;
											self._trigger( "selected", event, {
												item: ui.item.option
											});
										},
										change: function( event, ui ) {
											if ( !ui.item ) {
												var matcher = new RegExp( "^" + \$.ui.autocomplete.escapeRegex( \$(this).val() ) + "\$", "i" ),
													valid = false;
												select.children( "option" ).each(function() {
													if ( this.value.match( matcher ) ) {
														this.selected = valid = true;
														return false;
													}
												});
												if ( !valid ) {
													// remove invalid value, as it didn't match anything
													\$( this ).val( "" );
													select.val( "" );
													return false;
												}
											}
										}
									})
									.addClass( "ui-widget ui-widget-content ui-corner-left" );
				
								input.data( "autocomplete" )._renderItem = function( ul, item ) {
									return \$( "<li></li>" )
										.data( "item.autocomplete", item )
										.append( "<a>" + item.label + "</a>" )
										.appendTo( ul );
								};
				
								\$( "<button>&nbsp;</button>" )
									.attr( "tabIndex", -1 )
									.attr( "title", "Show All Items" )
									.insertAfter( input )
									.button({
										icons: {
											primary: "ui-icon-triangle-1-s"
										},
										text: false
									})
									.removeClass( "ui-corner-all" )
									.addClass( "ui-corner-right ui-button-icon" )
									.click(function() {
										// close if already visible
										if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
											input.autocomplete( "close" );
											return;
										}
				
										// pass empty string as value to search for, displaying all results
										input.autocomplete( "search", "" );
										input.focus();
									});
							}
						});
					})( jQuery );
				
					\$(function() {
						\$( "#combobox" ).combobox();
						\$( "#toggle" ).click(function() {
							\$( "#combobox" ).toggle();
						});
					});
				</script>
				
				<div class="demo">
					<div class="ui-widget">
						<label>Your preferred programming language: </label>
						<select id="combobox">
							<option value="">Select one...</option>
							<option value="ActionScript">ActionScript</option>
							<option value="AppleScript">AppleScript</option>
							<option value="Asp">Asp</option>
							<option value="BASIC">BASIC</option>
							<option value="C">C</option>
							<option value="C++">C++</option>
							<option value="Clojure">Clojure</option>
							<option value="COBOL">COBOL</option>
							<option value="ColdFusion">ColdFusion</option>
							<option value="Erlang">Erlang</option>
							<option value="Fortran">Fortran</option>
							<option value="Groovy">Groovy</option>
							<option value="Haskell">Haskell</option>
							<option value="Java">Java</option>
							<option value="JavaScript">JavaScript</option>
							<option value="Lisp">Lisp</option>
							<option value="Perl">Perl</option>
							<option value="PHP">PHP</option>
							<option value="Python">Python</option>
							<option value="Ruby">Ruby</option>
							<option value="Scala">Scala</option>
							<option value="Scheme">Scheme</option>
						</select>
					</div>
					<button id="toggle">Show underlying select</button>
				</div><!-- End demo -->
			
				<div class="demo-description">
					<p>A custom widget built by composition of Autocomplete and Button. You can either type something into the field to get filtered suggestions based on your input, or use the button to get the full list of selections.</p>
					<p>The input is read from an existing select-element for progressive enhancement, passed to Autocomplete with a customized source-option.</p>
				</div><!-- End demo-description -->
		"""
		
		out << html
		
		
		
	}
	
	
	
}
