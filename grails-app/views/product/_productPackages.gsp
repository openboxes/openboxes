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
                <warehouse:message code="package.price.label"/>
            </th>
            <th>
                <warehouse:message code="package.gtin.label"/>
            </th>
            <th>
                <warehouse:message code="default.actions.label"/>
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
                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: ['0.00'])}">
                        <g:formatNumber number="${pkg?.price}" />
                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                    </g:hasRoleFinance>
                </td>
                <td>
                    ${pkg?.gtin?:warehouse.message(code:'default.none.label') }
                </td>
                <td>
                    <a href="javascript:void(0);" dialog-id="editProductPackage-${pkg?.id }" class="open-dialog button icon edit">
                        <warehouse:message code="default.button.edit.label" />
                    </a>
                    <g:link controller="product" action="removePackage" id="${pkg.id }" params="['product.id':productInstance.id]" class="button icon trash">
                        <warehouse:message code="default.button.delete.label" />
                    </g:link>
                </td>

            </tr>
        </g:each>
        <g:unless test="${productInstance?.packages }">
            <tr>
                <td colspan="6">
                    <div class="empty center">
                        <warehouse:message code="package.packageNotFound.message"/>
                    </div>
                </td>
            </tr>
        </g:unless>

        </tbody>
        <tfoot>
            <tr>
                <td colspan="6">
                    <div class="right">
                        <a href="javascript:void(0);" class="open-dialog create button" dialog-id="uom-dialog">Add UoM</a>
                        <a href="javascript:void(0);" class="open-dialog create button" dialog-id="uom-class-dialog">Add UoM Class</a>
                    </div>
                    <div class="center">
                        <a href="javascript:void(0);" class="open-dialog create button primary icon add" dialog-id="product-package-dialog"><warehouse:message code="package.add.label"/></a>
                    </div>
                </td>
            </tr>

        </tfoot>
    </table>

</div>