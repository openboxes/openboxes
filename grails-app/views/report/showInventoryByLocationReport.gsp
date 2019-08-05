<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="report.inventoryByLocationReport.label" /></title>
</head>

<body>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="button-bar">
        <g:link controller="dashboard" action="index" class="button"><g:message code="default.button.backTo.label" args="['Dashboard']"/></g:link>
    </div>

    <div class="yui-gf">
        <div class="yui-u first">

            <div class="box">
                <h2 class="middle"><g:message code="report.parameters.label"/></h2>
                <g:form controller="report" action="showInventoryByLocationReport" method="GET">
                    <div class="filters">

                        <div class="prop">
                            <label>${warehouse.message(code:'locations.label')}</label>
                            <div>
                                <g:selectLocation name="locations" multiple="true"
                                                  class="chzn-select-deselect"
                                                  value="${command.locations?.collect { it.id }}"
                                                  noSelection="['null':'']"
                                                  data-placeholder=" "
                                                  activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"/>
                            </div>
                        </div>

                        <div class="prop">
                            <div class="center">
                                <button name="button" value="run" class="button"><g:message code="default.button.run.label"/></button>
                                <button name="button" value="download" class="button"><g:message code="default.button.download.label"/></button>
                            </div>
                        </div>
                    </div>
                </g:form>
            </div>

        </div>
        <div class="yui-u">

            <div class="box">
                <h2>
                    ${warehouse.message(code:'report.inventoryByLocationReport.label')} <small>(${command.entries?.keySet()?.size()} results)</small>
                </h2>

                <div >
                    <table class="dataTable">
                        <thead>

                        <tr>
                            <th><warehouse:message code="product.productCode.label" default="Code"/></th>
                            <th><warehouse:message code="product.label" default="Product"/></th>
                            <th><warehouse:message code="product.primaryCategory.label" default="Category"/></th>
                            <th><warehouse:message code="catalogs.label" default="Formularies"/></th>
                            <th><warehouse:message code="product.tags.label" default="Tags"/></th>
                            <g:each var="location" in="${command.locations}">
                                <th class="center"><warehouse:message code="product.QoH.label" default="QoH"/> ${location?.name}</th>
                            </g:each>
                            <th class="center"><warehouse:message code="product.totalQoH.label" default="QoH Total"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <g:each var="entry" in="${command.entries}">
                            <g:set var="row" value="${entry.value}"/>
                            <g:set var="product" value="${entry.key}"/>
                            <g:set var="totalQuantity" value="${row?.values()?.sum()}"/>
                            <g:set var="form" value='${product?.getProductCatalogs()?.collect{ it.name }?.join(",")}'/>

                            <tr>
                                <td>
                                    ${product?.productCode}
                                </td>
                                <td>
                                    ${product?.name}
                                </td>
                                <td>
                                    ${product?.category?.name}
                                </td>
                                <td>
                                    ${form}
                                </td>
                                <td>
                                    ${product?.tagsToString()}
                                </td>

                                <g:each var="location" in="${command.locations}">
                                    <td class="center">
                                        ${row[location?.id]}
                                    </td>
                                </g:each>
                                <td class="center">
                                    ${totalQuantity}
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
  $(document).ready(function() {

    $(".dataTable").dataTable({
      "bJQueryUI": true,
      "sPaginationType": "full_numbers",
      "iDisplayLength": 25
    });

  });
</script>

</body>
</html>
