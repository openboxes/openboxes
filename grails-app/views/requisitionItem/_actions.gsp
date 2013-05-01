<div class="action-menu">
	<button name="actionButtonDropDown" class="action-btn" id="requisitionItem-${requisitionItem?.id }-action">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
        <%--
        <div class="action-menu-item">
            <g:link controller="requisition" action="review" id="${requisition?.id }" params="['requisitionItem.id':requisitionItem?.id]">
                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                <warehouse:message code="default.button.review.label"/>
            </g:link>
        </div>
        <div class="action-menu-item">
            <g:link controller="requisitionItem" action="approveQuantity" id="${requisitionItem?.requisition?.id }"
                    params="['requisitionItem.id':requisitionItem?.id]">
                <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}"/>&nbsp;
                <warehouse:message code="requisitionItem.approveQuantity.label" default="Approve quantity"/>
            </g:link>
        </div>
        --%>
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
            <%--
            <div class="action-menu-item">
                <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }"
                        params="['requisitionItem.id':requisitionItem?.id, actionType:'changePackageSize']">
                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                    <warehouse:message code="requisitionItem.changePackageSize.label" default="Change package size"/>
                </g:link>
            </div>
            --%>
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
        <%--
        <div class="action-menu-item">
            <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }"
                    params="['requisitionItem.id':requisitionItem?.id, actionType:'supplementProduct']">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>&nbsp;
                <warehouse:message code="requisitionItem.supplement.label" default="Supplement with different product"/>
            </g:link>
        </div>
        --%>
        <div class="action-menu-item">
            <hr/>
        </div>
        <div class="action-menu-item">
            <g:link controller="requisitionItem" action="delete" id="${requisitionItem?.id }" fragment="${requisitionItem?.id}"
                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>&nbsp;
                <warehouse:message code="requisitionItem.delete.label"/>
            </g:link>
        </div>

        <%--
		<div class="action-menu-item">
			<g:link controller="requisitionItem" action="change">
				<img src="${resource(dir: 'images/icons/silk', file: 'box.png')}"/>&nbsp;
				<warehouse:message code="requisitionItem.pick.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="requisitionItem" action="substitute">
				<img src="${resource(dir: 'images/icons/silk', file: 'box.png')}"/>&nbsp;
				<warehouse:message code="requisitionItem.substitute.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="requisitionItem" action="cancel">
				<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>&nbsp;
				<warehouse:message code="requisitionItem.cancel.label"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		--%>
        <%--
        <g:link controller="requisition" action="review" id="${requisition?.id }" params="['requisitionItem.id':requisitionItem?.id]" class="button">
            <warehouse:message code="default.button.change.label"/>
        </g:link>
        <g:link controller="requisitionItem" action="change" id="${requisitionItem?.id }" class="button">
            <warehouse:message code="default.button.change.label"/>
        </g:link>
        <g:link controller="requisitionItem" action="delete" id="${requisitionItem?.id }" class="button"
                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
            <warehouse:message code="default.button.delete.label"/>
        </g:link>
        <g:link controller="requisitionItem" action="undoCancel" id="${requisitionItem?.id }" class="button"
                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
            <warehouse:message code="default.button.undoCancel.label"/>
        </g:link>
        --%>


	</div>
</div>	

