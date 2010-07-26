
<%@ page import="org.pih.warehouse.product.DrugProduct" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'drugProduct.label', default: 'DrugProduct')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
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
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.category.label" default="Category" /></td>
                            
                            <td valign="top" class="value"><g:link controller="category" action="show" id="${drugProductInstance?.category?.id}">${drugProductInstance?.category?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.genericType.label" default="Generic Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="genericType" action="show" id="${drugProductInstance?.genericType?.id}">${drugProductInstance?.genericType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.productType.label" default="Product Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="productType" action="show" id="${drugProductInstance?.productType?.id}">${drugProductInstance?.productType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.tags.label" default="Tags" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "tags")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.weight.label" default="Weight" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "weight")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.unverified.label" default="Unverified" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${drugProductInstance?.unverified}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.markAsImportant.label" default="Mark As Important" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${drugProductInstance?.markAsImportant}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.unit.label" default="Unit" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "unit")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.quantityPerUnit.label" default="Quantity Per Unit" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "quantityPerUnit")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.ean.label" default="Ean" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "ean")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.productCode.label" default="Product Code" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "productCode")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.genericName.label" default="Generic Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "genericName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.dosageStrength.label" default="Dosage Strength" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "dosageStrength")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.dosageForm.label" default="Dosage Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "dosageForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.dosageRegimen.label" default="Dosage Regimen" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "dosageRegimen")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.drugClass.label" default="Drug Class" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "drugClass")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.drugRouteType.label" default="Drug Route Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="drugRouteType" action="show" id="${drugProductInstance?.drugRouteType?.id}">${drugProductInstance?.drugRouteType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.majorDrugClass.label" default="Major Drug Class" /></td>
                            
                            <td valign="top" class="value"><g:link controller="drugClass" action="show" id="${drugProductInstance?.majorDrugClass?.id}">${drugProductInstance?.majorDrugClass?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.brandNames.label" default="Brand Names" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: drugProductInstance, field: "brandNames")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.categories.label" default="Categories" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${drugProductInstance.categories}" var="c">
                                    <li><g:link controller="category" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.conditionTypes.label" default="Condition Types" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${drugProductInstance.conditionTypes}" var="c">
                                    <li><g:link controller="conditionType" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="drugProduct.productAttributeValues.label" default="Product Attribute Values" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${drugProductInstance.productAttributeValues}" var="p">
                                    <li><g:link controller="productAttributeValue" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${drugProductInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
