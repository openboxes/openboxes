<table>
	<tr>
	
		<td>
		
			<g:if test="${productInstance?.id }">
	   			<g:if test="${productInstance?.productClass ==  org.pih.warehouse.product.ProductClass.DRUG}">
					<img src="${createLinkTo(dir:'images/icons/silk', file:'pill.png')}" style="vertical-align: middle;"/>
				</g:if>
	           			<g:elseif test="${productInstance?.productClass ==  org.pih.warehouse.product.ProductClass.CONSUMABLE}">
					<img src="${createLinkTo(dir:'images/icons/silk', file:'cup.png')}" style="vertical-align: middle;"/>
				</g:elseif>
	           			<g:elseif test="${productInstance?.productClass ==  org.pih.warehouse.product.ProductClass.DURABLE}">
					<img src="${createLinkTo(dir:'images/icons/silk', file:'computer.png')}" style="vertical-align: middle;"/>
				</g:elseif>
				&nbsp;
				<span style="font-weight: bold; font-size: 1.2em">${fieldValue(bean: productInstance, field: "name")}</span>				
			</g:if>
			<g:else>
				<img src="${createLinkTo(dir:'images/icons/silk', file:'new.png')}" style="vertical-align: middle;"/>
				&nbsp;
				<span style="font-weight: bold; font-size: 1.2em">New ${productInstance?.productClass?.name }</span>				
			
			</g:else>
		</td>
		<td style="text-align: right;">
		
			<%-- 
			<div style="font-size: 1.2em">
				<b>${productInstance?.active?'Active':'Inactive'}</b>
				<g:if test="${productInstance?.active}">
					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'tick.png') }"
         						style="vertical-align: bottom;" />
				</g:if>
				<g:else>
   					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'stop.png') }"
   						style="vertical-align: bottom;" />
				</g:else>
			</div>
			--%>
		</td>
	</tr>
</table>


