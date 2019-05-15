<%@ page import="util.ConfigHelper" %>
<div id="consumption" class="box">


    <h2>
        <warehouse:message code="stockCard.consumption.label" default="Consumption"/>
        <a href="javascript:void(-1);" id="consumption-config-btn" class="button small"><warehouse:message code="default.configure.label" default="Configure"/></a>
    </h2>
    <table id="data">
        <thead>
            <tr class="header odd">
                <th><warehouse:message code="requisition.monthRequested.label" default="Month requested"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityRequested.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityCanceled.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityApproved.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityPicked.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>
            </tr>
        </thead>
        <tbody>

            <g:set var="itemsByMonth" value="${issuedRequisitionItems.groupBy { it.requisition.monthRequested } }"/>
            <g:set var="totalQuantityRequested" value="${0 }"/>
            <g:set var="totalQuantityApproved" value="${0 }"/>
            <g:set var="totalQuantityCanceled" value="${0}"/>
            <g:set var="totalQuantityPicked" value="${0}"/>
            <g:set var="totalQuantityIssued" value="${0}"/>
            <g:set var="numberOfMonths" value="${itemsByMonth?.keySet()?.size()?:1}"/>

            <g:each var="entry" in="${itemsByMonth}" status="i">
                <g:set var="monthlyQuantityRequested" value="${entry?.value?.collect { it?.quantity?:0 }?.sum()?:0 }"/>
                <g:set var="monthlyQuantityApproved" value="${entry?.value?.collect { it?.quantityApproved?:0 }?.sum()?:0 }"/>
                <g:set var="monthlyQuantityCanceled" value="${entry?.value?.collect { it?.quantityCanceled?:0 }?.sum()?:0 }"/>
                <g:set var="monthlyQuantityPicked" value="${entry?.value?.collect { it?.calculateQuantityPicked()?:0 }?.sum()?:0 }"/>
                <g:set var="monthlyQuantityIssued" value="${monthlyQuantityRequested - monthlyQuantityCanceled}"/>
                <g:set var="totalQuantityRequested" value="${totalQuantityRequested+monthlyQuantityRequested}"/>
                <g:set var="totalQuantityApproved" value="${totalQuantityApproved+monthlyQuantityApproved}"/>
                <g:set var="totalQuantityCanceled" value="${totalQuantityCanceled+monthlyQuantityCanceled}"/>
                <g:set var="totalQuantityPicked" value="${totalQuantityPicked+monthlyQuantityPicked}"/>
                <g:set var="totalQuantityIssued" value="${totalQuantityIssued+monthlyQuantityIssued}"/>

                <tr class="prop header ${i%2?'even':'odd'}" style="cursor: pointer">
                    <td>
                        <label>${entry.key}</label>
                    </td>
                    <td class="center middle">
                        <g:formatNumber number="${monthlyQuantityRequested}"/>
                    </td>
                    <td class="center middle">
                        <g:formatNumber number="${monthlyQuantityCanceled}"/>
                    </td>
                    <td class="center middle">
                        <div class="${monthlyQuantityApproved != monthlyQuantityIssued ? 'discrepancy': ''}" title="Approved quantity should equal Issued quantity">
                            <g:formatNumber number="${monthlyQuantityApproved}"/>
                        </div>
                    </td>
                    <td class="center middle">
                        <div class="${monthlyQuantityPicked != monthlyQuantityIssued ? 'discrepancy': ''}" title="Picked quantity should equal Issued quantity">
                            <g:formatNumber number="${monthlyQuantityPicked}"/>
                        </div>
                    </td>
                    <td class="center middle">
                        <g:formatNumber number="${monthlyQuantityIssued}"/>
                    </td>
                </tr>
                <tr class="prop data fade">
                    <td colspan="7">
                        <div class="box">
                            <h2>${entry.key}</h2>
                            <table>
                                <tr>
                                    <th><warehouse:message code="requisition.dateRequested.label"/></th>
                                    <th><warehouse:message code="requisition.requestNumber.label"/></th>
                                    <th><warehouse:message code="requisition.status.label"/></th>
                                    <th><warehouse:message code="requisition.destination.label"/></th>
                                    <th><warehouse:message code="requisitionItem.status.label"/></th>
                                    <th><warehouse:message code="requisitionItem.cancelReasonCode.label"/></th>
                                    <th class="center middle"><warehouse:message code="requisitionItem.quantityRequested.label"/></th>
                                    <th class="center middle"><warehouse:message code="requisitionItem.quantityCanceled.label"/></th>
                                    <th class="center middle"><warehouse:message code="requisitionItem.quantityApproved.label"/></th>
                                    <th class="center middle"><warehouse:message code="requisitionItem.quantityPicked.label"/></th>
                                    <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>

                                </tr>


                                <g:set var="innerQuantityRequested" value="${0}"/>
                                <g:set var="innerQuantityApproved" value="${0}"/>
                                <g:set var="innerQuantityCanceled" value="${0}"/>
                                <g:set var="innerQuantityPicked" value="${0}"/>
                                <g:set var="innerQuantityIssued" value="${0}"/>
                                <g:each var="requisitionItem" in="${entry.value.sort { it.requisition.dateRequested }}" status="j">

                                    <g:set var="quantityRequested" value="${requisitionItem?.quantity?:0}"/>
                                    <g:set var="quantityApproved" value="${requisitionItem?.quantityApproved?:0}"/>
                                    <g:set var="quantityCanceled" value="${requisitionItem?.quantityCanceled?:0}"/>
                                    <g:set var="quantityPicked" value="${requisitionItem?.calculateQuantityPicked()?:0}"/>
                                    <g:set var="quantityIssued" value="${quantityRequested - quantityCanceled}"/>

                                    <g:set var="innerQuantityRequested" value="${innerQuantityRequested + quantityRequested}"/>
                                    <g:set var="innerQuantityApproved" value="${innerQuantityApproved + quantityApproved}"/>
                                    <g:set var="innerQuantityCanceled" value="${innerQuantityCanceled + quantityCanceled}"/>
                                    <g:set var="innerQuantityPicked" value="${innerQuantityPicked + quantityPicked}"/>
                                    <g:set var="innerQuantityIssued" value="${innerQuantityIssued + quantityIssued}"/>


                                    <tr class="prop ${j%2?'odd':'even'}">
                                        <td>
                                            <g:formatDate date="${requisitionItem.requisition.dateRequested}" format="MMM dd"/>
                                        </td>
                                        <td>
                                            <g:link controller="requisition" action="show" id="${requisitionItem?.requisition?.id}">
                                                ${requisitionItem.requisition.requestNumber}
                                            </g:link>
                                        </td>
                                        <td>
                                            ${requisitionItem.requisition.status}
                                        </td>
                                        <td>
                                            ${requisitionItem.requisition.destination}
                                        </td>
                                        <td>
                                            ${requisitionItem.status}
                                        </td>
                                        <td>
                                            ${requisitionItem.cancelReasonCode}
                                        </td>
                                        <td class="center middle">
                                            ${quantityRequested}
                                        </td>
                                        <td class="center middle">
                                            ${quantityCanceled}
                                        </td>
                                        <td class="center middle">
                                            ${quantityApproved}
                                        </td>
                                        <td class="center middle">
                                            ${quantityPicked}
                                        </td>
                                        <td class="center middle">
                                            ${quantityIssued}
                                        </td>
                                    </tr>
                                </g:each>
                                <tfoot>
                                    <tr>
                                        <td colspan="6">

                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${innerQuantityRequested}" maxFractionDigits="0"/>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${innerQuantityCanceled}" maxFractionDigits="0"/>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${innerQuantityApproved}" maxFractionDigits="0"/>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${innerQuantityPicked}" maxFractionDigits="0"/>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${innerQuantityIssued}" maxFractionDigits="0"/>
                                        </td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>



                    </td>

                </tr>
                <%--
                    <g:each var="requisitionItem" in="${entry.value}" status="j">
                        <tr class="prop ${j%2?'odd':'even'}">
                            <td>
                                ${requisitionItem.requisition.requestNumber} -
                                ${requisitionItem.requisition.status}
                            </td>
                            <td>
                                ${requisitionItem.status}
                            </td>
                            <td>
                                ${requisitionItem.cancelReasonCode}
                            </td>
                            <td class="center middle">
                                ${requisitionItem.quantity}
                            </td>
                            <td class="center middle">
                                ${requisitionItem.quantityCanceled}
                            </td>
                            <td class="center middle">
                                ${(requisitionItem.quantity?:0) - (requisitionItem.quantityCanceled?:0)}
                            </td>
                        </tr>
                    </g:each>
                    --%>
            </g:each>
        </tbody>

        <tfoot>
            <tr>
                <td><warehouse:message code="default.total.label" default="Total"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityRequested}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityCanceled}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityApproved}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityPicked}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityIssued}" maxFractionDigits="0"/></td>
            </tr>
            <tr>
                <td><warehouse:message code="default.monthly.label" default="Monthly"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityRequested/numberOfDays*30}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityCanceled/numberOfDays*30}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityApproved/numberOfDays*30}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityPicked/numberOfDays*30}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityIssued/numberOfDays*30}" maxFractionDigits="0"/></td>
            </tr>
            <tr>
                <td><warehouse:message code="default.daily.label" default="Daily"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityRequested/numberOfDays}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityCanceled/numberOfDays}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityApproved/numberOfDays}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityPicked/numberOfDays}" maxFractionDigits="0"/></td>
                <td class="center"><g:formatNumber number="${totalQuantityIssued/numberOfDays}" maxFractionDigits="0"/></td>
            </tr>
        </tfoot>
    </table>
</div>
<script type="text/javascript">
$(function () {
    $("#data tr.data").hide();
    $('#data tr.header').click(function(){
        $(this).next('tr.data').slideToggle(100);
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
        //event.preventDefault();
        $("#consumption-config-dialog").dialog('close');

    });

});
</script>
<div id="consumption-config-dialog" title="${warehouse.message(code:'consumption.configuration.label', default:'Configuration')}">
    <g:formRemote name="consumption" url="[controller: 'inventoryItem', action: 'showConsumption', params: [id: commandInstance?.product?.id]]" update="consumption">

        <table>
            <tr class="prop">
                <td class="name">
                    <warehouse:message code="reasonCode.label" default="Reason code"/>
                </td>
                <td class="value">
                    <g:set var="defaultReasonCodes" value="${ConfigHelper.listValue(grailsApplication.config.openboxes.stockCard.consumption.reasonCodes)}"/>

                    <ul>
                        <li><g:checkBox id="reasonCode-ALL" name="reasonCode" value="ALL"/> <label for="reasonCode-ALL">All reason codes</label></li>
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