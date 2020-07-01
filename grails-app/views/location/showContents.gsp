<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title> </title>
    <div style="font-weight: bold; margin: 5px"> ${binLocation.name} </div>
</head>

<body>

<div class="dialog">
    <fieldset>
        <table>
            <thead>
                <tr>
                    <th><g:message code="product.label"/></th>
                    <th><g:message code="inventoryItem.lotNumber.label"/></th>
                    <th><g:message code="inventoryItem.expirationDate.label"/></th>
                    <th><g:message code="default.quantity.label"/></th>
                </tr>
            </thead>
            <tbody>
                <g:each var="entry" in="${contents}" status="status">
                    <tr class="${status%2?'even':'odd'}">


                        <td>
                            <g:link controller="inventoryItem" action="showStockCard" id="${entry?.product?.id}">
                                ${entry?.product?.productCode}
                                ${entry?.product?.name}
                            </g:link>
                        </td>
                        <td>${entry?.inventoryItem?.lotNumber}</td>
                        <td><g:formatDate date="${entry?.inventoryItem?.expirationDate}" format="MMM yyyy"/></td>
                        <td>${entry?.quantity}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
        <g:unless test="${contents}">
            <div class="emtpty center fade">
                <g:message code="default.empty.label" default="Empty"/>
            </div>
        </g:unless>
    </fieldset>
</div>
</body>
</html>
