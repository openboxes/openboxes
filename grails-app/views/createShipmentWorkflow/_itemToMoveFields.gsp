
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.item.label" default="Item" /></label></td>                            
					<td valign="top" class="value">
						<g:hiddenField name="item.id" value="${itemToMove.id }"/>
					
						${item?.quantity } ${item?.product?.name }
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="From" /></label></td>                            
					<td valign="top" class="value">
						<g:if test="${!item.container}"><g:message code="shipmentItem.unpackedItems" default="Unpacked Items" /></g:if>
						<g:if test="${item?.container?.parentContainer }">${item?.container?.parentContainer?.name } &rsaquo;</g:if> ${item?.container?.name }
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="To" /></label></td>                            
					<td valign="top" class="value">
						<div style="height: 150px; overflow: auto;">
							<table>
								<g:if test="${item.container}">
									<tr>
										<td><g:radio name="container.id" value="-1"/>&nbsp; <g:message code="shipmentItem.unpackedItems" default="Unpacked Items" /></td>
									</tr>
								</g:if>
								<tr>
									<g:set var="count" value="${1}"/>
									<g:each var="containerTo" in="${shipmentInstance?.containers.sort{it.sortOrder}}">
										<g:if test="${containerTo != item.container}">
											<td>
												<g:set var="selected" test="${item?.container?.id == containerTo?.id}"/>
												<g:radio name="container.id" value="${containerTo?.id }"/>&nbsp;
													<g:if test="${containerTo?.parentContainer }">${containerTo?.parentContainer?.name } &rsaquo;</g:if> ${containerTo?.name }
											</td>
											<g:if test="${(count++ % 2 == 0)}"></tr><tr></g:if>
										</g:if>
									</g:each>
								</tr>
							</table>
						</div>
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

			
			