
<%@ page import="org.pih.warehouse.product.ProductSupplier" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productSupplierInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productSupplierInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productSupplier']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productSupplier']"/></g:link>
			</div>

			<g:form method="post" >
				<g:hiddenField name="id" value="${productSupplierInstance?.id}" />
				<g:hiddenField name="version" value="${productSupplierInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="product"><warehouse:message code="productSupplier.product.label" default="Product" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'product', 'errors')}">
                                    <g:autoSuggest name="product" class="medium text"
                                                   placeholder="${g.message(code:'product.search.label', default: 'Choose product')}"
                                                   jsonUrl="${request.contextPath }/json/findProductByName"
                                                   valueId="${productSupplierInstance?.product?.id}"
                                                   valueName="${productSupplierInstance?.product?.name}"/>

								    <g:link controller="product" action="edit" id="${productSupplierInstance?.product?.id}" class="button">
                                        <g:message code="default.show.label" args="[g.message(code:'product.label')]"/>
                                    </g:link>
                                </td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="code"><warehouse:message code="productSupplier.code.label" default="Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'code', 'errors')}">
									<g:textField class="text" size="80" name="code" value="${productSupplierInstance?.code}" />
								</td>
							</tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name"><g:message code="default.name.label"/></label>
                                </td>
                                <td class="value ">
                                    <g:textField name="name" size="80" class="medium text" value="${productSupplierInstance?.name}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name"><g:message code="default.description.label"/></label>
                                </td>
                                <td class="value ">
                                    <g:textArea name="description" class="medium text" value="${productSupplierInstance?.description}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name"><g:message code="productSupplier.productCode.label"/></label>
                                </td>
                                <td class="value ">
                                    <g:textArea name="productCode" class="medium text" value="${productSupplierInstance?.productCode}" />
                                </td>
                            </tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="ratingTypeCode"><warehouse:message code="productSupplier.ratingTypeCode.label" default="Rating Type Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'ratingTypeCode', 'errors')}">
									<g:select class="chzn-select-deselect" name="ratingTypeCode"
                                              from="${org.pih.warehouse.core.RatingTypeCode?.values()}"
                                              value="${productSupplierInstance?.ratingTypeCode}" noSelection="['': '']" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="preferenceTypeCode"><warehouse:message code="productSupplier.preferenceTypeCode.label" default="Preference Type Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'preferenceTypeCode', 'errors')}">
									<g:select class="chzn-select-deselect" name="preferenceTypeCode"
                                              from="${org.pih.warehouse.core.PreferenceTypeCode?.values()}"
                                              value="${productSupplierInstance?.preferenceTypeCode}" noSelection="['': '']" />
								</td>
							</tr>



						<tr class="prop">
								<td valign="top" class="name">
								  <label for="upc"><warehouse:message code="productSupplier.upc.label" default="Upc" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'upc', 'errors')}">
									<g:textField class="text" size="80" name="upc" maxlength="255" value="${productSupplierInstance?.upc}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="ndc"><warehouse:message code="productSupplier.ndc.label" default="Ndc" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'ndc', 'errors')}">
									<g:textField class="text" size="80" name="ndc" maxlength="255" value="${productSupplierInstance?.ndc}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="supplier"><warehouse:message code="productSupplier.supplier.label" default="Supplier" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'supplier', 'errors')}">
                                    <g:selectOrganization name="supplier"
                                                          value="${productSupplierInstance?.supplier?.id}"
                                                          noSelection="['':'']"
                                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_SUPPLIER]"
                                                          class="chzn-select-deselect"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="supplierCode"><warehouse:message code="productSupplier.supplierCode.label" default="Supplier Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'supplierCode', 'errors')}">
									<g:textField class="text" size="80" name="supplierCode" maxlength="255" value="${productSupplierInstance?.supplierCode}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="supplierName"><warehouse:message code="productSupplier.supplierName.label" default="Supplier Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'supplierName', 'errors')}">
									<g:textField class="text" size="80" name="supplierName" maxlength="255" value="${productSupplierInstance?.supplierName}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="modelNumber"><warehouse:message code="productSupplier.modelNumber.label" default="Model Number" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'modelNumber', 'errors')}">
									<g:textField class="text" size="80" name="modelNumber" maxlength="255" value="${productSupplierInstance?.modelNumber}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="brandName"><warehouse:message code="productSupplier.brandName.label" default="Brand Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'brandName', 'errors')}">
									<g:textField class="text" size="80" name="brandName" maxlength="255" value="${productSupplierInstance?.brandName}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="manufacturer"><warehouse:message code="productSupplier.manufacturer.label" default="Manufacturer" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'manufacturer', 'errors')}">
                                    <g:selectOrganization name="manufacturer"
                                                          noSelection="['':'']"
                                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                                          value="${productSupplierInstance?.manufacturer?.id}"
                                                          class="chzn-select-deselect"/>

								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="manufacturerCode"><warehouse:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'manufacturerCode', 'errors')}">
									<g:textField class="text" size="80" name="manufacturerCode" maxlength="255" value="${productSupplierInstance?.manufacturerCode}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="manufacturerName"><warehouse:message code="productSupplier.manufacturerName.label" default="Manufacturer Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'manufacturerName', 'errors')}">
									<g:textField class="text" size="80" name="manufacturerName" maxlength="255" value="${productSupplierInstance?.manufacturerName}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="standardLeadTimeDays"><warehouse:message code="productSupplier.standardLeadTimeDays.label" default="Standard Lead Time Days" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'standardLeadTimeDays', 'errors')}">
									<g:textField class="text" name="standardLeadTimeDays" value="${fieldValue(bean: productSupplierInstance, field: 'standardLeadTimeDays')}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="minOrderQuantity"><warehouse:message code="productSupplier.minOrderQuantity.label" default="Min Order Quantity" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'minOrderQuantity', 'errors')}">
									<g:textField class="text" name="minOrderQuantity" value="${fieldValue(bean: productSupplierInstance, field: 'minOrderQuantity')}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="comments"><warehouse:message code="productSupplier.comments.label" default="Comments" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'comments', 'errors')}">
									<g:textArea name="comments" value="${productSupplierInstance?.comments}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top"></td>
								<td valign="top left">
									<div class="buttons">
										<g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
										<g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
