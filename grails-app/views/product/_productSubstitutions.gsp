<div id="productSubstitutions">

    <div class="box">
        <h2>
            <warehouse:message code="product.substitutions.label" default="Substitutions"/>
        </h2>

        <div class="dialog">

            <table>
                <thead>
                    <tr>

                        <g:sortableColumn property="id" title="${warehouse.message(code: 'productAssociation.id.label', default: 'Id')}" />

                        <g:sortableColumn property="code" title="${warehouse.message(code: 'productAssociation.code.label', default: 'Code')}" />

                        <th><warehouse:message code="productAssociation.product.label" default="Product" /></th>

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

                            <td><g:link controller="productAssociation" action="edit" id="${productSubstitution.id}">${fieldValue(bean: productSubstitution, field: "id")}</g:link></td>

                            <td>${fieldValue(bean: productSubstitution, field: "code")}</td>

                            <td>
                                <g:link controller="product" action="edit" id="${productSubstitution?.product?.id}">
                                    ${fieldValue(bean: productSubstitution?.product, field: "productCode")}
                                    ${fieldValue(bean: productSubstitution?.product, field: "name")}
                                </g:link>

                            </td>

                            <td>
                                <g:link controller="product" action="edit" id="${productSubstitution?.product?.id}">
                                    ${fieldValue(bean: productSubstitution?.associatedProduct, field: "productCode")}
                                    ${fieldValue(bean: productSubstitution?.associatedProduct, field: "name")}
                                </g:link>
                            </td>

                            <td>${fieldValue(bean: productSubstitution, field: "quantity")}</td>

                            <td>${fieldValue(bean: productSubstitution, field: "comments")}</td>

                            <td><format:date obj="${productSubstitution.dateCreated}" /></td>

                            <td>
                                <g:remoteLink controller="product" action="removeFromProductAssociations"
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

            </table>
        </div>
    </div>
    <div class="center">
        <button class="button btn-show-dialog"
                data-target="#product-substitution-dialog"
                data-title="${g.message(code: 'default.create.label', args: [g.message(code:'productAssociation.label')])}"
                data-url="${request.contextPath}/productAssociation/dialog?product.id=${productInstance?.id}">
            ${g.message(code: 'default.create.label', args: [g.message(code:'productAssociation.label')])}
        </button>
    </div>

</div>

<div id="product-substitution-dialog" class="dialog hidden" title="Product Supplier">
    <div class="empty center">Loading ...</div>
</div>


<g:javascript>

    $(document).ready(function() {
        $(".btn-show-dialog").click(function(event) {
            var target = $(this).data("target")
            var url = $(this).data("url");
            var title = $(this).data("title");
            $(target).attr("title", title);
            $(target).dialog({
                autoOpen: false,
                modal: true,
                width: 800,
                open: function(event, ui) {
                    $(this).html("Loading...")
                    $(this).load(url, function (response, status, xhr) {

                        if (status == "error") {

                            // Clear error
                            $(this).text("")
                            $("<p/>").addClass("error").text("An unexpected error has occurred: " + xhr.status + " " + xhr.statusText).appendTo($(this));

                            // If in debug mode (which we always are, at the moment) we can display the error response
                            // from the server (or javascript error in case error response is not in JSON)
                            try {
                                var error = JSON.parse(response);
                                var stack = $("<div/>").addClass("stack empty").appendTo($(this));
                                $("<pre/>").text(error.errorMessage).appendTo(stack)
                            } catch (e) {
                                console.log("exception: ", e);
                                //$("<pre/>").text(e.stack).appendTo($(this));
                                $(this).append(response);
                            }

                        }

                        //$(this).dialog('open');

                    });
                }
            }).dialog('open');
        });

    });
</g:javascript>