
<%@ page import="org.pih.warehouse.product.DrugProduct" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'drugProduct.label', default: 'DrugProduct')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>    
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${drugProductInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${drugProductInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="drugProduct.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${drugProductInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="category"><g:message code="drugProduct.category.label" default="Category" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'category', 'errors')}">
                                    <g:select name="category.id" from="${org.pih.warehouse.product.Category.list()}" optionKey="id" value="${drugProductInstance?.category?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="genericType"><g:message code="drugProduct.genericType.label" default="Generic Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'genericType', 'errors')}">
                                    <g:select name="genericType.id" from="${org.pih.warehouse.product.GenericType.list()}" optionKey="id" value="${drugProductInstance?.genericType?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="productType"><g:message code="drugProduct.productType.label" default="Product Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'productType', 'errors')}">
                                    <g:select name="productType.id" from="${org.pih.warehouse.product.ProductType.list()}" optionKey="id" value="${drugProductInstance?.productType?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="tags"><g:message code="drugProduct.tags.label" default="Tags" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'tags', 'errors')}">
                                    <g:textField name="tags" value="${drugProductInstance?.tags}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="weight"><g:message code="drugProduct.weight.label" default="Weight" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'weight', 'errors')}">
                                    <g:textField name="weight" value="${fieldValue(bean: drugProductInstance, field: 'weight')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description"><g:message code="drugProduct.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${drugProductInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="unverified"><g:message code="drugProduct.unverified.label" default="Unverified" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'unverified', 'errors')}">
                                    <g:checkBox name="unverified" value="${drugProductInstance?.unverified}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="markAsImportant"><g:message code="drugProduct.markAsImportant.label" default="Mark As Important" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'markAsImportant', 'errors')}">
                                    <g:checkBox name="markAsImportant" value="${drugProductInstance?.markAsImportant}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="unit"><g:message code="drugProduct.unit.label" default="Unit" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'unit', 'errors')}">
                                    <g:textField name="unit" value="${drugProductInstance?.unit}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="quantityPerUnit"><g:message code="drugProduct.quantityPerUnit.label" default="Quantity Per Unit" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'quantityPerUnit', 'errors')}">
                                    <g:textField name="quantityPerUnit" value="${drugProductInstance?.quantityPerUnit}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="ean"><g:message code="drugProduct.ean.label" default="Ean" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'ean', 'errors')}">
                                    <g:textField name="ean" value="${drugProductInstance?.ean}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="productCode"><g:message code="drugProduct.productCode.label" default="Product Code" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'productCode', 'errors')}">
                                    <g:textField name="productCode" value="${drugProductInstance?.productCode}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="genericName"><g:message code="drugProduct.genericName.label" default="Generic Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'genericName', 'errors')}">
                                    <g:textField name="genericName" value="${drugProductInstance?.genericName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dosageStrength"><g:message code="drugProduct.dosageStrength.label" default="Dosage Strength" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'dosageStrength', 'errors')}">
                                    <g:textField name="dosageStrength" value="${drugProductInstance?.dosageStrength}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dosageForm"><g:message code="drugProduct.dosageForm.label" default="Dosage Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'dosageForm', 'errors')}">
                                    <g:textField name="dosageForm" value="${drugProductInstance?.dosageForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dosageRegimen"><g:message code="drugProduct.dosageRegimen.label" default="Dosage Regimen" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'dosageRegimen', 'errors')}">
                                    <g:textField name="dosageRegimen" value="${drugProductInstance?.dosageRegimen}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="drugClass"><g:message code="drugProduct.drugClass.label" default="Drug Class" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'drugClass', 'errors')}">
                                    <g:textField name="drugClass" value="${drugProductInstance?.drugClass}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="drugRouteType"><g:message code="drugProduct.drugRouteType.label" default="Drug Route Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'drugRouteType', 'errors')}">
                                    <g:select name="drugRouteType.id" from="${org.pih.warehouse.product.DrugRouteType.list()}" optionKey="id" value="${drugProductInstance?.drugRouteType?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="majorDrugClass"><g:message code="drugProduct.majorDrugClass.label" default="Major Drug Class" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'majorDrugClass', 'errors')}">
                                    <g:select name="majorDrugClass.id" from="${org.pih.warehouse.product.DrugClass.list()}" optionKey="id" value="${drugProductInstance?.majorDrugClass?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
