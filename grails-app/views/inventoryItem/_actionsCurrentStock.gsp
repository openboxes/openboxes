<div class="action-menu">
    <g:set var="dialogId" value="${itemInstance?.id}-${binLocation?.id}"/>
    <button class="action-btn">
        <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
    </button>
    <div class="actions">
        <g:if test="${isSuperuser}">
            <div class="action-menu-item">
                <a href="javascript:void(0);" class="btn-show-dialog" data-title="${g.message(code:'inventory.editItem.label')}"
                   data-url="${request.contextPath}/inventoryItem/showDialog?id=${itemInstance?.id}&binLocation=${binLocation?.id}&template=editItemDialog">
                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                    <g:message code="inventory.editItem.label"/>
                </a>
            </div>
        </g:if>
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
        <g:if test="${isSuperuser}">
            <g:set var="templates" value="${org.pih.warehouse.core.Document.findAllByDocumentCode(org.pih.warehouse.core.DocumentCode.ZEBRA_TEMPLATE)}"/>
            <g:each in="${templates}" var="template">
                <hr/>
                <div class="action-menu-item">
                    <a href="javascript:void(-1)">
                        <label>${template.name}</label>
                    </a>
                </div>
                <div class="action-menu-item">
                    <g:link controller="document" action="buildZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'brick.png')}"/>&nbsp;
                        <g:message code="default.build.label" args="[template.name]"/>
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="document" action="renderZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                        <img src="${resource(dir: 'images/icons', file: 'barcode.png')}"/>&nbsp;
                        <g:message code="default.render.label" args="[template.name]"/>
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="document" action="exportZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application_link.png')}"/>&nbsp;
                        <g:message code="default.export.label" args="[template.name]"/>
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="document" action="printZebraTemplate" id="${template.id}" params="['inventoryItem.id': itemInstance?.id]" target="_blank">
                        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}"/>&nbsp;
                        <g:message code="default.print.label" args="[template.name]"/>
                    </g:link>
                </div>
            </g:each>
        </g:if>
    </div>
</div>

