<style>
.chosen-container-multi .chosen-choices li.search-field input[type="text"] {
    height: 26px;
    line-height: 26px;
}

#accordion .ui-accordion-content {
    max-height: 200px;
}
</style>
<div class="filters">
	<g:form method="POST" controller="consumption" action="show">

        <h3><warehouse:message code="consumption.filters.label" default="Report parameters"/></h3>

        <label><warehouse:message code="consumption.date.label" default="Consumed between"/></label>

        <div class="filter-list-item">
            <g:jqueryDatePicker id="fromDate" name="fromDate"
                                placeholder="${message(code: 'consumption.afterDate.label', default: 'Consumed on or after')}"
                                value="${command?.fromDate}" format="MM/dd/yyyy" size="18"/>
        </div>
        <div class="filter-list-item">
            <g:jqueryDatePicker id="toDate" name="toDate"
                                placeholder="${message(code: 'consumption.beforeDate.label', default: 'Consumed on or before')}"
                                value="${command?.toDate}" format="MM/dd/yyyy" size="18"/>
        </div>
        <label>
            <warehouse:message code="consumption.products.label" default="Product(s)"/>
        </label>
        <div class="filter-list-item">
            <g:selectCategory name="selectedCategories" multiple="true" value="${command?.selectedCategories?.id}" class="chzn-select"
            data-placeholder='${warehouse.message(code:"consumption.includeProductsWithCategory.label", default:"Include products within the following categories")}'/>
        </div>
        <div class="filter-list-item">
            <g:selectTags name="selectedTags" value="${command?.selectedTags?.id}" multiple="true" class="chzn-select-deselect"
                data-placeholder='${warehouse.message(code:"consumption.includeProductsWithTag.label", default:"Include products within the following tags")}'/>
        </div>

        <label>
            <warehouse:message code="consumption.fromLocations.label" default="Source(s)"/>
        </label>
        <div class="filter-list-item">
            <g:selectLocation name="fromLocations" value="${command?.fromLocations?.id}" multiple="true" class="chzn-select-deselect"
                              data-placeholder="${warehouse.message(code:'consumption.fromLocations.label', default:'Source(s)')}"/>
        </div>
        <div class="filter-list-item">

            <g:if test="${command?.toLocations}">

                <label>
                    <warehouse:message code="consumption.toLocation.label" default="Destination(s)"/>

                    <g:unless test="${!command.toLocations}">
                        <div class="right">
                            <a id="selectAllLocations">Select All</a>&nbsp;|&nbsp;
                            <a id="selectNoLocations">Select None</a>
                        </div>
                    </g:unless>
                </label>
                <div class="filter-list-item">
                    <div id="toLocation-accordion">
                        <g:set var="count" value="${0}"/>
                        <g:unless test="${command.toLocations}">
                            <div class="empty fade center">
                                <warehouse:message code="consumption.chooseFromLocation.message" default="You must choose at least one source"/>
                            </div>
                        </g:unless>
                        <g:each var="entry" in="${command.toLocations.groupBy {it.locationGroup}}" >
                            <g:each var="locationTypeEntry" in="${entry.value.groupBy{it.locationType}}">
                                <p>
                                    ${entry.key?:warehouse.message(code:'default.other.label', default: 'Other')}
                                    <small>(<format:metadata obj="${locationTypeEntry.key}"/>)</small>
                                </p>
                                <div style="max-height: 150px; overflow: auto;" class="box">
                                    <table >
                                        <tbody>
                                            <g:each var="toLocation" in="${locationTypeEntry.value}">
                                                <tr>
                                                    <td class="middle center" width="1%">
                                                        <g:set var="selected" value="${command.selectedLocations.contains(toLocation)}"/>
                                                        <g:checkBox name="selectedLocation_${toLocation?.id}" checked="${selected}" class="toLocation"/>
                                                        <g:hiddenField name="toLocations[${count++}].id" value="${toLocation?.id}"/>
                                                    </td>
                                                    <td class="middle">
                                                        <format:metadata obj="${toLocation}"/>
                                                    </td>
                                                </tr>
                                            </g:each>
                                        </tbody>
                                    </table>
                                </div>
                            </g:each>
                        </g:each>
                        <%--
                        <g:each var="toLocation" in="${command.toLocations}" status="i">
                            <div>
                                <g:set var="selected" value="${command.selectedLocations.contains(toLocation)}"/>
                                <g:checkBox name="selectedLocation_${toLocation?.id}" checked="${selected}" class="toLocation"/>
                                <g:hiddenField name="toLocations[${i}].id" value="${toLocation?.id}"/>
                                <b><format:metadata obj="${toLocation?.locationType}"/></b>
                                <format:metadata obj="${toLocation}"/>
                            </div>
                        </g:each>
                        --%>
                    </div>
                </div>
                <g:unless test="${command.toLocations}">
                    <div class="center">
                        <warehouse:message code="consumption.destinations.message" default=""/>
                    </div>
                </g:unless>
            </g:if>
        </div>
        <hr/>
        <h3>
            <warehouse:message code="consumption.report.output.label" default="Report Output"/>
        </h3>

        <label>
            <warehouse:message code="consumption.format.label" default="Format"/>
        </label>

        <div class="filter-list-item">
            <span class="middle">
                <g:radio name="format" value="html" checked="${params.format=='html'||!params.format}"/> HTML
            </span>
            <span class="middle">
                <g:radio name="format" value="csv" checked="${false}" /> CSV
            </span>
        </div>
        <div class="filter-list-item">
            <g:checkBox name="includeLocationBreakdown" value="${command.includeLocationBreakdown}"/>
            <label for="includeLocationBreakdown">
                <warehouse:message code="consumption.includeLocationBreakdown.label" default="Include location breakdown in CSV"/>
            </label>
        </div>
        <label><warehouse:message code="consumption.additionalColumns.label" default="Additional columns"/></label>
        <div class="filter-list-item">
            <select name="selectedProperties" multiple="true" class="chzn-select-deselect" style="height: 100px;">
                <g:each var="property" in="${command.productDomain.properties}">
                    <g:if test="${!property.isAssociation() && property.typePropertyName != 'object'}">
                        <option value="${property.name}" ${command.selectedProperties?.toList()?.contains(property.name)?'selected':''}>
                            ${property.naturalName} (${property.typePropertyName})
                        </option>
                    </g:if>
                </g:each>
            </select>
        </div>
        <%--
        <div style="overflow: auto; max-height: 200px;" class="list">

            <g:each var="property" in="${command.productDomain.properties}">
                <g:if test="${!property.isAssociation() && property.typePropertyName != 'object'}">

                        <g:checkBox name="selectedProperties" value="${property.name}"
                            checked="${command.selectedProperties?.toList()?.contains(property.name)}"
                            class="property"/>

                            ${property.naturalName}
                            <span class="fade">${property.typePropertyName}</span>

                </g:if>
            </g:each>
        </div>
        --%>

        <div class="buttons">
            <button class="button icon search">
                <warehouse:message code="default.button.runReport.label" default="Run Report"/>
            </button>
            &nbsp;
            <g:link controller="consumption" action="show" class="button icon reload">${warehouse.message(code:'default.button.reset.label', default: 'Reset')}</g:link>
            <%--
            <a href="#" id="parameters-toggle" class="button icon settings">
                <warehouse:message code="consumption.parameters.view.label" default="View parameters"/></a>
            --%>
        </div>
        <%--
        <g:link params="[format:'csv']" controller="${controllerName}" action="${actionName}" class="button icon file">Download .csv</g:link>
        --%>


	</g:form>
</div>
<script>
    $(document).ready(function() {
        $("#selectAllLocations").click(function(event) {
            //var checked = ($(this).attr("checked") == 'checked');
            $("input.toLocation[type='checkbox']").attr("checked", true);
        });
        $("#selectNoLocations").click(function(event) {
            //var checked = ($(this).attr("checked") == 'checked');
            $("input.toLocation[type='checkbox']").attr("checked", false);
        });
        $("#selectAllProperties").click(function(event) {
            //var checked = ($(this).attr("checked") == 'checked');
            $("input.property[type='checkbox']").attr("checked", true);
        });
        $("#selectNoProperties").click(function(event) {
            //var checked = ($(this).attr("checked") == 'checked');
            $("input.property[type='checkbox']").attr("checked", false);
        });
        //$("#selectNone").click(function(event) {
        //    var checked = ($(this).attr("checked") == 'checked');
        //    $(".checkbox").attr("checked", checked);
        //});

//        $("#toLocation-accordion").accordion({
//            collapsible: true,
//            active: 'none',
//            autoHeight: false,
//            navigation: true
//        });

    });
</script>

