<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<style>
.nailthumb-container {
    width: 100%;
    overflow: hidden;
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
                    <g:if test="${productInstance?.uoms}">
                        <g:each var="productPackage" in="${productInstance?.uoms}">
                            <g:if test="${productPackage?.uom != 'EA' && productPackage?.quantity}">
                                <div>
                                    <span class="fade">
                                        <g:set var="quantityPerPackage" value="${totalQuantity / productPackage?.quantity}"/>
                                        ${g.formatNumber(number: quantityPerPackage, format: '###,###,###.#')}
                                        ${productPackage?.uom }/${productPackage.quantity}
                                    </span>
                                </div>
                            </g:if>
                        </g:each>
                    </g:if>
                </td>
            </tr>
            <g:if test="${totalQuantityAvailableToPromise >= 0}">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.quantityAvailableToPromise.label" default="Quantity Available"/></label>
                    </td>
                    <td class="value">
                        <div>
                            ${g.formatNumber(number: totalQuantityAvailableToPromise, format: '###,###,###') }
                        </div>
                    </td>
                </tr>
            </g:if>
            <g:if test="${grailsApplication.config.openboxes.forecasting.enabled}">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="forecasting.demand.label"/></label>
                    </td>
                    <td class="value" id="demand">
                        <img class="spinner" src="${resource(dir: 'images', file: 'spinner.gif')}" class="middle"/>
                         <g:message code="default.month.perMonth.label" default="per month" />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="forecasting.onHandMonths.label"/></label>
                    </td>
                    <td class="value" id="onHandMonths">
                        <img class="spinner" src="${resource(dir: 'images', file: 'spinner.gif')}" class="middle"/>
                        ${g.message(code: "default.time.unit.months.label", default: "Months").toLowerCase()}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="forecasting.stockoutDays.label" default="Stockout Days (last 30 days)"/></label>
                    </td>
                    <td class="value" id="stockoutDays">
                        <img class="spinner" src="${resource(dir: 'images', file: 'spinner.gif')}" class="middle"/>
                        <g:message code="default.days.label" default="Days" />
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
                <g:if test="${inventoryLevel?.preferredBinLocation}">
                    <tr class="prop">
                        <td class="label">
                            <label><warehouse:message code="product.preferredBin.label"/></label>
                        </td>
                        <td class="value middle">
                            ${inventoryLevel?.preferredBinLocation ?: warehouse.message(code:'default.none.label')}
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
                    <label title="${g.message(code: "product.lastPaid.label", default: "Last Paid")}">
                        <g:message
                                code="product.unitPrice.label"
                                default="Unit Price"
                        />
                    </label>
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
                <td class="label left">
                    <label><warehouse:message code="product.productFamily.label"/></label>
                </td>
                <td class="value">
                    <g:if test="${productInstance?.productFamily }">
                        <g:link controller="productGroup" action="edit" id="${productInstance?.productFamily?.id }">
                            ${productInstance?.productFamily?.name }
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="product" action="edit" id="${productInstance.id }" fragment="ui-tabs-1">
                            <warehouse:message code="default.button.edit.label"/>
                        </g:link>
                    </g:else>
                </td>
            </tr>
            <g:if test="${productInstance?.glAccount}">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.glCode.label"/></label>
                    </td>
                    <td class="value middle">
                        ${productInstance?.glAccount?.code}
                    </td>
                </tr>
            </g:if>
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
            <g:if test="${inventoryLevel?.abcClass || productInstance?.abcClass}">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.abcClass.label"/></label>
                    </td>
                    <td class="value middle">
                        <g:abcClassification product="${productInstance.id}"/>
                    </td>
                </tr>
            </g:if>
            <g:set var="status" value="${0 }"/>
            <g:each var="productAttribute" in="${productInstance?.attributes?.findAll{ !it?.productSupplier }}">
                <tr class="prop">
                    <td class="label left">
                        <label><format:metadata obj="${productAttribute?.attribute}"/></label>
                    </td>
                    <td>
                        <span class="">${productAttribute.value }</span>
                        <small>${productAttribute?.attribute?.unitOfMeasureClass?.baseUom?.name}</small>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>
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
                        ${g.formatDate(date: productInstance?.dateCreated)}
                    </span>

                </td>
            </tr>



            <tr class="prop">
                <td class="label"  >
                    <label><warehouse:message code="product.modifiedOn.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">
                        ${g.formatDate(date: productInstance?.lastUpdated)}
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

	function fetchData(url, data, success) {
	  var ajaxTimeout = ${grailsApplication.config.openboxes.ajaxRequest.timeout?:0}
      $.ajax({
        dataType: "json",
        timeout: ajaxTimeout,
        url: url,
        data: data,
        success: success,
      });
    }

    $(window).load(function(){
      var data = {
        "product.id": "${productInstance?.id}",
        "location.id": $("#currentLocationId").val()
      };

      fetchData("${request.contextPath}/json/getForecastingData", data,
        function (data) {
          $('#onHandMonths').prepend(data.onHandMonths.toFixed(1)).children().remove(".spinner")
          $('#demand').prepend(data.monthlyDemand).children().remove(".spinner")
        }
      );

      fetchData("${request.contextPath}/json/getStockoutData", data,
        function (data) {
          $('#stockoutDays').prepend(data.stockoutDays).children().remove(".spinner")
        }
      )

    });
</script>
