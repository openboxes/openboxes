
<%@ page import="org.pih.warehouse.product.ProductSupplier" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')}" />
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
                            <td valign="top" class="name"><warehouse:message code="productSupplier.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.product.label" default="Product" /></td>
                            
                            <td valign="top" class="value"><g:link controller="product" action="show" id="${productSupplierInstance?.product?.id}">${productSupplierInstance?.product?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.identifier.label" default="Identifier" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "identifier")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.upc.label" default="Upc" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "upc")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.ndc.label" default="Ndc" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "ndc")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.supplier.label" default="Supplier" /></td>
                            
                            <td valign="top" class="value"><g:link controller="organization" action="show" id="${productSupplierInstance?.supplier?.id}">${productSupplierInstance?.supplier?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.supplierCode.label" default="Supplier Code" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "supplierCode")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.supplierName.label" default="Supplier Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "supplierName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.modelNumber.label" default="Model Number" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "modelNumber")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.brandName.label" default="Brand Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "brandName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.manufacturer.label" default="Manufacturer" /></td>
                            
                            <td valign="top" class="value"><g:link controller="organization" action="show" id="${productSupplierInstance?.manufacturer?.id}">${productSupplierInstance?.manufacturer?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "manufacturerCode")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.manufacturerName.label" default="Manufacturer Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "manufacturerName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.standardLeadTimeDays.label" default="Standard Lead Time Days" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "standardLeadTimeDays")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.pricePerUnit.label" default="Price Per Unit" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "pricePerUnit")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.minOrderQuantity.label" default="Min Order Quantity" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "minOrderQuantity")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.ratingTypeCode.label" default="Rating Type Code" /></td>
                            
                            <td valign="top" class="value">${productSupplierInstance?.ratingTypeCode?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.preferenceTypeCode.label" default="Preference Type Code" /></td>
                            
                            <td valign="top" class="value">${productSupplierInstance?.preferenceTypeCode?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productSupplier.comments.label" default="Comments" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productSupplierInstance, field: "comments")}</td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons left">
					                <g:form>
					                    <g:hiddenField name="id" value="${productSupplierInstance?.id}" />
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
