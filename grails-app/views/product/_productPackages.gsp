<div class="box">
    <h2>
        <warehouse:message code="product.packages.label" default="Product packages"/>
    </h2>
    <table>
        <thead>
        <tr>
            <th>
                <warehouse:message code="package.name.label"/>
            </th>
            <th>
                <warehouse:message code="package.uom.label"/>
            </th>
            <th>
                <warehouse:message code="package.description.label"/>
            </th>
            <th>
                <warehouse:message code="productSupplier.label"/>
            </th>
            <th>
                <warehouse:message code="package.gtin.label"/>
            </th>
            <th>
                <warehouse:message code="package.price.label"/>
            </th>
            <th>

            </th>
        </tr>
        </thead>
        <tbody>
        <g:each var="pkg" in="${productInstance.packages }" status="status">
            <tr class="${status%2?'even':'odd'}">
                <td>
                    ${pkg?.name }
                </td>
                <td>
                    ${pkg?.uom?.name }
                </td>
                <td>
                    ${pkg?.uom?.code }/${pkg?.quantity }
                </td>
                <td>
                    <g:if test="${pkg.productSupplier}">
                        ${pkg?.productSupplier?.code}
                    </g:if>
                    <g:else>
                        ${warehouse.message(code:'default.none.label') }
                    </g:else>
                </td>
                <td>
                    ${pkg?.gtin?:warehouse.message(code:'default.none.label') }
                </td>
                <td>
                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: ['0.00'])}">
                        <g:formatNumber number="${pkg?.price}" />
                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                    </g:hasRoleFinance>
                </td>
                <td class="right">
                    <a href="javascript:void(0);" dialog-id="editProductPackage-${pkg?.id }" class="open-dialog button">
                        <img src="${createLinkTo(dir:'images/icons/silk', file:'pencil.png')}" />
                        <warehouse:message code="default.button.edit.label" />
                    </a>
                    <g:link controller="product" action="removePackage" id="${pkg.id }"
                            params="['product.id':productInstance.id]" class="button">
                        <img src="${createLinkTo(dir:'images/icons/silk', file:'delete.png')}" />
                        <warehouse:message code="default.button.delete.label" />
                    </g:link>
                </td>

            </tr>
        </g:each>
        <g:unless test="${productInstance?.packages }">
            <tr>
                <td colspan="7">
                    <div class="empty center">
                        <warehouse:message code="package.packageNotFound.message"/>
                    </div>
                </td>
            </tr>
        </g:unless>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="7">
                    <div class="right">
                        <a href="javascript:void(0);" class="open-dialog create button" dialog-id="uom-dialog">
                            <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png')}" />&nbsp;
                            <warehouse:message code="default.add.label" args="[g.message(code:'unitOfMeasure.label')]"/>
                        </a>
                        <a href="javascript:void(0);" class="open-dialog create button" dialog-id="uom-class-dialog">
                            <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png')}" />
                            <warehouse:message code="default.add.label" args="[g.message(code:'unitOfMeasureClass.label')]"/>
                        </a>
                    </div>
                    <div class="center">
                        <a href="javascript:void(0);" class="open-dialog create button"
                           dialog-id="product-package-dialog">
                            <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png')}" />&nbsp;
                            <warehouse:message code="default.create.label" args="[g.message(code:'productPackage.label')]"/>
                        </a>
                    </div>
                </td>
            </tr>
        </tfoot>
    </table>
</div>
