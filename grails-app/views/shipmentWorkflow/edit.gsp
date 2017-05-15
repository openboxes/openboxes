
<%@ page import="org.pih.warehouse.shipping.ShipmentWorkflow" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
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

            <g:form method="post" >
	                <g:hiddenField name="id" value="${shipmentWorkflowInstance?.id}" />
	                <g:hiddenField name="version" value="${shipmentWorkflowInstance?.version}" />
	                <div class="box">
                        <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
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
	                                    <g:textField class="text" name="excludedFields" value="${shipmentWorkflowInstance?.excludedFields}" size="100" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="documentTemplate"><warehouse:message code="shipmentWorkflow.documentTemplate.label" default="Document Template" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'documentTemplate', 'errors')}">
	                                    <g:textField class="text" name="documentTemplate" cols="40" rows="5" value="${shipmentWorkflowInstance?.documentTemplate}" size="100" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="referenceNumberTypes"><warehouse:message code="shipmentWorkflow.referenceNumberTypes.label" default="Reference Number Types" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'referenceNumberTypes', 'errors')}">
	                                    <g:select class="chzn-select-deselect" name="referenceNumberTypes" from="${org.pih.warehouse.shipping.ReferenceNumberType.list()}" multiple="yes" optionKey="id" size="5" value="${shipmentWorkflowInstance?.referenceNumberTypes}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="containerTypes"><warehouse:message code="shipmentWorkflow.containerTypes.label" default="Container Types" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'containerTypes', 'errors')}">
	                                    <g:select class="chzn-select-deselect" name="containerTypes" from="${org.pih.warehouse.shipping.ContainerType.list()}" multiple="yes" optionKey="id" size="5" value="${shipmentWorkflowInstance?.containerTypes}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="documentTemplates"><warehouse:message code="shipmentWorkflow.documentTemplates.label" default="Document Templates" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentWorkflowInstance, field: 'documentTemplates', 'errors')}">

                                        <g:select class="chzn-select-deselect" name="documentTemplates" from="${documentTemplates}" multiple="yes" optionKey="id" size="5" noSelection="['':'']"
                                                  value="${shipmentWorkflowInstance?.documentTemplates}" />
	                                </td>
	                            </tr>


                                <tr class="prop">
                                    <td></td>
                                    <td valign="top">
                                        <div class="buttons left">
                                            <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                            <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                        </div>
                                    </td>
                                </tr>
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
