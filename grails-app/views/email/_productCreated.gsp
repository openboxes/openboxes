<%@ page contentType="text/html"%>
<g:applyLayout name="email">

<div class="box">
    <h2>${warehouse.message(code:'default.summary.label', default:'Summary') }</h2>
    <div style="margin: 10px;">
        <g:set var="productName" value="${productInstance.productCode} ${productInstance.name}"/>
        <g:set var="createdBy" value="${productInstance?.createdBy?.name?:'unknown'}"/>
        ${warehouse.message(code: 'email.productCreated.message', args: [productName, createdBy])}
        <g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]" absolute="true">
            ${warehouse.message(code: 'email.link.label', args: [productInstance?.name])}
        </g:link>
    </div>
</div>

<div class="box">
    <h2>${warehouse.message(code:'default.details.label', default:'Details') }</h2>
    <table class="details stripe">
        <tbody>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.productCode.label') }</label></td>
            <td>${productInstance.productCode }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.label') }</label></td>
            <td>${productInstance.name }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.description.label') }</label></td>
            <td>${productInstance.description }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'category.label') }</label></td>
            <td>${productInstance.category }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'tags.label') }</label></td>
            <td>${productInstance.tagsToString() }</td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'product.unitOfMeasure.label') }</label>
            </td>
            <td class="value">
                ${productInstance.unitOfMeasure}
            </td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.manufacturer.label') }</label></td>
            <td>${productInstance.manufacturer }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.manufacturerCode.label') }</label></td>
            <td>${productInstance.manufacturerCode }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.vendor.label') }</label></td>
            <td>${productInstance.vendor }</td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'product.vendorCode.label') }</label></td>
            <td>${productInstance.vendorCode }</td>
        </tr>
            <tr class="prop">
                <td class="name"><label>${warehouse.message(code: 'product.pricePerUnit.label') }</label></td>
                <td>
                    <g:hasRoleFinance>
                        ${g.formatNumber(number: productInstance.pricePerUnit, type: "currency") }
                    </g:hasRoleFinance>
                </td>
            </tr>
    </table>
</div>
</g:applyLayout>
