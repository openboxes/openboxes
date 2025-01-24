<%@ page import="org.pih.warehouse.core.ActivityCode" %>
<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.core.DocumentCode" %>
<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>


<div id="order-summary" class="summary">
	<g:if test="${orderInstance?.id}">
		<g:set var="isAddingComment" value="${request.request.requestURL.toString().contains('addComment')}"/>
		<g:set var="isAddingDocument" value="${request.request.requestURL.toString().contains('addDocument')}"/>
        %{-- For fetching derived statuses (preparing the list of order ids to be sent with request) --}%
		<table width="50%">
			<tbody>
				<tr class="odd">
                    <td width="1%">
                        <g:render template="/order/actions" model="[orderInstance:orderInstance]"/>
                    </td>
					<td>
                        <div class="title">
                            <small class="font-weight-bold">${orderInstance?.orderNumber}</small>
                            <g:link controller="order" action="show" id="${orderInstance?.id}">
                                ${orderInstance?.name}</g:link>
						</div>
                    </td>
                    <td class="top right" width="1%">
                        <div class="tag tag-alert">
                            <span class="${orderInstance?.id}">${g.message(code: 'default.loading.label')}</span>
                        </div>
                    </td>
                </tr>
			</tbody>
		</table>
	</g:if>
	<g:else>
		<table>
			<tbody>
				<tr>
					<td>
						<div class="title">
                            <g:if test="${orderInstance?.name}">
                                ${orderInstance?.name }
                            </g:if>
                            <g:else>
                                <warehouse:message code="order.untitled.label"/>
                            </g:else>
						</div>
					</td>
					<td width="1%" class="right">
                        <div class="tag tag-alert">
                            <warehouse:message code="default.new.label"/>
                        </div>
					</td>
				</tr>
			</tbody>
		</table>

	</g:else>
</div>
<div class="buttonBar">
    <div class="button-container">
        <g:if test="${!orderInstance?.id}">
            <g:link controller="order" action="create" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.create.label" args="[g.message(code: 'order.label')]" default="Create purchase order" />
            </g:link>
        </g:if>
        <g:if test="${orderInstance?.id}">
            <g:hasRoleApprover>
                <g:set var="isApprover" value="${true}"/>
            </g:hasRoleApprover>
            <g:if test="${!isApprover}">
                <g:set var="disabledMessage" value="${g.message(code:'errors.noPermissions.label')}"/>
            </g:if>
            <g:elseif test="${orderInstance?.shipments}">
                <g:set var="disabledMessage" value="${g.message(code:'order.errors.rollback.message')}"/>
            </g:elseif>
            <g:hasRoleInvoice>
                <g:set var="hasRoleInvoice" value="${true}"/>
            </g:hasRoleInvoice>
            <g:if test="${!hasRoleInvoice}">
                <g:set var="disabledInvoiceMessage" value="${g.message(code:'errors.noPermissions.label')}"/>
            </g:if>
            <g:if test="${orderInstance?.orderType == OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
                <g:link controller="order" action="list" class="button" params="[orderType: Constants.PUTAWAY_ORDER]">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[g.message(code: 'orders.label')]" default="List orders"/>
                </g:link>
                <g:link controller="order" action="create" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.create.label" args="[g.message(code: 'order.label')]" default="Create order" />
                </g:link>
                <div class="button-group right">
                    <g:link controller="order" action="addComment" id="${orderInstance?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />&nbsp;
                        <warehouse:message code="order.addComment.label" default="Add comment"/>
                    </g:link>
                    <g:link controller="order" action="addDocument" id="${orderInstance?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
                        <warehouse:message code="order.addDocument.label" default="Add document"/>
                    </g:link>
                    <g:link controller="putAway" action="generatePdf" id="${orderInstance?.id}" class="button" target="_blank">
                        <img src="${resource(dir: 'images/icons', file: 'pdf.png')}" />&nbsp;
                        <warehouse:message code="putaway.generatePutawayList.label" default="Generate Putaway List"/>
                    </g:link>
                </div>
                <div class="button-group right">
                    <g:link controller="order" action="show" id="${orderInstance?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
                        <warehouse:message code="order.wizard.showOrder.label" default="Show Order"/>
                    </g:link>
                    <g:set var="disabled" value="${orderInstance?.status in [OrderStatus.COMPLETED, OrderStatus.CANCELED]}"/>
                    <g:link controller="putAway" action="create" id="${orderInstance?.id}" class="button" disabled="${disabled}" disabledMessage="This feature is not available for completed and canceled putaways">
                        <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                        <warehouse:message code="default.edit.label" args="[warehouse.message(code:'putawayOrder.label')]"/>
                    </g:link>
                </div>
            </g:if>
            <g:elseif test="${currentState}">
                <%-- Show Order --%>
                <g:if test="${currentState == 'showOrder'}">
                    <g:link controller="purchaseOrder" action="list" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                        <warehouse:message code="default.list.label" args="[g.message(code: 'orders.label')]" default="List purchase order"/>
                    </g:link>
                    <g:if test="${orderInstance?.status == OrderStatus.PENDING}">
                        <g:supports activityCode="${ActivityCode.PLACE_ORDER}">
                            <div class="button-group">
                                <g:link controller="purchaseOrder" action="addItems" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                                    <warehouse:message code="order.wizard.editOrder.label" default="Edit"/>
                                </g:link>
                                <g:link controller="order" action="placeOrder" id="${orderInstance?.id}" class="button"
                                        disabled="${orderInstance?.status >= OrderStatus.PLACED}"
                                        disabledMessage="Order has already been placed">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />&nbsp;
                                    ${warehouse.message(code: 'order.wizard.placeOrder.label')}
                                </g:link>
                            </div>
                            <div class="button-group">
                                <g:link controller="order" action="addComment" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />&nbsp;
                                    <warehouse:message code="order.addComment.label" default="Add comment"/>
                                </g:link>
                                <g:link controller="order" action="addDocument" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
                                    <warehouse:message code="order.addDocument.label" default="Add document"/>
                                </g:link>
                            </div>
                        </g:supports>
                        <div class="button-group right">
                            <g:link controller="order" action="print" id="${orderInstance?.id}" class="button"
                                    disabled="${orderInstance?.status < org.pih.warehouse.order.OrderStatus.PLACED}"
                                    disabledMessage="Order must be placed in order to print.">
                                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                                <warehouse:message code="order.wizard.printOrder.label" default="Print Order"/>
                            </g:link>
                            <g:link controller="order" action="download" id="${orderInstance?.id}" class="button" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <warehouse:message code="order.wizard.downloadOrder.label" default="Download Order"/>
                            </g:link>
                            <g:link controller="order" action="downloadOrderItems" id="${orderInstance?.id}" class="button" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <warehouse:message code="default.exportItems.label" default="Export items"/>
                            </g:link>
                        </div>
                    </g:if>
                    <g:elseif test="${orderInstance?.status > OrderStatus.PENDING}">
                        <div class="button-group">
                            <g:supports activityCode="${ActivityCode.PLACE_ORDER}">
                                <g:link controller="purchaseOrder" action="addItems" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                                    <warehouse:message code="order.wizard.editOrder.label" default="Edit"/>
                                </g:link>
                                <g:link controller="order" action="rollbackOrderStatus" id="${orderInstance?.id}" class="button"
                                        disabled="${orderInstance?.shipments || !isApprover}"
                                        disabledMessage="${disabledMessage}">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />&nbsp;
                                    ${warehouse.message(code: 'default.button.rollback.label')}
                                </g:link>
                            </g:supports>
                            <g:if test="${orderInstance?.isPrepaymentInvoiceAllowed}">
                                <g:link controller="invoice" action="generatePrepaymentInvoice" id="${orderInstance?.id}" class="button"
                                    disabled="${!hasRoleInvoice}"
                                    disabledMessage="${disabledInvoiceMessage}">
                                    <img src="${resource(dir: 'images/icons', file: 'document.png')}" />&nbsp;
                                    ${warehouse.message(code: 'default.button.generatePrepayment.label', default: "Generate Prepayment Invoice")}
                                </g:link>
                            </g:if>
                            <g:if test="${orderInstance?.canGenerateInvoice}">
                                <button id="generate-invoice" class="button">
                                    <img src="${resource(dir: 'images/icons', file: 'document.png')}" />&nbsp;
                                    ${warehouse.message(code: 'default.button.generateInvoice.label', default: "Generate Invoice")}
                                </button>
                            </g:if>
                            <g:link controller="order" action="createCombinedShipment" params="[direction:'INBOUND', orderId: orderInstance?.id]" class="button"
                                    disabled="${orderInstance?.status < OrderStatus.PLACED}"
                                    disabledMessage="Order must be placed in order to ship">
                                <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                                <g:message code="order.shipOrder.label" default="Ship Order"/>
                            </g:link>
                        </div>
                        <g:supports activityCode="${ActivityCode.PLACE_ORDER}">
                            <div class="button-group">
                                <g:link controller="order" action="addComment" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />&nbsp;
                                    <g:message code="order.addComment.label" default="Add comment"/>
                                </g:link>
                                <g:link controller="order" action="addDocument" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
                                    <g:message code="order.addDocument.label" default="Add document"/>
                                </g:link>
                            </div>
                        </g:supports>
                        <div class="button-group right">
                            <g:link controller="order" action="print" id="${orderInstance?.id}" class="button" target="_blank"
                                    disabled="${orderInstance?.status < org.pih.warehouse.order.OrderStatus.PLACED}"
                                    disabledMessage="Order must be placed in order to print.">
                                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                                <warehouse:message code="order.wizard.printOrder.label" default="Print Order"/>
                            </g:link>

                            <span class="action-menu">
                                <button class="action-btn button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'folder.png')}" />
                                    &nbsp; <g:message code="default.button.download.label"/>
                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                                </button>
                                <div class="actions">
                                    <g:each var="document" in="${stockMovement?.documents}">
                                        <g:if test="${!document.hidden}">
                                            <div class="action-menu-item">
                                                <g:link url="${document.uri}" target="_blank">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'page.png')}" class="middle"/>&nbsp;
                                                    ${document.name}
                                                </g:link>
                                            </div>
                                        </g:if>
                                    </g:each>
                                    <g:each var="documentTemplate" in="${org.pih.warehouse.core.Document.findAllByDocumentCode(org.pih.warehouse.core.DocumentCode.PURCHASE_ORDER_TEMPLATE)}">
                                        <div class="action-menu-item">
                                            <g:link controller="order" action="render" id="${orderInstance?.id}" target="_blank"
                                                params="['documentTemplate.id': documentTemplate.id]"
                                                disabled="${orderInstance?.status < org.pih.warehouse.order.OrderStatus.PLACED}"
                                                disabledMessage="Order must be placed in order to print.">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'page_word.png')}" />&nbsp;
                                                <warehouse:message code="default.download.label" default="Download" args="[documentTemplate?.name]"/>
                                            </g:link>
                                        </div>
                                    </g:each>
                                    <div class="action-menu-item">
                                        <g:link controller="order" action="download" id="${orderInstance?.id}" target="_blank" >
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                            <warehouse:message code="order.wizard.downloadOrder.label" default="Download Order"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="order" action="downloadOrderItems" id="${orderInstance?.id}" target="_blank">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                            <warehouse:message code="default.exportItems.label" default="Export items"/>
                                        </g:link>
                                    </div>
                                </div>
                            </span>



                        </div>
                    </g:elseif>
                </g:if>
                <g:elseif test="${currentState == 'addItems' || currentState == 'editOrder'}">
                    <g:if test="${orderInstance?.status == OrderStatus.PENDING}">
                        <div class="button-group">
                            <g:link controller="order" action="show" id="${orderInstance?.id}" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
                                <warehouse:message code="default.button.saveAndExit.label" default="Save and Exit"/>
                            </g:link>
                            <g:link controller="order" action="placeOrder" id="${orderInstance?.id}" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />&nbsp;
                                ${warehouse.message(code: 'order.wizard.placeOrder.label')}
                            </g:link>
                        </div>
                        <div class="button-group right">
                            <g:link controller="order" action="print" id="${orderInstance?.id}" class="button" target="_blank"
                                    disabled="${orderInstance?.status < org.pih.warehouse.order.OrderStatus.PLACED}"
                                    disabledMessage="Order must be placed in order to print.">
                                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                                <warehouse:message code="order.wizard.printOrder.label" default="Print Order"/>
                            </g:link>
                            <g:link controller="order" action="download" id="${orderInstance?.id}" class="button" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <g:message code="order.wizard.downloadOrder.label" default="Download Order"/>
                            </g:link>
                            <g:link controller="order" action="downloadOrderItems" id="${orderInstance?.id}" class="button" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <warehouse:message code="default.exportItems.label" default="Export items"/>
                            </g:link>
                        </div>
                    </g:if>
                    <g:elseif test="${orderInstance?.status > OrderStatus.PENDING}">
                        <div class="button-group">
                            <g:link controller="order" action="show" id="${orderInstance?.id}" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
                                <warehouse:message code="default.button.saveAndExit.label" default="Save and Exit"/>
                            </g:link>
                            <g:link controller="order" action="createCombinedShipment" params="[direction:'INBOUND', orderId: orderInstance?.id]" class="button"
                                    disabled="${orderInstance?.status < OrderStatus.PLACED}"
                                    disabledMessage="Order must be placed in order to ship">
                                <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                                <warehouse:message code="order.shipOrder.label" default="Ship Order"/>
                            </g:link>
                        </div>
                        <div class="button-group right">
                            <g:link controller="order" action="print" id="${orderInstance?.id}" class="button" target="_blank"
                                    disabled="${orderInstance?.status < org.pih.warehouse.order.OrderStatus.PLACED}"
                                    disabledMessage="Order must be placed in order to print.">
                                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                                <warehouse:message code="order.wizard.printOrder.label" default="Print Order"/>
                            </g:link>
                            <g:link controller="order" action="download" id="${orderInstance?.id}" class="button" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <warehouse:message code="order.wizard.downloadOrder.label" default="Download Order"/>
                            </g:link>
                            <g:link controller="order" action="downloadOrderItems" id="${orderInstance?.id}" class="button" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <warehouse:message code="default.exportItems.label" default="Export items"/>
                            </g:link>
                        </div>
                    </g:elseif>
                </g:elseif>
                <g:elseif test="${currentState == 'shipOrder'}">
                    <div class="button-group">
                        <g:link controller="order" action="list" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                            <warehouse:message code="default.list.label" args="[g.message(code: 'orders.label')]" default="List purchase order"/>
                        </g:link>
                        <g:link controller="order" action="show" id="${orderInstance?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
                            <warehouse:message code="order.wizard.showOrder.label" default="Show Order"/>
                        </g:link>
                        <g:link controller="purchaseOrder" action="addItems" id="${orderInstance?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                            <warehouse:message code="order.wizard.editOrder.label" default="Edit"/>
                        </g:link>
                    </div>
                </g:elseif>
            </g:elseif>
        </g:if>
    </div>
    <script>
      $(document).ready(function() {
        setTimeout(fetchOrderDerivedStatus, ${grailsApplication.config.openboxes.purchaseOrder.derivedStatusFetch.delay});

        $('#generate-invoice').on('click', function() {
          if (${!hasRoleInvoice}) {
            alert("${g.message(code:'errors.noPermissions.label')}")
            return
          }

          if (${!orderInstance.fullyInvoiceable}) {
            const dialogMessage = '${warehouse.message(code: 'invoice.partialInvoice.message', default: 'Not all lines on this purchase order are available to invoice. Generate partial invoice?')}';
            if (!confirm(dialogMessage)) {
              return
            }
          }

          window.location="${createLink(controller: 'invoice', action: 'generateInvoice', id: orderInstance?.id)}"
        });
      });

      function fetchOrderDerivedStatus() {
        const orderId = $('#orderId').val();
        if (orderId) {
          $.ajax({
            url: "${request.contextPath}/json/getOrdersDerivedStatus",
            data: "order.id=" + orderId,
            success: function(data, textStatus, jqXHR) {
              $("." + orderId).text(data[orderId]);
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
      }
    </script>
</div>
