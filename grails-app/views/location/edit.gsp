
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'location.suppliersCustomers.label')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" action="update">
            	<fieldset>
            		
	                <g:hiddenField name="id" value="${locationInstance?.id}" />
	                <g:hiddenField name="version" value="${locationInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="name"><warehouse:message code="location.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${locationInstance?.name}" />
	                                </td>
	                            </tr>
	                             <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="type"><warehouse:message code="location.type.label" default="Type" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
	                                   <g:select name="locationType.id"
									          from="${org.pih.warehouse.core.LocationType.list().findAll { it.id != org.pih.warehouse.core.Constants.WAREHOUSE_LOCATION_TYPE_ID } }"
									          value="${locationType?.id}"
									          optionKey="id" optionValue="${{format.metadata(obj:it)}}"/>
	                                </td>
	                            </tr>
	                           
	                          
	                            <tr>
	                            	<td valign="top"></td>
	                            	<td valign="top">
	                            			<button type="submit">								
												<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;${warehouse.message(code: 'default.button.save.label', default: 'Save')}
											</button>
											&nbsp;
											<g:link action="delete" id="${locationInstance.id}">
												<button type="button" class="negative" id="${transactionInstance?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
							    					<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
													${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}
												</button>
											</g:link>
											&nbsp;
											<g:link action="list">
								                    ${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}						
											</g:link>		
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
