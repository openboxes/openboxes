<div id="productSubstitutions">

    <div class="box">
        <h2>
            <warehouse:message code="product.substitutions.label" default="Substitutions"/>
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
                    <g:each in="${productInstance?.substitutions}" status="i" var="productSubstitution">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td>${fieldValue(bean: productSubstitution, field: "code")}</td>

                            <td>
                                <g:link controller="product" action="edit" id="${productSubstitution?.associatedProduct?.id}">
                                    ${fieldValue(bean: productSubstitution?.associatedProduct, field: "productCode")}
                                    ${fieldValue(bean: productSubstitution?.associatedProduct, field: "name")}
                                </g:link>
                            </td>

                            <td>1:${fieldValue(bean: productSubstitution, field: "quantity")}</td>

                            <td>${fieldValue(bean: productSubstitution, field: "comments")}</td>

                            <td><format:date obj="${productSubstitution.dateCreated}" /></td>

                            <td>

                                <g:link controller="productAssociation" action="edit" id="${productSubstitution.id}" class="button">
                                    <g:message code="default.button.edit.label"/>
                                </g:link>

                                <g:remoteLink controller="product" action="removeFromProductAssociations" class="button"
                                              id="${productSubstitution.id}" params="['product.id': productSubstitution.product.id]"
                                            update="productSubstitutions">
                                    <g:message code="default.button.delete.label"/>
                                </g:remoteLink>
                            </td>

                        </tr>
                    </g:each>
                    <g:unless test="${productInstance?.substitutions}">
                        <tr class="prop">
                            <td class="empty center" colspan="11">
                                <g:message code="default.empty.message" default="There are no {0}" args="[g.message(code:'product.substitutions.label')]"/>
                            </td>
                        </tr>
                    </g:unless>
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="8">
                        <div class="center">
                            <button class="button icon add btn-show-dialog"
                                    data-title="${g.message(code: 'default.create.label', args: [g.message(code:'productAssociation.label')])}"
                                    data-url="${request.contextPath}/productAssociation/dialog?product.id=${productInstance?.id}">
                                ${g.message(code: 'default.create.label', args: [g.message(code:'productAssociation.label')])}
                            </button>
                        </div>
                    </td>
                </tr>
                </tfoot>

            </table>
        </div>
    </div>
</div>


