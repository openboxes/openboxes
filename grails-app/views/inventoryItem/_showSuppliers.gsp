<div class="box">
    <h2>
        <warehouse:message code="product.sources.label" default="Product Sources"/>
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

            <th><g:message code="productSupplier.minOrderQuantity.label" default="Minimum Order Quantity" /></th>

            <th><g:message code="package.packSize.label" default="Pack Size" /></th>

            <th><g:message code="package.lastPrice.label" default="Last Price" /></th>

            <th><g:message code="productSupplier.contractPrice.label" default="Contract price (each) " /></th>

            <th><g:message code="productSupplier.contractValidUntil.label" default="Contract Valid Until" /></th>

            </thead>
            <tbody>
            <g:if test="${productInstance?.productSuppliers}">

                <g:each var="productSupplier" in="${productInstance?.productSuppliers.sort()}" status="status">
                    <g:set var="defaultProductPackage" value="${productSupplier.defaultProductPackage}"/>
                    <tr class="prop ${status%2==0?'odd':'even'}">

                        <td>${fieldValue(bean: productSupplier, field: "code")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "name")?:g.message(code:'default.none.label')}</td>

                        <td>${fieldValue(bean: productSupplier, field: "supplier")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "supplierCode")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "manufacturer")}</td>

                        <td>${fieldValue(bean: productSupplier, field: "manufacturerCode")}</td>

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
