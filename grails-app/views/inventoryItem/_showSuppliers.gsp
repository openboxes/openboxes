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

            <th><g:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></th>

            <th><g:message code="productSupplier.unitPrice.label" default="Unit Price" /></th>

            </thead>
            <tbody>
            <g:if test="${productInstance?.productSuppliers}">

                <g:each var="productSupplier" in="${productInstance?.productSuppliers.sort()}" status="status">
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

                        <td>${fieldValue(bean: productSupplier, field: "unitOfMeasure")}</td>

                        <td>
                            <g:hasRoleFinance>
                                ${fieldValue(bean: productSupplier, field: "unitPrice")}
                            </g:hasRoleFinance>
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