
<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'warehouse.label', default: 'Requisition')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">

            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${requisition}">
	            <div class="errors">
	                <g:renderErrors bean="${requisition}" as="list" />
	            </div>
            </g:hasErrors>


            <g:form method="post" action="save">
                <g:hiddenField name="id" value="${requisition?.id}" />
                <g:hiddenField name="version" value="${requisition?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="name"><warehouse:message code="default.name.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'name', 'errors')}">
									<g:textField name="name" value="${requisition?.name}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="origin.id"><warehouse:message code="requisition.origin.label" /></label>

                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                	<g:select name="origin.id" from="${org.pih.warehouse.core.Location.list()}"
                                		optionKey="id" optionValue="name" value="${requisition?.origin?.id}" noSelection="['null':'']" />

                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
									<label for="destination.id"><warehouse:message code="requisition.destination.label" /></label>

                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'destination', 'errors')}">
                                	<g:select name="destination.id" from="${org.pih.warehouse.core.Location.list()}"
                                		optionKey="id" optionValue="name" value="${requisition?.destination?.id}" noSelection="['null':'']" />

                                </td>
                            </tr>

                           <tr class="prop">
                                <td valign="top" class="name">
									<label for="requestedBy.id"><warehouse:message code="requisition.requestedBy.label" /></label>

                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisition, field: 'requestedBy', 'errors')}">
                                	<g:select name="requestedBy.id" from="${org.pih.warehouse.core.Person.list()}"
                                		optionKey="id" optionValue="name" value="${requisition?.requestedBy?.id}" noSelection="['null':'']" />

                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="requisition.dateRequested.label"/></label></td>
                                <td class="value ${hasErrors(bean: requisition, field: 'dateRequested', 'errors')}">
                                    <g:jqueryDatePicker id="dateRequested" name="dateRequested"
                                        value="${requisition?.dateRequested}" format="MM/dd/yyyy" maxDate="${new Date()}"/>
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label"/></label></td>
                                <td class="value ${hasErrors(bean: requisition, field: 'requestedDeliveryDate', 'errors')}">
                                    <g:jqueryDatePicker id="requestedDeliveryDate" name="requestedDeliveryDate"
                                        value="${requisition?.requestedDeliveryDate}" format="MM/dd/yyyy" minDate="${new Date().plus(1)}"/>
                                </td>
                            </tr>


                            <tr class="prop">

                            	<td valign="top" class="name">

                            	</td>
                            	<td class="value">
									<div class="buttons left">
					                   <button type="submit">
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
											<warehouse:message code="default.button.save.label"/>
										</button>
										&nbsp;
										<g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label')}
										</g:link>
									</div>
								</td>
							</tr>

                        </tbody>
                    </table>
                </div>


            </g:form>
        </div>

    </body>
</html>
