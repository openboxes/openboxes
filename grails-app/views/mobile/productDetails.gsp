<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="bootstrap" />
    <title><warehouse:message code="product.label" default="Product"/></title>
</head>
<body>

<div class="container">
    <g:set var="product" value="${productSummary.product}"/>
    <div class="row">
        <g:link controller="mobile" action="productList" class="nav nav-link">
            <i class="fa fa-chevron-left"></i>
            <g:message code="products.label"/>
        </g:link>
    </div>

    <div class="row">
        <div class="col-2">
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
        <div class="col-3">
            <h5><g:formatNumber number="${productSummary.quantityOnHand}" maxFractionDigits="0"/></h5>
            <small class="text-muted">${product?.unitOfMeasure}</small>
        </div>
    </div>
</div>
</body>
</html>
