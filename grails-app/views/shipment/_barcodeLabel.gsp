<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <style>
    .barcode {
        font-family: monospace;
        font-size: 20px;
        text-align: center;
    }

    .barcode img {
        display: block;
        margin-left: auto;
        margin-right: auto;
        width: 180px;
    }
    </style>
    <title>
    ${shipmentInstance?.shipmentNumber} ${shipmentInstance?.name}
    </title>
</head>

<body>

<h1>
    ${shipmentInstance?.shipmentNumber} ${shipmentInstance?.name}
</h1>

<div class="barcode">
    <rendering:inlinePng bytes="${shipmentNumberBytes}" />
    ${shipmentInstance?.shipmentNumber}
</div>

<hr />

<table>
    <g:each in="${shipmentItems}" var="shipmentItem">
        <tr>
            <td class="barcode">
                <rendering:inlinePng bytes="${shipmentItem?.productCodeBytes}" />
                ${shipmentItem?.productCode}
            </td>
            <td>
                ${shipmentItem?.productCode} ${shipmentItem?.productName}
            </td>
            <td class="barcode">
                <rendering:inlinePng bytes="${shipmentItem?.lotNumberBytes}" />
                ${shipmentItem?.lotNumber}
            </td>
        </tr>
    </g:each>
</table>

</body>
</html>
