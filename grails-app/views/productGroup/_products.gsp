<div id="products">
    <table id="productTable" class="zebra">
        <thead>
        <tr class="">
            <th><warehouse:message code="product.productCode.label"/></th>
            <th><warehouse:message code="product.label"/></th>
            <th><warehouse:message code="product.unitOfMeasure.label"/></th>
            <th><warehouse:message code="product.manufacturer.label"/></th>
            <th><warehouse:message code="product.vendor.label"/></th>
            <th><warehouse:message code="default.actions.label"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each var="product" in="${products}" status="i">
            <tr class="prop ${i%2?'even':'odd' }">
                <td class="middle">
                    ${product.productCode }
                </td>
                <td class="middle">
                    ${product.name }
                </td>
                <td class="middle">
                    ${product.unitOfMeasure }
                </td>
                <td class="middle">
                    ${product.manufacturer }
                    <div class="fade">${product?.manufacturerCode }</div>
                </td>
                <td class="middle">
                    ${product.vendor }
                    <div class="fade">${product?.vendorCode }</div>
                </td>
                <td class="middle">
                    <g:link controller="product" action="edit" id="${product.id}" class="button icon edit">
                        <warehouse:message code="default.button.edit.label"/>
                    </g:link>
                    <g:remoteLink controller="productGroup" action="deleteProductFromProductGroup" update="products" class="button icon trash"
                                  id="${productGroup.id}" params="['product.id':product.id]">
                        <warehouse:message code="default.button.delete.label"/>
                    </g:remoteLink>
                </td>
            </tr>
        </g:each>
        <g:unless test="${products}">
            <tr>
                <td class="center empty" colspan="8">
                    <warehouse:message code="default.none.label"/>
                </td>
            </tr>
        </g:unless>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="8">
                    <g:formRemote id="addProductToProductGroup" name="addProductToProductGroup"
                                  update="products" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()"
                                  url="[controller: 'productGroup', action:'addProductToProductGroup']">
                        <input name="id" type="hidden" value="${productGroup?.id}" />
                        <%--
                        <input id="product" type="text" name="product" value="" size="80" class="medium text"/>
                        --%>
                        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName" width="500" styleClass="text"/>
                        <button  class="button icon add">${warehouse.message(code:'default.button.add.label')}</button>
                    </g:formRemote>
                </td>
            </tr>
        </tfoot>
    </table>
</div>
<script>
    function onSuccess() {
        $("#product").focus();
    }

    function onComplete() {
        $("#product").focus();
    }

</script>