<table>
	<tr>
	
		<td>
			<g:if test="${warehouseInstance?.logo }">
				<img class="photo" width="25" height="25" 
					src="${createLink(controller:'warehouse', action:'viewLogo', id:warehouseInstance.id)}" style="vertical-align: bottom" />		            				
				&nbsp;
			</g:if>
			<span style="font-weight: bold; font-size: 2em">${fieldValue(bean: warehouseInstance, field: "name")}</span>
			&nbsp;
			<g:link class="edit" action="edit" id="${warehouseInstance?.id}" >${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
			&nbsp;
			<g:link class="delete" action="delete" id="${warehouseInstance?.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">${message(code: 'default.button.delete.label', default: 'Delete')}</g:link>
		</td>
		<td style="text-align: right;">
			<div style="font-size: 1.2em">
				<b>${warehouseInstance?.active?'Active':'Inactive'}</b>
				<g:if test="${warehouseInstance?.active}">
					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'tick.png') }"
         						style="vertical-align: bottom;" />
				</g:if>
				<g:else>
   					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'stop.png') }"
   						style="vertical-align: bottom;" />
				</g:else>
			</div>
		
		</td>
	</tr>
</table>