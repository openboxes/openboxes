<div id="productAssociations">

    <div class="box">
        <h2>
            <warehouse:message code="productAssociations.label" default="Product Associations"/>
        </h2>

        <div class="dialog">

            <table>
                <thead>
                <tr>

                    <g:sortableColumn property="code" title="${warehouse.message(code: 'productAssociation.code.label', default: 'Code')}" />

                    <th><warehouse:message code="productAssociation.associatedProduct.label" default="Associated Product" /></th>

                    <g:sortableColumn property="quantity" title="${warehouse.message(code: 'productAssociation.quantity.label', default: 'Quantity')}" />

                    <g:sortableColumn property="comments" title="${warehouse.message(code: 'productAssociation.comments.label', default: 'Comments')}" />

                    <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'productAssociation.dateCreated.label', default: 'Date Created')}" />

                    <th><g:message code="default.actions.label"/></th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${productInstance?.associations}" status="i" var="productAssociation">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td>${fieldValue(bean: productAssociation, field: "code")}</td>

                        <td>
                            <g:link controller="product" action="edit" id="${productAssociation?.associatedProduct?.id}">
                                ${fieldValue(bean: productAssociation?.associatedProduct, field: "productCode")}
                                ${fieldValue(bean: productAssociation?.associatedProduct, field: "name")}
                            </g:link>
                        </td>

                        <td>1:${fieldValue(bean: productAssociation, field: "quantity")}</td>

                        <td>${fieldValue(bean: productAssociation, field: "comments")}</td>

                        <td><format:date obj="${productAssociation.dateCreated}" /></td>

                        <td>

                            <g:link controller="productAssociation" action="edit" id="${productAssociation.id}" class="button">
                                <g:message code="default.button.edit.label"/>
                            </g:link>

                            <g:if test="${productAssociation?.mutualAssociation}">
                                <button type="button"
                                        class="button"
                                        onclick="$('#product-association-delete-dialog')
                                          .data('productAssociationId', `${productAssociation?.id}`)
                                          .data('reload', true)
                                          .dialog('open')">
                                    ${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}
                                </button>
                            </g:if>
                            <g:else>
                                <button class="button"
                                        onclick="deleteSingleProductAssociation('${productAssociation?.id}')">
                                    <g:message code="default.button.delete.label"/>
                                </button>
                            </g:else>
                        </td>

                    </tr>
                </g:each>
                <g:unless test="${productInstance?.associations}">
                    <tr class="prop">
                        <td colspan="11">
                            <div class="padded fade center">
                                <g:message code="default.empty.message" default="There are no {0}" args="[g.message(code:'productAssociations.label')]"/>
                            </div>
                        </td>
                    </tr>
                </g:unless>
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="8">
                        <div class="center">
                            <button class="button btn-show-dialog"
                                    data-title="${g.message(code: 'default.create.label', args: [g.message(code:'productAssociation.label')])}"
                                    data-url="${request.contextPath}/productAssociation/dialog?product.id=${productInstance?.id}">
                                <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png')}" />
                                ${g.message(code: 'default.create.label', args: [g.message(code:'productAssociation.label')])}
                            </button>
                        </div>
                    </td>
                </tr>
                </tfoot>

            </table>
        </div>
    </div>
    <g:render template="/productAssociation/productAssociationDeleteDialog" />
</div>

<script>
  function deleteSingleProductAssociation(productAssociationId) {
    if (confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
      deleteProductAssociation(productAssociationId, false, true);
    }
  }
</script>
