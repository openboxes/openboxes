
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.add.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.add.label" args="[entityName]" /></content>
    </head>    
    <body>
    
        <div class="body">
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post">
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><g:message code="product.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${productInstance?.name}" />
	                                </td>
	                            </tr>
			 					<tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="productType.id"><g:message code="product.productType.label" default="Product Type" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'type', 'errors')}">
	                                    <g:select name="productType.id" from="${org.pih.warehouse.product.ProductType.list()}" optionKey="id" value="${productInstance?.productType?.id}"  />
	                                </td>
	                            </tr>                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="upc"><g:message code="product.upc.label" default="UPC" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
	                                    <g:textField name="upc" value="${productInstance?.upc}" />
	                                </td>
	                            </tr>                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><g:message code="product.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${productInstance?.description}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                            	<td valign="top" class="name"></td>
	                            	<td>
						                <div class="buttons">
											<button type="submit" class="positive">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> 
												${message(code: 'default.button.save.label', default: 'Save')}</button>
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
