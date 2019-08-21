<div id="productGroups">
    <table id="productGroupTable" class="zebra">
        <thead>
            <tr>
                <th><warehouse:message code="productGroup.label" default="Description"/></th>
                <th><warehouse:message code="productGroup.products.label" default="Products"/></th>
                <th><warehouse:message code="default.actions.label" default="Actions"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each var="productGroup" in="${productGroups}">
                <tr>
                    <td class="top">
                        <g:link controller="productGroup" action="show" id="${productGroup.id}">
                            ${productGroup.name}
                        </g:link>
                    </td>
                    <td class="middle">
                        <table>
                            <g:each in="${productGroup.products}" var="product">
                                <tr>
                                    <td><g:link controller="product" action="edit" id="${product.id}" fragment="tabs-2">${product.productCode} &rsaquo; ${product.name}</g:link></td>
                                </tr>
                            </g:each>
                        </table>


                    </td>
                    <td class="middle">
                        <g:link controller="productGroup" action="show" id="${productGroup.id}" class="button icon search">
                            <warehouse:message code="default.button.show.label" default="Show"/>
                        </g:link>
                        <g:link controller="productGroup" action="edit" id="${productGroup.id}" class="button icon edit">
                            <warehouse:message code="default.button.edit.label"/>
                        </g:link>
                        <g:remoteLink controller="product" action="removeFromProductGroups" update="productGroups" class="button icon trash"
                                      id="${productGroup.id}" params="[productId:product.id]">
                            <warehouse:message code="default.button.unlink.label" default="Unlink"/>
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
                        <g:autoSuggestString id="productGroup" name="productGroup" size="80" class="medium text"
                                             jsonUrl="${request.contextPath}/json/autoSuggestProductGroups"
                                             value=""
                                             placeholder="${warehouse.message(code:'product.addProductGroup.label', default: 'Enter product group name')}"/>

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