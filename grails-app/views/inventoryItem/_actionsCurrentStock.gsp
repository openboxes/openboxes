<div class="action-menu">
    <g:isUserManager>
        <g:set var="dialogId" value="${itemInstance?.id}-${binLocation?.id}"/>
        <button class="action-btn">
            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
        </button>
        <div class="actions">
            <div class="action-menu-item">
                <a href="javascript:void(0);" class="btn-show-dialog" data-title="${g.message(code:'inventory.editItem.label')}"
                   data-url="${request.contextPath}/inventoryItem/showDialog?id=${itemInstance?.id}&binLocation=${binLocation?.id}&template=editItemDialog">
                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                    <g:message code="inventory.editItem.label"/>
                </a>
            </div>
            <g:supports activityCode="${org.pih.warehouse.core.ActivityCode.ADJUST_INVENTORY}">
                <div class="action-menu-item">
                    <a href="javascript:void(0);" class="btn-show-dialog" data-title="${g.message(code:'inventory.adjustStock.label')}"
                       data-url="${request.contextPath}/inventoryItem/showDialog?id=${itemInstance?.id}&binLocation=${binLocation?.id}&template=adjustStock">
                        <img src="${resource(dir: 'images/icons/silk', file: 'book_open.png')}"/>&nbsp;
                        <g:message code="inventory.adjustStock.label"/>
                    </a>
                </div>
            </g:supports>
            <div class="action-menu-item">
                <a href="javascript:void(0);" class="btn-show-dialog" data-title="${g.message(code:'inventory.transferStock.label')}"
                   data-url="${request.contextPath}/inventoryItem/showDialog?id=${itemInstance?.id}&binLocation=${binLocation?.id}&template=transferStock">
                    <img src="${resource(dir: 'images/icons/silk', file: 'book_next.png')}"/>&nbsp;
                    <g:message code="inventory.transferStock.label" default="Issue stock"/>
                </a>
            </div>
            <div class="action-menu-item">
                <a href="javascript:void(0);" class="btn-show-dialog" data-title="${g.message(code:'inventory.returnStock.label')}"
                    data-url="${request.contextPath}/inventoryItem/showDialog?id=${itemInstance?.id}&binLocation=${binLocation?.id}&template=returnStock">
                    <img src="${resource(dir: 'images/icons/silk', file: 'book_previous.png')}"/>&nbsp;
                    <g:message code="inventory.returnStock.label" default="Return stock"/>
                </a>
            </div>
            <div class="action-menu-item">
                <a href="javascript:void(0);" class="btn-show-dialog" data-title="${g.message(code:'shipping.addToShipment.label')}"
                    data-url="${request.contextPath}/inventoryItem/showDialog?id=${itemInstance?.id}&binLocation=${binLocation?.id}&template=addToShipment">
                    <img src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"/>&nbsp;
                    <g:message code="shipping.addToShipment.label"/>
                </a>
            </div>
            <g:isSuperuser>
                <g:set var="templates" value="${org.pih.warehouse.core.Document.findAllByDocumentCode(org.pih.warehouse.core.DocumentCode.ZEBRA_TEMPLATE)}"/>
                <g:each in="${templates}" var="template">
                    <div class="action-menu-item">
                        <g:link controller="document" action="renderZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                            <img src="${resource(dir: 'images/icons/silk', file: 'paintbrush.png')}"/>&nbsp;
                            <g:message code="default.button.render.label" default="Render"/> ${template.name}
                        </g:link>
                    </div>
                    <div class="action-menu-item">
                        <g:link controller="document" action="exportZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                            <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
                            <g:message code="default.button.export.label" default="Export"/> ${template.name}
                        </g:link>
                    </div>
                    <div class="action-menu-item">
                        <g:link controller="document" action="printZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}"/>&nbsp;
                            <g:message code="default.button.print.label" default="Print"/> ${template.name}
                        </g:link>
                    </div>
                </g:each>
            </g:isSuperuser>


        <%--
            <g:render template="editItemDialog" model="[dialogId:dialogId, inventoryInstance:commandInstance?.inventory, binLocation:binLocation, itemInstance:itemInstance, itemQuantity: itemQuantity]"/>
            <g:render template="adjustStock" model="[dialogId:dialogId, inventoryInstance:commandInstance?.inventory, binLocation:binLocation, itemInstance:itemInstance, itemQuantity: itemQuantity]" />
            <g:render template="transferStock" model="[dialogId:dialogId, inventoryInstance:commandInstance?.inventory, binLocation:binLocation, itemInstance:itemInstance, itemQuantity: itemQuantity]" />
            <g:render template="returnStock" model="[dialogId:dialogId, inventoryInstance:commandInstance?.inventory, binLocation:binLocation, itemInstance:itemInstance, itemQuantity: itemQuantity]" />
            <g:render template="addToShipment" model="[dialogId:dialogId, commandInstance:commandInstance, binLocation:binLocation, itemInstance:itemInstance, itemQuantity: itemQuantity]" />
        --%>
        </div>
    </g:isUserManager>
</div>

