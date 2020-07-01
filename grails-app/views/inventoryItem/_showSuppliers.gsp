<div class="box">
    <h2>
        <warehouse:message code="product.sources.label" default="Product Sources"/>
    </h2>

    <div class="dialog">
        <table>
            <thead>
            <g:sortableColumn property="code" title="${warehouse.message(code: 'productSupplier.code.label', default: 'Code')}" />

            <th><g:message code="productSupplier.productCode.label" /></th>

            <th><g:message code="default.name.label" default="Name" /></th>

            <th><g:message code="productSupplier.supplier.label" default="Supplier" /></th>

            <th><g:message code="productSupplier.supplierCode.label" default="Supplier Code" /></th>

            <th><g:message code="productSupplier.manufacturer.label" default="Manufacturer" /></th>

            <th><g:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></th>

            <th><g:message code="productSupplier.preferenceTypeCode.label" default="Preference Type" /></th>

            <th><g:message code="productSupplier.ratingTypeCode.label" default="Rating Type" /></th>

            <th><g:message code="unitOfMeasure.label" default="Unit of Measure" /></th>

            <th><g:message code="productPackage.price.label" default="Price" /></th>

            </thead>
            <tbody>
            <g:if test="${productInstance?.productSuppliers}">

                <g:each var="productSupplier" in="${productInstance?.productSuppliers.sort()}" status="status">
                    <g:set var="defaultProductPackage" value="${productSupplier.defaultProductPackage}"/>
                    <tr class="prop ${status%2==0?'odd':'even'}">

                        <td>${fieldValue(bean: productSupplier, field: "code")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "productCode")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "name")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "supplier")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "supplierCode")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "manufacturer")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "manufacturerCode")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "preferenceTypeCode")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "ratingTypeCode")}</td>

                        <td>
                            <g:if test="${defaultProductPackage}">
                                ${fieldValue(bean: defaultProductPackage?.uom, field: "code")}/${fieldValue(bean: defaultProductPackage, field: "quantity")}
                            </g:if>
                        </td>

                        <td>
                            <g:if test="${defaultProductPackage}">
                                <g:hasRoleFinance>
                                    ${defaultProductPackage?.price}
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </g:if>
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
