<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div class="left smpad">
	Showing ${commandInstance?.allTransactionLogMap?.keySet()?.size() } of ${commandInstance?.allTransactionLogMap?.keySet()?.size() } transaction(s)
</div>				

<div>		

	<div>
		<g:form method="GET" action="showTransactionLog">
			<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>

				<!--  Filter -->
			
				<table style="border: 1px solid lightgrey">
				<%--
					<tr class="odd">
						<td>
							<label><warehouse:message code="transactionLog.from.label"/></label>
						</td>
					</tr>
					<tr class="even"> 
						<td class="middle left">
							<g:jqueryDatePicker 
								id="startDate" 
								name="startDate" 
								value="${commandInstance?.startDate }" 
								format="MM/dd/yyyy"
								size="15"
								showTrigger="false" />
							<warehouse:message code="transactionLog.to.label"/>
							<g:jqueryDatePicker 
								id="endDate" 
								name="endDate" 
								value="${commandInstance?.endDate }" 
								format="MM/dd/yyyy"
								size="15"
								showTrigger="false" />
			
							<g:select name="transactionType.id" 
								from="${org.pih.warehouse.inventory.TransactionType.list()}" 
								optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${commandInstance?.transactionType?.id }" 
								noSelection="['0': warehouse.message(code:'default.all.label')]" /> 
						
							<button  class="" name="filter">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" style="vertical-align:middle"/>
								&nbsp;<warehouse:message code="default.button.filter.label"/>
							</button>
						</td>
					</tr>
					 --%>
					<tr>
						<td style="padding: 0px;">					
							<g:set var="enableFilter" value="${!params.disableFilter}"/>
							<table >
								<thead>
									<tr class="odd prop">
                                        <th>

                                        </th>
										<th>
											${warehouse.message(code: 'default.date.label')}
										</th>
                                        <th>
                                            ${warehouse.message(code: 'default.time.label')}
                                        </th>
                                        <th>
                                            ${warehouse.message(code: 'transaction.transactionNumber.label')}
                                        </th>
										<th>
											${warehouse.message(code: 'default.type.label')}
										</th>
                                        <th class="border-right">
                                            ${warehouse.message(code: 'shipment.label')} /
                                            ${warehouse.message(code: 'requisition.label')}
                                        </th>
                                        <th class="border-right center" width="7%">
                                            ${warehouse.message(code: 'transaction.credit.label', default: 'Credit')}
                                        </th>
                                        <th class="border-right center" width="7%">
                                            ${warehouse.message(code: 'transaction.debit.label', default: 'Debit')}
                                        </th>
                                        <th class="center" width="7%">
                                            ${warehouse.message(code: 'stockCard.balance.label', default: 'Balance')}
                                        </th>
									</tr>
                                    <tr>
                                        <th colspan="6" class="left border-right">
                                            <warehouse:message code="stockCard.initialBalance.label" default="Initial balance"/>
                                        </th>
                                        <th class="center border-right">

                                        </th>
                                        <th class="center border-right">

                                        </th>
                                        <th class="center border-right">
                                            0
                                        </th>

                                    </tr>
			
								</thead>
								<!--  Transaction Log -->
								<tbody>			
									
									<g:set var="transactionMap" value="${commandInstance?.getTransactionLogMap(enableFilter.toBoolean())}"/>
									<g:if test="${!transactionMap }">
										<tr>
											<td colspan="6" class="even center">		
												<div class="fade padded">
													<warehouse:message code="transaction.noTransactions.label"/>
													<%-- 
													<warehouse:message code="transaction.noTransactions.message" args="[format.metadata(obj:commandInstance?.transactionType),commandInstance?.startDate,commandInstance?.endDate]"/>
													--%>
												</div>
											</td>
										</tr>
									</g:if>
									<g:else>
                                        <g:set var="balance" value="${0}"/>
                                        <g:set var="balanceByInventoryItem" value="${[:]}"/>
										<g:set var="totalQuantityChange" value="${0 }"/>
                                        <g:each var="transaction" in="${transactionMap?.keySet()?.sort {it.transactionDate} }" status="status">
                                            <tr class="transaction ${(status%2==0)?'even':'odd' } prop border-top">
                                                <td style="text-align: center">
                                                    <g:set var="quantityChange" value="${0 }"/>
                                                    <g:each var="transactionEntry" in="${commandInstance?.getTransactionLogMap(enableFilter.toBoolean())?.get(transaction) }" status="status2">
                                                        <g:set var="quantityChange" value="${transaction?.transactionEntries.findAll{it?.inventoryItem?.product == commandInstance?.productInstance}.quantity?.sum() }"/>
                                                    </g:each>
                                                    <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                                        <g:set var="balance" value="${balance - quantityChange}"/>
                                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'decline.png' )}"/>
                                                    </g:if>
                                                    <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                                        <g:set var="balance" value="${balance + quantityChange}"/>
                                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png' )}" />
                                                    </g:elseif>
                                                    <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.INVENTORY}">
                                                        <g:set var="balance" value="${quantityChange}"/>
                                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'clipboard.png' )}" />
                                                    </g:elseif>
                                                    <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY}">
                                                        <g:set var="balance" value="${quantityChange}"/>
                                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'clipboard.png' )}" />
                                                    </g:elseif>
                                                </td>
												<td nowrap="nowrap">
													<format:date obj="${transaction?.transactionDate}" format="dd/MMM/yyyy"/>
												</td>
                                                <td>
                                                    <format:date obj="${transaction?.transactionDate}" format="hh:mma"/>
                                                </td>
                                                <td>
                                                    <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                                                        ${transaction?.transactionNumber }
                                                    </g:link>
                                                </td>
												<td>
                                                    <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                                                        <format:metadata obj="${transaction?.transactionType}"/>
                                                        <g:if test="${transaction?.source }">
                                                            ${warehouse.message(code:'default.from.label')}
                                                            ${transaction?.source?.name }
                                                        </g:if>
                                                        <g:elseif test="${transaction?.destination }">
                                                            ${warehouse.message(code:'default.to.label')}
                                                            ${transaction?.destination?.name }
                                                        </g:elseif>
                                                    </g:link>

                                                </td>
												<td class="border-right">
                                                    <div>
                                                        <g:if test="${transaction?.incomingShipment }">
                                                            <g:link controller="shipment" action="showDetails" id="${transaction?.incomingShipment?.id }">
                                                                ${transaction.incomingShipment?.shipmentNumber } |
                                                                ${transaction.incomingShipment?.name }
                                                            </g:link>
                                                        </g:if>
                                                        <g:elseif test="${transaction?.outgoingShipment }">
                                                            <g:link controller="shipment" action="showDetails" id="${transaction?.outgoingShipment?.id }">
                                                                ${transaction.outgoingShipment?.shipmentNumber } |
                                                                ${transaction.outgoingShipment?.name }
                                                            </g:link>
                                                        </g:elseif>
                                                        <g:elseif test="${transaction?.requisition }">
                                                            <g:link controller="requisition" action="show" id="${transaction?.requisition?.id }">
                                                                ${transaction?.requisition?.requestNumber } |
                                                                ${transaction?.requisition?.name }
                                                            </g:link>
                                                        </g:elseif>
                                                        <g:else>
                                                            <span class="fade">${warehouse.message(code:'default.none.label') }</span>
                                                        </g:else>
                                                    </div>
                                                </td>
                                                <td class="border-right center">
                                                    <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                                        ${quantityChange }
                                                    </g:if>
                                                </td>
                                                <td  class="border-right center">
                                                    <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                                        (${quantityChange })
                                                    </g:if>
                                                </td>
                                                <td class="border-right center">
                                                    ${balance}
												</td>
											</tr>
                                            <g:set var="previousMonth" value="${transaction?.transactionDate?.month}"/>

                                        </g:each>
										<%-- 
										<!-- Commented out because it's a little confusing --> 
										<tr class="prop" style="height: 3em;">
											<td colspan="3" style="text-align: right; font-size: 1.5em; vertical-align: middle;">
												Recent changes
											</td>
											<td style="text-align: center; font-size: 1.5em; vertical-align: middle">
												<g:if test="${totalQuantityChange>0}">${totalQuantityChange}</g:if>
												<g:else><span style="color: red;">${totalQuantityChange}</span></g:else>	
											</td>
										</tr>
										--%>															
									</g:else>
								</tbody>
                                <tfoot>
                                    <tr>
                                        <th colspan="6" class="left border-right">
                                            <warehouse:message code="stockCard.endBalance.label" default="End balance"/>
                                        </th>
                                        <th class="center border-right">

                                        </th>
                                        <th class="center border-right">

                                        </th>
                                        <th class="center border-right">
                                            ${balance}
                                        </th>

                                    </tr>
                                </tfoot>
							</table>
						</td>
					</tr>
				</table>
		</g:form>
	</div>
</div>

	