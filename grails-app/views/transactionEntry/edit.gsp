<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transactionEntry.label', default: 'Transaction entry')}" />
        <title><warehouse:message code="${transactionEntry?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${transactionEntry}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionEntry}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="dialog">
				<g:render template="/transaction/summary" model="[transactionInstance:transactionEntry?.transaction]"/>

			<div id="edit-transaction-entry">
				<div class="yui-gf">
					<div class="yui-u first">
						<g:render template="/transaction/details" model="[transactionInstance:transactionEntry?.transaction]"/>
					</div>
					<div class="yui-u">
						<g:form method="post" >
			            	<div class="box">


                                <h2>
                                    <warehouse:message code="transactionEntry.label"/> &rsaquo;
                                    <format:product product="${transactionEntry?.inventoryItem?.product}"/></h2>
				                <g:hiddenField name="id" value="${transactionEntry?.id}" />
				                <g:hiddenField name="version" value="${transactionEntry?.version}" />
				                <div class="dialog">
				                    <table>
				                        <tbody>
				                            <tr class="prop">
				                                <td valign="top" class="name">
				                                  <label for="transaction.id"><warehouse:message code="transactionEntry.transaction.label" default="Transaction" /></label>
				                                </td>
				                                <td valign="top" class="value ${hasErrors(bean: transactionEntry, field: 'transaction', 'errors')}">
				                                   	<g:hiddenField name='transaction.id' value='${transactionEntry?.transaction?.id }'/>
				                                   	${transactionEntry?.transaction?.transactionNumber?:transactionEntry?.transaction?.id }
				                                </td>
				                            </tr>

                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="product"><warehouse:message code="product.label" default="Product" /></label>
                                                </td>
                                                <td valign="top" class="value">
                                                    <span id="product">
                                                        ${transactionEntry?.inventoryItem?.product?.productCode}
                                                        ${transactionEntry?.inventoryItem?.product?.name}
                                                    </span>

                                                </td>
                                            </tr>
											<tr class="prop">
												<td valign="top" class="name">
													<label for="product"><warehouse:message code="location.binLocation.label" default="Bin Location" /></label>
												</td>
												<td valign="top" class="value">
													<g:selectBinLocation name="binLocation" value="${transactionEntry?.binLocation?.id}" noSelection="['':'']" class="chzn-select-deselect"/>
												</td>
											</tr>


				                            <tr class="prop">
				                                <td valign="top" class="name">
				                                  <label for="inventoryItem.id"><warehouse:message code="transactionEntry.inventoryItem.label" default="Inventory Item" /></label>
				                                </td>
				                                <td valign="top" class="value ${hasErrors(bean: transactionEntry, field: 'inventoryItem', 'errors')}">
				                                    <g:select name="inventoryItem.id" class="chzn-select-deselect" from="${org.pih.warehouse.inventory.InventoryItem.findAllByProduct(transactionEntry?.inventoryItem?.product)}"
															optionKey="${{ it.id }}" optionValue="${{ it.lotNumber }}"
															value="${transactionEntry?.inventoryItem?.id}"  />
				                                </td>
				                            </tr>

				                            <tr class="prop">
				                                <td valign="top" class="name">
				                                  <label for="quantity"><warehouse:message code="transactionEntry.quantity.label" default="Quantity" /></label>
				                                </td>
				                                <td valign="top" class="value ${hasErrors(bean: transactionEntry, field: 'quantity', 'errors')}">
				                                    <g:textField name="quantity" value="${transactionEntry?.quantity }" size="10" class="text"/>
				                                </td>
				                            </tr>
											<tr class="prop">
												<td valign="top" class="name">
													<label for="unitOfMeasure"><warehouse:message code="product.unitOfMeasure.label" default="Unit of measure" /></label>
												</td>
												<td valign="top" class="value">
													<span id="unitOfMeasure">
														${transactionEntry?.inventoryItem?.product?.unitOfMeasure}
													</span>

												</td>
											</tr>

				                            <tr class="prop">
				                                <td valign="top" class="name">
				                                  <label for="comments"><warehouse:message code="transactionEntry.comments.label" default="Comments" /></label>
				                                </td>
				                                <td valign="top" class="value ${hasErrors(bean: transactionEntry, field: 'comments', 'errors')}">
				                                    <g:textArea name="comments" cols="100" rows="5" value="${transactionEntry?.comments}" />
				                                </td>
				                            </tr>


			                            	<tr class="prop">
					                        	<td valign="top"></td>
					                        	<td valign="top">
									                <div class="">
									                    <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
									                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
									                	&nbsp;
									                	<g:link controller='inventory' action='showTransaction' id='${transactionEntry?.transaction?.id }'>
															${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
														</g:link>

									                </div>
					    						</td>
				                        	</tr>
				                        </tbody>
				                    </table>
				                </div>
			                </div>
			            </g:form>

					</div>
				</div>
			</div>
		</div>
	</body>
</html>
