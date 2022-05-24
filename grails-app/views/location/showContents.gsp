<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title> </title>
</head>

<body>

<div class="dialog">
        <div class="center">

            <table id="locationSummary" border="0">
                <tbody>
                    <tr>
                        <td>
                            <p>
                                <label><g:message code="location.name.label"/></label> ${binLocation.name}
                            </p>
                            <p>
                                <label><g:message code="location.locationNumber.label"/></label> ${binLocation.locationNumber}
                            </p>
                            <p>
                                <label><g:message code="location.locationType.label"/></label> ${binLocation.locationType?.name}
                            </p>
                            <p>
                                <label><g:message code="location.zone.label"/></label> ${binLocation.zone?.name?:g.message(code: 'default.none.label')}
                            </p>
                        </td>
                        <td class="center" width="1%">
                            <g:if test="${binLocation?.locationNumber}">
                                <g:displayBarcode showData="${true}" data="${binLocation?.locationNumber}"/>
                            </g:if>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
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
                    <td>${entry?.inventoryItem?.lotNumber?:g.message(code: 'default.label')}</td>
                    <td>
                        <g:if test="${entry?.inventoryItem?.expirationDate}">
                            <g:formatDate date="${entry?.inventoryItem?.expirationDate}" format="dd MMM yyyy"/>
                        </g:if>
                        <g:else>
                            <g:message code="default.never.label"/>
                        </g:else>
                    </td>
                    <td>${entry?.quantity}</td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <g:unless test="${contents}">
        <div class="empty center fade">
            <g:message code="default.noItems.label" />
        </div>
    </g:unless>
</div>
</body>
</html>
