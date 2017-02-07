
<%@ page import="org.pih.warehouse.core.LocationType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationType.label', default: 'LocationType')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">

            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                </ul>
            </div>

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationTypeInstance}">
                <div class="errors">
                    <g:renderErrors bean="${locationTypeInstance}" as="list" />
                </div>
            </g:hasErrors>


            <g:form action="save" method="post" >
				<div class="dialog">

	                <div class="box">
						<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
                        <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><warehouse:message code="locationType.name.label" default="Name"/></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationTypeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${locationTypeInstance?.name}"  class="text"/>
	                                </td>
	                            </tr>
	                        	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><warehouse:message code="locationType.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationTypeInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${locationTypeInstance?.description}"  class="text"/>
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
	                                	<g:select name="supportedActivities" multiple="true" from="${activityList }" size="${activityList.size() }"
                                            class="chzn-select-deselect"
	                                		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${warehouseInstance?.supportedActivities}" />
	                                </td>
	                            </tr>	                            
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="sortOrder"><warehouse:message code="locationType.sortOrder.label" default="Sort Order" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationTypeInstance, field: 'sortOrder', 'errors')}">
	                                    <g:textField name="sortOrder" value="${fieldValue(bean: locationTypeInstance, field: 'sortOrder')}" class="text" />
	                                </td>
	                            </tr>
	                        
	                        
		                        <tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">
									   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />

									   <g:link action="list" class="button">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
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
