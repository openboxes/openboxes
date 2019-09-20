
<g:applyLayout name="stockCard">

	<content tag="title">
		<format:product product="${commandInstance?.product}"/>
	</content>

	<content tag="heading">
		<format:product product="${commandInstance?.product}"/>
	</content>

	<content tag="content">
		<g:render template="showStockCard"/>
	</content>

</g:applyLayout>