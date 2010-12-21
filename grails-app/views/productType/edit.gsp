
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'productType.label', default: 'ProductType')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
			<div class="nav">            	
				<g:render template="nav"/>
           	</div>
        
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productTypeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productTypeInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
            	<fieldset>
	                <g:hiddenField name="id" value="${productTypeInstance?.id}" />
	                <g:hiddenField name="version" value="${productTypeInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="name"><g:message code="productType.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${productTypeInstance?.name}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="productClass"><g:message code="productType.productClass.label" default="Product Class" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'productClass', 'errors')}">
	                                    <g:select name="productClass" from="${org.pih.warehouse.product.ProductClass?.values()}" value="${productTypeInstance?.productClass}" noSelection="['': '']" />
	                                </td>
	                            </tr>
	                        
<%-- 	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="code"><g:message code="productType.code.label" default="Code" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'code', 'errors')}">
	                                    <g:textField name="code" value="${productTypeInstance?.code}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="description"><g:message code="productType.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${productTypeInstance?.description}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="sortOrder"><g:message code="productType.sortOrder.label" default="Sort Order" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'sortOrder', 'errors')}">
	                                    <g:textField name="sortOrder" value="${fieldValue(bean: productTypeInstance, field: 'sortOrder')}" />
	                                </td>
	                            </tr>
--%>	                        
<%-- 	                        
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="attributes"><g:message code="productType.attributes.label" default="Attributes" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'attributes', 'errors')}">
	                                    <g:select name="attributes" from="${org.pih.warehouse.product.Attribute.list()}" multiple="yes" optionKey="id" optionValue="name" size="5" value="${productTypeInstance?.attributes}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="categories"><g:message code="productType.categories.label" default="Categories" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'categories', 'errors')}">
	                                    <g:select name="categories" from="${org.pih.warehouse.product.Category.list()}" multiple="yes" optionKey="id" optionValue="name" size="5" value="${productTypeInstance?.categories}" />
	                                </td>
	                            </tr>
--%>	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="categories"><g:message code="productType.categories.label" default="Categories" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'categories', 'errors')}">
	                                	<div style="padding: 10px;">
	                                	
	                                   		<g:set var="counter" value="${1 }"/>
		                                    <g:each var="category" in="${org.pih.warehouse.product.Category.list().sort() { it?.categories?.size() }.reverse() }" status="status">
		                                    	<g:if test="${!category.parentCategory }">
		                                    		<div>
			                                    		<g:checkBox name="category_${category.id}" value="${productTypeInstance?.categories?.contains(category) }"/> 
			                                    		<g:if test="${!category.parentCategory }"><b>${category.name }</b></g:if>
														<g:else>${category.name }</g:else>
													</div>
													<div style="padding-left: 35px;">									
				                                    	<g:each var="childCategory" in="${category.categories}">														
				                                    		<g:checkBox name="category_${childCategory.id}" value="${productTypeInstance?.categories?.contains(childCategory) }"/> 
				                                    		<g:if test="${!childCategory.parentCategory }"><b>${childCategory.name }</b></g:if>
															<g:else>${childCategory.name }</g:else>
															<br/>
				                                    	</g:each>
			                                    	</div>
		                                    	</g:if>
		                                    </g:each>                                    		
		                            	</div>
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
