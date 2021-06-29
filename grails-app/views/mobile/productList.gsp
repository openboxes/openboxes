<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="products.label" default="Products"/></title>
</head>

<body>
    <div class="row">
        <div class="col-12">
            <form class="form form-horizontal">
                <div class="input-group" id="adv-search">
                    <input name="q" type="text" class="form-control" placeholder="Search for products" value="${params.q}" />
                    <div class="input-group-btn">
                        <div class="btn-group" role="group">
                            <button type="button" class="btn btn-primary"><span class="fa fa-search" aria-hidden="true"></span></button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <div class="row g-0">
        <table class="table table-borderless">
            <g:each var="productSummary" in="${productSummaries}">
                <g:set var="product" value="${productSummary.product}"/>
                <tr class="border-bottom bg-white">
                    <td class="row">
                        <picture>
                            <g:if test="${product.images}">
                                <g:set var="image" value="${product?.images?.sort()?.first()}"/>
                                <img src="${createLink(controller:'product', action:'renderImage', id:image?.id)}" class="img-fluid"/>
                            </g:if>
                            <g:else>
                                <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                            </g:else>
                        </picture>
                        <div class="text-center img-fluid">
                        <g:displayBarcode format="${com.google.zxing.BarcodeFormat.CODE_128}" data="${product.productCode}" showData="${false}"/>
                        </div>
                   </td>
                    <td>
                        <h5>${product.productCode} ${product.name}</h5>
                        <g:renderHandlingIcons product="${product}"/>
                        <g:if test="${product.description}">
                            <div class="d-flex align-items-center justify-content-between mt-1">
                                ${product.description}
                            </div>
                        </g:if>
                    </td>
                    <td>
                        <g:formatNumber number="${productSummary.quantityOnHand}" maxFractionDigits="0"/>
                        <small class="text-muted">${product?.unitOfMeasure?:"EA"}</small>
                    </td>
                    <td>
                        <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}" class="btn btn-link">
                            <i class="fa fa-chevron-right"></i>
                        </a>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
</body>
</html>
