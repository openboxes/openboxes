<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="transaction.transactionLog.label"/>
	</content>
	
	<content tag="heading">	
		<format:product product="${commandInstance?.productInstance}"/>
	</content>

	<content tag="content">
		<g:render template="showTransactionLog"/>
	</content>
</g:applyLayout>