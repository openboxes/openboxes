<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <style>
            @page {
                size: letter;
                background: white;
                @top-center { content: element(header) }
                @bottom-center { content: element(footer) }
            }
            body {
                font: 11px "Inter", sans-serif;
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
            span.page:before {
                content: counter(page);
            }
            span.pagecount:before {
                content: counter(pages);
            }
            table {
                width: 100%;
                border-collapse: collapse;
                table-layout: fixed;
                border-spacing: 0;
                margin: 5px;
                -fs-table-paginate: paginate;
            }
            .content table {
                margin-bottom: 20px;
            }
            thead { display: table-header-group; }
            tr { page-break-inside: avoid; page-break-after: auto; }
            td { vertical-align: top; padding: 5px; border: 1px solid black; word-wrap: break-word; }
            th { font-weight: bold; padding: 5px; border: 1px solid black; }
            .no-border-table td, .no-border-table th { border: 0; }
            .small-font {font-size: xx-small; }
            .b-t0 { border-top: 0; }
            .b-r0 { border-right: 0; }
            .b-b0 { border-bottom: 0; }
            .b-l0 { border-left: 0; }
            .no-padding { padding: 0; }
        </style>
    </head>

    <body>
        <div class="header">
            <div id="page-header" class="small-font">
                <table class="no-border-table no-padding">
                    <tr>
                        <td align="left" class="no-padding">${g.message(code: 'cycleCount.facility.label')}: ${facilityName}</td>
                        <td align="right" class="no-padding">${g.message(code: 'cycleCount.datePrinted.label')}: <g:formatDate date="${datePrinted}" format="MMM d, yyyy"/></td>
                    </tr>
                </table>
            </div>
        </div>

        <div class="footer">
            <div id="page-footer" class="small-font">
                <div>
                    Page <span class="page" /> of <span class="pagecount" />
                </div>
            </div>
        </div>

        <div class="content">
            <g:each var="cycleCount" in="${cycleCounts}">
                <g:set var="firstItem" value="${cycleCount.cycleCountItems?.find { it.countIndex == 0 }}"/>
                <g:set var="product" value="${firstItem?.product}"/>
                <g:set var="assignee" value="${firstItem?.assignee}"/>
                <g:set var="dateCounted" value="${firstItem?.dateCounted}"/>
                <table>
                    <thead>
                        <tr>
                            <th colspan="4" class="b-r0 b-b0" style="padding: 10px 0 10px 10px;">
                                ${g.message(code: 'cycleCount.product.label')}
                                ${product?.name}
                            </th>
                            <th align="right" class="b-l0 b-b0" style="padding: 10px 10px 10px 0;">
                                ${g.message(code: 'cycleCount.productCode.label')}
                                ${product?.productCode}
                            </th>
                        </tr>
                        <tr>
                            <th colspan="2" class="b-r0 b-t0" style="padding: 0 0 10px 10px;">
                                ${g.message(code: 'cycleCount.dateCounted.label')}:
                                <g:formatDate date="${dateCounted}" format="MMM d, yyyy"/>
                            </th>
                            <th colspan="2" class="b-r0 b-l0 b-t0" style="padding-bottom: 10px;">
                                ${g.message(code: 'cycleCount.userCounted.label')}:
                                ${assignee}
                            </th>
                            <th class="b-l0 b-t0" style="padding-bottom: 10px;"></th>
                        </tr>
                        <tr>
                            <th>${g.message(code: 'cycleCount.binLocation.label')}</th>
                            <th>${g.message(code: 'cycleCount.lotNumber.label')}</th>
                            <th>${g.message(code: 'cycleCount.expirationDate.label')}</th>
                            <th>${g.message(code: 'cycleCount.quantity.label')}</th>
                            <th>${g.message(code: 'cycleCount.comments.label')}</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:unless test="${cycleCount.cycleCountItems}">
                        <tr>
                            <td align="center" colspan="5">
                                <span>
                                    <warehouse:message code="default.none.label"/>
                                </span>
                            </td>
                        </tr>
                    </g:unless>
                    <g:each in="${cycleCount.cycleCountItems.findAll { it.countIndex == 0 }}" status="i" var="cycleCountItem">
                        <tr>
                            <td>${cycleCountItem.binLocation?.name}&nbsp;</td>
                            <td>${cycleCountItem.inventoryItem?.lotNumber}&nbsp;</td>
                            <td><g:formatDate date="${cycleCountItem.inventoryItem?.expirationDate}" format="MMM d, yyyy"/>&nbsp;</td>
                            <td></td>
                            <td></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </g:each>
        </div>
    </body>
</html>
