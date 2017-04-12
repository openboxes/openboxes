<g:applyLayout name="stockCard">

	<content tag="title">
		<format:product product="${commandInstance?.product}"/>
	</content>
	
	<content tag="heading">	
		<warehouse:message code="transaction.transactionLog.label"/>
	</content>

	<content tag="content">
		<g:render template="showTransactionLog"/>
	</content>
</g:applyLayout>