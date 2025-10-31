<%@ page import="org.pih.warehouse.order.Order" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'order.label', default: 'Order').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
        <asset:stylesheet src="badge.css"/>

        <!-- Specify content to overload like global navigation links, page titles, etc. -->
        <style>
            .canceled-item { background-color: grey; }
        </style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

                <g:hiddenField id="orderId" name="orderId" value="${orderInstance?.id}"/>
                <g:render template="summary" model="[orderInstance:orderInstance,currentState:'showOrder']"/>

                <g:hasErrors bean="${orderInstance}">
                    <div class="errors" role="alert" aria-label="error-message">
                        <g:renderErrors bean="${orderInstance}" as="list" />
                    </div>
                </g:hasErrors>

                <div class="yui-gf">

                    <div class="yui-u first">
                        <div id="details" class="box">
                            <h2>
                                <g:message code="order.header.label" default="Order Header"/>
                            </h2>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderNumber.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.orderNumber}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.status.label" /></label>
                                    </td>
                                    <td valign="top" id="status" class="value">
                                        <span class="${orderInstance?.id}">${g.message(code: 'default.loading.label')}</span>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderTypeCode.label" /></label>
                                    </td>
                                    <td valign="top" id="orderTypeCode" class="value">
                                        <format:metadata obj="${orderInstance?.orderType?.name}"/>
                                    </td>
                                </tr>
                                <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="order.originCode.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${orderInstance?.origin?.organization?.code }
                                        </td>
                                    </tr>
                                </g:if>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.origin.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.origin?.name}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.destination.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.destination?.name}
                                    </td>
                                </tr>
                                <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="paymentTerm.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            <g:if test="${orderInstance?.paymentTerm}">
                                                <div>${orderInstance?.paymentTerm?.name}</div>
                                            </g:if>
                                            <g:else>
                                                <g:message code="default.none.label"/>
                                            </g:else>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="order.paymentMethodType.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            <g:if test="${orderInstance?.paymentMethodType}">
                                                <div>${orderInstance?.paymentMethodType?.name}</div>
                                            </g:if>
                                            <g:else>
                                                <g:message code="default.none.label"/>
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:if>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.subtotal.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.subtotal?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="orderAdjustments.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.totalAdjustments?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.totalPrice.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.total?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>

                                </tbody>
                            </table>
                        </div>
                        <div class="box">
                            <h2><g:message code="default.auditing.label"/></h2>
                            <table>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.orderedBy}">
                                            <div>${orderInstance?.orderedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateOrdered}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.approvedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.approvedBy}">
                                            <div>${orderInstance?.approvedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateApproved}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.completedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.completedBy}">
                                            <div>${orderInstance?.completedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateCompleted}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.createdBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.createdBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.dateCreated}"/></small>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                            <label><warehouse:message code="default.updatedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.updatedBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.lastUpdated}"/></small>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="tabs tabs-ui">
                            <ul>
                                <li>
                                    <a href="${request.contextPath}/order/orderSummary/${orderInstance.id}">
                                        <warehouse:message code="default.summary.label" default="Summary"/>
                                    </a>
                                </li>
                                <li>
                                    <a href="${request.contextPath}/order/itemStatus/${orderInstance.id}">
                                        <g:message code="order.itemStatus.message" default="Item Status"/>
                                    </a>
                                </li>
                                <li>
                                    <a href="${request.contextPath}/order/itemDetails/${orderInstance.id}">
                                        <g:message code="order.itemDetails.message" default="Item Details"/>
                                    </a>
                                </li>
                                <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                    <li>
                                    <a href="${request.contextPath}/order/orderAdjustments/${orderInstance.id}">
                                            <warehouse:message code="orderAdjustments.label"/>
                                        </a>
                                    </li>
                                    <li>
                                    <a href="${request.contextPath}/order/orderShipments/${orderInstance.id}">
                                            <warehouse:message code="shipments.label"/>
                                        </a>
                                    </li>
                                    <li>
                                    <a href="${request.contextPath}/order/orderInvoices/${orderInstance.id}">
                                            <warehouse:message code="invoices.label"/>
                                        </a>
                                    </li>
                                </g:if>
                                <li>
                                    <a href="${request.contextPath}/order/orderDocuments/${orderInstance.id}">
                                        <warehouse:message code="documents.label"/>
                                    </a>
                                </li>
                                <li
                                    data-count="${orderInstance.comments.size() < 1000 ? orderInstance.comments.size() : '999+' }"
                                    class="${orderInstance.comments.size() > 0 ? 'tab-badge' : ''}"
                                >
                                    <a href="${request.contextPath}/order/orderComments/${orderInstance.id}">
                                        <warehouse:message code="comments.label" default="Comments"/>
                                    </a>
                                </li>
                            </ul>
                            <div class="loading">Loading...</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            $(document).ready(function() {

              const orderType = ${orderInstance.orderType.isPutawayOrder()} ? 'inbound' : 'purchasing';
              applyActiveSection(orderType);

                $(".tabs").tabs({
                    cookie: {
                        expires: 1
                    },
                    ajaxOptions: {
                      error: function(xhr, status, index, anchor) {
                        // Reload the page if session has timed out
                        if (xhr.statusCode == 401) {
                          window.location.reload();
                        }
                      },
                      beforeSend: function() {
                        $('.loading').show();
                      },
                      complete: function() {
                        $(".loading").hide();
                      }
                    },
                    selected: ${params.tab ? params.tab : 0}
                });
            });

            function filterTableItems(cellsIndex, filterValue, tableRows) {
              // Loop through all table rows, and hide those who don't match the search query
              $.each(tableRows, function(index, currentRow) {
                // If filter matches text value then we display, otherwise hide

                // Make one string from cells values at given indexes
                const txtValue = cellsIndex.reduce((acc, index) => {
                  const cellValue = $(currentRow)
                    .find("td")
                    .eq(index)
                    .text();
                  return acc + cellValue
                }, '');
                // If concated string includes the filter value, show the row, otherwise hide
                if (txtValue.toUpperCase().includes(filterValue)) {
                  $(currentRow).show();
                  return;
                }
                $(currentRow).hide();
              });
            }

            function fetchOrderItemsDerivedStatus() {
              const orderId = $('#orderId').val();
              $.ajax({
                url: "${request.contextPath}/purchaseOrderApi/orderItemsDerivedStatus",
                data: "id=" + orderId,
                success: function(data, textStatus, jqXHR){
                  if (data && Object.keys(data)) {
                    Object.keys(data).forEach(
                      orderItemId => $("." + orderItemId).text(data[orderItemId])
                    );
                  }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                  console.error(jqXHR, textStatus, errorThrown);
                  if (jqXHR.responseText) {
                    $.notify(jqXHR.responseText, "error");
                  } else {
                    $.notify("An error occurred", "error");
                  }
                }
              });
            }
        </script>
    </body>
</html>
