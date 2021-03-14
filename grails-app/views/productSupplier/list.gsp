
<%@ page import="org.pih.warehouse.product.ProductSupplier" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
				<div class="button-bar">
                    <g:link class="button" action="list">
                        <img src="${createLinkTo(dir:'images/icons/silk', file:'application_view_list.png')}" />
                        <warehouse:message code="default.list.label" args="[g.message(code:'productSupplier.label')]"/>
                    </g:link>
                    <g:link class="button" action="create">
                        <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png')}" />
                        <warehouse:message code="default.add.label" args="[g.message(code:'productSupplier.label')]"/>
                    </g:link>
                    <g:link class="button" action="export" params="[format: 'xls']">
                        <img src="${createLinkTo(dir:'images/icons/silk', file:'page_excel.png')}" />
                        <warehouse:message code="default.export.label" args="[g.message(code:'productSuppliers.label')]"/>
                    </g:link>
                    <g:link class="button" controller="batch" action="downloadExcel" params="[type:'ProductSupplierPreference']">
                        <img src="${createLinkTo(dir:'images/icons/silk', file:'page_excel.png')}" />
                        <warehouse:message code="default.export.label" args="[g.message(code:'productSupplier.productSourcePreference.label')]"/>
                    </g:link>
	        	</div>
                <div class="box">
                    <h2><warehouse:message code="default.filters.label"/></h2>
                    <g:form action="list" method="GET">
                        <g:hiddenField name="max" value="${params.max?:10}"/>
                        <g:hiddenField name="offset" value="${0}"/>
                        <g:hiddenField name="sourceType" value="${params?.sourceType}" />
                        <div class="filter-list">
                            <table>
                                <tr>
                                    <td width="25%">
                                        <label><warehouse:message code="default.search.label"/></label>
                                        <p>
                                            <g:textField name="q" style="width:100%" class="text" value="${params.q}" placeholder="Search by product code, supplier code, etc"/>
                                        </p>
                                    </td>
                                    <td width="25%">
                                        <label><warehouse:message code="product.label"/></label>
                                        <p>
                                            <g:autoSuggest id="product" name="product" styleClass="text"
                                                           jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true"
                                                           valueId="${params?.product?.id}"
                                                           valueName="${params?.product?.value}"/>
                                        </p>
                                    </td>
                                    <td width="25%">
                                        <label><warehouse:message code="productSupplier.supplier.label"/></label>
                                        <p>
                                            <g:selectOrganization name="supplierId"
                                                                  noSelection="['':'']"
                                                                  roleTypes="[org.pih.warehouse.core.RoleType.ROLE_SUPPLIER]"
                                                                  value="${params?.supplierId}"
                                                                  class="chzn-select-deselect"/>
                                        </p>
                                    </td>
                                    <td width="25%">
                                        <label><warehouse:message code="productSupplier.manufacturer.label"/></label>
                                        <p>
                                            <g:selectOrganization name="manufacturerId"
                                                                  noSelection="['':'']"
                                                                  roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                                                  value="${params?.manufacturerId}"
                                                                  class="chzn-select-deselect"/>
                                        </p>
                                    </td>
                                    <td>
                                        <button name="search" class="button">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />&nbsp;
                                            ${warehouse.message(code:'default.search.label')}
                                        </button>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </g:form>

                </div>
                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table class="dataTable">
                        <thead>
                            <tr>
                                <th><g:message code="product.code.label" default="Product Code" /></th>
                                <th><g:message code="productSupplier.sourceCode.label" default="Source Code" /></th>
                                <th><g:message code="product.label" default="Product" /></th>
                                <th><g:message code="productSupplier.sourceName.label" default="Source Name" /></th>
                                <th><g:message code="productSupplier.supplier.label" default="Supplier" /></th>
                                <th><g:message code="product.supplierCode.label" default="Supplier Code" /></th>
                                <th><g:message code="productSupplier.manufacturer.label" default="Manufacturer" /></th>
                                <th><g:message code="product.manufacturerCode.label" default="Manufacturer Code" /></th>
                                <th><g:message code="preferenceType.label" default="Preference Type" /></th>
                                <th><g:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></th>
                                <th><g:message code="productSupplier.unitPrice.label" default="Unit Price" /></th>
                                <th><g:message code="default.actions.label" default="Actions" /></th>
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${productSupplierInstanceList}" status="i" var="productSupplierInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td>
                                    ${fieldValue(bean: productSupplierInstance, field: "product.productCode")}
                                </td>
                                <td>${fieldValue(bean: productSupplierInstance, field: "code")?:g.message(code:'default.none.label')}</td>
                                <td>
                                    ${fieldValue(bean: productSupplierInstance, field: "product")}
                                </td>
                                <td>${fieldValue(bean: productSupplierInstance, field: "name")?:g.message(code:'default.none.label')}</td>
                                <td>${fieldValue(bean: productSupplierInstance, field: "supplier")}</td>
                                <td>${fieldValue(bean: productSupplierInstance, field: "supplierCode")}</td>
                                <td>${fieldValue(bean: productSupplierInstance, field: "manufacturer")}</td>
                                <td>${fieldValue(bean: productSupplierInstance, field: "manufacturerCode")}</td>
                                <td>
                                    <g:if test="${productSupplierInstance?.productSupplierPreferences?.size() > 1}">
                                        <g:each in="${productSupplierInstance?.productSupplierPreferences}" var="productSupplierPreference">
                                            <g:set var="title" value="${title ?
                                                    title + (productSupplierPreference?.destinationParty?.name ? productSupplierPreference?.destinationParty?.name : 'NONE') + ' - ' + productSupplierPreference?.preferenceType?.name + '\r\n' :
                                                    (productSupplierPreference?.destinationParty?.name ? productSupplierPreference?.destinationParty?.name : 'NONE') + ' - ' + productSupplierPreference?.preferenceType?.name + '\r\n'}" />
                                        </g:each>
                                        <div title="${title}">
                                            <g:message code="default.multiple.label" default="Multiple" />
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <g:each in="${productSupplierInstance?.productSupplierPreferences}" var="productSupplierPreference">
                                           ${productSupplierPreference.preferenceType?.name}
                                        </g:each>
                                    </g:else>
                                </td>
                                <td>
                                    <g:if test="${productSupplierInstance?.defaultProductPackage}">
                                        ${fieldValue(bean: productSupplierInstance?.defaultProductPackage?.uom, field: "code")}/${fieldValue(bean: productSupplierInstance?.defaultProductPackage, field: "quantity")}
                                    </g:if>
                                </td>
                                <td>
                                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'default.none.label')])}">
                                        ${g.formatNumber(number: productSupplierInstance?.defaultProductPackage?.productPrice?.price?:0.0)}
                                    </g:hasRoleFinance>
                                </td>
                                <td>
                                    <div class="button-group">
                                        <g:link action="edit" controller="productSupplier" id="${productSupplierInstance?.id}" class="button">
                                            <img src="${createLinkTo(dir:'images/icons/silk', file:'pencil.png')}" />
                                            &nbsp;${g.message(code: "default.button.edit.label")}
                                        </g:link>
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${productSupplierInstanceList}">
                        <div class="center empty fade">
                            <g:message code="default.none.label"/>
                        </div>
                    </g:unless>
                    <div class="paginateButtons">
                        <g:set var="pageParams" value="${['supplierId': params?.supplierId, 'product.id': params?.product?.id,
                                                          'product.value': params?.product?.value,'manufacturerId': params?.manufacturerId, q: params.q]}"/>
                        <g:paginate total="${productSupplierInstanceTotal}" params="${pageParams.findAll {it.value}}"/>
                    </div>
                </div>
            </div>
        </div>
    <script>
      $(document).ready(function() {
        $(".dataTable").dataTable({
          "bProcessing": false,
          "bScrollCollapse": true,
          "bScrollInfinite": true,
          "bJQueryUI": true,
          "bInfo": false,
        });
      });
    </script>
    </body>
</html>
