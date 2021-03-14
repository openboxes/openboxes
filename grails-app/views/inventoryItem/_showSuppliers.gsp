<%@ page import="org.pih.warehouse.core.ValidationCode" %>
<html>
<head>
    <style>
        .show-all {
            right: 5px;
            position: absolute;
            top: 8px;
        }
    </style>
    <script>
        $(document).ready(function() {
            $('.show-all').on('click', function () {
                var showAllElement = $('#show-all');
                var showAllNewValue = showAllElement.val() === "true" ? "false" : "true";
                if (showAllNewValue === "true") {
                  $(".preference-hide").removeClass("hidden");
                } else {
                  $(".preference-hide").addClass("hidden");
                }
                showAllElement.val(showAllNewValue).trigger("change");
            });
        });
    </script>
</head>
</html>
<div class="box">
    <h2>
        <div>
            <warehouse:message code="product.sources.label" default="Product Sources"/>
            <div class="show-all button">
                ${warehouse.message(code: 'default.showAll.label')}
            </div>
        </div>
    </h2>

    <div class="dialog">
        <table>
            <thead>
            <g:sortableColumn property="code" title="${warehouse.message(code: 'productSupplier.code.label', default: 'Code')}" />

            <th><g:message code="default.name.label" default="Name" /></th>

            <th><g:message code="productSupplier.supplier.label" default="Supplier" /></th>

            <th><g:message code="productSupplier.supplierCode.label" default="Supplier Code" /></th>

            <th><g:message code="productSupplier.manufacturer.label" default="Manufacturer" /></th>

            <th><g:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></th>

            <th><g:message code="productSupplier.preferenceTypeCode.label" default="Product Supplier Preference" /></th>

            <th><g:message code="productSupplier.minOrderQuantity.label" default="Minimum Order Quantity" /></th>

            <th><g:message code="package.packSize.label" default="Pack Size" /></th>

            <th><g:message code="package.lastPrice.label" default="Last Price" /></th>

            <th><g:message code="productSupplier.contractPrice.label" default="Contract price (each) " /></th>

            <th><g:message code="productSupplier.contractValidUntil.label" default="Contract Valid Until" /></th>

            </thead>
            <tbody>
            <g:if test="${productInstance?.productSuppliers}">
                <g:hiddenField id="show-all" name="show-all" value="false" />

                <g:each var="productSupplier" in="${productInstance?.productSuppliers.sort()}" status="status">
                    <g:set var="defaultProductPackage" value="${productSupplier.defaultProductPackage}"/>
                    <g:set var="defaultPreference" value="${productSupplier.productSupplierPreferences.find { it.destinationParty == currentLocation.organization }}"/>
                    <g:set var="globalPreference" value="${productSupplier.productSupplierPreferences.find { !it.destinationParty }}"/>
                    <tr class="prop ${status%2==0?'odd':'even'}
                        ${defaultPreference?.preferenceType?.validationCode == org.pih.warehouse.core.ValidationCode.HIDE ||
                            (defaultPreference?.preferenceType?.validationCode != org.pih.warehouse.core.ValidationCode.HIDE &&
                                globalPreference?.preferenceType?.validationCode == org.pih.warehouse.core.ValidationCode.HIDE) ? 'preference-hide hidden' : '' }">

                        <td>${fieldValue(bean: productSupplier, field: "code")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "name")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "supplier")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "supplierCode")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "manufacturer")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "manufacturerCode")}</td>

                        <td>
                            <g:if test="${defaultPreference}">
                                ${fieldValue(bean: defaultPreference?.preferenceType, field: "name")}
                            </g:if>
                            <g:elseif test="${globalPreference}">
                                ${fieldValue(bean: globalPreference?.preferenceType, field: "name")}
                            </g:elseif>
                        </td>

                        <td>${fieldValue(bean: productSupplier, field: "minOrderQuantity")}</td>

                        <td>
                            <g:if test="${defaultProductPackage}">
                                ${fieldValue(bean: defaultProductPackage?.uom, field: "code")}/${fieldValue(bean: defaultProductPackage, field: "quantity")}
                            </g:if>
                        </td>

                        <td>
                            <g:if test="${defaultProductPackage}">
                                <g:hasRoleFinance>
                                    <g:formatNumber number="${defaultProductPackage?.productPrice?.price}" />
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </g:if>
                        </td>

                        <td>
                            <g:formatNumber number="${productSupplier?.contractPrice?.price}"/>
                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                        </td>

                        <td>
                            <g:prettyDateFormat date="${productSupplier?.contractPrice?.toDate}"/>
                        </td>

                    </tr>
                </g:each>
            </g:if>
            <g:unless test="${productInstance?.productSuppliers}">
                <tr class="prop">
                    <td class="empty center" colspan="10">
                        <g:message code="productSuppliers.empty.label" default="There are no product suppliers"/>
                    </td>
                </tr>
            </g:unless>
            </tbody>
        </table>
    </div>
</div>
