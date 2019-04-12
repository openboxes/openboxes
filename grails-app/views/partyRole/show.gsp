
<%@ page import="org.pih.warehouse.core.PartyRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'partyRole.label', default: 'PartyRole')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="box">
                <h2><warehouse:message code="default.show.label" args="[entityName]" /></h2>
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="partyRole.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: partyRoleInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="partyRole.party.label" default="Party" /></td>
                            
                            <td valign="top" class="value"><g:link controller="party" action="show" id="${partyRoleInstance?.party?.id}">${partyRoleInstance?.party?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="partyRole.roleType.label" default="Role Type" /></td>
                            
                            <td valign="top" class="value">${partyRoleInstance?.roleType?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="partyRole.startDate.label" default="Start Date" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${partyRoleInstance?.startDate}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="partyRole.endDate.label" default="End Date" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${partyRoleInstance?.endDate}" /></td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons left">
					                <g:form>
					                    <g:hiddenField name="id" value="${partyRoleInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
