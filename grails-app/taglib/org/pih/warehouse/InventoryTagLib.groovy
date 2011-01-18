package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.InventoryLot;

class InventoryTagLib {
	
	def lotNumberComboBox = { attrs, body ->
		def id = attrs.id
		def name = attrs.name
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 100;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findLotsByName";

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
				
				<input id="${id}-id" type="hidden" name="${name}" value="${valueId}"/>
				<input id="${id}-suggest" type="text" name="${name}" value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};">
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
		
	def lotOrSerialNumber = { attrs, body ->
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
	
}
