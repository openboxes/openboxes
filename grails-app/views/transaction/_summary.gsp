<table class="summary">
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
		<td class="right">
			<div class="center">
				<img src="${createLink(controller:'product',action:'barcode',params:[data:transactionInstance?.transactionNumber,format:'CODE_128']) }"/>
				<div class="transactionNumber">${transactionInstance?.transactionNumber }</div>
			</div>
		</td>
	</tr>
</table>



