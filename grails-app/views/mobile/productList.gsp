<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="products.label" default="Products"/></title>
</head>

<body>
<div class="row">
    <div class="col">
        <div class="card">
            <h5 class="card-header">Products</h5>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                        <tr>
                            <th scope="col">Picture</th>
                            <th scope="col">Details</th>
                            <th scope="col">Unit</th>
                            <th scope="col"></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="productSummary" in="${productSummaries}">
                            <g:set var="product" value="${productSummary.product}"/>
                            <tr>
                                <th scope="row">
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
                                </th>
                                <td >
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
                                    <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="btn btn-primary btn-sm">
                                        Details <i class="fa fa-chevron-right"></i>
                                    </a>
                                </td>
                            </tr>
                        </g:each>

                        </tbody>
                    </table>
                </div>
                <div class="paginateButtons">
                    <g:paginate total="${productSummaries.totalCount}"/>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
