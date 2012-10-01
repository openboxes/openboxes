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

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class InventoryTagLib {
	
	def lotNumberComboBox = { attrs, body ->
		def id = attrs.id
		def name = attrs.name
		
		def onSelectCallback = attrs.onSelectCallback;
		def width = (attrs.width) ? attrs.width : "100px";
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def searchUrl = (attrs.searchUrl) ? attrs.searchUrl : "";
		
		def spanDisplay = "";
		def suggestDisplay = "inline";
		
		def html = """
			<div>
				
				<input id="${id}-suggest" type="text" name="${name}" style="display: ${suggestDisplay};">
				
				<script>
					\$(document).ready(function() {
					
						\$('#${id}-suggest').val("Enter serial number, lot number, or barcode");
						\$('#${id}-suggest').addClass("fade")
					
						\$('#${id}-suggest').click(function() { 
							\$('#${id}-suggest').val("");
						});
						\$('#${id}-suggest').focus(function() { 
							\$('#${id}-suggest').val("");
						});
						
					
						\$("#${id}-suggest").autocomplete( {
							source: function(req, add){
								\$.getJSON('${searchUrl}', req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
							},
					        focus: function(event, ui) {	
								\$('#${id}-suggest').val("");
					      		//\$('#${id}-suggest').val(ui.item.valueText);					
					      		//return false;
					        },	
					        change: function(event, ui) { 
								//alert("changed " + ui.item)
								\$('#${id}-id').val(0);
								//\$('#${id}-suggest').val(ui.item.valueText);
					        },
					        close: function(even, ui) { 
								//alert("closed" + ui.item);
							},
							select: function(event, ui) {
								//alert("selected " + ui.item)
								\$('#${id}-id').val(ui.item.value);
								//\$('#${id}-suggest').val(ui.item.valueText);
								//\$('#${id}-suggest').val("Enter serial number, lot number, or barcode");
								\$('#${id}-span').html(ui.item.valueText);
								\$('#${id}-name').html(ui.item.lotNumber);
								
								if (ui.item.expirationDate=='') {
									\$('#${id}-date').html(ui.item.expirationDate);
								}
								else { 
									\$('#${id}-date').html('<span class="fade">${warehouse.message(code: 'default.never.label')}</span>');								
								}
								\$('#lotNumberDate').val(ui.item.expirationDate);
								\$('#${id}-suggest').focus();
								
								
								// Call our own callback function
								${onSelectCallback}(event, ui);
								
								
								return false;
							}							  
						});
						
						\$("#${id}-suggest").blur(function(event, ui) { 	
							// Call our own callback function
							${onSelectCallback}(event, ui);
							
							
							return false;
											
							//\$('#${id}-suggest').val("Enter serial number, lot number, or barcode");
						});                        
						\$("#${id}-span").click(function() {
							//alert("onclick");
							//\$('#${id}-span').hide();							
							//\$('#${id}-suggest').show();
							//\$('#${id}-suggest').val('');
							//\$('#${id}-span').html('');
							//\$('#${id}-id').val('');
						});
						
						
					});
					
				</script>
			</div>
		""";
		
		out << html;
	}
		

	
}
