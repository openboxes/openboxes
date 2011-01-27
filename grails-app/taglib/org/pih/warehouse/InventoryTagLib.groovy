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
		def searchUrl = (attrs.searchUrl) ? attrs.searchUrl : "/warehouse/json/findLotsByName";
		
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
							select: function(event, ui) {
								//alert("selected " + ui.item)
								\$('#${id}-id').val(ui.item.value);
								//\$('#${id}-suggest').val(ui.item.valueText);
								//\$('#${id}-suggest').val("Enter serial number, lot number, or barcode");
								\$('#${id}-span').html(ui.item.valueText);
								\$('#${id}-name').html(ui.item.lotNumber);
								\$('#${id}-description').html(ui.item.description);
								if (ui.item.expirationDate=='') {
									\$('#${id}-date').html(ui.item.expirationDate);
								}
								else { 
									\$('#${id}-date').html('<span class="fade">never</span>');								
								}
								\$('#lotNumberDescription').val(ui.item.description);
								\$('#lotNumberDate').val(ui.item.expirationDate);
								\$('#${id}-suggest').focus();
								
								
								// Call our own callback function
								${onSelectCallback}(event, ui);
								
								
								return false;
							}							  
						});
						
						\$("#${id}-suggest").blur(function() { 							
							\$('#${id}-suggest').val("Enter serial number, lot number, or barcode");
						});                        
						\$("#${id}-span").click(function() {
							alert("onclick");
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
