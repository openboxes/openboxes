<div class="box">
    <h2>
        <warehouse:message code="product.productSuppliers.label" default="Product Sources"/>
    </h2>

    <div class="dialog">
        <table>
            <thead>
            <%--
                <g:sortableColumn property="id" title="${warehouse.message(code: 'productSupplier.id.label', default: 'Id')}" />

                <th><g:message code="productSupplier.product.label" default="Product" /></th>
            --%>
            <g:sortableColumn property="code" title="${warehouse.message(code: 'default.code.label', default: 'Code')}" />

            <th><g:message code="productSupplier.productCode.label" /></th>

            <th><g:message code="default.name.label" default="Name" /></th>

            <th><g:message code="productSupplier.supplier.label" default="Supplier" /></th>

            <th><g:message code="productSupplier.supplierCode.label" default="Supplier Code" /></th>

            <th><g:message code="productSupplier.manufacturer.label" default="Manufacturer" /></th>

            <th><g:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></th>

            <th><g:message code="productSupplier.preferenceTypeCode.label" default="Preference Type" /></th>

            <th><g:message code="productSupplier.ratingTypeCode.label" default="Rating Type" /></th>

            <th><g:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></th>

            <th><g:message code="productSupplier.unitPrice.label" default="Unit Price" /></th>

            <th><g:message code="default.actions.label" default="Actions" /></th>


            </thead>
            <tbody>
                <g:if test="${productInstance?.productSuppliers}">

                    <g:each var="productSupplier" in="${productInstance?.productSuppliers.sort()}" status="status">
                        <tr class="prop ${status%2==0?'odd':'even'}">

                            <%--
                            <td><g:link controller="productSupplier" action="edit" id="${productSupplier.id}">${fieldValue(bean: productSupplier, field: "id")}</g:link></td>

                            <td>
                                <a href="javascript:void(0);" class="btn-show-dialog"
                                   data-target="#product-supplier-dialog"
                                   data-title="${g.message(code:'productSupplier.label')}"
                                   data-url="${request.contextPath}/productSupplier/dialog?id=${productSupplier?.id}&product.id=${productInstance?.id}">
                                    ${fieldValue(bean: productSupplier, field: "product")}
                                </a>
                            </td>
                            --%>
                            <td>${fieldValue(bean: productSupplier, field: "code")?:g.message(code:'default.none.label')}</td>

                            <td>${fieldValue(bean: productSupplier, field: "productCode")?:g.message(code:'default.none.label')}</td>

                            <td>${fieldValue(bean: productSupplier, field: "name")?:g.message(code:'default.none.label')}</td>

                            <td>${fieldValue(bean: productSupplier, field: "supplier")}</td>

                            <td>${fieldValue(bean: productSupplier, field: "supplierCode")}</td>

                            <td>${fieldValue(bean: productSupplier, field: "manufacturer")}</td>

                            <td>${fieldValue(bean: productSupplier, field: "manufacturerCode")}</td>

                            <td>${fieldValue(bean: productSupplier, field: "preferenceTypeCode")}</td>

                            <td>${fieldValue(bean: productSupplier, field: "ratingTypeCode")}</td>

                            <td>${fieldValue(bean: productSupplier, field: "unitOfMeasure")}</td>

                            <td>
                                <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: ['0.00'])}">
                                    ${fieldValue(bean: productSupplier, field: "unitPrice")}
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </td>

                            <td>
                                <a href="javascript:void(0);" class="btn-show-dialog button"
                                   data-position="top"
                                   data-title="${g.message(code:'productSupplier.label')}"
                                   data-url="${request.contextPath}/productSupplier/dialog?id=${productSupplier?.id}&product.id=${productInstance?.id}">
                                    <img src="${createLinkTo(dir:'images/icons/silk', file:'pencil.png')}" />
                                    <g:message code="default.button.edit.label"/>
                                </a>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:unless test="${productInstance?.productSuppliers}">
                    <tr class="prop">
                        <td class="empty center" colspan="11">
                            <g:message code="productSuppliers.empty.label" default="There are no product suppliers"/>
                        </td>
                    </tr>
                </g:unless>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="12">
                    <div class="center">
                        <button class="button icon add btn-show-dialog" data-position="top"
                                data-title="${g.message(code: 'default.add.label', args: [g.message(code:'productSupplier.label')])}"
                                data-url="${request.contextPath}/productSupplier/dialog?product.id=${productInstance?.id}">
                            ${g.message(code: 'default.create.label', default: 'Create', args: [g.message(code:'productSupplier.label')])}
                        </button>
                    </div>
                </td>
            </tr>
            </tfoot>
        </table>
    </div>
</div>


<g:javascript>



    //$('#demo-modal').load('get-dynamic-content.php?modal='+modal).dialog(options).dialog('open');
    $(document).ready(function() {


        $(".tabs").livequery(function() {
            $(this).tabs({});
        });
        %{--$(".btn-show-dialog").click(function(event) {--}%
            %{--var target = $(this).data("target")--}%
            %{--var url = $(this).data("url");--}%
            %{--var title = $(this).data("title");--}%
            %{--$(target).attr("title", title);--}%
            %{--$(target).dialog({--}%
                %{--autoOpen: false,--}%
                %{--modal: true,--}%
                %{--width: 800,--}%
                %{--autoResize:true,--}%
                %{--resizable: true,--}%
                %{--minHeight:"auto",--}%
                %{--position: {--}%
                    %{--my: "center top",--}%
                    %{--at: "center top",--}%
                    %{--of: window--}%
                %{--},--}%
                %{--open: function(event, ui) {--}%
                    %{--$(this).html("Loading...")--}%
                    %{--$(this).load(url, function (response, status, xhr) {--}%

                        %{--if (status == "error") {--}%

                            %{--// Clear error--}%
                            %{--$(this).text("")--}%
                            %{--$("<p/>").addClass("error").text("An unexpected error has occurred: " + xhr.status + " " + xhr.statusText).appendTo($(this));--}%

                            %{--// If in debug mode (which we always are, at the moment) we can display the error response--}%
                            %{--// from the server (or javascript error in case error response is not in JSON)--}%
                            %{--try {--}%
                                %{--var error = JSON.parse(response);--}%
                                %{--var stack = $("<div/>").addClass("stack empty").appendTo($(this));--}%
                                %{--$("<pre/>").text(error.errorMessage).appendTo(stack)--}%
                            %{--} catch (e) {--}%
                                %{--console.log("exception: ", e);--}%
                                %{--//$("<pre/>").text(e.stack).appendTo($(this));--}%
                                %{--$(this).append(response);--}%
                            %{--}--}%

                        %{--}--}%

                        %{--//$(this).dialog('open');--}%

                    %{--});--}%
                %{--}--}%
            %{--}).dialog('open');--}%
        %{--});--}%
    });
</g:javascript>