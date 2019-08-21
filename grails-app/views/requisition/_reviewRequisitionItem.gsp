<g:set var="selected" value="${requisitionItem == selectedRequisitionItem}"/>
<g:set var="quantityOnHand" value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
<g:set var="quantityOnHandForSubstitution" value="${quantityOnHandMap[requisitionItem?.substitution?.product?.id]} "/>
<g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<g:set var="isCanceled" value="${requisitionItem?.isCanceled()}"/>
<g:set var="isChanged" value="${requisitionItem?.isChanged()}"/>
<g:set var="hasSubstitution" value="${requisitionItem?.hasSubstitution()}"/>
<g:set var="quantityOnHand" value="${quantityOnHand.toInteger()}"/>
<g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>
<g:set var="isAvailableForSubstitution" value="${hasSubstitution && (quantityOnHandForSubstitution > 0) && (quantityOnHandForSubstitution >= requisitionItem?.substitution?.totalQuantity()) }"/>
<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled()?'canceled':'')} ${!selectedRequisitionItem?'':selected?'selected':'unselected'}">
    <td class="left">
    <a name="${selectedRequisitionItem?.id}"></a>
        <g:if test="${!isChild }">
            <%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
            <div class="action-menu">
                <button name="actionButtonDropDown" class="action-btn" id="requisitionItem-${requisitionItem?.id }-action">
                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                </button>
                <div class="actions">
                    <g:if test="${requisitionItem?.canApproveQuantity()}">
                        <div class="action-menu-item">
                            <g:link controller="requisitionItem" action="approveQuantity" id="${requisitionItem?.id }" fragment="${requisitionItem?.id}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/>&nbsp;
                                <warehouse:message code="requisitionItem.approveQuantity.label" default="Approve quantity"/>
                            </g:link>
                        </div>
                    </g:if>
                    <g:if test="${requisitionItem.canChangeQuantity()}">
                        <div class="action-menu-item">
                            <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }"
                                    params="['requisitionItem.id':requisitionItem?.id, actionType:'changeQuantity']" fragment="${requisitionItem?.id}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                                <warehouse:message code="requisitionItem.changeQuantity.label" default="Change quantity"/>
                            </g:link>
                        </div>
                    </g:if>

                    <g:if test="${requisitionItem.canChooseSubstitute()}">
                        <div class="action-menu-item">
                            <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }" fragment="${requisitionItem?.id}"
                                    params="['requisitionItem.id':requisitionItem?.id, actionType:'chooseSubstitute']" >
                                <img src="${resource(dir: 'images/icons/silk', file: 'arrow_switch.png')}"/>&nbsp;
                                <warehouse:message code="requisitionItem.substitute.label" default="Choose substitute"/>
                            </g:link>
                        </div>
                    </g:if>
                    <g:if test="${requisitionItem.canChangeQuantity()}">
                        <div class="action-menu-item">
                            <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }"
                                    params="['requisitionItem.id':requisitionItem?.id, actionType:'cancelQuantity']" fragment="${requisitionItem?.id}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}"/>&nbsp;
                                <warehouse:message code="requisitionItem.cancel.label" default="Cancel requisition item"/>
                            </g:link>
                        </div>
                    </g:if>
                    <g:if test="${requisitionItem.canUndoChanges()}">
                        <div class="action-menu-item">
                            <g:link controller="requisitionItem" action="undoChanges" id="${requisitionItem?.id }" params="[redirectAction:'review']" fragment="${requisitionItem?.id}"
                                    onclick="return confirm('${warehouse.message(code: 'default.button.undo.confirm.message', default: 'Are you sure?')}');">
                                <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}"/>&nbsp;
                                <warehouse:message code="requisitionItem.undoChange.label" default="Undo changes"/>
                            </g:link>
                        </div>
                    </g:if>
                </div>
            </div>



        </g:if>
    </td>
    <td class="center middle">
        ${requisitionItem?.product?.productCode}
    </td>


    <td class="product middle">
        <g:if test="${isCanceled||hasSubstitution}">
            <div class="canceled">
                <format:metadata obj="${requisitionItem?.product?.name}" />
            </div>
            <div class="">
                <format:metadata obj="${requisitionItem?.change?.product?.name}" />
            </div>
        </g:if>
        <g:else>
            <div>
                <format:metadata obj="${requisitionItem?.product?.name}" />
            </div>
        </g:else>

    </td>
    <td class="center middle" style="width: 10%;">
        <div class="${isCanceled?'canceled':''}" title="${requisitionItem?.cancelReasonCode}">
            ${requisitionItem.status}
        </div>
        <g:if test="${requisitionItem?.cancelReasonCode}">
            <p>${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}</p>
            <p class="fade">${requisitionItem?.cancelComments}</p>
        </g:if>
    </td>
    <td class="quantity center middle">
        <div class="${isCanceled||isChanged?'canceled':''}">
            ${requisitionItem?.quantity}
        </div>
        <g:if test="${requisitionItem?.change}">
            ${requisitionItem?.change?.quantity}
        </g:if>


    </td>
    <td class="center middle">
        <div class="${isCanceled||isChanged?'canceled':''}">
            ${requisitionItem?.totalQuantity()}
        </div>
        <g:if test="${requisitionItem?.change}">
            ${requisitionItem?.change?.totalQuantity()}
        </g:if>
    </td>
    <td class="center middle">
        <g:if test="${isAvailable||isAvailableForSubstitution}">
            <div class="box available">
                <g:if test="${requisitionItem?.hasSubstitution()}">
                    <div class="${isCanceled||isChanged?'canceled':''}">
                        ${quantityOnHand?:0}
                    </div>
                    ${quantityOnHandForSubstitution?:0}
                </g:if>
                <g:else>
                    ${quantityOnHand?:0}
                </g:else>
            </div>
        </g:if>
        <g:else>
            <div class="box unavailable">
                ${warehouse.message(code:'inventory.unavailable.label',default:'Unavailable')}
                ${quantityOnHand?:0}
            </div>
        </g:else>
    </td>
    <td class="center middle">
        EA/1
    </td>
    <td class="center middle">
        ${requisitionItem.orderIndex}
    </td>
</tr>
