<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="bootstrap" />
    <title><warehouse:message code="products.label" default="Products"/></title>
</head>

<body>
    <div class="container-fluid">
        <div class="row">
            <div class="col-12">
                <form class="form form-horizontal">
                    <div class="input-group" id="adv-search">
                        <input name="q" type="text" class="form-control" placeholder="Search for products" />
                        <div class="input-group-btn">
                            <div class="btn-group" role="group">
                                <button type="button" class="btn btn-primary"><span class="fa fa-search" aria-hidden="true"></span></button>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="container-fluid">
        <div class="row">
            <div class="list-group">
                <g:each var="productSummary" in="${productSummaries}">
                    <g:set var="product" value="${productSummary.product}"/>
                    <div class="nav-link list-group-item">
                        <div class="row align-items-center">
                            <div class="col-2 text-center">
                                <picture>
                                    <g:if test="${product.images}">
                                        <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                                    </g:if>
                                    <g:else>
                                        <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                                    </g:else>
                                </picture>
                            </div>
                            <div class="col">
                                <h5>${product.productCode}</h5>
                                <h6>
                                    ${product.name}
                                    <g:renderHandlingIcons product="${product}"/>
                                </h6>
                                <g:if test="${product.description}">
                                    <p class="text-truncate">
                                        ${product.description}
                                    </p>
                                </g:if>
                            </div>
                            <div class="col-3 text-center">
                                <h5>
                                    <g:formatNumber number="${productSummary.quantityOnHand}" maxFractionDigits="0"/>
                                    ${product?.unitOfMeasure}
                                </h5>
                                <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="btn btn-link">
                                    <g:message code="default.details.label"/>
                                    <i class="fa fa-chevron-right"></i>
                                </a>

                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</body>
</html>
