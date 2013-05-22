<div class="action-menu">
    <g:isUserManager>
        <button class="action-btn">
            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
        </button>
        <div class="actions">
            <div class="action-menu-item">
                <a href="javascript:void(0);" id="btnEditItem-${itemInstance?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                <warehouse:message code="inventory.editItem.label"/>
                </a>
            </div>
            <div class="action-menu-item">
                <a  href="javascript:void(0);" id="btnAdjustStock-${itemInstance?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'book_open.png')}"/>&nbsp;
                <warehouse:message code="inventory.adjustStock.label"/>
                </a>
            </div>
            <div class="action-menu-item">
                <a  href="javascript:void(0);" id="btnTransferStock-${itemInstance?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'book_next.png')}"/>&nbsp;
                <warehouse:message code="inventory.transferStock.label" default="Issue stock"/>
                </a>
            </div>
            <div class="action-menu-item">
                <a  href="javascript:void(0);" id="btnReturnStock-${itemInstance?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'book_previous.png')}"/>&nbsp;
                <warehouse:message code="inventory.returnStock.label" default="Return stock"/>
                </a>
            </div>
            <div class="action-menu-item">
                <a  href="javascript:void(0);" id="btnAddToShipment-${itemInstance?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"/>&nbsp;
                <warehouse:message code="shipping.addToShipment.label"/>
                </a>
            </div>

            <g:render template="editItemDialog" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]"/>
            <g:render template="adjustStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
            <g:render template="transferStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
            <g:render template="returnStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
            <g:render template="addToShipment" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
        </div>
    </g:isUserManager>
</div>