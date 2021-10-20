<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <style>

        table {border-collapse: collapse; page-break-inside: auto;}
        thead {display: table-header-group;}
        tr {page-break-inside: avoid; page-break-after: auto;}
        td {vertical-align: top; }
        th { background-color: lightgrey; font-weight: bold;}
        body { font-size: 11px; }
        * {
            padding: 0;
            margin: 0;
        }
        div.header {
            display: block;
            text-align: center;
            position: running(header);
        }
        div.footer {
            display: block;
            text-align: center;
            position: running(footer);
        }

        @page {
            size: letter; /*letter landscape*/
            background: white;
            @top-center { content: element(header) }
            @bottom-center { content: element(footer) }
        }
        .small {font-size: xx-small;}
        .line{border-bottom: 1px solid black}
        .logo { width: 20px; height: 20px; }
        .canceled { text-decoration: line-through; }
        .page-start {
            -fs-page-sequence: start;
            page-break-before: avoid;
        }
        table {
            -fs-table-paginate: paginate;
            page-break-inside: avoid;
            border-collapse: collapse;
            border-spacing: 0;
            margin: 5px;

        }
        .page-content {
            page-break-before: always;
            page-break-after: avoid;

        }

        .page-header {
            page-break-before: avoid;
        }

        /* forces a page break */
        .break {page-break-after:always}

        span.page:before { content: counter(page); }
        span.pagecount:before { content: counter(pages); }
        body {
            font: 11px "lucida grande", verdana, arial, helvetica, sans-serif;
        }
        table td, table th {
            padding: 5px;
            border: 1px solid black;
        }
    </style>

</head>

<body>

    <div class="header">
        <div id="page-header" class="small">
            ${order.orderNumber} - ${order?.name }
        </div>
    </div>

    <div class="footer">
        <div id="page-footer" class="small">
            <div>
                Page <span class="page" /> of <span class="pagecount" />
            </div>
        </div>
    </div>

    <div class="content">
        <h1><warehouse:message code="picklist.label"/></h1>
        <table>
            <tr>
                <td>
                    <g:displayReportLogo/>
                </td>
                <td>
                    ${order.orderNumber}

                </td>
                <td>
                    ${order?.name }
                </td>
            </tr>
        </table>

        <div>
            <h2>${warehouse.message(code:"default.details.label", default: 'Details')}</h2>
            <table class="border">
                <tr>
                    <td>
                        <label><warehouse:message code="order.origin.label"/>:</label>
                    </td>
                    <td>
                        ${order.origin?.name}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label><warehouse:message code="order.destination.label"/>:</label>
                    </td>
                    <td>
                        ${order.destination?.name}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label><warehouse:message code="picklist.datePrinted.label" default="Date printed"/>:</label>
                    </td>
                    <td>
                        <g:formatDate
                            date="${new Date()}" format="MMM d, yyyy hh:mma"/>
                    </td>
                </tr>

            </table>
        </div>

    <div class="center">
        <h2>${warehouse.message(code:"default.signatures.label", default: 'Signatures')}</h2>
        <table>
            <tr>
                <td width="15%"></td>
                <td width="20%"><warehouse:message code="default.name.label"/></td>
                <td width="40%"><warehouse:message code="default.signature.label"/></td>
                <td width="15%" class="center"><warehouse:message code="default.date.label"/></td>
                <td width="10%" class="center"><warehouse:message code="default.time.label"/></td>
            </tr>
            <tr>
                <td>
                    <label><warehouse:message code="order.orderedBy.label"/></label>
                </td>
                <td>
                    ${order?.orderedBy?.name}
                </td>
                <td>

                </td>
                <td>
                    <g:formatDate date="${order?.dateOrdered}" format="MMM d, yyyy"/>
                </td>
                <td>
                    <g:formatDate date="${order?.dateOrdered}" format="hh:mma"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label><warehouse:message code="order.createdBy.label"/></label>
                </td>
                <td>
                    ${order?.createdBy?.name}
                </td>
                <td>

                </td>
                <td>
                    <g:formatDate date="${order?.dateCreated}" format="MMM d, yyyy"/>
                </td>
                <td>
                    <g:formatDate date="${order?.dateCreated}" format="hh:mma"/>
                </td>
            </tr>
        </table>
    </div>

    <g:set var="allOrderItems" value='${order.orderItems.sort { it.product.name }}'/>
    <g:set var="allPickListItems" value='${allOrderItems*.retrievePicklistItems()?.flatten()}'/>
    <g:set var="zoneNames" value='${allPickListItems?.collect { it?.binLocation?.zone?.name }?.unique()?.sort{ a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}'/>
    <g:set var="allPickListItems" value='${allOrderItems*.retrievePicklistItems()?.flatten()}'/>
    <g:set var="pickListItemsByZone" value='${allPickListItems?.groupBy { it?.binLocation?.zone?.name } ?: [:]}'/>

    <g:each var="zoneName" status="i" in="${zoneNames}">

        <g:set var="pickListItemsByOrder" value='${pickListItemsByZone[zoneName]?.groupBy { it?.orderItem?.id } ?: [:]}'/>

        <g:if test="${!zoneName}">
            <g:set var="orderItems" value='${allOrderItems.findAll { !it.picklistItems?.size() || pickListItemsByOrder[it.id]?.size() }}'/>
        </g:if>
        <g:else>
            <g:set var="orderItems" value='${allOrderItems.findAll { pickListItemsByOrder[it.id]?.size() }}'/>
        </g:else>

        <g:set var="orderItemsColdChain" value='${orderItems.findAll { it?.product?.coldChain }}'/>
        <g:set var="orderItemsControlled" value='${orderItems.findAll {it?.product?.controlledSubstance}}'/>
        <g:set var="orderItemsHazmat" value='${orderItems.findAll {it?.product?.hazardousMaterial}}'/>
        <g:set var="orderItemsOther" value='${orderItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

        <g:set var="showZoneName" value='${zoneName || zoneNames.size() > 1}'/>

        <div class="page-content" style="page-break-after: ${showZoneName && i < zoneNames.size() - 1 ? 'always':'avoid'};">
            <g:if test="${showZoneName}">
                <h1 class="subtitle">
                    ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
                </h1>
            </g:if>

            <g:if test="${orderItemsColdChain}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
                </g:set>
                <g:render template="/picklist/returnPage" model="[pageTitle: pageTitle, orderItems:orderItemsColdChain, pickListItemsByOrder: pickListItemsByOrder, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${orderItemsControlled}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled substance')}
                </g:set>
                <g:render template="/picklist/returnPage" model="[pageTitle: pageTitle,orderItems:orderItemsControlled, pickListItemsByOrder: pickListItemsByOrder, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${orderItemsHazmat}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous material')}
                </g:set>
                <g:render template="/picklist/returnPage" model="[pageTitle: pageTitle,orderItems:orderItemsHazmat, pickListItemsByOrder: pickListItemsByOrder, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${orderItemsOther}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}
                </g:set>
                <g:render template="/picklist/returnPage" model="[pageTitle: pageTitle,orderItems:orderItemsOther, pickListItemsByOrder: pickListItemsByOrder, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
        </div>
    </g:each>
</div>

</body>
</html>
