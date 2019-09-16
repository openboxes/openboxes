<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="container.name.label"/></label>
	</td>                            
	<td valign="top" class="value">
		<g:textField id="name" name="name" size="50" class="text" value="${container ? container?.name : box?.name}"/> 
	</td>
</tr>
<script>
$(function() { 		
	$("#name").focus();
});
</script>
<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="default.weight.label"/></label>
	</td>                            
	<td valign="top" class="value">
		<g:textField id="weight" name="weight" size="6" class="text" value="${container ? container?.weight : box?.weight}"/>&nbsp;
		<g:select name="weightUnits" from="${org.pih.warehouse.core.Constants.WEIGHT_UNITS}" value="${container ? container?.weightUnits : box?.weightUnits}" />
	</td>
</tr>
<tr class="prop">
	<td valign="top" class="name">
		<label><warehouse:message code="shipping.dimensions.label"/></label>
	</td>                            
	<td class="value bottom">
		<g:textField id="height" name="height" size="5" value="${container ? container?.height : box?.height}" class="text" placeholder="Height"/>&nbsp;x&nbsp;
		<g:textField id="width" name="width" size="5" value="${container ? container?.width : box?.width}" class="text" placeholder="Width"/>&nbsp;x&nbsp;
		<g:textField id="length" name="length" size="5" value="${container ? container?.length : box?.length}" class="text" placeholder="Length"/>&nbsp;
		<g:select name="volumeUnits" from="${org.pih.warehouse.core.Constants.VOLUME_UNITS}" value="${container ? container?.volumeUnits : box?.volumeUnits}" />


	</td>
</tr>
<tr class="prop">
	<td valign="top" class="name"><label><warehouse:message code="shipping.recipient.label"/></label></td>                            
	<td valign="top" class="value">
		<g:autoSuggest id="recipient" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
			width="180" size="30"
			valueId="${container?.recipient?.id?:'null' }" 
			valueName="${container?.recipient?.name }"
			styleClass="text"
			/>	
	</td>
</tr>