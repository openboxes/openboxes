<div class="box">
    <h2>${warehouse.message(code: 'transaction.details.label')}</h2>


	<table>
		<tr class="prop">
			<td class=""><label><warehouse:message
						code="transaction.transactionNumber.label" /></label>
            </td>
            <td>
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
			<td class=""><label><warehouse:message
						code="transaction.type.label" /></label>
            </td>
            <td>
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
        <tr id="inventory" class="prop">
            <td class=""><label><warehouse:message
                    code="inventory.label" /></label>
            </td>
            <td>

                <div> <format:metadata
                    obj="${transactionInstance?.inventory?.warehouse }" />
            </div></td>
        </tr>
		<g:if test="${transactionInstance?.outgoingShipment }">
			<tr class="prop">
				<td class=""><label><warehouse:message
							code="shipping.shipment.label" /></label>
                </td>
                <td>

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
			<tr class="prop">
				<td>
                    <label><warehouse:message
                                code="shipping.shipment.label" /></label>
                </td>
                <td>

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
        <g:if test="${transactionInstance?.order }">
            <tr class="prop">
                <td>
                    <label><warehouse:message
                            code="order.label" /></label>
                </td>
                <td>

                    <div>
                        <g:link controller="order"
                                action="show"
                                id="${transactionInstance?.order?.id }">
                            ${transactionInstance?.order?.name}
                        </g:link>
                    </div>
                </td>
            </tr>
        </g:if>
		<tr class="prop">
			<td class=""><label><warehouse:message
						code="transaction.date.label" /></label>
            </td>
            <td>

                <div><format:datetime
						obj="${transactionInstance?.transactionDate}" />
            </div></td>
		</tr>
        <tr class="prop">
			<td class=""><label><warehouse:message
						code="default.createdBy.label" /></label>
            </td>
            <td>

                <div>
				    ${transactionInstance?.createdBy?:warehouse.message(code:'default.noone.label')}
                </div>
			</td>
		</tr>
        <tr class="prop">
            <td class=""><label><warehouse:message
                    code="default.updatedBy.label" /></label>
            </td>
            <td>

                <div>
                    ${transactionInstance?.updatedBy?:warehouse.message(code:'default.none.label')}
                </div>
            </td>
        </tr>
		<tr class="prop">
			<td class=""><label><warehouse:message
						code="default.dateCreated.label" /></label>

            </td>
            <td>
                <div>
                    <format:datetime
					    obj="${transactionInstance?.dateCreated}" /></div></td>
		</tr>
		<tr class="prop">
			<td class=""><label><warehouse:message
						code="default.lastUpdated.label" /></label>
            </td>
            <td>
                <div><format:datetime
					obj="${transactionInstance?.lastUpdated}" /></div></td>
		</tr>
        <tr class="prop">
            <td class=""><label><warehouse:message
                    code="transaction.localTransfer.label" default="Local transfer"/></label>
            </td>
            <td>
                <div class="localTransfer">
                    ${transactionInstance?.localTransfer?"yes":"no"}
                </div>
            </td>
        </tr>

        <g:if test="${transactionInstance?.localTransfer }">


			<tr class="prop">
				<td class="">
					<label><warehouse:message code="localTransfer.destinationTransaction.label" default="Destination"/></label>
                </td>
                <td>
                    <div>
                        <g:link id="${transactionInstance?.localTransfer?.destinationTransaction?.id}" controller="inventory" action="showTransaction">
                            ${transactionInstance?.localTransfer?.destinationTransaction?.transactionNumber?:transactionInstance?.localTransfer?.destinationTransaction?.transactionType?.name }
                        </g:link>
                    </div>

				</td>
			</tr>
			<tr class="prop">
				<td class="">
					<label><warehouse:message code="localTransfer.sourceTransaction.label" default="Source"/></label>
                </td>
                <td>
                    <div>
                        <g:link id="${transactionInstance?.localTransfer.sourceTransaction?.id}" controller="inventory" action="showTransaction">
                            ${transactionInstance?.localTransfer?.sourceTransaction?.transactionNumber?:transactionInstance?.localTransfer?.sourceTransaction?.transactionType?.name }
                        </g:link>
                    </div>
				</td>
			</tr>
		</g:if>
        <g:if test="${transactionInstance?.comment }">
            <tr class="prop">
                <td class=""><label><warehouse:message
                        code="default.comment.label" /></label>
                </td>
                <td>
                    <div>
                        ${transactionInstance?.comment }
                    </div>
                </td>
            </tr>
        </g:if>

	</table>
</div>