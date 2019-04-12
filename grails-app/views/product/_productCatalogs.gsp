<div id="productCatalogs">

    <div class="box dialog">
        <h2>
            <warehouse:message code="product.catalogs.label" default="Product Catalogs"/>
        </h2>
        <table>
            <thead>
            <tr>
                <th>
                    <warehouse:message code="default.code.label"/>
                </th>
                <th>
                    <warehouse:message code="default.name.label"/>
                </th>
                <th>
                    <warehouse:message code="default.actions.label" default="Actions"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <g:each var="productCatalog" in="${productCatalogs }" status="status">
                <tr class="prop ${status%2?'even':'odd'}">
                    <td>
                        <g:link controller="productCatalog" action="edit" id="${productCatalog.id}">
                            ${productCatalog?.code }
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="productCatalog" action="edit" id="${productCatalog.id}">
                            ${productCatalog?.name }
                        </g:link>
                    </td>
                    <td>
                        <g:remoteLink action="removeFromProductCatalog" id="${product.id}" params="['productCatalog.id':productCatalog.id]"
                                      class="button" update="productCatalogs">
                            <g:message code="default.button.delete.label"/>
                        </g:remoteLink>
                    </td>
                </tr>
            </g:each>
            <g:unless test="${productCatalogs }">
                <tr>
                    <td colspan="3">
                        <div class="padded fade center">
                            <warehouse:message code="default.empty.label"/>
                        </div>
                    </td>
                </tr>
            </g:unless>
            </tbody>

            <tfoot>
                <tr>
                    <td colspan="3" class="center">
                    </td>
                </tr>
            </tfoot>
        </table>

        <div>
            <g:formRemote name="addToProductCatalog"
                          update="productCatalogs" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()"
                          url="[controller: 'product', action:'addToProductCatalog']">
                <g:hiddenField name="product.id" value="${product?.id}"/>
                <table>
                    <tr>
                        <td>
                            <g:select name="productCatalog.id"
                                      noSelection="['':'']" placeholder="Select a product catalog"
                                      from="${org.pih.warehouse.product.ProductCatalog.list()}"
                                      optionKey="id" optionValue="name" class="chzn-select-deselect"/>

                        </td>
                        <td>
                            <button class="button">${warehouse.message(code:'default.button.add.label')}</button>
                        </td>
                    </tr>
                </table>

            </g:formRemote>

        </div>
    </div>

</div>
