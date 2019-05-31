<%@ page import="org.pih.warehouse.core.Constants" %>
<style>
.chosen-container-multi .chosen-choices li.search-field input[type="text"] {
    height: 26px;
    line-height: 26px;
}
</style>
<div class="filters">
	<g:form method="get" controller="consumption" action="show">

        <g:hiddenField name="parametersHash" value="${command?.generateParametersHash()}"/>

        <div class="box">
            <h2><warehouse:message code="consumption.parameters.label" default="Parameters"/></h2>
            <fieldset>

                <div class="filter-list-item">
                    <label>
                        <warehouse:message code="consumption.fromLocations.label" default="Origin(s)"/>
                    </label>
                    <g:selectLocation name="fromLocations" value="${command?.fromLocations?.id?:session?.warehouse?.id}"
                                      multiple="true" class="chzn-select-deselect"
                                      data-placeholder="${warehouse.message(code:'consumption.fromLocations.label', default:'Origins(s)')}"/>

                    <div class="fade">NOTE: Using multiple Origin locations may provide inaccurate results.</div>
                </div>
                <div class="filter-list-item">
                    <label>
                        <warehouse:message code="consumption.afterDate.label" default="Consumed on or after"/>
                    </label>
                    <div>
                        <g:jqueryDatePicker id="fromDate" name="fromDate" value="${command?.fromDate}" format="MM/dd/yyyy" cssClass="large" autocomplete="off"/>
                    </div>
                </div>
                <div class="filter-list-item">
                    <label>
                        <warehouse:message code="consumption.beforeDate.label" default="Consumed on or before"/>
                    </label>
                    <div>
                        <g:jqueryDatePicker id="toDate" name="toDate" value="${command?.toDate}" format="MM/dd/yyyy" cssClass="large" autocomplete="off"/>
                    </div>
                </div>
            </fieldset>

            <h2><warehouse:message code="consumption.filters.label" default="Filters"/></h2>
            <fieldset>

                <div class="filter-list">

                    <div class="filter-list-item">
                        <label>
                            <warehouse:message code="consumption.categories.label" default="Categories"/>
                        </label>
                        <g:selectCategory name="selectedCategories" multiple="true" value="${command?.selectedCategories?.id}" class="chzn-select"/>
                    </div>
                    <div class="filter-list-item">
                        <label>
                            <warehouse:message code="consumption.tags.label" default="Tags"/>
                        </label>
                        <g:selectTags name="selectedTags" value="${command?.selectedTags?.id}" multiple="true" class="chzn-select-deselect"/>
                    </div>
                    <g:if test="${!command?.toLocations}">
                        <div class="filter-list-item">
                            <label>
                                <warehouse:message code="consumption.toLocations.label" default="Destinations(s)"/>
                            </label>
                            <g:selectLocation name="selectedLocations"
                                              value="${command?.selectedLocations?.id}" multiple="true" class="chzn-select-deselect"/>
                            <span class="fade"><g:message code="consumption.destinations.optional.message"/></span>
                        </div>
                    </g:if>
                    <g:elseif test="${command?.toLocations}">
                        <div class="filter-list-item">
                            <label>
                                <warehouse:message code="consumption.toLocation.label" default="Destination(s)"/>
                            </label>
                            <g:selectLocationWithOptGroup name="selectedLocations" from="${command?.toLocations}"
                                                          value="${command.selectedLocations*.id}"
                                                          class="chzn-select-deselect" multiple="multiple"/>
                        </div>
                    </g:elseif>
                    <div class="filter-list-item">

                        <label><warehouse:message code="consumption.transactionTypes.label" default="Transaction Types"/></label>
                        <g:selectTransactionType name="selectedTransactionTypes" from="${command?.transactionTypes}"
                                                 transactionCode="${org.pih.warehouse.inventory.TransactionCode.DEBIT}"
                                                 multiple="true" class="chzn-select-deselect"
                                                 value="${command?.selectedTransactionTypes?.id}"/>
                    </div>
                </div>

            </fieldset>
            <h2><warehouse:message code="consumption.renderOptions.label" default="Render Options"/></h2>
            <fieldset>

                <div class="filter-list-item">
                    <label><warehouse:message code="consumption.additionalColumns.label" default="Additional columns (CSV only)"/></label>
                    <select name="selectedProperties" multiple="true" class="chzn-select-deselect">

                        <g:hasRoleFinance>
                            <g:set var="hasRoleFinance" value="${true}"/>
                        </g:hasRoleFinance>
                        <g:each var="property" in="${command.productDomain.properties}">
                            <g:if test="${!property.isAssociation() && property.typePropertyName != 'object'}">
                                <g:set var="disabled" value="${'pricePerUnit'.equals(property?.name) && !hasRoleFinance}"/>
                                <option value="${property.name}"
                                    ${command.selectedProperties?.toList()?.contains(property.name)?'selected':''}
                                    ${disabled?'disabled':''}>
                                    ${property.naturalName} (${property.typePropertyName})
                                </option>
                            </g:if>
                        </g:each>
                    </select>
                </div>
                <div class="filter-list-item">
                    <label>
                        <warehouse:message code="consumption.format.label" default="Format"/>
                    </label>
                    <div>
                        <label><g:radio name="format" value="html" checked="${params.format=='html'||!params.format}"/> HTML</label>
                        <label><g:radio name="format" value="csv" checked="${false}" /> CSV</label>
                    </div>
                </div>
                <div class="filter-list-item">
                    <label>
                        <warehouse:message code="consumption.options.label" default="Options"/>
                    </label>
                    <div class="checkbox">
                        <g:checkBox name="includeQuantityOnHand" value="${command.includeQuantityOnHand}"/>
                        <label for="includeQuantityOnHand">
                            <warehouse:message code="consumption.includeQuantityOnHand.label" default="Include quantity on hand"/>
                        </label>
                    </div>
                    <div class="checkbox">
                        <g:checkBox name="includeLocationBreakdown" value="${command.includeLocationBreakdown}"/>
                        <label for="includeLocationBreakdown">
                            <warehouse:message code="consumption.includeLocationBreakdown.label" default="Include location breakdown in CSV"/>
                        </label>
                    </div>
                    <div class="checkbox">
                        <g:checkBox name="includeMonthlyBreakdown" value="${command.includeMonthlyBreakdown}"/>
                        <label for="includeMonthlyBreakdown">
                            <warehouse:message code="consumption.includeMonthlyBreakdown.label" default="Include monthly breakdown in CSV"/>
                        </label>
                    </div>
                </div>
                <div class="filter-list-item">
                    <div class="buttons">
                        <button class="button">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'cog_go.png')}" />
                            <warehouse:message code="default.button.run.label" default="Run"/>
                        </button>

                        <g:link controller="consumption" action="show" class="button">
                            <img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_undo.png')}" />
                            ${warehouse.message(code:'default.button.reset.label', default: 'Reset')}
                        </g:link>
                    </div>
                </div>
            </fieldset>
        </div>
    </g:form>

</div>

