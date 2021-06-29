<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
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

    <div class="card">

        <div class="card-body">

            <h1><small>${product.productCode}</small> ${product.name}</h1>

            <picture>
                <g:if test="${product.images}">
                    <g:set var="image" value="${product?.images?.sort()?.first()}"/>
                    <img src="${createLink(controller:'product', action:'renderImage', id:image?.id)}" class="img-fluid"/>
                </g:if>
                <g:else>
                    <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                </g:else>
            </picture>

            <h3>Description</h3>
            <g:if test="${product.description}">
                <p>
                    ${product.description}
                </p>
            </g:if>


            <h3>Details</h3>
            <ul class="list-group">
                <li class="list-group-item">
                    <label>QoH</label>
                    <span>
                        <g:formatNumber number="${productSummary.quantityOnHand?:0}" maxFractionDigits="0"/>
                        <small class="text-muted">${product?.unitOfMeasure?:"EA"}</small>
                    </span>
                </li>

                <g:each var="productAttribute" in="${product.attributes}">
                    <li class="list-group-item">
                        <label>${productAttribute.attribute.name}</label>
                        <span>
                            ${productAttribute?.value?:0}
                            ${productAttribute?.unitOfMeasure?.name}
                            ${productAttribute?.attribute?.unitOfMeasureClass?.baseUom?.name}
                        </span>
                    </li>
                </g:each>
            </ul>

            <h3>Barcode</h3>
            <g:displayBarcode format="${com.google.zxing.BarcodeFormat.CODE_128}" data="${product.productCode}" showData="${false}"/>


        </div>
    </div>
</div>
</body>
</html>
