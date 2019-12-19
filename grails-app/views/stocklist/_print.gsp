<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode" contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <style>

        table {
            border-collapse: collapse;
            page-break-inside: auto;
            -fs-table-paginate: paginate;
            border-spacing: 0;
            margin: 5px;
        }
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
        .canceled { text-decoration: line-through; }
        .page-start {
            -fs-page-sequence: start;
            page-break-before: avoid;
        }

        .page-content { page-break-after: avoid; }
        .page-header { page-break-before: avoid; }
        .break {page-break-after:always}
        span.page:before { content: counter(page); }
        span.pagecount:before { content: counter(pages); }
        body { font: 11px "lucida grande", verdana, arial, helvetica, sans-serif; }
        table td, table th {
            padding: 5px;
            border: 1px solid black;
            vertical-align: middle;
        }
        .first-line {
            display: flex;
            justify-content: space-between;
        }

        .no-border-table td, .no-border-table th { border: 0 !important; }
        .b-0 { border: 0 !important; }
        .b-t0 { border-top: 0 !important; }
        .b-r0 { border-right: 0 !important; }
        .b-b0 { border-bottom: 0 !important; }
        .b-l0 { border-left: 0 !important; }
        .no-padding { padding: 0 !important; }
        .w100 { width: 100% !important; }
        .no-wrap { white-space: nowrap; }
        .gray-background { background-color: #ddd !important; }
        .fixed-layout { table-layout: fixed; }
    </style>

</head>

<body>
    <div class="header">
        <div id="page-header" class="small">
            <table class="no-border-table no-padding w100">
                <tr class="w100">
                    <td align="left" class="no-padding">${stocklist?.destination} ${stocklist?.requisition?.name}</td>
                    <td align="right" class="no-padding">${warehouse.message(code: 'report.lastUpdated.label')}: <g:formatDate date="${stocklist?.requisition?.lastUpdated}" format="MMM d, yyyy"/></td>
                </tr>
                <tr>
                    <td colspan="2" class="w100 no-padding" align="left">${stocklist?.requisition?.replenishmentPeriod?:"0"} ${warehouse.message(code: 'report.dayReplenishment.label')}</td>
                </tr>
            </table>
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
         <table class="w100 fixed-layout">
            <tr>
                <%-- Icon and title --%>
                <td colspan="2" class="b-0">
                    <table class="no-border-table">
                        <tr>
                            <td>
                                <g:displayReportLogo/>
                            </td>
                            <td>
                                <h3>${warehouse.message(code: 'report.stockRequisition.label')}: ${stocklist?.requisition?.name} </h3>
                                <h3>${stocklist?.destination}</h3>
                            </td>
                        </tr>
                    </table>
                </td>
                <%-- Table for warehouse use --%>
                <td rowspan="2" align="center" class="b-0">
                    <table class="w100 no-wrap gray-background">
                        <tr>
                            <td colspan="2" align="center" class="b-b0">${warehouse.message(code: 'report.forWarehouseUse.label')}:</td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0 b-b0">${warehouse.message(code: 'deliveryNote.approvedBy.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0 b-b0">${warehouse.message(code: 'default.signature.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0 b-b0">${warehouse.message(code: 'default.date.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0 b-b0">${warehouse.message(code: 'requisition.processedBy.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0 b-b0">${warehouse.message(code: 'default.signature.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0 b-b0">${warehouse.message(code: 'default.date.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td align="left" class="b-r0">${warehouse.message(code: 'requisition.requisitionNumber.label')}:</td>
                            <td class="w100"></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <%-- Signatures table 1 --%>
                <td class="b-0">
                    <table class="w100 no-wrap">
                        <tr>
                            <td class="b-r0 b-b0">${warehouse.message(code: 'default.date.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td class="b-r0 b-b0">${warehouse.message(code: 'requisition.requestedBy.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td class="b-r0">${warehouse.message(code: 'default.signature.label')}:</td>
                            <td class="w100"></td>
                        </tr>
                    </table>
                </td>
                <%-- Signatures table 2 --%>
                <td class="b-0">
                    <table class="w100 no-wrap">
                        <tr>
                            <td class="b-r0 b-b0">${warehouse.message(code: 'default.date.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td class="b-r0 b-b0">${warehouse.message(code: 'deliveryNote.approvedBy.label')}:</td>
                            <td class="w100 b-b0"></td>
                        </tr>
                        <tr>
                            <td class="b-r0">${warehouse.message(code: 'default.signature.label')}:</td>
                            <td class="w100"></td>
                        </tr>
                    </table>
                </td>
            </tr>
         </table>

        <g:set var="sortByCode" value="${stocklist?.requisition?.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX}"/>

        <%-- Stock list items table --%>
        <g:set var="requisitionItems" value='${stocklist?.requisition?."$sortByCode.methodName"}'/>

        <div>
            <g:if test="${requisitionItems}">
                <g:render template="/stocklist/itemList" model="[requisitionItems:requisitionItems, stocklist:stocklist]"/>
            </g:if>
        </div>
    </div>
</body>
</html>
