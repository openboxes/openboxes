
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
    </head>    

    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
            
            
            	<fieldset>
            		<legend>${fieldValue(bean: productInstance, field: "name")}</legend>
	                <table>
	                    <tbody>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="product.id.label" default="ID" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "id")}</td>                            
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="product.type.label" default="Type" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "productType.name")}</td>                            
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="product.upc.label" default="UPC" /></td>                            
	                            <td valign="top" class="value">
	                            	${fieldValue(bean: productInstance, field: "upc")}
	                            	<span class="menuButton" style="padding-left: 25px;">                            
	                            		<a class="browse" target="_new" href="http://www.upcdatabase.com/item/${fieldValue(bean: productInstance, field: "upc")}"><b>Lookup this UPC</b></a>
	                            	</span>
	                            </td>                            
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="product.description.label" default="Description" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "description")}</td>                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="product.categories.label" default="Categories" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "categories")}</td>                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"></td>                            
	                            <td valign="top" class="value">
						            <div class="buttons">
						                <g:form>
						                    <g:hiddenField name="id" value="${productInstance?.id}" />
						                    <g:link controller="product" action="edit" id="${productInstance.id}">
							                    <img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Edit" />
						    					${message(code: 'default.button.edit.label', default: 'Edit')}        
						                    </g:link>
						                    <button class="negative" name="_action_delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
						    					<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" />
							                    ${message(code: 'default.button.delete.label', default: 'Delete')}
						                    </button> 
						                </g:form>
						            </div>
	
								</td>                            
	                        </tr>
	                    </tbody>
	                </table>
                </fieldset>
            </div>
        </div>
    </body>
</html>
