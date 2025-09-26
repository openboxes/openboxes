<%@page import="org.pih.warehouse.inventory.LotStatusCode" %>
<div id="schedule" class="box dialog">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'calendar.png')}" alt="contents" style="vertical-align: middle"/>
        ${warehouse.message(code:'stockMovement.schedule.label', default: 'Scheduling')}
    </h2>

        <g:form controller="stockMovement" action="saveSchedule">
			<g:hiddenField name="id" value="${stockMovement?.id}" />
			<table>
				<tbody>
                <tr class="prop">
                    <td valign="top" class="name"><label><warehouse:message
                            code="requisition.priority.label" default="Priority"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: stockMovement, field: 'priority', 'errors')}">
                        <g:textField name="priority" value="${stockMovement.requisition.priority}" class="text medium"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name"><label><warehouse:message
                            code="requisition.deliveryTypeCode.label" default="Delivery Type"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: stockMovement, field: 'deliveryTypeCode', 'errors')}">
                        <g:select name="deliveryTypeCode"
                                  class="chzn-select-deselect"
                                  noSelection="['null': '']"
                                  from="${org.pih.warehouse.core.DeliveryTypeCode.values()}"
                                  value="${stockMovement.requisition.deliveryTypeCode}"/>

                    </td>
                </tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="requisition.requestedDeliveryDate.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: stockMovement, field: 'requestedDeliveryDate', 'errors')}">
						<g:datePicker name="requestedDeliveryDate" value="${stockMovement?.requisition?.requestedDeliveryDate}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="shipping.expectedShippingDate.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: stockMovement, field: 'expectedShippingDate', 'errors')}">
						<g:datePicker name="expectedShippingDate" value="${stockMovement?.shipment?.expectedShippingDate}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="shipping.expectedDeliveryDate.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: stockMovement, field: 'expectedDeliveryDate', 'errors')}">
						<g:datePicker name="expectedDeliveryDate" value="${stockMovement?.expectedDeliveryDate}"/>
					</td>
				</tr>
			</tbody>
			</table>
			<table>
			<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="stockMovement.receivingLocation.label"  default="Receiving Location"/></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: stockMovement, field: 'receivingLocation', 'errors')}">
						<g:selectInternalLocation name="receivingLocation.id" value="${stockMovement?.receivingLocation?.id}"
												  class="chzn-select-deselect" noSelection="['':'']"
												  activityCode="${org.pih.warehouse.core.ActivityCode.RECEIVE_STOCK}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="stockMovement.packingLocation.label" default="Packing Location" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: stockMovement, field: 'packingLocation', 'errors')}">
						<g:selectInternalLocation name="packingLocation.id" value="${stockMovement?.packingLocation?.id}"
												  class="chzn-select-deselect" noSelection="['':'']"
												  activityCode="${org.pih.warehouse.core.ActivityCode.PACK_STOCK}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="stockMovement.loadingLocation.label" default="Loading Location"/></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: stockMovement, field: 'loadingLocation', 'errors')}">
						<g:selectInternalLocation name="loadingLocation.id" value="${stockMovement?.loadingLocation?.id}"
												  class="chzn-select-deselect" noSelection="['':'']"
												  activityCode="${org.pih.warehouse.core.ActivityCode.LOAD_STOCK}"/>
					</td>
				</tr>

				</tbody>
				<tfoot>
				<tr>
					<td>
					</td>
					<td>
						<div class="buttons left">
							<button type="submit" class="button">${warehouse.message(code:'default.button.save.label')}</button>
						</div>
					</td>
				</tr>
				</tfoot>
			</table>
		</g:form>

</div>