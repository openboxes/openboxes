<g:form action="update" method="post">
    <g:hiddenField name="action" value="save"/>
    <g:hiddenField name="id" value="${productInstance?.id}" />
    <g:hiddenField name="version" value="${productInstance?.version}" />
    <div class="box" >
        <h2>
            <warehouse:message code="product.sources.label" default="Product Sources"/>
        </h2>

        <div id="uom-dialog" class="dialog hidden" title="Add a unit of measure">
            <g:render template="uomDialog" model="[productInstance:productInstance]"/>
        </div>

        <g:unless test="${productInstance?.productSuppliers}">
            <div class="empty center fade">
                There are no product sources.
            </div>
        </g:unless>
        <table>
            <tbody>
            <g:each var="productSupplier" in="${productInstance?.productSuppliers}" status="status">
                <tr>
                    <td>${productSupplier}</td>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="2">
                    <div class="center">

                        <button class="button icon approve">
                            ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                        </button>
                        &nbsp;
                        <g:if test="${productInstance?.id }">
                            <g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="button icon remove">
                                ${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="inventory" action="browse" class="button icon remove">
                                ${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
                            </g:link>
                        </g:else>
                    </div>
                </td>
            </tr>

            </tfoot>
        </table>
    </div>

</g:form>