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

    <div class="box">
        <h2><warehouse:message code="consumption.filters.label" default="Report parameters"/></h2>
        <table>
            <tr>
                <td colspan="2">
                    <h3>
                        <warehouse:message code="consumption.dateRange.label" default="Date range"/>
                    </h3>


                </td>

            </tr>
            <tr>
                <td class="middle">
                    <label><warehouse:message code="consumption.afterDate.label" default="Consumed on or after"/></label>
                </td>
                <td>
                    <g:jqueryDatePicker id="fromDate" name="fromDate" value="${command?.fromDate}" format="MM/dd/yyyy" size="18"/>
                </td>
            </tr>
            <tr>
                <td class="middle">
                    <label>
                        <warehouse:message code="consumption.beforeDate.label" default="Consumed on or before"/>
                    </label>
                </td>
                <td>
                    <g:jqueryDatePicker id="toDate" name="toDate" value="${command?.toDate}" format="MM/dd/yyyy" size="18"/>
                </td>
            </tr>
            <tr class="prop">
                <td colspan="2">
                    <h3>
                        <warehouse:message code="consumption.products.label" default="Product(s)"/>
                    </h3>


                </td>

            </tr>
            <tr>
                <td colspan="2">
                    <g:selectCategory name="selectedCategories" multiple="true" value="${command?.selectedCategories?.id}" class="chzn-select"
                    data-placeholder='${warehouse.message(code:"consumption.includeProductsWithCategory.label", default:"Include products within the following categories")}'/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <g:selectTags name="selectedTags" value="${command?.selectedTags?.id}" multiple="true" class="chzn-select-deselect"
                        data-placeholder='${warehouse.message(code:"consumption.includeProductsWithTag.label", default:"Include products within the following tags")}'/>
                </td>
            </tr>

            <tr class="prop">
                <td colspan="2">
                    <h3>
                        <warehouse:message code="consumption.fromLocations.label" default="Source(s)"/>
                    </h3>
                    <g:selectLocation name="fromLocations" value="${command?.fromLocations?.id}" multiple="true" class="chzn-select-deselect"
                                      data-placeholder="${warehouse.message(code:'consumption.fromLocations.label', default:'Source(s)')}"/>
                </td>
            </tr>

            <g:if test="${command?.toLocations}">
                <tr class="prop">
                    <td colspan="2">
                        <h3>
                            <warehouse:message code="consumption.toLocation.label" default="Destination(s)"/>

                            <g:unless test="${!command.toLocations}">
                                <div class="right">
                                    <a id="selectAllLocations">Select All</a>&nbsp;|&nbsp;
                                    <a id="selectNoLocations">Select None</a>
                                </div>
                            </g:unless>
                        </h3>
                    </td>
                </tr>
                <tr class="">
                    <td colspan="2">
                        <div> <!-- style="max-height: 300px; overflow: auto; border: 1px lightgrey solid"-->
                            <div id="toLocation-accordion">
                                <g:set var="count" value="${0}"/>
                                <g:unless test="${command.toLocations}">
                                    <div class="empty fade center">
                                        <warehouse:message code="consumption.chooseFromLocation.message" default="You must choose at least one source"/>
                                    </div>
                                </g:unless>
                                <g:each var="entry" in="${command.toLocations.groupBy {it.locationGroup}}" >
                                    <g:each var="locationTypeEntry" in="${entry.value.groupBy{it.locationType}}">
                                        <fieldset>
                                            <legend>
                                                ${entry.key?:warehouse.message(code:'default.other.label', default: 'Other')} &rsaquo;
                                                <format:metadata obj="${locationTypeEntry}"/>
                                            </legend>
                                            <div style="max-height: 150px; overflow: auto;">
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
                                        </fieldset>
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
                    </td>
                </tr>
            </g:if>
            <tr class="prop">
                <td colspan="2">
                    <h3>
                        <warehouse:message code="consumption.report.output.label" default="Report Output"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td>
                    <label>
                        <warehouse:message code="consumption.format.label" default="Format"/>
                    </label>
                </td>
                <td>
                    <span class="middle">
                        <g:radio name="format" value="html" checked="${params.format=='html'||!params.format}"/> HTML
                    </span>
                    <span class="middle">
                        <g:radio name="format" value="csv" checked="${false}" /> CSV
                    </span>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <label>
                        <warehouse:message code="consumption.includeLocationBreakdown.label" default="Include location breakdown in CSV"/>
                    </label>
                    <g:checkBox name="includeLocationBreakdown" value="${command.includeLocationBreakdown}"/>


                </td>
            </tr>

            <tr>
                <td colspan="2">
                    <label><warehouse:message code="consumption.additionalColumns.label" default="Additional columns"/></label>
                    <select name="selectedProperties" multiple="true" class="chzn-select-deselect" style="height: 100px;">
                        <g:each var="property" in="${command.productDomain.properties}">
                            <g:if test="${!property.isAssociation() && property.typePropertyName != 'object'}">
                                <option value="${property.name}" ${command.selectedProperties?.toList()?.contains(property.name)?'selected':''}>
                                    ${property.naturalName} (${property.typePropertyName})
                                </option>
                            </g:if>
                        </g:each>
                    </select>
                    <%--
                <div style="overflow: auto; max-height: 200px;" class="list">
                    <table>
                    <g:each var="property" in="${command.productDomain.properties}">
                        <g:if test="${!property.isAssociation() && property.typePropertyName != 'object'}">
                            <tr>
                                <td class="middle left">
                                <g:checkBox name="selectedProperties" value="${property.name}"
                                    checked="${command.selectedProperties?.toList()?.contains(property.name)}"
                                    class="property"/>
                                </td>
                                <td class="middle left">
                                    ${property.naturalName}
                                    <span class="fade">${property.typePropertyName}</span>
                                </td>
                            </div>
                        </g:if>
                    </g:each>
                    </table>
                </div>
                    --%>
                </td>
            </tr>
            <tr>
                <td class="center" colspan="2">
                    <div>
                        <button class="button icon search">
                            <warehouse:message code="default.button.getData.label" default="Get data"/>
                        </button>
                        &nbsp;
                        <g:link controller="consumption" action="show">${warehouse.message(code:'default.button.reset.label', default: 'Reset')}</g:link>
                        <%--
                        <a href="#" id="parameters-toggle" class="button icon settings">
                            <warehouse:message code="consumption.parameters.view.label" default="View parameters"/></a>
                            --%>
                    </div>

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

