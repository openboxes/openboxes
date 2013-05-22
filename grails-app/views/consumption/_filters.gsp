<div class="filters">
	<g:form method="POST" controller="consumption" action="show">
		<div class="box">
            <h2><warehouse:message code="default.filters.label"/></h2>
            <table>
                <tr>
                    <td colspan="2">
                        <label>
                            <warehouse:message code="consumption.betweenDates.label" default="Between dates"/>
                        </label>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <g:jqueryDatePicker id="fromDate" name="fromDate" value="${command?.fromDate}" format="MM/dd/yyyy"/>
                        <g:jqueryDatePicker id="toDate" name="toDate" value="${command?.toDate}" format="MM/dd/yyyy"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td colspan="2">
                        <label style="display: block;">
                            <warehouse:message code="consumption.fromLocation.label" default="From location"/>
                        </label>
                        <g:selectLocation name="fromLocation.id" value="${command?.fromLocation?.id}" class="chzn-select-deselect"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td colspan="2">
                        <label style="display: block;">
                            <warehouse:message code="consumption.toLocation.label" default="To location"/>
                        </label>
                        <table>
                            <g:if test="${command?.toLocations}">
                                <tr>
                                    <td class="middle center">
                                        <g:checkBox id="toggleCheckbox" name="toggleCheckbox"/>
                                    </td>
                                    <td class="middle">
                                        <warehouse:message code="default.selectAll.label" default="Select all"/>

                                    </td>
                                </tr>
                                <g:each var="toLocation" in="${command.toLocations}" status="i">
                                    <tr>
                                        <td class="center middle">
                                            <g:set var="selected" value="${command.selectedLocations.contains(toLocation)}"/>
                                            <g:checkBox name="selectedLocation_${toLocation.id}" checked="${selected}"/>
                                            <g:hiddenField name="toLocations[${i}].id" value="${toLocation?.id}"/>
                                        </td>
                                        <td class="left middle">
                                            <b><format:metadata obj="${toLocation?.locationType}"/></b>
                                            <format:metadata obj="${toLocation}"/>
                                        </td>
                                    </tr>
                                </g:each>
                            </g:if>
                        </table>
                        <g:unless test="${command.toLocations}">
                            <div class="empty center">
                                <warehouse:message code="default.empty.label"/>
                            </div>
                        </g:unless>
                    </td>
                </tr>

                <tr class="prop">
                    <td colspan="2" class="right">
                        <button class="button icon search">
                            <warehouse:message code="default.search.label"/>
                        </button>
                    </td>
                </tr>
            </table>

        </div>
	</g:form>
</div>
<script>
    $(document).ready(function() {
        $("#toggleCheckbox").click(function(event) {
            var checked = ($(this).attr("checked") == 'checked');
            $(".checkbox").attr("checked", checked);
        });
    });
</script>

