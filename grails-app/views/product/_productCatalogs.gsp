<div id="productCatalogs">
    <div class="box dialog">
        <h2>
            <warehouse:message code="product.catalogs.label" default="Product Catalogs"/>
        </h2>
        <g:formRemote name="addToProductCatalog"
                          update="productCatalogs" onSuccess="onSuccess()" onComplete="onComplete()"
                          url="[controller: 'product', action:'addToProductCatalog']">
            <g:hiddenField name="product.id" value="${productInstance?.id}"/>
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
                <g:each var="productCatalog" in="${productInstance.productCatalogs }" status="status">
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
                            <g:remoteLink action="removeFromProductCatalog" id="${productInstance.id}" params="['productCatalog.id':productCatalog.id]"
                                          class="button" update="productCatalogs">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="View" />
                                <g:message code="default.button.delete.label"/>
                            </g:remoteLink>
                        </td>
                    </tr>
                </g:each>
                <g:unless test="${productInstance.productCatalogs }">
                    <tr>
                        <td colspan="3">
                            <div class="padded fade center">

                            </div>
                        </td>
                    </tr>
                </g:unless>
                </tbody>

                <tfoot>
                    <tr>
                        <td colspan="2">
                            <g:select name="productCatalog.id"
                                      noSelection="['':'']" placeholder="Select a product catalog"
                                      from="${org.pih.warehouse.product.ProductCatalog.list()}"
                                      optionKey="id" optionValue="name" class="chzn-select-deselect"/>
                        </td>
                        <td>
                            <button class="button">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" />
                                ${warehouse.message(code:'default.button.add.label')}
                            </button>
                        </td>
                    </tr>
                </tfoot>
            </table>
        </g:formRemote>
    </div>
</div>
<script type="text/javascript">
    function onSuccess() {
        $("#productCatalogs").focus();
    }

    function onComplete() {
        $("#productCatalogs").focus();
    }
</script>
