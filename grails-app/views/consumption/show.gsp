<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'consumption.label', default: 'Consumption').toLowerCase()}" />
    <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${command}">
            <div class="errors">
                <g:renderErrors bean="${command}" as="list" />
            </div>
        </g:hasErrors>
        <div class="yui-gf">
            <div class="yui-u first">
                <g:render template="filters" model="[command:command]"/>
            </div>
            <div class="yui-u">

                ${command.productMap?.keySet()?.size()}

                <div class="box">
                    <h2><warehouse:message code="consumption.label" default="Consumption"/></h2>
                    <table>
                        <thead>
                            <tr>
                                <th class="center"><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.name.label"/></th>
                                <th class="center"><warehouse:message code="product.unitOfMeasure.label"/></th>
                                <th class="center"><warehouse:message code="consumption.quantity.label" default="Quantity consumed"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:unless test="${command?.productMap}">
                                <tr class="prop">
                                    <td colspan="4" class="empty center">
                                        <warehouse:message code="default.empty.label"/>
                                    </td>
                                </tr>
                            </g:unless>
                            <g:each var="entry" in="${command.productMap}" status="i">
                                <tr class="prop ${i%2?'odd':'even'}">
                                    <td class="center">
                                        ${entry?.key?.productCode}

                                    </td>
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${entry?.key?.id}">
                                            ${entry?.key?.name}
                                        </g:link>
                                    </td>
                                    <td class="center">
                                        <g:link controller="inventoryItem" action="showStockCard" id="${entry?.key?.id}">
                                            ${entry?.key?.unitOfMeasure}
                                        </g:link>
                                    </td>
                                    <td class="center">
                                        ${entry.value}
                                    </td>
                                    <td>

                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
     </div>
</body>
</html>