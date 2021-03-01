<%@ page import="util.ConfigHelper" %>

<style>
    #startDate-datePicker, #endDate-datePicker {
        min-width: 100px;
        width: auto;
        height: 28px;
    }
    #destination_id_chosen {
        min-width: 200px;
        max-width: 200px;
    }

    .filters-container {
        display: flex;
        flex-wrap: wrap;
        justify-content: flex-start;
        align-items: center;
        margin: 10px 0;
    }
</style>

<div id="demand-table">
    <div class="message information">
        Totals and monthly averages are for the time period specified. The default time period is 12 months,
        excluding the current month and any month before the oldest request.
    </div>
    <g:formRemote name="demand" onLoading="showLoading()" onComplete="hideLoading()"
                  url="[controller: 'inventoryItem', action: 'showDemand', params: [id: commandInstance?.product?.id, from: 0]]"
                  update="demand-table" style="margin: 5px;">
        <div class="date-picker-width filters-container">
            <label class="name"><warehouse:message code="consumption.startDate.label"/></label>
            <g:jqueryDatePicker id="startDate" name="startDate" value="${params.startDate}" class="text middle" size="12" format="dd/MMM/yyyy" />

            <label class="name" style="margin-left: 10px;"><warehouse:message code="consumption.endDate.label"/></label>
            <g:jqueryDatePicker id="endDate" name="endDate" value="${params.endDate}" class="text middle" size="12" format="dd/MMM/yyyy" />

            <label for="destination.id" style="margin-left: 10px;"><warehouse:message code="requisition.destination.label" default="Destination Location" /></label>
            <g:selectLocation name="destination.id" value="${params.destination?.id ?: null}"
                              class="chzn-select-deselect location-picker-width" noSelection="['null':'All']"
                              from="${destinations}" />

            <g:submitButton id="refresh-btn" name="Refresh data" value="Refresh data" class="button" style="margin-left: 10px;" />
        </div>
    </g:formRemote>
    <div class="box">
        <h2>
            <warehouse:message code="forecasting.demand.label" default="Demand"/>
        </h2>
        <table id="data">
            <thead>
                <tr class="header odd">
                    <th><warehouse:message code="requisition.monthIssued.label" default="Month issued"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityRequested.label"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>
                    <th class="center middle"><warehouse:message code="requisitionItem.quantityDemand.label" default="Demand"/></th>
                </tr>
            </thead>
            <tbody>

                <g:set var="itemsByMonth" value="${requisitionItems.groupBy { it.monthIssued } }"/>
                <g:set var="totalQuantityRequested" value="${0 }"/>
                <g:set var="totalQuantityIssued" value="${0}"/>
                <g:set var="totalQuantityDemand" value="${0}"/>

                <g:each var="monthKey" in="${monthKeys}" status="i">
                    <g:set var="entry" value="${itemsByMonth.find { it.key == monthKey}}"/>
                    <g:if test="${entry}">
                        <g:set var="monthlyQuantityRequested" value="${entry?.value?.collect { it?.quantityRequested?:0 }?.sum()?:0 }"/>
                        <g:set var="monthlyQuantityIssued" value="${entry?.value?.collect { it?.quantityIssued?:0 }?.sum()?:0 }"/>
                        <g:set var="monthlyQuantityDemand" value="${entry?.value?.collect { it?.quantityDemand?:0 }?.sum()?:0 }"/>
                        <g:set var="totalQuantityRequested" value="${totalQuantityRequested+monthlyQuantityRequested}"/>
                        <g:set var="totalQuantityIssued" value="${totalQuantityIssued+monthlyQuantityIssued}"/>
                        <g:set var="totalQuantityDemand" value="${totalQuantityDemand+monthlyQuantityDemand}"/>

                        <tr class="prop header ${i%2?'even':'odd'}" style="cursor: pointer">
                            <td>
                                ${monthKey}
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${monthlyQuantityRequested}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${monthlyQuantityIssued}" maxFractionDigits="0"/>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${monthlyQuantityDemand}" maxFractionDigits="0"/>
                            </td>
                        </tr>
                        <tr class="prop data fade">
                            <td colspan="5">
                                <div class="box">
                                    <table>
                                        <tr>
                                            <th><warehouse:message code="requisition.requestNumber.label"/></th>
                                            <th><warehouse:message code="requisition.dateRequested.label"/></th>
                                            <th><warehouse:message code="requisition.dateIssued.label"/></th>
                                            <th><warehouse:message code="requisition.destination.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityRequested.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>
                                            <th><warehouse:message code="requisitionItem.cancelReasonCode.label"/></th>
                                            <th class="center middle"><warehouse:message code="requisitionItem.quantityDemand.label" default="Demand"/></th>
                                        </tr>
                                        <g:set var="innerQuantityRequested" value="${0}"/>
                                        <g:set var="innerQuantityIssued" value="${0}"/>
                                        <g:set var="innerQuantityDemand" value="${0}"/>
                                        <g:each var="requisitionItem" in="${entry.value.sort { it?.dateRequested }}" status="j">
                                            <g:set var="quantityRequested" value="${requisitionItem?.quantityRequested?:0}"/>
                                            <g:set var="quantitySubstituted" value="${requisitionItem?.quantitySubstituted?:0}"/>
                                            <g:set var="quantityIssued" value="${requisitionItem?.quantityIssued?:0}"/>
                                            <g:set var="quantityDemand" value="${requisitionItem?.quantityDemand?:0}"/>
                                            <g:set var="innerQuantityRequested" value="${innerQuantityRequested + quantityRequested}"/>
                                            <g:set var="innerQuantityIssued" value="${innerQuantityIssued + quantityIssued}"/>
                                            <g:set var="innerQuantityDemand" value="${innerQuantityDemand + quantityDemand}"/>


                                            <tr class="prop ${j%2?'odd':'even'}">
                                                <td>
                                                    <g:link controller="requisition" action="show" id="${requisitionItem?.requisitionId}">
                                                        ${requisitionItem?.requestNumber}
                                                    </g:link>
                                                </td>
                                                <td>
                                                    <g:formatDate date="${requisitionItem?.dateRequested}" format="MM/dd/yyyy"/>
                                                </td>
                                                <td>
                                                    <g:formatDate date="${requisitionItem?.dateIssued}" format="MM/dd/yyyy"/>
                                                </td>
                                                <td>
                                                    ${requisitionItem?.destination}
                                                </td>
                                                <td class="center middle">
                                                    <g:formatNumber number="${quantityRequested}" maxFractionDigits="0"/>
                                                </td>
                                                <td class="center middle">
                                                    <g:formatNumber number="${quantityIssued}" maxFractionDigits="0"/>
                                                </td>
                                                <td>
                                                    ${requisitionItem?.reasonCode}
                                                </td>
                                                <td class="center middle">
                                                    <g:formatNumber number="${quantityDemand}" maxFractionDigits="0"/>
                                                </td>
                                            </tr>
                                        </g:each>
                                        <tfoot>
                                            <tr class="prop">
                                                <th colspan="4">
                                                    ${g.message(code:'default.total.label')}
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityRequested}" maxFractionDigits="0"/>
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityIssued}" maxFractionDigits="0"/>
                                                </th>
                                                <th class="center">
                                                    &nbsp;
                                                </th>
                                                <th class="center">
                                                    <g:formatNumber number="${innerQuantityDemand}" maxFractionDigits="0"/>
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
                        </tr>
                    </g:else>
                </g:each>
            </tbody>

            <tfoot>
                <tr>
                    <th><warehouse:message code="default.monthly.label" default="Monthly"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityRequested/numberOfMonths}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityIssued/numberOfMonths}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityDemand/numberOfMonths}" maxFractionDigits="0"/></th>
                </tr>
                <tr>
                    <th><warehouse:message code="default.total.label" default="Total"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityRequested}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityIssued}" maxFractionDigits="0"/></th>
                    <th class="center"><g:formatNumber number="${totalQuantityDemand}" maxFractionDigits="0"/></th>
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
    });
</script>
