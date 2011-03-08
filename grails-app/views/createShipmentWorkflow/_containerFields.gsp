<tr class="prop">
	<td valign="top" class="name"><label>Name</label></td>                            
	<td valign="top" class="value">
		<g:textField id="name" name="name" size="15" value="${container ? container?.name : box?.name}"/> 
	</td>
</tr>

<tr class="prop">
	<td valign="top" class="name"><label>Dimensions</label></td>                            
	<td valign="top" class="value">
		H: <g:textField id="height" name="height" size="5" value="${container ? container?.height : box?.height}"/>&nbsp;
		W: <g:textField id="width" name="width" size="5" value="${container ? container?.width : box?.width}"/>&nbsp;
		L: <g:textField id="length" name="length" size="5" value="${container ? container?.length : box?.length}"/>&nbsp;
		(${ container?.volumeUnits ?: box?.volumeUnits ?: org.pih.warehouse.core.Constants.DEFAULT_VOLUME_UNITS })
	</td>
</tr>

<tr class="prop">
	<td valign="top" class="name"><label>Weight</label></td>                            
	<td valign="top" class="value">
		<g:textField id="weight" name="weight" size="15" value="${container ? container?.weight : box?.weight}"/>&nbsp;
		(${ container?.weightUnits ?: box?.weightUnits ?: org.pih.warehouse.core.Constants.DEFAULT_WEIGHT_UNITS })
	</td>
</tr>