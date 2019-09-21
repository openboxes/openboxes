<div id="productCatalogItems" class="dialog">
    <table class="dataTable">
        <thead>
        <tr>
            <th><warehouse:message code="product.productCode.label"/></th>
            <th><warehouse:message code="product.name.label"/></th>
            <th><warehouse:message code="category.label"/></th>
            <th><warehouse:message code="default.actions.label"/></th>
        </tr>
        </thead>
        <tbody>
        <g:set var="productCatalogItems" value="${productCatalogInstance?.productCatalogItems.sort {it.product.name }}"/>
        <g:each var="productCatalogItem" in="${productCatalogItems}" status="status">
            <tr>
                <td>
                    ${productCatalogItem?.product?.productCode}
                </td>
                <td>
                    <g:link controller="inventoryItem" action="showStockCard" id="${productCatalogItem?.product?.id}">
                        ${productCatalogItem.product.name}
                    </g:link>
                </td>
                <td>
                    ${productCatalogItem.product.category.name}
                </td>
                <td>
                    <g:remoteLink action="removeProductCatalogItem" id="${productCatalogItem.id}" class="button" update="productCatalogItems" >
                        <warehouse:message code="default.button.delete.label"/>
                    </g:remoteLink>

                </td>
            </tr>

        </g:each>
        <g:unless test="${productCatalogInstance?.productCatalogItems}">
            <tr>
                <td colspan="4">
                    <p class="empty fade center"><g:message code="default.empty.label"/></p>
                </td>
            </tr>
        </g:unless>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="4" class="center">
                    <g:formRemote name="addProductCatalogItem" url="[controller: 'productCatalog', action:'addProductCatalogItem']"
                                  update="productCatalogItems" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()">

                        <g:hiddenField name="productCatalog.id" value="${productCatalogInstance.id}"/>

                        <g:autoSuggest id="product" name="product" size="80" class="medium text"
                                       placeholder="${warehouse.message(code:'product.label', default: 'Search for a product to add as a component')}"
                                       jsonUrl="${request.contextPath }/json/findProductByName" width="500" styleClass="text"/>

                        <button class="button">${warehouse.message(code:'default.button.add.label')}</button>

                    </g:formRemote>
                </td>
            </tr>
        </tfoot>
    </table>
</div>
