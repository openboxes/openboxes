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
							code="requisition.requestedDeliveryDate.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: documentInstance, field: 'requestedDeliveryDate', 'errors')}">
						<g:datePicker name="requestedDeliveryDate" value="${stockMovement?.requestedDeliveryDate}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="shipping.expectedShippingDate.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: documentInstance, field: 'expectedShippingDate', 'errors')}">
						<g:datePicker name="expectedShippingDate" value="${stockMovement?.expectedShippingDate}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="shipping.expectedDeliveryDate.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: documentInstance, field: 'expectedDeliveryDate', 'errors')}">
						<g:datePicker name="expectedDeliveryDate" value="${stockMovement?.expectedDeliveryDate}"/>
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
