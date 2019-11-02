<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'buttons.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'openboxes.css')}" type="text/css" media="all" />

    <style>

    body { font: 11px "lucida grande", verdana, arial, helvetica, sans-serif; }
    table {border-collapse: collapse; page-break-inside: auto;}
    thead {display: table-header-group;}

    table td, table th {
        padding: 5px;
        border: 1px solid lightgrey;
        vertical-align: middle;
    }

    .no-border-table td, .no-border-table th { border: 0 !important; }
    .m-0 { margin: 0 !important; }
    .w100 { width: 100% !important; }
    .right { text-align: right; }

    .stockHistory a:link, a:visited    {
        text-decoration:  none;
        color:            black;
        pointer-events: none;
    }

    @media print {
        .print-header { display:none; }
        /* to make sure background color is printed we set box-shadow instead of background-color for odd and even rows */
        .odd {
            box-shadow: inset 0 0 0 1000px #f7f7f7;
        }
        .even {
            box-shadow: inset 0 0 0 1000px #fff;
        }
        .stockHistory td {
            max-width: 200px;
            word-wrap: break-word;
        }
    }

    .print-history { display:none !important; }

    </style>

</head>

<body>

    <div class="print-header">
        <table class="w100 fixed-layout no-border-table">
            <tr>
                <td>
                    <h1 class="m-0">${g.message(code: 'inventory.printStockHistory.label')}</h1>
                </td>
                <td class="right">
                    <div class="button-container" >
                        <a href="#" id="print-button" onclick="window.print()" class="button">
                            ${warehouse.message(code: "default.button.print.label", default:"Print")}
                        </a>

                        <a href="${request.contextPath}/inventoryItem/showStockCard/${commandInstance?.product?.id}" class="button">
                            ${warehouse.message(code: "default.button.close.label")}
                        </a>
                    </div>
                </td>
            </tr>
        </table>
        <hr/>
    </div>

    <div class="content">
            <g:render template="showStockHistoryPrintable" model="[commandInstance:commandInstance, stockHistoryList: stockHistoryList,
                                                           totalBalance:totalBalance, totalCount:totalCount, totalCredit:totalCredit, totalDebit:totalDebit]"/>
    </div>

</body>
</html>
