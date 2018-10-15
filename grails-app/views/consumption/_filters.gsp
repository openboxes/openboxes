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
	<g:form method="get" controller="consumption" action="show">

    <div class="box dialog">
        <h2><warehouse:message code="consumption.filters.label" default="Report parameters"/></h2>
        <table>
            <tr>
                <td class="middle" colspan="2">
                    <label><warehouse:message code="consumption.afterDate.label" default="Consumed on or after"/></label>
                    <div>
                        <g:jqueryDatePicker id="fromDate" name="fromDate" value="${command?.fromDate}" format="MM/dd/yyyy" size="30"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <label>
                        <warehouse:message code="consumption.beforeDate.label" default="Consumed on or before"/>
                    </label>
                    <div>
                        <g:jqueryDatePicker id="toDate" name="toDate" value="${command?.toDate}" format="MM/dd/yyyy" size="30"/>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td colspan="2">
                    <label>
                        <warehouse:message code="consumption.products.label" default="Product(s)"/>
                    </label>
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
                    <label>
                        <warehouse:message code="consumption.fromLocations.label" default="Source(s)"/>
                    </label>
                    <g:selectLocation name="fromLocations" value="${command?.fromLocations?.id}" multiple="true" class="chzn-select-deselect"
                                      data-placeholder="${warehouse.message(code:'consumption.fromLocations.label', default:'Source(s)')}"/>
                </td>
            </tr>

            <g:if test="${!command?.toLocations || command?.toLocations?.size() < 25}">
                <tr class="prop">
                    <td colspan="2">
                        <label>
                            <warehouse:message code="consumption.toLocations.label" default="Destinations(s)"/>
                        </label>
                        <g:selectLocation name="toLocations" value="${command?.toLocations?.id}" multiple="true" class="chzn-select-deselect"
                                          data-placeholder="${warehouse.message(code:'consumption.toLocations.label', default:'Destinations(s)')}"/>
                        <span class="fade"><g:message code="consumption.destinations.optional.message"/></span>
                    </td>
                </tr>
            </g:if>
            <g:elseif test="${command?.toLocations}">
                <tr class="prop">
                    <td colspan="2">
                        <label>
                            <warehouse:message code="consumption.toLocation.label" default="Destination(s)"/>
                        </label>
                        <g:unless test="${!command.toLocations}">
                            <div class="right">
                                <a id="selectAllLocations">Select All</a>&nbsp;|&nbsp;
                                <a id="selectNoLocations">Select None</a>
                            </div>
                        </g:unless>
                    </td>
                </tr>
                <tr>
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
                                                <table style="width: auto;">
                                                    <tbody>
                                                        <g:each var="toLocation" in="${locationTypeEntry.value}">
                                                            <tr>
                                                                <td class="middle center" width="1%">
                                                                    <g:set var="selected" value="${command.selectedLocations.contains(toLocation)}"/>

                                                                    <g:checkBox name="selectedLocation_${toLocation?.id}" checked="${selected}" class="toLocation"/>
                                                                    <g:hiddenField name="toLocations[${count++}].id" value="${toLocation?.id}"/>
                                                                </td>
                                                                <td class="middle">
                                                                    <label for="selectedLocation_${toLocation?.id}" style="white-space: pre-wrap;"><format:metadata obj="${toLocation}"/></label>
                                                                </td>
                                                            </tr>
                                                        </g:each>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </fieldset>
                                    </g:each>
                                </g:each>
                            </div>
                        </div>
                        <g:unless test="${command.toLocations}">
                            <div class="center">
                                <warehouse:message code="consumption.destinations.message" default=""/>
                            </div>
                        </g:unless>
                    </td>
                </tr>
            </g:elseif>
            <tr class="prop">
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
                </td>
            </tr>
            <tr class="prop">
                <td>
                    <label>
                        <warehouse:message code="consumption.format.label" default="Format"/>
                    </label>
                    <div>
                        <label><g:radio name="format" value="html" checked="${params.format=='html'||!params.format}"/> HTML</label>
                        <label><g:radio name="format" value="csv" checked="${false}" /> CSV</label>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td colspan="2">
                    <g:checkBox name="includeQuantityOnHand" value="${command.includeQuantityOnHand}"/>
                    <label for="includeQuantityOnHand">
                        <warehouse:message code="consumption.includeQuantityOnHand.label" default="Include Quantity on Hand (slow)"/>
                    </label>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <g:checkBox name="includeLocationBreakdown" value="${command.includeLocationBreakdown}"/>
                    <label for="includeLocationBreakdown">
                        <warehouse:message code="consumption.includeLocationBreakdown.label" default="Include location breakdown in CSV"/>
                    </label>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <g:checkBox name="includeMonthlyBreakdown" value="${command.includeMonthlyBreakdown}"/>
                    <label for="includeMonthlyBreakdown">
                        <warehouse:message code="consumption.includeMonthlyBreakdown.label" default="Include monthly breakdown in CSV"/>
                    </label>


                </td>
            </tr>


            <tr class="prop">
                <td class="center" colspan="2">
                    <div>
                        <button class="button icon search">
                            <warehouse:message code="default.button.runReport.label" default="Run Report"/>
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

