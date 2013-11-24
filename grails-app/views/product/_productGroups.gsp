<div id="productGroups">
    <table id="productGroupTable" class="zebra">
        <thead>
            <tr>
                <th><warehouse:message code="productGroup.type.label" default="Type"/></th>
                <th><warehouse:message code="productGroup.description.label" default="Description"/></th>
                <th><warehouse:message code="productGroup.products.label" default="Products"/></th>
                <th><warehouse:message code="default.actions.label" default="Actions"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each var="productGroup" in="${productGroups}">
                <tr>
                    <td class="middle">
                        Substitutable
                    </td>
                    <td class="middle">
                        ${productGroup.description}
                    </td>
                    <td class="middle">
                        <ul>
                        <g:each in="${productGroup.products}" var="product">
                            <li><g:link controller="product" action="edit" id="${product.id}" fragment="tabs-2">${product.name}</g:link></li>
                        </g:each>
                        </ul>
                    </td>
                    <td class="middle">
                        <g:link controller="productGroup" action="edit" id="${productGroup.id}" class="button icon edit">
                            <warehouse:message code="default.button.edit.label"/>
                        </g:link>
                        <g:remoteLink controller="product" action="deleteProductGroup" update="productGroups" class="button icon trash"
                                      id="${productGroup.id}" params="[productId:product.id]">
                            <warehouse:message code="default.button.delete.label"/>
                        </g:remoteLink>
                    </td>
                </tr>
            </g:each>
            <g:unless test="${productGroups}">
                <tr>
                    <td class="center empty" colspan="4">
                        <warehouse:message code="default.none.label"/>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="4">
                    <g:formRemote id="addProductGroupToProduct" name="addProductGroupToProduct"
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
<script>
    function onSuccess() {
        $("#productGroup").focus();
    }

    function onComplete() {
        $("#productGroup").focus();
    }

</script>