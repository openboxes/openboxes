<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>

<h1>
    ${shipmentInstance?.shipmentNumber} ${shipmentInstance?.name}
    <rendering:inlineJpeg bytes="${shipmentNumberBytes}" width="170"/>
</h1>
<hr/>

<table>

<g:each in="${shipmentItems}" var="shipmentItem">
    <tr>
        <td>
            <rendering:inlineJpeg bytes="${shipmentItem?.productCodeBytes}" width="170"/>
        </td>
        <td>
            ${shipmentItem?.productCode} ${shipmentItem?.productName}
        </td>
        <td>
            <rendering:inlineJpeg bytes="${shipmentItem?.lotNumberBytes}" width="170"/>
        </td>
    </tr>
</g:each>
</table>

</body>
</html>
