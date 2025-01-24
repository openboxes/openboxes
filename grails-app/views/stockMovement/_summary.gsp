<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<section class="summary" aria-label="summary">
    <table id="stockMovement-summary">
        <tbody>
        <tr>
            <td class="middle" width="1%">
                <g:if test="${shipmentInstance?.shipmentType }">
                    <img src="${resource(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType, locale:null) + '.png')}"
                         alt="${format.metadata(obj:shipmentInstance?.shipmentType)}" style="vertical-align: middle; width: 24px; height: 24px;" />
                </g:if>
                <g:else>
                    <img src="${resource(dir:'images/icons/shipmentType',file: 'ShipmentTypeDefault.png')}"
                         alt="${format.metadata(obj:shipmentInstance?.shipmentType)}" style="vertical-align: middle; width: 24px; height: 24px;" />
                </g:else>
            </td>

            <td class="center" width="1%">
                <g:if test="${stockMovement?.identifier }">
                    <div class="box-barcode">
                        <img src="${createLink(controller:'product',action:'barcode',params:[data:stockMovement?.identifier,width:100,height:30,format:'CODE_128']) }"/>
                        <div class="barcode">${stockMovement.identifier}</div>
                    </div>
                </g:if>
            </td>

            <td class="middle">
                <g:if test="${stockMovement?.id}">

                    <div class="title" data-testid="title">
                        <small class="font-weight-bold">${stockMovement?.identifier}</small>
                        <g:link controller="stockMovement" action="show" id="${stockMovement?.id }">
                            ${stockMovement?.name}
                        </g:link>
                        <small class="fade uppercase font-weight-bold" data-testid="direction-tag">
                            <g:if test="${shipmentInstance?.origin?.id == session?.warehouse?.id}">
                                <warehouse:message code="default.outbound.label" default="outbound"/>
                            </g:if>
                            <g:elseif test="${shipmentInstance?.destination?.id == session?.warehouse?.id}">
                                <warehouse:message code="default.inbound.label" default="inbound"/>
                            </g:elseif>
                        </small>
                    </div>

                    <div>
                        <g:if test="${stockMovement?.identifier }">
                            <span class="identifier">
                                <warehouse:message code="stockMovement.identifier.label"/>:
                                <label>${format.metadata(obj:stockMovement?.identifier)}</label>
                            </span>
                        </g:if>
                        <g:if test="${stockMovement?.shipmentType }">
                            <span class="shipmentType">
                                <warehouse:message code="shipping.shipmentType.label"/>:
                                <label>${format.metadata(obj:stockMovement?.shipmentType)}</label>
                            </span>
                        </g:if>
                        <g:if test="${stockMovement?.origin }">
                            <span class="origin">
                                <warehouse:message code="shipping.origin.label"/>:
                                <label>${format.metadata(obj:stockMovement?.origin)}</label>
                            </span>
                        </g:if>
                        <g:if test="${stockMovement?.destination }">
                            <span class="destination">
                                <warehouse:message code="shipping.destination.label"/>:
                                <label>${format.metadata(obj:stockMovement?.destination)}</label>
                            </span>
                        </g:if>
                        <span>
                            <warehouse:message code="stockMovement.lineItems.label"/>:
                            <label>${stockMovement.lineItems.size()?:0 }</label>
                        </span>
                        <span>
                            <warehouse:message code="shipping.totalValue.label"/>:
                            <label>
                                <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'default.none.label')])}">
                                    <g:formatNumber format="###,###,##0.00" number="${stockMovement?.shipment?.calculateTotalValue() ?: 0.00 }" />
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </label>
                        </span>
                        <span>
                            <warehouse:message code="shipping.totalWeight.label"/>:
                            <label>
                                <g:formatNumber format="#,##0.00" number="${shipmentInstance?.totalWeightInPounds() ?: 0.00 }" /> <warehouse:message code="default.lbs.label"/>
                            </label>
                        </span>
                    </div>
                    <div>
                        <g:if test="${!stockMovement?.dateRequested }">
                            <span class="dateRequested">
                                <warehouse:message code="stockMovement.dateRequested.label"/>:
                                <label>
                                    <g:formatDate
                                            format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                            date="${stockMovement?.dateRequested}"
                                    />
                                </label>
                            </span>
                        </g:if>
                        <g:if test="${!shipmentInstance?.hasShipped() }">
                            <g:if test="${shipmentInstance?.expectedShippingDate }">
                                <span class="expectedShippingDate">
                                    <warehouse:message code="shipping.expectedShippingDate.label"/>:
                                    <label>
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${shipmentInstance?.expectedShippingDate}"
                                        />
                                    </label>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            <span class="actualShippingDate">
                                <warehouse:message code="shipping.actualShippingDate.label"/>:
                                <label>
                                    <g:formatDate
                                        format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                        date="${shipmentInstance?.actualShippingDate}"
                                    />
                                </label>
                            </span>
                        </g:else>
                        <g:if test="${!shipmentInstance?.wasReceived() }">
                            <g:if test="${shipmentInstance?.expectedDeliveryDate }">
                                <span class="expectedDeliveryDate">
                                    <warehouse:message code="shipping.expectedDeliveryDate.label"/>:
                                    <label>
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${shipmentInstance?.expectedDeliveryDate}"
                                        />
                                    </label>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            <span class="actualDeliveryDate">
                                <warehouse:message code="shipping.actualDeliveryDate.label"/>
                                <label>
                                    <g:formatDate
                                            format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                            date="${shipmentInstance?.actualDeliveryDate}"
                                    />
                                </label>
                            </span>
                        </g:else>
                        <g:if test="${stockMovement.lastUpdated}">
                            <span class="lastUpdated">
                                <warehouse:message code="default.lastUpdated.label" default="Last updated"/>
                                <label><g:prettyDateFormat date="${stockMovement.lastUpdated}"/></label>
                            </span>
                        </g:if>
                    </div>
                </g:if>
            </td>
            <td class="center" width="1%">
                <div class="tag tag-alert" data-testid="status-tag">
                    ${stockMovement?.displayStatus?.label}
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</section>
