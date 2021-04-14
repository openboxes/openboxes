<div id="tabs-1">
    <div class="box">
        <h2><g:message code="data.dataQuality.label" default="Data Quality"/></h2>
        <table>
            <thead>
            <tr>
                <th>Indicator</th>
                <th>Count</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr class="prop">
                <td class="name">
                    Receipts that have been received but don't have an inbound
                    transaction associated with them
                </td>
                <td class="value">
                    <div class="load-indicator"
                         data-url="${request.contextPath}/migration/receiptsWithoutTransaction?format=count">
                        <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                    </div>
                </td>
                <td>
                    <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="true"
                       data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                       data-url="${request.contextPath}/migration/receiptsWithoutTransaction">
                        <g:message code="default.button.list.label"/>
                    </a>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    Shipments with status shipped but don't have an outbound
                    transaction associated with them
                </td>
                <td class="value">
                    <div class="load-indicator"
                         data-url="${request.contextPath}/migration/shipmentsWithoutTransactions?format=count">
                        <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                    </div>
                </td>
                <td>
                    <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="true"
                       data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                       data-url="${request.contextPath}/migration/shipmentsWithoutTransactions">
                        <g:message code="default.button.list.label"/>
                    </a>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    Stock movements with status issued without shipment items
                </td>
                <td class="value">
                    <div class="load-indicator"
                         data-url="${request.contextPath}/migration/stockMovementsWithoutShipmentItems?format=count">
                        <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                    </div>
                </td>
                <td>
                    <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="true"
                        data-height="300"
                       data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                       data-url="${request.contextPath}/migration/stockMovementsWithoutShipmentItems">
                        <g:message code="default.button.list.label"/>
                    </a>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<g:javascript>
    $(document).ready(function() {
        $(".load-indicator").each(function(index, object){
          var url = $(this).data("url")
          $(this).load(url);
        });
    });
</g:javascript>
