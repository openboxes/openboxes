<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <style>

        thead {display: table-header-group;}
        tr {page-break-inside: avoid; page-break-after: auto;}
        td {vertical-align: top; }
        th { background-color: lightgrey; font-weight: bold;}
        body { font-size: 11px; }

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
            ${requisition.requestNumber} - ${requisition?.name }
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
                <td style="white-space: nowrap;">
                    ${requisition.requestNumber}

                </td>
                <td style="white-space: nowrap;">
                    ${requisition?.name }
                </td>
            </tr>
        </table>

        <div>
            <h2>${warehouse.message(code:"default.details.label", default: 'Details')}</h2>
            <table class="border">
                <tr>
                    <td style="white-space: nowrap;">
                        <label><warehouse:message code="requisition.origin.label"/>:</label>
                    </td>
                    <td style="white-space: nowrap;">
                        ${requisition.origin?.name}
                    </td>
                </tr>
                <tr>
                    <td style="white-space: nowrap;">
                        <label><warehouse:message code="requisition.destination.label"/>:</label>
                    </td>
                    <td style="white-space: nowrap;">
                        ${requisition.destination?.name}
                    </td>
                </tr>

                <tr>
                    <td style="white-space: nowrap;">
                        <label><warehouse:message code="requisition.date.label"/>:</label>
                    </td>
                    <td style="white-space: nowrap;">
                        <g:formatDate
                                date="${requisition?.dateRequested}" format="MMM d, yyyy  hh:mma"/>
                    </td>
                </tr>
                <tr>
                    <td style="white-space: nowrap;">
                        <label><g:message code="picklist.datePrinted.label" default="Date printed"/>:</label>
                    </td>
                    <td style="white-space: nowrap;">
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
                <th width="15%"></th>
                <th width="20%"><warehouse:message code="default.name.label"/></th>
                <th width="40%"><warehouse:message code="default.signature.label"/></th>
                <th width="15%" class="center"><warehouse:message code="default.date.label"/></th>
                <th width="10%" class="center"><warehouse:message code="default.time.label"/></th>
            </tr>
            <tr>
                <td style="white-space: nowrap;">
                    <label><warehouse:message code="requisition.requestedBy.label"/></label>
                </td>
                <td style="white-space: nowrap;">
                    ${requisition?.requestedBy?.name}
                </td>
                <td>

                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateRequested}" format="MMM d, yyyy"/>
                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateRequested}" format="hh:mma"/>
                </td>
            </tr>
            <tr>
                <td style="white-space: nowrap;">
                    <label><warehouse:message code="requisition.createdBy.label"/></label>
                </td>
                <td style="white-space: nowrap;">
                    ${requisition?.createdBy?.name}
                </td>
                <td>

                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateCreated}" format="MMM d, yyyy"/>
                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateCreated}" format="hh:mma"/>
                </td>
            </tr>
            <tr>
                <td style="white-space: nowrap;">
                    <label><warehouse:message code="requisition.verifiedBy.label"/></label>
                </td>
                <td style="white-space: nowrap;">
                    ${requisition?.verifiedBy?.name}
                </td>
                <td>

                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateVerified}" format="MMM d, yyyy"/>
                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateVerified}" format="hh:mma"/>
                </td>
            </tr>
            <tr>
                <td style="white-space: nowrap;">
                    <label><warehouse:message code="requisition.pickedBy.label"/></label>
                </td>
                <td style="white-space: nowrap;">
                    ${picklist?.picker?.name}
                </td>
                <td>

                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${picklist?.datePicked}" format="MMM d, yyyy"/>
                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${picklist?.datePicked}" format="hh:mma"/>
                </td>
            </tr>
            <tr>
                <td style="white-space: nowrap;">
                    <label><warehouse:message code="requisition.reviewedBy.label" default="Checked by"/></label>
                </td>
                <td style="white-space: nowrap;">
                    ${requisition?.checkedBy?.name}
                </td>
                <td>

                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateChecked}" format="MMM d, yyyy"/>
                </td>
                <td style="white-space: nowrap;">
                    <g:formatDate date="${requisition?.dateChecked}" format="hh:mma"/>
                </td>
            </tr>
        </table>
    </div>

    <g:set var="allRequisitionItems" value='${requisition.requisitionItems.sort { it.product.name }}'/>
    <g:set var="allPickListItems" value='${allRequisitionItems*.retrievePicklistItems()?.flatten()}'/>
    <g:set var="zoneNames" value='${allPickListItems?.collect { it?.binLocation?.zone?.name }?.unique()?.sort{ a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}'/>
    <g:set var="allPickListItems" value='${allRequisitionItems*.retrievePicklistItems()?.flatten()}'/>
    <g:set var="pickListItemsByZone" value='${allPickListItems?.groupBy { it?.binLocation?.zone?.name } ?: [:]}'/>

    <g:each var="zoneName" status="i" in="${zoneNames}">

        <g:set var="pickListItemsByRequisition" value='${pickListItemsByZone[zoneName]?.groupBy { it?.requisitionItem?.id } ?: [:]}'/>

        <g:if test="${!zoneName}">
            <g:set var="requisitionItems" value='${allRequisitionItems.findAll { !it.picklistItems?.size() || pickListItemsByRequisition[it.id]?.size() }}'/>
        </g:if>
        <g:else>
            <g:set var="requisitionItems" value='${allRequisitionItems.findAll { pickListItemsByRequisition[it.id]?.size() }}'/>
        </g:else>

        <g:set var="requisitionItemsCanceled" value='${requisitionItems.findAll { it.isCanceled()}}'/>
        <g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
        <g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
        <g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance}}'/>
        <g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial}}'/>
        <g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

        <g:set var="showZoneName" value='${zoneName || zoneNames.size() > 1}'/>

        <div class="page-content" style="page-break-after: ${showZoneName && i < zoneNames.size() - 1 ? 'always':'avoid'};">
            <g:if test="${showZoneName}">
                <h1 class="subtitle">
                    ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
                </h1>
            </g:if>

            <g:if test="${requisitionItemsColdChain}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
                </g:set>
                <g:render template="/picklist/page" model="[pageTitle: pageTitle, requisitionItems:requisitionItemsColdChain, pickListItemsByRequisition: pickListItemsByRequisition, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${requisitionItemsControlled}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled substance')}
                </g:set>
                <g:render template="/picklist/page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsControlled, pickListItemsByRequisition: pickListItemsByRequisition, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${requisitionItemsHazmat}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous material')}
                </g:set>
                <g:render template="/picklist/page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsHazmat, pickListItemsByRequisition: pickListItemsByRequisition, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${requisitionItemsOther}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}
                </g:set>
                <g:render template="/picklist/page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsOther, pickListItemsByRequisition: pickListItemsByRequisition, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
            <g:if test="${requisitionItemsCanceled}">
                <g:set var="pageTitle">
                    ${warehouse.message(code:'default.canceled.label', default:'Canceled items')}
                </g:set>
                <g:render template="/picklist/page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsCanceled, pickListItemsByRequisition: pickListItemsByRequisition, location: location, picklist:picklist, pageBreakAfter: 'avoid', sorted:sorted]"/>
            </g:if>
        </div>
    </g:each>
</div>

</body>
</html>
