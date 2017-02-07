
<%@ page import="org.pih.warehouse.shipping.Shipper" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipper.label', default: 'Shipper')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${shipperInstance}">
                <div class="errors">
                    <g:renderErrors bean="${shipperInstance}" as="list" />
                </div>
            </g:hasErrors>



            <g:form action="save" method="post" >
				<div class="dialog">

                    <div class="nav" role="navigation">
                        <ul>
                            <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                            <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                            <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                        </ul>
                    </div>

					<div class="box">
						<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><warehouse:message code="default.name.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipperInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" cols="40" rows="5" value="${shipperInstance?.name}" class="text" size="80"/>
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><warehouse:message code="default.description.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipperInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" cols="40" rows="5" value="${shipperInstance?.description}" class="text" size="80"/>
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="trackingUrl"><warehouse:message code="shipper.trackingUrl.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipperInstance, field: 'trackingUrl', 'errors')}">
	                                    <g:textField name="trackingUrl" cols="40" rows="5" value="${shipperInstance?.trackingUrl}" class="text" size="80" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="trackingFormat"><warehouse:message code="shipper.trackingFormat.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipperInstance, field: 'trackingFormat', 'errors')}">
	                                    <g:textField name="trackingFormat" cols="40" rows="5" value="${shipperInstance?.trackingFormat}" class="text" size="80" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="parameterName"><warehouse:message code="shipper.parameterName.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipperInstance, field: 'parameterName', 'errors')}">
	                                    <g:textField name="parameterName" cols="40" rows="5" value="${shipperInstance?.parameterName}" class="text" size="80" />
	                                </td>
	                            </tr>
	                        
	                        
		                        <tr class="prop">
		                        	<td valign="top" class="name"></td>
		                        	<td valign="top" class="value">
									   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label')}" />

									   <g:link action="index">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
		                        	</td>
		                        </tr>
		                        
	                        </tbody>
	                    </table>

					</div>

				</div>
            </g:form>
        </div>
    </body>
</html>
