
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
                <g:render template="summary" model="[requisition:requisition]"/>
                <div id="tabs-details">
                    <table>
                        <tbody>
                            <tr class='prop'>
                                <td valign='top' class='name'>
                                    <label for='source'><warehouse:message code="requisition.status.label"/></label>
                                </td>
                                <td valign='top' class='value'>
                                    ${requisition?.status?.encodeAsHTML()}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for='description'><warehouse:message code="default.description.label" default="Description" /></label>
                                </td>

                                <td valign="top" class="value">${fieldValue(bean: requestInstance, field: "name")}</td>

                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='source'><warehouse:message code="requisition.requestFrom.label"/></label></td>
                                <td valign='top' class='value'>
                                    ${requisition?.origin?.name?.encodeAsHTML()}
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for="destination"><warehouse:message code="requisition.requestFor.label"/></label></td>
                                <td valign='top' class='value'>
                                    ${requisition?.destination?.name?.encodeAsHTML()}
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for="recipientProgram"><warehouse:message code="requisition.recipientProgram.label"/></label></td>
                                <td valign='top' class='value'>
                                    ${requisition?.recipientProgram }
                                </td>
                            </tr>

                            <tr class='prop'>
                                <td valign='top' class='name'><label for="recipient"><warehouse:message code="requisition.recipient.label"/></label></td>
                                <td valign='top' class='value'>
                                    ${requisition?.recipient?.name}
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='requestedBy'><warehouse:message code="requisition.requestedBy.label"/></label></td>
                                <td valign='top'class='value'>
                                    ${requisition?.requestedBy?.name }
                                </td>
                            </tr>
                            <tr class="prop">
                                <td> </td>
                            </tr>
                            <tr class="prop">
                                <td colspan="2">
                                    <table>
                                        <thead>
                                            <tr class="odd">
                                                <th><warehouse:message code="default.type.label"/></th>
                                                <th><warehouse:message code="product.label"/></th>
                                                <th><warehouse:message code="requisition.quantity.label"/></th>
                                                <th><warehouse:message code="picklist.quantity.label"/></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <g:if test="${requisition?.requisitionItems?.size() == 0}">
                                            <tr class="prop odd">
                                                <td colspan="4" class="center">
                                                    <warehouse:message code="requisition.noRequisitionItems.message"/>
                                                </td>
                                            </tr>
                                        </g:if>
                                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                                                <td>
                                                    <g:if test="${requisitionItem?.product }">
                                                        <warehouse:message code="product.label"/>
                                                    </g:if>
                                                    <g:elseif test="${requisitionItem?.productGroup }">
                                                        <warehouse:message code="productGroup.label"/>
                                                    </g:elseif>
                                                    <g:elseif test="${requisitionItem?.category }">
                                                        <warehouse:message code="category.label"/>
                                                    </g:elseif>
                                                    <g:else>
                                                        <warehouse:message code="default.unclassified.label"/>
                                                    </g:else>
                                                </td>
                                                <td>
                                                    %{--<format:metadata obj="${requisitionItem.displayName()}"/>--}%
                                                </td>
                                                <td>
                                                    ${requisitionItem?.quantity}
                                                </td>
                                                <td>
                                                    %{--<g:set var="quantityPicked" value="${requisitionItem?.picklistItems?.sum { it.quantity }?:0 }"/>--}%
                                                    %{--${ quantityPicked }--}%
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </body>
</html>
