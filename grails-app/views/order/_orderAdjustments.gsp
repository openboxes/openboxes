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
                <th class="right"><g:message code="default.actions.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="orderAdjustment" in="${orderInstance.orderAdjustments}" status="status">
                <tr class="${status%2==0?'odd':'even'}">
                    <td>
                        ${orderAdjustment?.orderItem?.product?:g.message(code:'default.all.label')}
                    </td>
                    <td>
                        ${orderAdjustment?.orderAdjustmentType?.name}
                    </td>
                    <td>
                        ${orderAdjustment.description}
                    </td>
                    <td>
                        ${orderAdjustment.percentage}
                    </td>
                    <td>
                        <g:if test="${orderAdjustment.amount}">
                            ${orderAdjustment.amount}
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
                    <td class="right">
                        <g:hasRoleApprover>
                            <g:set var="isApprover" value="${true}"/>
                        </g:hasRoleApprover>
                        <g:set var="canManageAdjustments" value="${orderInstance?.status >= OrderStatus.PLACED && isApprover
                                || orderInstance?.status == OrderStatus.PENDING}"/>
                        <g:link action="editAdjustment" id="${orderAdjustment.id}" params="['order.id':orderInstance?.id]" class="button"
                                disabled="${!canManageAdjustments}"
                                disabledMessage="${g.message(code:'errors.noPermissions.label')}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" />
                            <g:message code="default.button.edit.label"/>
                        </g:link>

                        <g:link action="deleteAdjustment" id="${orderAdjustment.id}" params="['order.id':orderInstance?.id]" class="button"
                                disabled="${!canManageAdjustments}"
                                disabledMessage="${g.message(code:'errors.noPermissions.label')}"
                                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete" />
                            <g:message code="default.button.delete.label"/>
                        </g:link>

                    </td>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <th colspan="4">
                </th>
                <th>
                    <g:formatNumber number="${orderInstance.totalAdjustments}"/>
                </th>
                <th></th>
            </tr>
            </tfoot>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>
