<%@ page import="org.pih.warehouse.inventory.TransactionEntry" %>



<div class="fieldcontain ${hasErrors(bean: transactionEntryInstance, field: 'inventoryItem', 'error')} required">
	<label for="inventoryItem">
		<g:message code="transactionEntry.inventoryItem.label" default="Inventory Item" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="inventoryItem" name="inventoryItem.id" from="${org.pih.warehouse.inventory.InventoryItem.list()}" optionKey="id" required="" value="${transactionEntryInstance?.inventoryItem?.id}" class="many-to-one"/>

</div>

<div class="fieldcontain ${hasErrors(bean: transactionEntryInstance, field: 'quantity', 'error')} required">
	<label for="quantity">
		<g:message code="transactionEntry.quantity.label" default="Quantity" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="quantity" from="${0..2147483646}" class="range" required="" value="${fieldValue(bean: transactionEntryInstance, field: 'quantity')}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: transactionEntryInstance, field: 'comments', 'error')} ">
	<label for="comments">
		<g:message code="transactionEntry.comments.label" default="Comments" />

	</label>
	<g:textArea name="comments" cols="40" rows="5" maxlength="255" value="${transactionEntryInstance?.comments}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: transactionEntryInstance, field: 'transaction', 'error')} required">
	<label for="transaction">
		<g:message code="transactionEntry.transaction.label" default="Transaction" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="transaction" name="transaction.id" from="${org.pih.warehouse.inventory.Transaction.list()}" optionKey="id" required="" value="${transactionEntryInstance?.transaction?.id}" class="many-to-one"/>

</div>

