<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'report.forecastReport.label', default: 'Forecast').toLowerCase()}" />
    <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
</head>

<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <div class="yui-gf">
            <div class="yui-u first">
                <div class="sidebar">
                    <div class="box">
                        <h2><warehouse:message code="report.parameters.label" default="Parameters"/></h2>
                        <div class="parameters">
                            <div class="filter-list-item">
                                <label>
                                    <warehouse:message code="report.location.label"/>
                                </label>
                                <g:textField name="origin" value="${params?.origin}" disabled="true" style="width: 100%"/>
                            </div>
                            <div class="filter-list-item">
                                <label>
                                    <warehouse:message code="report.demandDateRange.label"/>
                                </label>
                                <g:jqueryDatePicker name="startDate" value="${params.startDate}" autocomplete="off" format="dd/MMM/yyyy" />
                                <g:jqueryDatePicker name="endDate" value="${params.endDate}" autocomplete="off" format="dd/MMM/yyyy" />
                            </div>
                            <div class="filter-list-item">
                                <label>
                                    <warehouse:message code="report.orderPeriod.label"/>
                                </label>
                                <g:textField name="replenishmentPeriodDays" value="${params.replenishmentPeriodDays}" style="width: 100%"/>
                                <span class="fade"><g:message code="report.orderPeriod.optional.label"/></span>
                            </div>
                            <div class="filter-list-item">
                                <label>
                                    <warehouse:message code="report.leadTime.label"/>
                                </label>
                                <g:textField name="leadTimeDays" value="${params.leadTimeDays}" style="width: 100%"/>
                                <span class="fade"><g:message code="report.leadTime.optional.label"/></span>
                            </div>
                        </div>

                        <h2><warehouse:message code="report.optionalFilters.label" default="Optional Filters"/></h2>
                        <div class="filters">
                            <div class="filter-list">
                                <div class="filter-list-item">
                                    <label>
                                        <warehouse:message code="report.destination.message" default="Demand Destination"/>
                                    </label>
                                    <div class="location-container">
                                        <g:selectLocation name="selectedLocations"
                                                          value="${params?.locations}" multiple="true" class="chzn-select-deselect"/>
                                    </div>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="category.label"/></label>
                                    <p>
                                        <g:selectCategory id="category"
                                                          class="chzn-select-deselect filter"
                                                          data-placeholder="Select a category"
                                                          name="category"
                                                          noSelection="['':'']"
                                                          multiple="true"
                                                          value="${params?.category}"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="catalogs.name.label"/></label>
                                    <p>
                                        <g:selectCatalogs id="catalogs"
                                                          name="catalogs"
                                                          noSelection="['':'']"
                                                          value="${params?.catalogs}"
                                                          style="width:100%;"
                                                          class="chzn-select-deselect"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="tag.label"/></label>
                                    <p>
                                        <g:selectTags name="tags"
                                                      id="tags"
                                                      noSelection="['':'']"
                                                      value="${params?.tags}"
                                                      multiple="true"
                                                      class="chzn-select-deselect"/>
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="buttons">
                            <button class="download-button button" name="button" params="[print:true]>
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />
                                <g:set var="dataLabel" value="${g.message(code:'', default: '')}"/>
                                ${g.message(code: 'default.download.label', args: [dataLabel])}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="loading">
        Loading...
    </div>
<script>
    $(document).ready(function() {
        $(".loading").hide();

        $(".download-button").click(function(event) {
            event.preventDefault();
            var params = {
                startDate: $("#startDate").val(),
                endDate: $("#endDate").val(),
                replenishmentPeriodDays: $("#replenishmentPeriodDays").val(),
                leadTimeDays: $("#leadTimeDays").val(),
                locations: $("#selectedLocations").val(),
                category: $("#category").val(),
                tags: $("#tags").val(),
                catalogs: $("#catalogs").val(),
                format: "text/csv",
                print: $("#print").val(),
            };
            var queryString = $.param(params, true);
            window.location.href = '${request.contextPath}/report/showForecastReport?' + queryString;
        });
    });

</script>
</body>
</html>
