
<span style="display: inline; font-size: 1.2em" class="middle"> 
	<format:metadata obj="${transactionInstance?.transactionType }" /> 
	<g:if test="${transactionInstance?.source }">
		from <format:metadata obj="${transactionInstance?.source }" />
	</g:if> 
	<g:if test="${transactionInstance?.destination }">
		to <format:metadata obj="${transactionInstance?.destination }" />
	</g:if> 
	<g:if test="${transactionInstance?.inventory }">
		<format:metadata obj="${transactionIntance?.inventory }" />
	</g:if> 
	- <format:date obj="${transactionInstance?.transactionDate }" format="dd MMMMM yyyy"/>
</span>
