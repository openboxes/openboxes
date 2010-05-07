
<%@ page import="org.pih.warehouse.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>


	    <g:uploadForm action="upload">
		<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
		Upload a document: <input name="contents" type="file" />
		<g:submitButton name="upload" value="Upload"/>
	    </g:uploadForm>


            <div class="dialog">
                <table>
                    <tbody>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.id.label" default="Id" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "id")}</td>                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.expectedShippingDate.label" default="Expected Shipping Date" /></td>                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.expectedShippingDate}" /></td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.actualShippingDate.label" default="Actual Shipping Date" /></td>                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.actualShippingDate}" /></td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.source.label" default="Source" /></td>                            
                            <td valign="top" class="value"><g:link controller="warehouse" action="show" id="${shipmentInstance?.source?.id}">${shipmentInstance?.source?.encodeAsHTML()}</g:link></td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.target.label" default="Target" /></td>
                            <td valign="top" class="value"><g:link controller="warehouse" action="show" id="${shipmentInstance?.target?.id}">${shipmentInstance?.target?.encodeAsHTML()}</g:link></td>                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.products.label" default="Products" /></td>
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
				    <g:each in="${shipmentInstance.products}" var="p">
					<li><g:link controller="product" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
				    </g:each>
                                </ul>
                            </td>                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.documents.label" default="Documents" /></td>
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
				    <g:each in="${shipmentInstance.documents}" var="document">
					<li><g:link controller="product" action="show" id="${document.id}">${document?.encodeAsHTML()}</g:link></li>
				    </g:each>
                                </ul>
                            </td>
                        </tr>

                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${shipmentInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
