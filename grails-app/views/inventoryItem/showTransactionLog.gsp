<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="transaction.transactionLog.label"/>
	</content>
	
	<content tag="heading">	
		<span class="fade">
			<format:product product="${commandInstance?.productInstance}"/>
		</span>
		&rsaquo;
		<warehouse:message code="transaction.transactionLog.label"/>
	</content>

	<content tag="content">
		<g:render template="showTransactionLog"/>
	</content>
</g:applyLayout>