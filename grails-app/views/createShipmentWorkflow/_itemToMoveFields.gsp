
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.item.label" default="Item" /></label></td>                            
					<td valign="top" class="value">
						<g:hiddenField name="item.id" value="${itemToMove.id }"/>
						<b>${item?.quantity }</b> x ${item?.product?.name }
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="From" /></label></td>                            
					<td valign="top" class="value">
						<div>
							<g:set var="count" value="${1 }"/>
							<table>
								<tr class="${count++ % 2 ? 'odd':'even' }">
									<th>Container</th>
									<th>Quantity</th>
								</tr>
								<tr>
									<td>
										<g:if test="${!item.container}"><g:message code="shipmentItem.unpackedItems" default="Unpacked Items" /></g:if>
										<g:if test="${item?.container?.parentContainer }">${item?.container?.parentContainer?.name } &rsaquo;</g:if> ${item?.container?.name }
									</td>
									<td>
										${item?.quantity}
									</td>									
								</tr>
							</table>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="To" /></label></td>                            
					<td valign="top" class="value">
						<div style="height: 150px; overflow: auto;">
							<g:set var="count" value="${1 }"/>
							<table>
								<tr class="${count++ % 2 ? 'odd':'even' }">
									<th>Container</th>
									<th>Quantity</th>
								</tr>
							
								<g:if test="${item.container}">
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<td>
											<%-- <g:checkBox name="container.id" value="-1"/>&nbsp;--%> 
											<g:message code="shipmentItem.unpackedItems" default="Unpacked Items" />
										</td>
										<td>
											<g:textField id="quantity-unpackedItems" class="updateQuantity" name="quantity-0" size="3" value="0"></g:textField>
										</td>
									</tr>
								</g:if>
								
								<g:each var="containerTo" in="${shipmentInstance?.containers.sort{it.sortOrder}}">
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<td>
											<g:set var="selected" test="${item?.container?.id == containerTo?.id}"/>
											<%--
											 <g:checkBox name="container.id" value="${containerTo?.id }"/>&nbsp;--%>
											<g:if test="${containerTo?.parentContainer }">${containerTo?.parentContainer?.name } &rsaquo;</g:if> ${containerTo?.name }
										</td>
										<td>
											<g:if test="${containerTo != item.container}">
												<g:textField id="newQuantity-${containerTo?.id}" class="updateQuantity" name="quantity-${containerTo?.id}" size="3" value="0"></g:textField>
											</g:if>
											<g:else>
												<g:textField id="currentQuantity" class="currentQuantity" name="quantity-${containerTo?.id}" size="3" readonly="readonly" value="${item?.quantity}"></g:textField>
											</g:else>
										</td>
									</tr>									
								</g:each>
							</table>
							
						</div>
						<g:hiddenField id="totalQuantity" class="totalQuantity" name="totalQuantity" size="3" disabled="true" value="${item?.quantity}"/>
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="moveItemToContainer" value="Move"></g:submitButton>
							<button name="cancelDialog" type="reset" onclick="$('#dlgMoveItem').dialog('close');">Cancel</button>
						</div>
					</td>
				</tr>
				
				

				
			
			