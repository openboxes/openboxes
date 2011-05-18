
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${orderInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${orderInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
            	<fieldset>
	                <g:hiddenField name="id" value="${orderInstance?.id}" />
	                <g:hiddenField name="version" value="${orderInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                        <tbody>                      
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="orderNumber"><g:message code="order.orderNumber.label" default="Order Number" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: orderInstance, field: 'orderNumber', 'errors')}">
	                                    ${orderInstance?.orderNumber}
	                                </td>
	                            </tr>

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="description"><g:message code="order.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: orderInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${orderInstance?.description}" />
	                                </td>
	                            </tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for='source'>Order from:</label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:orderInstance,field:'origin','errors')}'>
										<g:jqueryComboBox />
										<div class="ui-widget"> 
											<g:select class="combobox" name="origin.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${orderInstance?.origin?.id}" noSelection="['':'']" />
										</div>
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for="destination">Destination:</label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:orderInstance,field:'destination','errors')}'>
										<g:jqueryComboBox />
										<div class="ui-widget"> 
											<g:select class="combobox" name="destination.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${orderInstance?.destination?.id}" noSelection="['':'']"/>
										</div>
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for='dateOrdered'>Order date:</label></td>
									<td valign='top'
										class='value ${hasErrors(bean:orderInstance,field:'dateOrdered','errors')}'>								
										<g:jqueryDatePicker 
											id="dateOrdered" 
											name="dateOrdered" 
											value="${orderInstance?.dateOrdered }" 
											format="MM/dd/yyyy"
											size="8"
											showTrigger="false" />								
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for='orderedBy'>Ordered by:</label></td>
									<td valign='top'
										class='value ${hasErrors(bean:orderInstance,field:'orderedBy','errors')}'>
										<g:select class="combobox" name="orderedBy.id" from="${org.pih.warehouse.core.Person.list().sort{it.lastName}}" optionKey="id" value="${orderInstance?.orderedBy?.id}" noSelection="['':'']"/>
		
									</td>
								</tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="dateCreated"><g:message code="order.dateCreated.label" default="Date Created" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: orderInstance, field: 'dateCreated', 'errors')}">
	                                    <g:datePicker name="dateCreated" precision="day" value="${orderInstance?.dateCreated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="lastUpdated"><g:message code="order.lastUpdated.label" default="Last Updated" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: orderInstance, field: 'lastUpdated', 'errors')}">
	                                    <g:datePicker name="lastUpdated" precision="day" value="${orderInstance?.lastUpdated}"  />
	                                </td>
	                            </tr>
	                        
	                        	                        
                            	<tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">                        	
						                <div class="buttons">
						                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
						                    <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
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
