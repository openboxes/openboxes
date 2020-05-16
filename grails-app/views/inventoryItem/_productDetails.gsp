<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<style>
.nailthumb-container {
    width: 100%;
    overflow: hidden;
}
.galleryItem {
    color: #797478;
    font: 10px/1.5 Verdana, Helvetica, sans-serif;
    float: left;
    margin: 2px;
}

.galleryItem img {
    max-width: 100%;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    border-radius: 5px;
}
</style>

<div id="product-details">

<div class="box">
    <h2>
        ${warehouse.message(code: 'product.status.label') }
    </h2>
    <table>
        <tbody>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.onHandQuantity.label"/></label>
                </td>
                <td class="value">
                    <div>
                        ${g.formatNumber(number: totalQuantity, format: '###,###,###') }
                        <g:if test="${productInstance?.unitOfMeasure }">
                            <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.each.label') }
                        </g:else>
                    </div>
                    <g:if test="${productInstance?.packages }">
                        <g:each var="productPackage" in="${productInstance?.packages }">
                            <g:if test="${productPackage?.uom?.code != 'EA' }">
                                <div>
                                    <span class="fade">
                                        <g:set var="quantityPerPackage" value="${totalQuantity / productPackage?.quantity }"/>
                                        ${g.formatNumber(number: quantityPerPackage, format: '###,###,###.#') }
                                        ${productPackage?.uom?.code }/${productPackage.quantity }
                                    </span>
                                </div>
                            </g:if>
                        </g:each>
                    </g:if>
                </td>
            </tr>
            <g:if test="${grailsApplication.config.openboxes.forecasting.enabled}">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="forecasting.demand.label"/></label>
                    </td>
                    <td class="value" id="demand">
                        <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="forecasting.onHandMonths.label"/></label>
                    </td>
                    <td class="value" id="onHandMonths">
                        <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                    </td>
                </tr>
            </g:if>
            <g:set var="inventoryLevel" value="${productInstance?.getInventoryLevel(session.warehouse.id)}"/>
            <g:if test="${inventoryLevel}">
                <g:if test="${inventoryLevel?.minQuantity}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="inventoryLevel.minQuantity.label"/></label>
                        </td>
                        <td class="value">
                            ${g.formatNumber(number: inventoryLevel?.minQuantity?:0, format: '###,###,###') }
                            <g:if test="${productInstance?.unitOfMeasure }">
                                <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.each.label') }
                            </g:else>

                        </td>
                    </tr>
                </g:if>
                <g:if test="${inventoryLevel?.forecastQuantity}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="inventoryLevel.forecastQuantity.label"/></label>
                        </td>
                        <td class="value" id="forecastQuantity">
                            <div>
                                ${g.formatNumber(number: inventoryLevel?.monthlyForecastQuantity?:0, format: '###,###,###') }
                                <g:message code="default.perMonth.label" default="per month"/>
                            </div>
                        </td>
                    </tr>
                </g:if>
                <g:if test="${inventoryLevel?.reorderQuantity}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="inventoryLevel.reorderQuantity.label"/></label>
                        </td>
                        <td class="value">
                            ${g.formatNumber(number: inventoryLevel?.reorderQuantity?:0, format: '###,###,###') }
                            <g:if test="${productInstance?.unitOfMeasure }">
                                <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.each.label') }
                            </g:else>
                        </td>
                    </tr>
                </g:if>
                <g:if test="${inventoryLevel?.maxQuantity}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="inventoryLevel.maxQuantity.label"/></label>
                        </td>
                        <td class="value">
                            ${g.formatNumber(number: inventoryLevel?.maxQuantity?:0, format: '###,###,###') }
                            <g:if test="${productInstance?.unitOfMeasure }">
                                <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.each.label') }
                            </g:else>
                        </td>
                    </tr>
                </g:if>
                <g:if test="${inventoryLevel?.abcClass}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="product.abcClass.label"/></label>
                        </td>
                        <td class="value middle">
                            <g:abcClassification product="${productInstance.id}"/>
                        </td>
                    </tr>
                </g:if>
                <g:if test="${inventoryLevel?.binLocation}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="product.preferredBin.label"/></label>
                        </td>
                        <td class="value middle">
                            ${inventoryLevel?.binLocation ?: warehouse.message(code:'default.none.label')}
                        </td>
                    </tr>
                </g:if>
            </g:if>
            <g:set var="latestInventoryDate"
               value="${productInstance?.latestInventoryDate(session.warehouse.id)}" />

            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.latestInventoryDate.label"/></label>
                </td>
                <td class="value">
                    <span class="">
                        <g:if test="${latestInventoryDate}">
                            <p title="${g.formatDate(date: latestInventoryDate, format: 'dd MMMMM yyyy hh:mm a') }">
                                ${g.prettyDateFormat(date: latestInventoryDate)}
                            </p>

                        </g:if>
                        <g:else>
                            <p class="fade"><warehouse:message code="default.never.label" /></p>
                        </g:else>
                    </span>
                </td>
            </tr>

            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.pricePerUnit.label"/></label>
                </td>
                <td class="value middle">
                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'default.none.label')])}">
                        ${g.formatNumber(number: (productInstance?.pricePerUnit?:0), format: '###,###,##0.00##')}
                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                    </g:hasRoleFinance>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.totalValue.label"/></label>
                </td>
                <td class="value middle">
                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'default.none.label')])}">
                        ${g.formatNumber(number: (totalQuantity?:0) * (productInstance?.pricePerUnit?:0), format: '###,###,##0.00') }
                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                    </g:hasRoleFinance>
                </td>
            </tr>

        </tbody>
    </table>
</div>
<div class="box">
    <h2>
        ${warehouse.message(code: 'product.details.label') }
    </h2>
    <table>
        <tbody>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.unitOfMeasure.label"/></label>
                </td>
                <td class="value" id="unitOfMeasure">
                    <g:if test="${productInstance?.unitOfMeasure }">
                        <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                    </g:if>
                    <g:else>
                        <span class="fade"><warehouse:message code="default.none.label"/></span>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label>${warehouse.message(code: 'product.productCode.label') }</label>
                </td>
                <td class="value">
                    ${productInstance?.productCode}
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="category.label"/></label>
                </td>
                <td class="value" id="productCategory">
                    <span class="dont-break-out">
                        <g:if test="${productInstance?.category?.name }">
                            <g:link controller="inventory" action="browse" params="[subcategoryId:productInstance?.category?.id,showHiddenProducts:'on',showOutOfStockProducts:'on',searchPerformed:true]">
                                <format:category category="${productInstance?.category}"/>
                            </g:link>
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                    <g:each var="category" in="${productInstance?.categories }">
                        <div>
                            <g:link controller="inventory" action="browse" params="[subcategoryId:category?.id,showHiddenProducts:true,showOutOfStockProducts:true,searchPerformed:true]">
                                <format:category category="${category}"/>
                            </g:link>
                        </div>
                    </g:each>

                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label>${warehouse.message(code: 'product.description.label') }</label>
                </td>
                <td class="value">
                    <g:set var="maxLength" value="${productInstance?.description?.length() }"/>
                    <g:if test="${maxLength > 50 }">
                        <span title="${productInstance?.description }">${productInstance?.description?.substring(0,50)}...</span>
                    </g:if>
                    <g:else>
                        ${productInstance?.description?:g.message(code:'default.none.label') }
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td class="label left">
                    <label><warehouse:message code="productSuppliers.label"/></label>
                </td>
                <td class="value">
                    <g:if test="${productInstance?.productSuppliers }">
                        <ul>
                            <g:each var="productSupplier" in="${productInstance?.productSuppliers }">
                                <li>
                                    <g:link controller="product" action="edit" id="${productInstance.id }" fragment="ui-tabs-1">
                                        ${productSupplier?.code }
                                    </g:link>
                                </li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <g:link controller="product" action="edit" id="${productInstance.id }" fragment="tabs-productGroups">
                            <warehouse:message code="default.button.manage.label"/>
                        </g:link>
                    </g:else>
                </td>
            </tr>

            <tr class="prop">
                <td class="label left">
                    <label><warehouse:message code="productGroups.label"/></label>
                </td>
                <td class="value">
                    <g:if test="${productInstance?.productGroups }">
                        <g:each var="productGroup" in="${productInstance?.productGroups }">
                            <g:link controller="product" action="edit" id="${productInstance.id }" fragment="tabs-productGroups">
                                ${productGroup?.name }
                            </g:link>
                        </g:each>
                    </g:if>
                    <g:else>
                        <g:link controller="product" action="edit" id="${productInstance.id }" fragment="tabs-productGroups">
                            <warehouse:message code="default.button.manage.label"/>
                        </g:link>
                    </g:else>
                </td>
            </tr>

            <g:set var="status" value="${0 }"/>
            <g:each var="productAttribute" in="${productInstance?.attributes}">
                <tr class="prop">
                    <td class="label left">
                        <label><format:metadata obj="${productAttribute?.attribute}"/></label>
                    </td>
                    <td>
                        <span class="">${productAttribute.value }</span>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>
<g:if test="${productInstance?.packages }">
    <div class="box">
        <h2>
            ${warehouse.message(code: 'product.packaging.label') }
        </h2>
        <table>
            <tbody>
                <g:each var="productPackage" in="${productInstance?.packages?.sort { it.quantity }}">
                    <tr class="prop">
                        <td class="label">
                            <label>${productPackage?.uom }</label>
                        </td>
                        <td class="value">
                            ${productPackage?.uom?.code }/${productPackage?.quantity }
                            @ ${productPackage?.price } ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>
<div class="box">
    <h2>
        ${warehouse.message(code: 'default.auditing.label') }
    </h2>
    <table>
        <tbody>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.createdBy.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">${productInstance?.createdBy?.name?:warehouse.message(code: 'default.unknown.label') }</span> <br/>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.modifiedBy.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">${productInstance?.updatedBy?.name?:warehouse.message(code: 'default.unknown.label') }</span> <br/>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.createdOn.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">
                        ${g.formatDate(date: productInstance?.dateCreated, format: 'd-MMM-yyyy')}
                        ${g.formatDate(date: productInstance?.dateCreated, format: 'hh:mma')}
                    </span>

                </td>
            </tr>



            <tr class="prop">
                <td class="label"  >
                    <label><warehouse:message code="product.modifiedOn.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">
                        ${g.formatDate(date: productInstance?.lastUpdated, format: 'd-MMM-yyyy')}
                        ${g.formatDate(date: productInstance?.lastUpdated, format: 'hh:mma')}
                    </span>
                </td>
            </tr>

        </tbody>
    </table>
</div>

</div>
<script>
	function openDialog(dialogId, imgId) {
		$(dialogId).dialog({autoOpen: true, modal: true, width: 600, height: 400});
	}
	function closeDialog(dialogId, imgId) {
		$(dialogId).dialog('close');
	}

    $(window).load(function(){
      var ajaxTimeout = ${grailsApplication.config.openboxes.ajaxRequest.timeout?:0}
      $.ajax({
        dataType: "json",
        timeout: ajaxTimeout,
        url: "${request.contextPath}/json/getForecastingData/${productInstance?.id}",
        success: function (data) {
          var onHandMonths = data.onHandMonths.toFixed(1);
          var demand = data.monthlyDemand;

          $('#onHandMonths').html(onHandMonths + " months");
          $('#demand').html(demand + " per month");
        },
      });
    });
</script>


