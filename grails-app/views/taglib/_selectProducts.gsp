
<g:set var="selectedProducts" value="${attrs.value }"/>
<div class="box">
    <h2>Selected (${selectedProducts.size() })</h2>
    <table id="selectedProducts" class="products">
        <thead>
            <tr class="">
                <th class="middle center"><input type="checkbox" class="checkAll" > </th>
                <th><warehouse:message code="product.label"/></th>
                <th><warehouse:message code="product.unitOfMeasure.label"/></th>
                <th><warehouse:message code="product.manufacturer.label"/></th>
                <th><warehouse:message code="product.manufacturerCode.label"/></th>
                <th><warehouse:message code="product.vendor.label"/></th>
                <th><warehouse:message code="product.vendorCode.label"/></th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${selectedProducts}">
                <tr>
                    <td class="center empty" colspan="7">
                        <warehouse:message code="default.none.label"/>

                    </td>
                </tr>

            </g:unless>
            <g:each var="product" in="${selectedProducts }" status="i">
                <tr class="prop ${i%2?'even':'odd' }">
                    <td width="1%" style="padding: 0; margin: 0;" class="middle center">
                        <g:hiddenField name="${attrs.name }" value="${product.id }"></g:hiddenField>
                        <g:checkBox name="delete-product.id" value="${product?.id }" class="selectedProduct" checked="false"/>
                    </td>
                    <td class="middle">
                        ${product.name }
                    </td>
                    <td class="middle">
                        ${product.unitOfMeasure }
                    </td>
                    <td class="middle">
                         ${product.manufacturer }
                    </td>
                    <td class="middle">
                        <g:if test="${product?.manufacturerCode }">
                            <span class="fade">#${product?.manufacturerCode }</span>
                        </g:if>
                    </td>
                    <td class="middle">
                        ${product.vendor }
                    </td>
                    <td class="middle">
                        <g:if test="${product?.vendorCode }">
                            <span class="fade">#${product?.vendorCode }</span>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="7">
                    <g:formRemote id="addToProductProductGroup" name="addToProductProductGroup"
                                  update="productGroups" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()"
                                  url="[controller: 'product', action:'addProductGroupToProduct']">
                        <input name="id" type="hidden" value="${product?.id}" />
                        <input id="productGroup" type="text" name="productGroup" value="" size="80" class="medium text"/>
                        <button  class="button icon add">${warehouse.message(code:'default.button.add.label')}</button>
                    </g:formRemote>

                </td>
            </tr>

        </tfoot>
    </table>


</div>
