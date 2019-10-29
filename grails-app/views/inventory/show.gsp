<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
</head>
<body>
    <div class="body">

        <div class="yui-gf">
            <div class="yui-u first">

                <div class="box">
                    <h2 class="middle">
                        Baseline QoH Report
                    </h2>
                    <g:form controller="inventory" action="search">
                        <div class="filters">

                            <div class="filter-list-item">
                                <h3><g:message code="report.parameters.label"/></h3>
                            </div>
                            <div class="filter-list-item">
                                <label>${warehouse.message(code:'locations.label')}</label>
                                <div>
                                    <g:selectLocation name="locations"
                                                      multiple="true"
                                                      class="chzn-select-deselect"
                                                      value="${command?.locations?.id}"
                                                      noSelection="['':'']"
                                                      data-placeholder=" "
                                                      activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"/>

                                </div>
                            </div>
                            <div class="filter-list-item">
                                <label><warehouse:message code="reporting.startDate.label" default="Start date"/></label>
                                <div>
                                    <g:jqueryDatePicker id="startDate" name="startDate" value="${command?.startDate}" format="MM/dd/yyyy" size="30" autocomplete="off"/>
                                </div>
                            </div>
                            <div class="filter-list-item">
                                <label><warehouse:message code="reporting.endDate.label" default="End date"/></label>
                                <div>
                                    <g:jqueryDatePicker id="endDate" name="endDate" value="${command?.endDate}" format="MM/dd/yyyy" size="30" autocomplete="off"/>
                                </div>
                            </div>
                            <div class="filter-list-item">
                                <label><warehouse:message code="reporting.frequency.label" default="Frequency"/></label>
                                <div>
                                    <g:select name="frequency" value="${command?.frequency}" from="['','Daily','Weekly','Monthly','Quarterly','Annually']" class="chzn-select-deselect" />
                                </div>
                            </div>
%{--                            <div class="filter-list-item">--}%
%{--                                <h3><g:message code="default.filters.label"/></h3>--}%
%{--                            </div>--}%
%{--                            <div class="filter-list-item">--}%
%{--                                <label><warehouse:message code="tag.label"/></label>--}%
%{--                                <div>--}%
%{--                                    <g:selectTag name="tags" class="chzn-select-deselect" multiple="multiple" value="${command?.tags}" noSelection="['':'']"/>--}%
%{--                                </div>--}%
%{--                            </div>--}%
                            <hr/>

                            <div class="filter-list-item">
                                <div class="center">
                                    <button name="button" value="search" class="button icon search">
                                        <g:message code="report.runReport.label"/>
                                    </button>
                                    <button name="button" value="download" class="button icon log">
                                        <g:message code="default.button.download.label"/>
                                    </button>
                                    <g:link controller="inventory" action="show" class="button icon reload">
                                        <g:message code="default.button.reset.label"/>
                                    </g:link>
                                </div>
                            </div>
                        </div>
                    </g:form>
                </div>

            </div>
            <div class="yui-u">

                <g:hasErrors bean="${command}">
                    <div class="errors">
                        <g:renderErrors bean="${command}" as="list" />
                    </div>
                </g:hasErrors>

                <g:if test="${session.quantityMapByDate}">
                    <g:link controller="inventory" action="export" class="button icon log">Export as CSV</g:link>

                </g:if>
                <div class="box">
                    <h2>Results <g:if test="${command?.products}"><small>Returned ${command?.products.size()} results in ${elapsedTime/1000} seconds</small></g:if></h2>
                    <table class="dataTable">
                        <thead>
                            <tr>
                                <th><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th><warehouse:message code="category.label"/></th>
                                <th class="border-right"><warehouse:message code="product.uom.label"/></th>
                                <g:each var="date" in="${command?.dates}">
                                    <th class="center"><g:formatDate date="${date}" format="MMM dd HH:mm z" timeZone="${TimeZone.default}"/></th>
                                </g:each>
                            </tr>
                        </thead>
                        <g:each var="product" in="${command?.products}" status="i">
                            <tr>
                                <td>
                                    ${product?.productCode}
                                </td>
                                <td>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">
                                        ${product?.name}
                                    </g:link>
                                </td>
                                <td>
                                    ${product?.category?.name}
                                </td>
                                <td class="border-right">
                                    ${product?.unitOfMeasure}
                                </td>
                                <g:each var="date" in="${command.dates}">
                                    <td class="center">
                                        <g:formatNumber number="${quantityMapByDate[date][product]}" format="#,###"/>
                                    </td>
                                </g:each>
                            </tr>
                        </g:each>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <script>
      $(document).ready(function(){
        $(".dataTable").dataTable({
          "bProcessing": true,
          "iDisplayLength": 25,
          "sServerMethod": "GET",
          "bSearch": false,
          "bScrollCollapse": true,
          "bJQueryUI": true,
          "bAutoWidth": true,
          "sPaginationType": "full_numbers",
        });
      });
    </script>

</body>
</html>
