<tr class="prop">
    <th valign="top" class="name">

    </th>
    <th class="list-header">
        ${warehouse.message(code: 'requisitionItem.item.label')}
    </th>
    <th class="center">
        ${warehouse.message(code: 'requisitionItem.quantity.label')}
    </th>
    <th class="center">
        ${warehouse.message(code: 'requisitionItem.substitutable.label')}
    </th>
    <th class="list-header">
        ${warehouse.message(code: 'requisitionItem.recipient.label')}
    </th>
    <th class="list-header">
        ${warehouse.message(code: 'requisitionItem.comment.label')}
    </th>
    <th class="list-header center">
        ${warehouse.message(code: 'requisitionItem.delete.label')}
    </th>
</tr>
<g:each var="requisitionItem" in="${requisition.requisitionItems?.sort{it.orderIndex}}" status="i">
    <tr class="requisitionItem">
        <g:render template="rowItem" model="[requisition: requisition, requisitionItem:requisitionItem, rowIndex:requisitionItem.orderIndex]"/>
    </tr>
</g:each>
<g:if test="${requisition.requisitionItems == null || requisition.requisitionItems?.size() == 0}">
    <tr class="requisitionItem">
        <g:render template="rowItem" model="[requisition: requisition, rowIndex: 0]" />
    </tr>
</g:if>

<tr>
    <td></td>
    <td>
        <button type="button" id="addItemButton" name="addItemButton">
            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
            <warehouse:message code="requisitionItem.addrow.label"/>
        </button>
    </td>
</tr>