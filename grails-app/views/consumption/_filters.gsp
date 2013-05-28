<div class="filters">
	<g:form method="POST" controller="consumption" action="show">
		<div class="box">
            <h2><warehouse:message code="default.filters.label"/></h2>
            <table border="0">
                <tr class="prop">
                    <td colspan="2">
                        <label>
                            <warehouse:message code="consumption.fromLocations.label" default="From location(s)"/>
                        </label>
                    </td>
                </tr>
                <tr class="">
                    <td colspan="2">
                        <g:selectLocation name="fromLocations" value="${command?.fromLocations?.id}" multiple="true" class="chzn-select-deselect"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle">
                        <label>
                            <warehouse:message code="consumption.afterDate.label" default="Consumed after"/>
                        </label>
                    </td>
                    <td class="right middle">
                        <g:jqueryDatePicker id="fromDate" name="fromDate" value="${command?.fromDate}" format="MM/dd/yyyy"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td>
                        <label>
                            <warehouse:message code="consumption.beforeDate.label" default="Consumed before"/>
                        </label>
                    </td>
                    <td class="right middle">
                        <g:jqueryDatePicker id="toDate" name="toDate" value="${command?.toDate}" format="MM/dd/yyyy"/>
                    </td>
                </tr>
                <%--
                <tr class="prop">
                    <td>
                        <label>
                            <warehouse:message code="consumption.transactionType.label" default="transaction types"/>
                        </label>
                    </td>
                    <td>
                        <g:each var="transactionType" in="${command.transactionTypes}">
                            <div>
                                <format:metadata obj="${transactionType}"/>
                            </div>
                        </g:each>


                    </td>
                </tr>
                --%>

                <g:if test="${command?.toLocations}">
                    <tr class="prop">
                        <td colspan="2" class="bottom">
                            <label>
                                <warehouse:message code="consumption.toLocation.label" default="To location(s)"/>
                            </label>
                            <div class="right">
                                <a id="selectAll">Select all</a>&nbsp;|&nbsp;
                                <a id="selectNone">Select none</a>
                            </div>
                        </td>
                    </tr>
                    <tr class="">
                        <td colspan="2">
                            <div style="overflow: auto; max-height: 200px;" class="list">
                                <table>
                                    <g:each var="toLocation" in="${command.toLocations}" status="i">
                                        <tr class="prop">
                                            <td class="center middle">
                                                <g:set var="selected" value="${command.selectedLocations.contains(toLocation)}"/>
                                                <g:checkBox name="selectedLocation_${toLocation?.id}" checked="${selected}"/>
                                                <g:hiddenField name="toLocations[${i}].id" value="${toLocation?.id}"/>
                                            </td>
                                            <td class="left middle">
                                                <b><format:metadata obj="${toLocation?.locationType}"/></b>
                                                <format:metadata obj="${toLocation}"/>
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                            </div>
                            <g:unless test="${command.toLocations}">
                                <div class="empty center">
                                    <warehouse:message code="default.empty.label"/>
                                </div>
                            </g:unless>
                        </td>
                    </tr>
                </g:if>

                <tr class="prop">
                    <td>
                        <label>
                            <warehouse:message code="consumption.format.label" default="Format"/>
                        </label>
                    </td>
                    <td>
                        <div class="middle">
                            <g:radio name="format" value="csv" checked="${params.format=='csv'}" /> CSV
                        </div>
                        <div class="middle">
                            <g:radio name="format" value="html" checked="${params.format=='html'||!params.format}"/> HTML
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="center" colspan="2">
                        <button class="button icon search">
                            <warehouse:message code="default.runReport.label" default="Run report"/>
                        </button>

                        <g:link controller="consumption" action="show" class="button icon reload">Reset</g:link>
                        <%--
                        <g:link params="[format:'csv']" controller="${controllerName}" action="${actionName}" class="button icon file">Download .csv</g:link>
                        --%>
                    </td>
                </tr>
            </table>

        </div>
	</g:form>
</div>
<script>
    $(document).ready(function() {
        $("#selectAll").click(function(event) {
            //var checked = ($(this).attr("checked") == 'checked');
            $("input[type='checkbox']").attr("checked", true);
        });
        $("#selectNone").click(function(event) {
            //var checked = ($(this).attr("checked") == 'checked');
            $("input[type='checkbox']").attr("checked", false);
        });
        //$("#selectNone").click(function(event) {
        //    var checked = ($(this).attr("checked") == 'checked');
        //    $(".checkbox").attr("checked", checked);
        //});

    });
</script>

