<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'attribute.label', default: 'Attribute')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">  
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>


            <div class="buttonBar">
                <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></g:link>
                <g:isUserAdmin>
                    <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></g:link>
                </g:isUserAdmin>
            </div>


            <div class="box">

                <h2>${attributeInstance.name}</h2>

                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="default.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: attributeInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="default.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value"><format:metadata obj="${attributeInstance}"/></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="default.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${attributeInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="default.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${attributeInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="attribute.allowOther.label" default="Allow Other" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${attributeInstance?.allowOther}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="attribute.options.label" default="Options" /></td>
                            
                            <td valign="top" class="value">${attributeInstance.options.join(", ")}</td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top" colspan="2">
					            <div class="buttons">
					                <g:form>
					                    <g:hiddenField name="id" value="${attributeInstance?.id}" />
					                    <g:actionSubmit class="button" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
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
