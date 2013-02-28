<table class="summary" border="0">
	<tr>
		<td class="top" style="width:1%">
			<g:render template="../transaction/actions"/>
		</td>
		<td class="top">
			<div class="title"> 
				${transactionInstance?.transactionNumber }
				<g:link controller="inventory" action="showTransaction" id="${transactionInstance?.id }">
					${org.pih.warehouse.util.LocalizationUtil.getLocalizedString(transactionInstance) }
				</g:link>
			</div>
		
		</td>
		<td class="right" style="width: 1%">	
			<g:if test="${transactionInstance?.transactionNumber }">
				<img src="${createLink(controller:'product',action:'barcode',params:[data:transactionInstance?.transactionNumber,format:'CODE_128',height: 20]) }"/>
			</g:if>
			<div class="transactionNumber center">${transactionInstance?.transactionNumber }</div>
		</td>
	</tr>
</table>



