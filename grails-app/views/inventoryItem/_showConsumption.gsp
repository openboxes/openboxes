<%@ page import="util.ConfigHelper" %>
<div id="consumption">
    <div class="message information">
        <div class="right">
            <a href="javascript:void(-1);" id="consumption-config-btn" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'cog_edit.png')}" />&nbsp;
                <warehouse:message code="default.configure.label" default="Configure"/>
            </a>
        </div>
        Totals and monthly averages are for the last 6 months only (excluding the current month and
        any month before the oldest request). Click the Configure button to change the
        <span title="${monthKeys}">dates</span> and <span title="${reasonCodes}">reason codes</span>
        considered in the totals.
    </div>
    <div class="box">
        <h2>
            <warehouse:message code="stockCard.consumption.label" default="Consumption"/>
        </h2>
        <table id="data">
            <thead>
                <tr class="header odd">
                    <th><warehouse:message code="requisition.monthRequested.label" default="Month requested"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityRequested.label"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityApproved.label"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityPicked.label"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>
                </tr>
            </thead>
            <tbody>

                <g:set var="itemsByMonth" value="${requisitionItems.groupBy { it.monthRequested } }"/>
                <g:set var="totalQuantityRequested" value="${0 }"/>
                <g:set var="totalQuantityApproved" value="${0 }"/>
                <g:set var="totalQuantityPicked" value="${0}"/>
                <g:set var="totalQuantityIssued" value="${0}"/>

                <g:each var="monthKey" in="${monthKeys}" status="i">
                    <g:set var="entry" value="${itemsByMonth.find { it.key == monthKey}}"/>
                    <g:if test="${entry}">
                        <g:set var="monthlyQuantityRequested" value="${entry?.value?.collect { it?.quantityRequested?:0 }?.sum()?:0 }"/>
                        <g:set var="monthlyQuantityApproved" value="${entry?.value?.collect { it?.quantityApproved?:0 }?.sum()?:0 }"/>
                        <g:set var="monthlyQuantityPicked" value="${entry?.value?.collect { it?.quantityPicked?:0 }?.sum()?:0 }"/>
                        <g:set var="monthlyQuantityIssued" value="${entry?.value?.collect { it?.quantityIssued?:0 }?.sum()?:0 }"/>
                        <g:set var="totalQuantityRequested" value="${totalQuantityRequested+monthlyQuantityRequested}"/>
                        <g:set var="totalQuantityApproved" value="${totalQuantityApproved+monthlyQuantityApproved}"/>
                        <g:set var="totalQuantityPicked" value="${totalQuantityPicked+monthlyQuantityPicked}"/>
                        <g:set var="totalQuantityIssued" value="${totalQuantityIssued+monthlyQuantityIssued}"/>

                        <tr class="prop header ${i%2?'even':'odd'}" style="cursor: pointer">
                            <td>
                                ${monthKey}
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${monthlyQuantityRequested}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                    <g:formatNumber number="${monthlyQuantityApproved}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                <div class="${monthlyQuantityPicked < monthlyQuantityApproved ? 'discrepancy': ''}" title="Picked should not be less than Approved">
                                    <g:formatNumber number="${monthlyQuantityPicked}" maxFractionDigits="0"/>
                                </div>
                            </td>
                            <td class="center middle">
                                <div class="${monthlyQuantityIssued < monthlyQuantityPicked ? 'discrepancy': ''}" title="Issued should not be less than Picked">
                                    <g:formatNumber number="${monthlyQuantityIssued}" maxFractionDigits="0"/>
                                </div>
                            </td>
                        </tr>
                        <tr class="prop data fade">
                            <td colspan="5">
                                <div class="box">
                                    <table>
                                        <tr>
                                            <th><warehouse:message code="requisition.requestNumber.label"/></th>
                                            <th><warehouse:message code="requisition.status.label"/></th>
                                            <th><warehouse:message code="requisition.dateRequested.label"/></th>
                                            <th><warehouse:message code="requisition.dateIssued.label"/></th>
                                            <th><warehouse:message code="requisition.destination.label"/></th>
                                            <th><warehouse:message code="requisitionItem.status.label"/></th>
                                            <th><warehouse:message code="requisitionItem.cancelReasonCode.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityRequested.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityApproved.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityPicked.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>
                                        </tr>
                                        <g:set var="innerQuantityRequested" value="${0}"/>
                                        <g:set var="innerQuantityApproved" value="${0}"/>
                                        <g:set var="innerQuantityPicked" value="${0}"/>
                                        <g:set var="innerQuantityIssued" value="${0}"/>
                                        <g:each var="requisitionItem" in="${entry.value.sort { it?.dateRequested }}" status="j">
                                            <g:set var="quantityRequested" value="${requisitionItem?.quantityRequested?:0}"/>
                                            <g:set var="quantityApproved" value="${requisitionItem?.quantityApproved?:0}"/>
                                            <g:set var="quantityPicked" value="${requisitionItem?.quantityPicked?:0}"/>
                                            <g:set var="quantityIssued" value="${requisitionItem?.quantityIssued?:0}"/>
                                            <g:set var="innerQuantityRequested" value="${innerQuantityRequested + quantityRequested}"/>
                                            <g:set var="innerQuantityApproved" value="${innerQuantityApproved + quantityApproved}"/>
                                            <g:set var="innerQuantityPicked" value="${innerQuantityPicked + quantityPicked}"/>
                                            <g:set var="innerQuantityIssued" value="${innerQuantityIssued + quantityIssued}"/>


                                            <tr class="prop ${j%2?'odd':'even'}">
                                                <td>
                                                    <g:link controller="requisition" action="show" id="${requisitionItem?.requisitionId}">
                                                        ${requisitionItem?.requestNumber}
                                                    </g:link>
                                                </td>
                                                <td>
                                                    ${requisitionItem?.requestStatus}
                                                </td>
                                                <td>
                                                    <g:formatDate date="${requisitionItem?.dateRequested}" format="MMM dd"/>
                                                </td>
                                                <td>
                                                    <g:formatDate date="${requisitionItem?.dateIssued}" format="MMM dd"/>
                                                </td>
                                                <td>
                                                    ${requisitionItem?.destination}
                                                </td>
                                                <td>
                                                    ${requisitionItem?.status}
                                                </td>
                                                <td>
                                                    ${requisitionItem?.reasonCode}
                                                </td>
                                                <td class="center middle">
                                                    <g:formatNumber number="${quantityRequested}" maxFractionDigits="0"/>
                                                </td>
                                                <td class="center middle">
                                                        <g:formatNumber number="${quantityApproved}" maxFractionDigits="0"/>
                                                </td>
                                                <td class="center middle">
                                                    <div class="${quantityPicked < quantityApproved ? 'discrepancy': ''}" title="Picked should not be less than approved">
                                                        <g:formatNumber number="${quantityPicked}" maxFractionDigits="0"/>
                                                    </div>
                                                </td>
                                                <td class="center middle">
                                                    <div class="${quantityIssued < quantityPicked ? 'discrepancy': ''}" title="Issued should not be less than picked">
                                                        <g:formatNumber number="${quantityIssued}" maxFractionDigits="0"/>
                                                    </div>
                                                </td>
                                            </tr>
                                        </g:each>
                                        <tfoot>
                                            <tr class="prop">
                                                <th colspan="7">
                                                    ${g.message(code:'default.total.label')}
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityRequested}" maxFractionDigits="0"/>
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityApproved}" maxFractionDigits="0"/>
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityPicked}" maxFractionDigits="0"/>
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityIssued}" maxFractionDigits="0"/>
                                                </th>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </td>
                        </tr>
                    </g:if>
                    <g:else>
                        <tr class="prop ${i%2?'even':'odd'}" style="cursor: pointer">
                            <td>
                                ${monthKey}
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${0}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${0}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${0}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${0}" maxFractionDigits="0"/>
                            </td>
                        </tr>
                    </g:else>
                </g:each>
                <tr class="prop">
                    <td><warehouse:message code="default.monthly.label" default="Monthly"/></td>
                    <td class="center"><g:formatNumber number="${totalQuantityRequested/numberOfMonths}" maxFractionDigits="0"/></td>
                    <td class="center"><g:formatNumber number="${totalQuantityApproved/numberOfMonths}" maxFractionDigits="0"/></td>
                    <td class="center"><g:formatNumber number="${totalQuantityPicked/numberOfMonths}" maxFractionDigits="0"/></td>
                    <td class="center"><g:formatNumber number="${totalQuantityIssued/numberOfMonths}" maxFractionDigits="0"/></td>
                </tr>
            </tbody>

            <tfoot>
                <tr>
                    <th><warehouse:message code="default.total.label" default="Total"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityRequested}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityApproved}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityPicked}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityIssued}" maxFractionDigits="0"/></th>
                </tr>
            </tfoot>
        </table>
    </div>
</div>
<script type="text/javascript">

function showLoading() {
    $('.loading').show();
}

function hideLoading() {
    $('.loading').hide();
}

$(function () {
    $("#data tr.data").hide();
    $('#data tr.header').click(function(){
        $(this).next('tr.data').toggle(100);
    });

    $("#consumption-config-dialog").dialog({
        autoOpen: false,
        modal: true,
        width: '800px',
        top: 10
    });

    $("#reasonCode-ALL").click(function() {
        var checked = ($(this).attr("checked") == 'checked');
        $("input[type='checkbox']").attr("checked", checked);
    });

    $('#consumption-config-btn').click(function(){
        $("#consumption-config-dialog").dialog('open');
    });

    $("#refresh-btn").click(function(ui, event) {
        $("#consumption-config-dialog").dialog('close');

    });

});
</script>
<div id="consumption-config-dialog" title="${warehouse.message(code:'consumption.configuration.label', default:'Configuration')}">
    <g:formRemote name="consumption" onLoading="showLoading()" onComplete="hideLoading()"
                  url="[controller: 'inventoryItem', action: 'showConsumption', params: [id: commandInstance?.product?.id]]"
                  update="consumption">

        <table>
            <tr class="prop">
                <td class="name">
                    <warehouse:message code="reasonCode.label" default="Reason Code"/>
                </td>
                <td class="value">
                    <g:set var="defaultReasonCodes" value="${ConfigHelper.listValue(grailsApplication.config.openboxes.stockCard.consumption.reasonCodes)}"/>
                    <ul>
                        <li><g:checkBox id="reasonCode-ALL" name="reasonCode" value="ALL" checked="${false}"/> <label for="reasonCode-ALL">All reason codes</label></li>
                        <g:each var="reasonCode" in="${org.pih.warehouse.core.ReasonCode.list()}">
                            <li>
                                <g:checkBox id="reasonCode-${reasonCode}" name="reasonCode" value="${reasonCode}" checked="${defaultReasonCodes.contains(reasonCode)}"/>
                                <label for="reasonCode-${reasonCode}" title="${reasonCode}">
                                    <warehouse:message code="enum.ReasonCode.${reasonCode}"/>
                                </label>

                            </li>
                        </g:each>
                    </ul>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <warehouse:message code="consumption.startDate.label"/>
                </td>
                <td class="value">
                    <g:jqueryDatePicker id="startDate" name="startDate" value="${params.startDate}" class="text middle" size="15"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <warehouse:message code="consumption.endDate.label"/>
                </td>
                <td class="value">
                    <g:jqueryDatePicker id="endDate" name="endDate" value="${params.endDate}" class="text middle" size="15"/>

                </td>
            </tr>
            <tr>
                <td class="name">

                </td>
                <td class="value">
                    <g:submitButton id="refresh-btn" name="Refresh data"></g:submitButton>
                </td>
            </tr>
        </table>
    </g:formRemote>
</div>
