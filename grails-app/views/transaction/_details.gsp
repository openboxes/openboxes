<div class="box">
    <h2>${warehouse.message(code: 'transaction.details.label')}</h2>


	<table>
		<tr class="propOff">
			<td class=""><label><warehouse:message
						code="transaction.transactionNumber.label" /></label>
                <div class="transactionNumber"> <g:if
						test="${transactionInstance?.transactionNumber }">
						${transactionInstance?.transactionNumber }
					</g:if> <g:else>
						<span class="fade"><warehouse:message
								code="transaction.new.label" /></span>
					</g:else>
			    </div>
            </td>
		</tr>
		
		
		<tr class="propOff">
			<td class=""><label><warehouse:message
						code="transaction.type.label" /></label>
                <div class="transaction-type-${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
					<format:metadata obj="${transactionInstance?.transactionType}" /> <g:if
						test="${transactionInstance?.source }">
						<warehouse:message code="default.from.label" />
						${transactionInstance?.source?.name }
					</g:if> <g:if test="${transactionInstance?.destination }">
						<warehouse:message code="default.to.label" />
						${transactionInstance?.destination?.name }
					</g:if>
                </div>
			</td>
		</tr>
        <tr id="inventory" class="propOff">
            <td class=""><label><warehouse:message
                    code="inventory.label" /></label><div> <format:metadata
                    obj="${transactionInstance?.inventory?.warehouse }" />
            </div></td>
        </tr>
		<g:if test="${transactionInstance?.outgoingShipment }">
			<tr class="propOff">
				<td class=""><label><warehouse:message
							code="shipping.shipment.label" /></label>
                    <div><g:link controller="shipment"
                            action="showDetails"
                            id="${transactionInstance?.outgoingShipment?.id }">
                            ${transactionInstance?.outgoingShipment?.name}
                        </g:link>
                    </div>
                </td>
			</tr>
		</g:if>
		<g:if test="${transactionInstance?.incomingShipment }">
			<tr class="propOff">
				<td>
                    <label><warehouse:message
                                code="shipping.shipment.label" /></label>
				    <div>
                        <g:link controller="shipment"
                            action="showDetails"
                            id="${transactionInstance?.incomingShipment?.id }">
                            ${transactionInstance?.incomingShipment?.name}
                        </g:link>
                    </div>
                </td>
			</tr>
		</g:if>
		<tr class="propOff">
			<td class=""><label><warehouse:message
						code="transaction.date.label" /></label><div><format:datetime
						obj="${transactionInstance?.transactionDate}" />
            </div></td>
		</tr>
		<g:if test="${transactionInstance?.comment }">
			<tr class="propOff">
				<td class=""><label><warehouse:message
							code="default.comment.label" /></label><div> ${transactionInstance?.comment }
                </div></td>
			</tr>
		</g:if>
		<tr class="propOff">
			<td class=""><label><warehouse:message
						code="default.createdBy.label" /></label>
                <div>
				    ${transactionInstance?.createdBy?:warehouse.message(code:'default.noone.label')}
                </div>
			</td>
		</tr>
        <tr class="propOff">
            <td class=""><label><warehouse:message
                    code="default.updatedBy.label" /></label>
                <div>
                    ${transactionInstance?.updatedBy?:warehouse.message(code:'default.none.label')}
                </div>
            </td>
        </tr>
		<tr class="propOff">
			<td class=""><label><warehouse:message
						code="default.dateCreated.label" /></label><div><format:datetime
					obj="${transactionInstance?.dateCreated}" /></div></td>
		</tr>
		<tr class="propOff">
			<td class=""><label><warehouse:message
						code="default.lastUpdated.label" /></label><div><format:datetime
					obj="${transactionInstance?.lastUpdated}" /></div></td>
		</tr>
		
		<g:if test="${localTransfer }">
			<tr class="propOff">
				<td class="">
					<label><warehouse:message code="localTransfer.destinationTransaction.label" default="Incoming transaction"/></label>
                    <div>
                        <g:link id="${localTransfer.destinationTransaction?.id}" controller="inventory" action="showTransaction">
                            ${localTransfer?.destinationTransaction?.transactionNumber?:localTransfer?.destinationTransaction?.transactionType?.name }
                        </g:link>
                    </div>
					
				</td>
			</tr>
			<tr class="propOff">
				<td class="">
					<label><warehouse:message code="localTransfer.sourceTransaction.label" default="Outgoing transaction"/></label>
                    <div>
                        <g:link id="${localTransfer.sourceTransaction?.id}" controller="inventory" action="showTransaction">
                            ${localTransfer?.sourceTransaction?.transactionNumber?:localTransfer?.sourceTransaction?.transactionType?.name }
                        </g:link>
                    </div>
				</td>
			</tr>
		</g:if>

	</table>
</div>