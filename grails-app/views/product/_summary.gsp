<table>
	<tr>
	
		<td>
		
			<g:if test="${productInstance?.id }">
				<span style="font-weight: bold; font-size: 1.2em">${fieldValue(bean: productInstance, field: "name")}</span>				
			</g:if>
			<g:else>
				<img src="${createLinkTo(dir:'images/icons/silk', file:'new.png')}" style="vertical-align: middle;"/>
				&nbsp;
				<span style="font-weight: bold; font-size: 1.2em">New Product</span>				
			
			</g:else>
		</td>
		<td style="text-align: right;">
		
		
		</td>
	</tr>
</table>


