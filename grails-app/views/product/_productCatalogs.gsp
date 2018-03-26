<div class="box" id="productCatalogs">
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
                <warehouse:message code="default.description.label" />
            </th>
            <th>
                <warehouse:message code="default.actions.label" default="Actions"/>
            </th>
        </tr>
        </thead>
        <tbody>
        <g:each var="productCatalog" in="${productCatalogs }" status="status">
            <tr class="${status%2?'even':'odd'}">
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
                    ${productCatalog?.description }
                </td>
            </tr>
        </g:each>
        <g:unless test="${productCatalogs }">
            <tr>
                <td colspan="6">
                    <div class="padded fade center">
                        <warehouse:message code="default.empty.label"/>
                    </div>
                </td>
            </tr>
        </g:unless>
        </tbody>

        <tfoot>
            <tr>
                <td colspan="6" class="center">


                    <g:formRemote name="addProductCatalog"
                                  update="productCatalogs" onSuccess="onSuccess(data,textStatus)" onComplete="onComplete()"
                                  url="[controller: 'product', action:'addProductCatalog']">

                        <g:hiddenField name="product.id" value="${product?.id}"/>

                        <g:select name="productCatalog.id" from="${org.pih.warehouse.product.ProductCatalog.list()}"
                                  optionKey="id" optionValue="name"/>

                        <button class="button icon add">${warehouse.message(code:'default.button.add.label')}</button>
                    </g:formRemote>
                </td>
            </tr>
        </tfoot>
    </table>

</div>