
<%@ page import="org.pih.warehouse.shipping.ShipmentWorkflow" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${shipmentWorkflowInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${shipmentWorkflowInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <div class="button-bar">
                <g:link class="button" action="create"><warehouse:message code="default.list.label" args="['Shipment Workflow']"/></g:link>
                <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['Shipment Workflow']"/></g:link>
            </div>

            <g:form action="save" method="post" >
                <div class="box">
                    <h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="shipmentWorkflow.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'name', 'errors')}">
                                    <g:textField class="text" name="name" value="${shipmentWorkflowInstance?.name}" size="100"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="shipmentType.id"><warehouse:message code="shipmentWorkflow.shipmentType.label" default="Shipment Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'shipmentType', 'errors')}">
                                    <g:select class="chzn-select-deselect" name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" optionKey="id" value="${shipmentWorkflowInstance?.shipmentType?.id}"  />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="excludedFields"><warehouse:message code="shipmentWorkflow.excludedFields.label" default="Excluded Fields" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'excludedFields', 'errors')}">
                                    <g:textField class="text" name="excludedFields" value="${shipmentWorkflowInstance?.excludedFields}" size="100"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="documentTemplate"><warehouse:message code="shipmentWorkflow.documentTemplate.label" default="Document Template" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'documentTemplate', 'errors')}">
                                    <g:textField class="text" name="documentTemplate" value="${shipmentWorkflowInstance?.documentTemplate}" size="100"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated"><warehouse:message code="shipmentWorkflow.dateCreated.label" default="Date Created" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'dateCreated', 'errors')}">
                                    <g:datePicker name="dateCreated" precision="minute" value="${shipmentWorkflowInstance?.dateCreated}" noSelection="['': '']" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated"><warehouse:message code="shipmentWorkflow.lastUpdated.label" default="Last Updated" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'lastUpdated', 'errors')}">
                                    <g:datePicker name="lastUpdated" precision="minute" value="${shipmentWorkflowInstance?.lastUpdated}" noSelection="['': '']" />
                                </td>
                            </tr>


                            <tr class="prop">
                                <td></td>
                                <td valign="top">
                                   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

                                   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
            </g:form>
        </div>
    </body>
</html>
