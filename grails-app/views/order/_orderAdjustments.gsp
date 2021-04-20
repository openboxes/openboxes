<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<div class="box">
    <h2>
        <warehouse:message code="orderAdjustments.label"/>
    </h2>
    <g:if test="${orderInstance?.orderAdjustments }">
        <table class="table table-bordered">
            <thead>
            <tr class="odd">
                <th><warehouse:message code="order.orderItem.label"/></th>
                <th><warehouse:message code="default.type.label"/></th>
                <th><warehouse:message code="default.description.label"/></th>
                <th><warehouse:message code="orderAdjustment.percentage.label"/></th>
                <th><warehouse:message code="orderAdjustment.amount.label"/></th>
                <th><warehouse:message code="orderAdjustment.budgetCode.label"/></th>
                <th><warehouse:message code="invoice.invoiced.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="orderAdjustment" in="${orderInstance.orderAdjustments}" status="status">
                <g:set var="isAdjustmentCanceled" value="${orderAdjustment.canceled}"/>
                <tr class="${status%2==0?'odd':'even'}" style="${isAdjustmentCanceled ? 'background-color: #ffcccb;' : ''}">
                    <td>
                        ${orderAdjustment?.orderItem?.product?:g.message(code:'default.all.label')}
                    </td>
                    <td>
                        ${orderAdjustment?.orderAdjustmentType?.name}
                    </td>
                    <g:if test="${!isAdjustmentCanceled}">
                    <td>
                        ${orderAdjustment.description}
                    </td>
                    <td>
                        ${orderAdjustment.percentage}
                    </td>
                    <td>
                        <g:if test="${orderAdjustment.amount}">
                            <g:formatNumber number="${orderAdjustment.amount}"/>
                        </g:if>
                        <g:elseif test="${orderAdjustment.percentage}">
                            <g:if test="${orderAdjustment.orderItem}">
                                <g:formatNumber number="${orderAdjustment.orderItem.totalAdjustments}"/>
                            </g:if>
                            <g:else>
                                <g:formatNumber number="${orderAdjustment.totalAdjustments}"/>
                            </g:else>
                        </g:elseif>
                    </td>
                    <td>
                        ${orderAdjustment?.budgetCode?.code}
                    </td>
                    <td>
                        ${orderAdjustment?.isInvoiced ? g.message(code:'default.yes.label') : g.message(code:'default.no.label')}
                    </td>
                    </g:if>
                    <g:else>
                        <td colspan="6"></td>
                    </g:else>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <th colspan="4">
                </th>
                <th colspan="3">
                    <g:formatNumber number="${orderInstance.totalAdjustments}"/>
                </th>
            </tr>
            </tfoot>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>
