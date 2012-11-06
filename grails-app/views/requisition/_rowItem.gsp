<td class="name"></td>
<td class='value ${hasErrors(bean:requisitionItem,field:'product','errors')}'>
    <g:searchProduct id="requisitionItems-${rowIndex}--product" name="requisitionItems[${rowIndex}].product" jsonUrl="${request.contextPath }/json/searchProduct"
                   styleClass="text"
                   placeholder="${warehouse.message(code:'requisition.addItem.label')}"
                   valueId="${requisitionItem?.product?.id}"
                   valueName="${requisitionItem?.product?.name}"/>
</td>
<td valign='top' class='value center ${hasErrors(bean:requisitionItem,field:'quantity','errors')}'>
    <input type="text" class="center" name='requisitionItems[${rowIndex}].quantity' value="${requisitionItem?.quantity}" size="5" style="height:24px" />
</td>
<td valign='top' class='value center ${hasErrors(bean:requisitionItem,field:'substitutable','errors')}'>
    <g:checkBox name="requisitionItems[${rowIndex}].substitutable" checked="${requisitionItem?.substitutable}" />
</td>
<td valign='top' class='value ${hasErrors(bean:requisitionItem,field:'recipient','errors')}'>
    <input type="text" name='requisitionItems[${rowIndex}].recipient' value="${requisitionItem?.recipient}" size="20" style="height:24px" />
</td>
<td valign='top' class='value ${hasErrors(bean:requisitionItem,field:'comment','errors')}'>
    <input type="text" name='requisitionItems[${rowIndex}].comment' value="${requisitionItem?.comment}" size="100" style="height:24px" />
</td>
<td>
    <div class="center">
        <button type="button" class="deleteRequisitionItem">
            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png')}" class="top"/>
        </button>
    </div>
</td>
<input class="order-index" type="hidden" name="requisitionItems[${rowIndex}].orderIndex" value="${requisitionItem?.orderIndex ?: 0}"/>
<input class="id" type="hidden" name="requisitionItems[${rowIndex}].id" value="${requisitionItem?.id}"/>