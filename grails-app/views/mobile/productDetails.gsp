<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="product.label" default="Product"/></title>
</head>
<body>

<div>
    <g:set var="product" value="${productSummary.product}"/>
    <div class="row">
        <g:link controller="mobile" action="productList" class="nav nav-link">
            <i class="fa fa-chevron-left"></i>
            <g:message code="default.button.back.label" />
        </g:link>
    </div>

    <div class="card">

        <div class="card-body">

            <h5 class="display-5"><small class="text-small">${product.productCode}</small> ${product.name}</h5>

            <picture>
                <g:if test="${product.images}">
                    <g:set var="image" value="${product?.images?.sort()?.first()}"/>
                    <img src="${createLink(controller:'product', action:'renderImage', id:image?.id)}" class="img-fluid"/>
                </g:if>
                <g:else>
                    <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                </g:else>
            </picture>

            <h3 class="display-3">Description</h3>
            <g:if test="${product.description}">
                <p>
                    ${product.description}
                </p>
            </g:if>


            <h3 class="display-3">Details</h3>
            <ul class="list-group">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    Status
                    <g:if test="${productSummary.quantityOnHand > 0}">
                        <div class="text-success">In Stock</div>
                    </g:if>
                    <g:else>
                        <div class="text-danger">Out of Stock</div>
                    </g:else>
                </li>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <div class="label">QoH</div>
                    <span class="badge badge-primary badge-pill text-secondary">
                        <g:formatNumber number="${productSummary.quantityOnHand?:0}" maxFractionDigits="0"/>
                        ${product?.unitOfMeasure?:"EA"}
                    </span>
                </li>
                <g:each var="productAttribute" in="${product.attributes}">
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        ${productAttribute.attribute.name}
                        <span class="badge badge-primary badge-pill text-secondary">
                            ${productAttribute?.value?:0}
                            ${productAttribute?.unitOfMeasure?.name?:productAttribute?.attribute?.unitOfMeasureClass?.baseUom?.name}
                        </span>
                    </li>
                </g:each>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    Barcode
                    <g:displayBarcode format="${com.google.zxing.BarcodeFormat.CODE_128}" data="${product.productCode}" showData="${false}" height="20"/>
                </li>
            </ul>



        </div>
    </div>
</div>
</body>
</html>
