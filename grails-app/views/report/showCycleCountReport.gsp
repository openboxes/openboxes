<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="report.cycleCountReport.label" /></title>
    <style>
        .download {
            display: inline-block;
            margin: 5px;
        }
    </style>
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
        <div>
            <div class="box" style="padding: 5px;">
                <div>
                    <warehouse:message code="report.cycleCountReport.welcome.label"/>
                </div>
                <div>
                    <warehouse:message code="report.cycleCountReport.instructions.label"/>
                </div>
            </div>
            <div class="box">
                <h2>
                    ${warehouse.message(code:'report.cycleCountReport.label')}
                    <div class="download right center">
                        <g:link controller="report" action="showCycleCountReport" params="[print:true]" class="button">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
                            ${warehouse.message(code: 'default.button.download.label')}
                        </g:link>
                    </div>
                </h2>
                <div >
                    <table class="dataTable">
                        <thead>
                        <tr>
                            <th><warehouse:message code="product.productCode.label" default="Code"/></th>
                            <th><warehouse:message code="product.label" default="Product"/></th>
                            <th><warehouse:message code="product.genericProduct.label" default="Generic Product"/></th>
                            <th><warehouse:message code="product.primaryCategory.label" default="Category"/></th>
                            <th><warehouse:message code="catalogs.label" default="Formularies"/></th>
                            <th><warehouse:message code="inventory.lotNumber.label" default="Lot number"/></th>
                            <th><warehouse:message code="import.expirationDate.label" default="Expiration date"/></th>
                            <th><warehouse:message code="product.abcClass.label" default="ABC Classification"/></th>
                            <th><warehouse:message code="inventoryItem.binLocation.label" default="Bin location"/></th>
                            <th><warehouse:message code="default.status.label" default="Status"/></th>
                            <th><warehouse:message code="inventory.lastInventoryDate.label" default="Last inventory date"/></th>
                            <th><warehouse:message code="product.QoH.label" default="QoH"/></th>
                        </tr>
                        </thead>
                        <tbody>
                            <g:each var="row" in="${rows}">
                                <tr>
                                    <td>
                                        ${row?.productCode}
                                    </td>
                                    <td>
                                        ${row?.productName}
                                    </td>
                                    <td>
                                        ${row?.genericProduct}
                                    </td>
                                    <td>
                                        ${row?.category}
                                    </td>
                                    <td>
                                        ${row?.formularies}
                                    </td>
                                    <td>
                                        ${row?.lotNumber}
                                    </td>
                                    <td>
                                        ${row?.expirationDate}
                                    </td>
                                    <td>
                                        ${row?.abcClassification}
                                    </td>
                                    <td>
                                        ${row?.binLocation}
                                    </td>
                                    <td>
                                        ${row?.status}
                                    </td>
                                    <td>
                                        ${row?.lastInventoryDate}
                                    </td>
                                    <td>
                                        ${row?.quantityOnHand}
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
