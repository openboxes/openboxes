<table class="summary">
	<tr>
		<td class="middle" style="width:1%">
			<g:render template="../transaction/actions"/>
		</td>
		<td class="middle">
			<div class="title"> 
				<small class="transactionNumber">${transactionInstance?.transactionNumber }</small>
				<g:link controller="inventory" action="showTransaction" id="${transactionInstance?.id }">

					<format:metadata obj="${transactionInstance?.transactionType}" /> <g:if
						test="${transactionInstance?.source }">
					<warehouse:message code="default.from.label" />
					${transactionInstance?.source?.name }
					</g:if> <g:if test="${transactionInstance?.destination }">
						<warehouse:message code="default.to.label" />
						${transactionInstance?.destination?.name }
					</g:if>
				</g:link>
			</div>
		
		</td>
		<td class="right" style="width: 1%">
            <g:if test="${transactionInstance?.transactionNumber }">
                <img src="${createLink(controller:'product',action:'barcode',params:[data:transactionInstance?.transactionNumber,format:'CODE_128',height: 36]) }"/>
            </g:if>
            <div class="transactionNumber center">${transactionInstance?.transactionNumber }</div>
		</td>
	</tr>
</table>
