<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	${warehouse.message(code: 'email.productCreated.message', args: [product.name])}	
	<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]"  absolute="true">
		${warehouse.message(code: 'email.link.label', args: [product?.name])}
	</g:link>		
</g:applyLayout>
