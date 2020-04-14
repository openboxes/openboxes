<div class="box">
    <h2>${warehouse.message(code: 'transaction.details.label')}</h2>


	<table>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.transactionNumber.label" /></label>
            </td>
            <td class="value">
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


		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.type.label" /></label>
            </td>
            <td class="value">
                <div class="transaction-type-${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
					<format:metadata obj="${transactionInstance?.transactionType}" />
                </div>
			</td>
		</tr>
        <g:if test="${transactionInstance?.source }">
            <tr id="source" class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.source.label" /></label>
                </td>
                <td class="value">
                    <format:metadata obj="${transactionInstance?.source?.name }" />
                </td>
            </tr>
        </g:if>
        <g:if test="${transactionInstance?.destination }">
            <tr id="destination" class="prop">
                <td class="name">
                    <label><warehouse:message code="transaction.destination.label" /></label>
                </td>
                <td class="value">
                    <format:metadata obj="${transactionInstance?.destination?.name }" />
                </td>
            </tr>
        </g:if>
        <tr id="inventory" class="prop">
            <td class="name"><label><warehouse:message
                    code="inventory.label" /></label>
            </td>
            <td class="value">

                <div> <format:metadata
                    obj="${transactionInstance?.inventory?.warehouse }" />
            </div></td>
        </tr>
		<g:if test="${transactionInstance?.outgoingShipment }">
			<tr class="prop">
				<td class="name"><label><warehouse:message
							code="shipping.shipment.label" /></label>
                </td>
                <td class="value">
                    <g:link controller="shipment"
                            action="showDetails"
                            id="${transactionInstance?.outgoingShipment?.id }">
                            ${transactionInstance?.outgoingShipment?.shipmentNumber}
                    </g:link>
                </td>
			</tr>
		</g:if>
		<g:if test="${transactionInstance?.incomingShipment }">
			<tr class="prop">
				<td class="name">
                    <label><warehouse:message
                                code="shipping.shipment.label" /></label>
                </td>
                <td class="value">
                    <g:link controller="shipment"
                        action="showDetails"
                        id="${transactionInstance?.incomingShipment?.id }">
                        ${transactionInstance?.incomingShipment?.shipmentNumber}
                    </g:link>
                </td>
			</tr>
		</g:if>
		<g:if test="${transactionInstance?.receipt }">
			<tr class="prop">
				<td class="name">
                    <label><warehouse:message
                                code="shipping.receipt.label" /></label>
                </td>
                <td class="value">
                    <g:link controller="receipt"
                        action="show"
                        id="${transactionInstance?.receipt?.id }">
                        ${transactionInstance?.receipt?.receiptNumber}
                    </g:link>
                </td>
			</tr>
		</g:if>
        <g:if test="${transactionInstance?.order }">
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message
                            code="order.label" /></label>
                </td>
                <td class="value">
                    <g:link controller="order"
                            action="show"
                            id="${transactionInstance?.order?.id }">
                        ${transactionInstance?.order?.name}
                    </g:link>
                </td>
            </tr>
        </g:if>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.date.label" /></label>
            </td>
            <td class="value">
                <format:datetime
						obj="${transactionInstance?.transactionDate}" />
            </td>
		</tr>
        <tr class="prop">
			<td class="name"><label><warehouse:message
						code="default.createdBy.label" /></label>
            </td>
            <td class="value">
                ${transactionInstance?.createdBy?:warehouse.message(code:'default.noone.label')}
			</td>
		</tr>
        <tr class="prop">
            <td class="name"><label><warehouse:message
                    code="default.updatedBy.label" /></label>
            </td>
            <td class="value">
                ${transactionInstance?.updatedBy?:warehouse.message(code:'default.none.label')}
            </td>
        </tr>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="default.dateCreated.label" /></label>

            </td>
            <td class="value">
                <format:datetime obj="${transactionInstance?.dateCreated}" />
            </td>
		</tr>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="default.lastUpdated.label" /></label>
            </td>
            <td class="value">
                <format:datetime obj="${transactionInstance?.lastUpdated}" /></td>
		</tr>
        <tr class="prop">
            <td class="name"><label><warehouse:message
                    code="transaction.localTransfer.label" default="Local transfer"/></label>
            </td>
            <td class="value">
                <div class="localTransfer">
                    ${transactionInstance?.localTransfer?"yes":"no"}
                </div>
            </td>
        </tr>

        <g:if test="${transactionInstance?.localTransfer }">
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="localTransfer.destinationTransaction.label" default="Destination"/></label>
                </td>
                <td class="value">
                    <g:link id="${transactionInstance?.localTransfer?.destinationTransaction?.id}" controller="inventory" action="showTransaction">
                        ${transactionInstance?.localTransfer?.destinationTransaction?.transactionNumber?:transactionInstance?.localTransfer?.destinationTransaction?.transactionType?.name }
                    </g:link>
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="localTransfer.sourceTransaction.label" default="Source"/></label>
                </td>
                <td class="value">
                    <g:link id="${transactionInstance?.localTransfer.sourceTransaction?.id}" controller="inventory" action="showTransaction">
                        ${transactionInstance?.localTransfer?.sourceTransaction?.transactionNumber?:transactionInstance?.localTransfer?.sourceTransaction?.transactionType?.name }
                    </g:link>
				</td>
			</tr>
		</g:if>
        <g:if test="${transactionInstance?.comment }">
            <tr class="prop">
                <td class="name"><label><warehouse:message
                        code="default.comment.label" /></label>
                </td>
                <td class="value">
                    ${transactionInstance?.comment }
                </td>
            </tr>
        </g:if>

	</table>
</div>
