<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="products.label" default="Products"/></title>
</head>

<body>

    <div class="row g-0">
        <table class="table table-borderless">
            <g:each var="productSummary" in="${productSummaries}">
                <g:set var="product" value="${productSummary.product}"/>
                <tr class="border-bottom">
                    <td class="col-2">
                        <picture>
                            <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="text-decoration-none">
                                <g:if test="${product.images}">
                                    <g:set var="image" value="${product?.images?.sort()?.first()}"/>
                                    <img src="${createLink(controller:'product', action:'renderImage', id:image?.id)}" class="img-fluid"/>
                                </g:if>
                                <g:else>
                                    <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                                </g:else>
                            </a>
                        </picture>
                    </td>
                    <td class="col-8">
                        <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="text-decoration-none text-reset">
                            <h5>${product.productCode} ${product.name}</h5>
                        </a>
                        <g:renderHandlingIcons product="${product}"/>
                        <g:if test="${product.description}">
                            <div class="d-flex align-items-center justify-content-between mt-1">
                                ${product.description}
                            </div>
                        </g:if>
                    </td>
                    <td>
                        <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="text-decoration-none text-reset">
                            <g:formatNumber number="${productSummary.quantityOnHand}" maxFractionDigits="0"/>
                            <small>${product?.unitOfMeasure?:"EA"}</small>
                        </a>
                    </td>
                    <td>
                        <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="btn btn-link">
                            <i class="fa fa-chevron-right"></i>
                        </a>
                    </td>
                </tr>
            </g:each>

        </table>
        <div class="pagination">
            <g:paginate total="${productSummaries.totalCount}"/>
        </div>
    </div>
</body>
</html>
