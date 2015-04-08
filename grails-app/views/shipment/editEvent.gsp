<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'event.label', default: 'Event')}" />
	<title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="shipping.addNewEvent.label"/></content>
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	

		<div class="dialog">
			<g:form action="saveEvent" method="POST">
				<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
				<g:hiddenField name="eventId" value="${eventInstance?.id}" />
				<g:render template="summary"/>

				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
							<tr class="prop">
							   <td valign="top" class="name"><label><warehouse:message code="shipping.eventType.label"/></label></td>
							   <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventType', 'errors')}">
									<g:if test="${!eventInstance?.eventType}">
										<g:select id="eventType.id" name='eventType.id' noSelection="['':warehouse.message(code:'default.selectOne.label')]"
											from='${org.pih.warehouse.core.EventType.list()}' optionKey="id"
											optionValue="name" value="${eventInstance.eventType}" >
										</g:select>
									</g:if>
									<g:else>
										<g:hiddenField name="eventType.id" value="${eventInstance?.eventType?.id}"/>
										<format:metadata obj="${eventInstance?.eventType}"/>
									</g:else>
								</td>
							</tr>

							<tr class="prop">
								   <td valign="top" class="name"><label><warehouse:message code="shipping.eventDate.label" /></label></td>
								   <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
									   <g:datePicker name="eventDate" value="${eventInstance?.eventDate}" precision="minute"/>

								   </td>
							</tr>

							<tr class="prop">
							   <td valign="top" class="name"><label><warehouse:message code="location.label" /></label></td>
							   <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
								<g:select id="eventLocation.id" name='eventLocation.id' noSelection="['':warehouse.message(code:'default.selectOne.label')]"
									from='${org.pih.warehouse.core.Location.list()}' optionKey="id" optionValue="name"
									value="${eventInstance?.eventLocation?.id}" class="chzn-select-deselect">
									</g:select>
								  </td>
							</tr>
						</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<div class="buttons">
									<button type="submit" class="button icon approve">
										<warehouse:message code="default.button.save.label"/>
									</button>
									&nbsp;
									<g:link action="deleteEvent" id="${eventInstance?.id}" params="[shipmentId:shipmentInstance.id]" class="button icon trash">
										<warehouse:message code="default.button.delete.label"/>
									</g:link>
									&nbsp;
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="">
										<warehouse:message code="default.button.cancel.label"/>
									</g:link>
								</div>
							</td>
						</tr>
					</tfoot>
					</table>
				</div>
			</g:form>
		</div>
	</div>
</body>
</html>
