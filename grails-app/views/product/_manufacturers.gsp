<g:form action="update" method="post">
    <g:hiddenField name="action" value="save"/>
    <g:hiddenField name="id" value="${productInstance?.id}" />
    <g:hiddenField name="version" value="${productInstance?.version}" />
    <g:hiddenField name="categoryId" value="${params?.category?.id }"/>

    <div class="box">

        <h2>
            <warehouse:message code="product.manufacturerDetails.label" default="Manufacturer details"/>
        </h2>

        <table>
            <tbody>
            <tr class="prop">
                <td class="name middle"><label for="brandName"><warehouse:message
                        code="product.brandName.label" /></label></td>
                <td
                        class="value ${hasErrors(bean: productInstance, field: 'brandName', 'errors')}">
                    <g:autoSuggestString id="brandName" name="brandName" size="50" class="text"
                                         jsonUrl="${request.contextPath}/json/autoSuggest"
                                         value="${productInstance?.brandName}"
                                         placeholder="e.g. Advil, Tylenol"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name middle"><label for="manufacturer"><warehouse:message
                        code="product.manufacturer.label" /></label></td>
                <td
                        class="value ${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
                    <g:autoSuggestString id="manufacturer" name="manufacturer" size="50" class="text"
                                         jsonUrl="${request.contextPath}/json/autoSuggest"
                                         value="${productInstance?.manufacturer}"
                                         placeholder="e.g. Pfizer, Beckton Dickson"/>

                </td>
            </tr>

            <tr class="prop">
                <td class="name middle"><label for="manufacturerCode"><warehouse:message
                        code="product.manufacturerCode.label"/></label></td>
                <td class="value ${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
                    <g:textField name="manufacturerCode" value="${productInstance?.manufacturerCode}" size="50" class="text"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name middle"><label for="manufacturerName"><warehouse:message
                        code="product.manufacturerName.label"/></label></td>
                <td class="value ${hasErrors(bean: productInstance, field: 'manufacturerName', 'errors')}">
                    <g:textField name="manufacturerName" value="${productInstance?.manufacturerName}" size="50" class="text"/>
                </td>
            </tr>


            <tr class="prop">
                <td class="name middle"><label for="modelNumber"><warehouse:message
                        code="product.modelNumber.label" /></label></td>
                <td
                        class="value ${hasErrors(bean: productInstance, field: 'modelNumber', 'errors')}">
                    <g:textField name="modelNumber" value="${productInstance?.modelNumber}" size="50" class="text"/>
                </td>
            </tr>

            <tr class="prop">
                <td class="name middle"><label for="vendor"><warehouse:message
                        code="product.vendor.label" /></label></td>
                <td
                        class="value ${hasErrors(bean: productInstance, field: 'vendor', 'errors')}">
                    <g:autoSuggestString id="vendor" name="vendor" size="50" class="text"
                                         jsonUrl="${request.contextPath}/json/autoSuggest"
                                         value="${productInstance?.vendor}"
                                         placeholder="e.g. IDA, IMRES, McKesson"/>

                </td>
            </tr>
            <tr class="prop">
                <td class="name middle"><label for="vendorCode"><warehouse:message
                        code="product.vendorCode.label"/></label></td>
                <td class="value ${hasErrors(bean: productInstance, field: 'vendorCode', 'errors')}">
                    <g:textField name="vendorCode" value="${productInstance?.vendorCode}" size="50" class="text"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name middle"><label for="vendorName"><warehouse:message
                        code="product.vendorName.label"/></label></td>
                <td class="value ${hasErrors(bean: productInstance, field: 'vendorName', 'errors')}">
                    <g:textField name="vendorName" value="${productInstance?.vendorName}" size="50" class="text"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name middle"><label for="pricePerUnit"><warehouse:message
                        code="product.pricePerUnit.label"/></label></td>
                <td class="value middle ${hasErrors(bean: productInstance, field: 'pricePerUnit', 'errors')}">
                    <g:textField name="pricePerUnit" placeholder="Price per unit (${grailsApplication.config.openboxes.locale.defaultCurrencyCode})"
                                 value="${g.formatNumber(number:productInstance?.pricePerUnit, format:'###,###,##0.####') }"
                                 class="text" size="50" />

                    <span class="fade">${grailsApplication.config.openboxes.locale.defaultCurrencyCode}</span>

                </td>
            </tr>
            </tbody>
            <tfoot>
                <tr>
                    <td></td>
                    <td>
                        <div>
                            <button type="submit" class="button icon approve">
                                ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                            </button>
                        </div>
                    </td>
                </tr>
            </tfoot>

        </table>
    </div>
</g:form>
