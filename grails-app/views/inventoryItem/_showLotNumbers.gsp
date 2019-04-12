
<div id="showLotNumbers" class="box">
	<h2><warehouse:message code="inventory.showLotNumbers.label"/></h2>
		<g:form controller="inventoryItem" action="create">
		<table>
			<thead>
				<tr class="odd">
					<th class="center" style=""><warehouse:message code="default.actions.label"/></th>
					<th><warehouse:message code="default.lotSerialNo.label"/></th>
					<th><warehouse:message code="default.expires.label"/></th>
				</tr>
			</thead>
			<tbody>
				<g:if test="${!commandInstance?.product?.inventoryItems}">
					<tr class="even" style="min-height: 100px;">
						<td colspan="5" style="text-align: center; vertical-align: middle">
							<warehouse:message code="inventory.noItemsCurrentlyInStock.message"
											   args="[format.product(product:commandInstance?.product)]"/>
						</td>
					</tr>
				</g:if>
				<g:set var="count" value="${0 }"/>
				<g:each var="inventoryItem" in="${commandInstance?.product?.inventoryItems}" status="status">

					<tr class="prop">
						<td class="middle center" nowrap="nowrap">
							<div class="action-menu">
								<button class="action-btn">
									<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
								</button>
								<div class="actions left">

									<div class="action-menu-item">
										<a href="javascript:void(0);" class="btn-show-dialog"
										   data-title="${g.message(code:'inventory.editItem.label')}"
										   data-url="${request.contextPath}/inventoryItem/showDialog?id=${inventoryItem?.id}&template=editItemDialog">
											<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
											<g:message code="inventory.editItem.label"/>
										</a>
									</div>
									<div class="action-menu-item">
										<g:link controller="inventoryItem" action="delete" id="${inventoryItem?.id}">
											<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>&nbsp;
											<warehouse:message code="inventoryItem.delete.label"/>
										</g:link>
									</div>
								</div>
							</div>
						</td>
						<td class="middle">
						   ${inventoryItem?.lotNumber?:warehouse.message(code:'default.label')}
						</td>
						<td class="middle">
							<g:if test="${inventoryItem?.expirationDate}">
								<format:expirationDate obj="${inventoryItem?.expirationDate}"/>
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.never.label"/></span>
							</g:else>
						</td>
					</tr>

				</g:each>
					<g:isUserManager>
						<tr class="prop">
							<td class="middle center">
								<img src="${resource(dir: 'images/icons/silk', file: 'new_blue.png')}"/>

							</td>
							<td>
								<g:hiddenField name="product.id" value="${commandInstance?.product?.id }"/>
								<g:textField name="lotNumber" class="text lotNumber" placeholder="Enter lot number"/>
							</td>
							<td>
								<g:set var="yearStart" value="${new Date().format('yyyy')as int}"/>
								<g:set var="yearEnd" value="${2050}"/>
								<g:datePicker name="expirationDate" precision="day" noSelection="['null':'']" value=""
									years="${yearStart..yearEnd }"/>
								<button class="button icon add">
									<warehouse:message code="default.button.save.label"/>
								</button>
							</td>
						</tr>
					</g:isUserManager>

			</tbody>
		</table>
	</g:form>
</div>

