<table>
	<tr>
	
		<td>
			<g:if test="${locationInstance?.logo }">
				<img class="photo" width="25" height="25" 
					src="${createLink(controller:'location', action:'viewLogo', id:locationInstance.id)}" style="vertical-align: bottom" />		            				
				&nbsp;
			</g:if>
			<span style="font-weight: bold; font-size: 2em">${fieldValue(bean: locationInstance, field: "name")}</span>
		</td>
		<td style="text-align: right;">
			<div style="font-size: 1.2em">
				<b>${locationInstance?.active ? warehouse.message(code:'warehouse.active.label') : warehouse.message(code:'warehouse.inactive.label')}</b>
				<g:if test="${locationInstance?.active}">
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