
<%@ page import="org.pih.warehouse.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>		
		<g:javascript library="prototype" />
    </head>    

    <body>
       	<h1>Show Product</h1>
        <div class="body">
        
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.id.label" default="ID" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "id")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.name.label" default="Name" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "name")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.ean.label" default="UPC" /></td>                            
                            <td valign="top" class="value">
                            	${fieldValue(bean: productInstance, field: "ean")}
                            	
                            	<span class="menuButton" style="padding-left: 25px;">                            
                            		<a class="browse" target="_new" href="http://www.upcdatabase.com/item/${fieldValue(bean: productInstance, field: "ean")}"><b>Lookup this UPC</b></a>
                            	</span>
                            </td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.type.label" default="Type" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "type.name")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.subtype.label" default="Subtype" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "subType.name")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.description.label" default="Description" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "description")}</td>                            
                        </tr>                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${productInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
