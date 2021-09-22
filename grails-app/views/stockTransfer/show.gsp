<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'inventory.stockTransfers.label', default: 'Stock Transfers')}" />
    <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">

        <g:render template="summary" model="[orderInstance:orderInstance,currentState:'showOrder']"/>

        <g:hasErrors bean="${orderInstance}">
            <div class="errors">
                <g:renderErrors bean="${orderInstance}" as="list" />
            </div>
        </g:hasErrors>

        <div class="yui-gf">

            <div class="yui-u first">
                <div id="details" class="box">
                    <h2>
                        <warehouse:message code="order.orderHeader.label" default="Order Header"/>
                    </h2>
                    <table>
                        <tbody>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="inventory.stockTransfers.orderNumber.label"/></label>
                            </td>
                            <td valign="top" class="value">
                                ${orderInstance?.orderNumber}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="default.status.label" /></label>
                            </td>
                            <td valign="top" id="status" class="value">
                                <format:metadata obj="${orderInstance?.status}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="location.label"/></label>
                            </td>
                            <td valign="top" class="value">
                                ${orderInstance?.origin?.name}
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div class="box">
                    <h2><g:message code="default.auditing.label"/></h2>
                    <table>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="order.createdBy.label"/></label>
                            </td>
                            <td valign="top" class="value">
                                <div>${orderInstance?.createdBy?.name }</div>
                                <small><format:date obj="${orderInstance?.dateCreated}"/></small>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="default.updatedBy.label"/></label>
                            </td>
                            <td valign="top" class="value">
                                <div>${orderInstance?.updatedBy?.name }</div>
                                <small><format:date obj="${orderInstance?.lastUpdated}"/></small>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="order.completedBy.label"/></label>
                            </td>
                            <td valign="top" class="value">
                                <g:if test="${orderInstance?.completedBy}">
                                    <div>${orderInstance?.completedBy?.name }</div>
                                    <small><format:date obj="${orderInstance?.dateCompleted}"/></small>
                                </g:if>
                                <g:else>
                                    <g:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="yui-u">
                <div class="tabs tabs-ui">
                    <ul>
                        <li><a href="#tabs-summary"><warehouse:message code="default.summary.label" default="Summary"/></a></li>
                    </ul>
                    <div id="tabs-summary" class="ui-tabs-hide">
                        <g:render template="orderSummary"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
  $(document).ready(function() {
    $(".tabs").tabs({
      cookie: {
        expires: 1
      },
      selected: ${params.tab ? params.tab : 0}
    });
  });
</script>
</body>
</html>
