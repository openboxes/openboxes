<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="transaction.transactionLog.label"/>
	</content>
	
	<content tag="heading">	
		<warehouse:message code="transaction.transactionLog.label"/>
		&rsaquo;
		<span class="fade">
			<format:product product="${commandInstance?.productInstance}"/>
		</span>
	</content>

	<content tag="content">
		<g:render template="showTransactionLog"/>
	</content>
</g:applyLayout>