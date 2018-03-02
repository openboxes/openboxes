<g:hasErrors bean="${inventoryLevelInstance}">
    <div class="errors">
        <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
    </div>
</g:hasErrors>

<g:form controller="product" action="updateInventoryLevels">
    <div class="box">

        <h2>
            <warehouse:message code="product.stockLevels.label" default="Stock levels"/>
        </h2>
        <table>
            <thead>
            <tr class="odd">
                <th><warehouse:message code="inventoryLevel.status.label"/></th>
                <th><warehouse:message code="inventory.label"/></th>
                <th><warehouse:message code="inventoryLevel.binLocation.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Class"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.minQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.reorderQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.maxQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.preferred.label"/></th>
                <th class="center"><warehouse:message code="default.lastUpdated.label"/></th>
                <th><warehouse:message code="default.actions.label"/></th>
            </tr>
            </thead>
            <tbody>

            <g:each var="inventoryLevelInstance" in="${productInstance?.inventoryLevels}" status="i">

                <tr class="prop ${i%2?'even':'odd'}">
                    <td>
                        <%--
                        <g:select name="inventoryLevels[${i}].status"
                           from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
                           optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance?.status}"
                           noSelection="['':warehouse.message(code:'inventoryLevel.chooseStatus.label')]" />&nbsp;&nbsp;
                        --%>
                        ${inventoryLevelInstance?.status}
                    </td>
                    <td>
                        ${inventoryLevelInstance?.inventory?.warehouse?.name }
                        <%--<g:hiddenField name="inventoryLevels[${i}].inventory.id" value="${inventoryLevelInstance?.id}"/>--%>
                    </td>
                    <td>
                        <%--
                        <g:textField name="inventoryLevels[${i}].binLocation"
                            value="${inventoryLevelInstance?.binLocation }" size="20" class="text"/>
                        --%>
                        ${inventoryLevelInstance?.binLocation}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.abcClass?:warehouse.message(code:'default.none.label')}
                    </td>
                    <td class="center">
                        <%--<g:textField name="inventoryLevels[${i}].minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="10" class="text"/>--%>
                        ${inventoryLevelInstance?.minQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        <%--<g:textField name="inventoryLevels[${i}].reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="10" class="text"/>--%>
                        ${inventoryLevelInstance?.reorderQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        <%--<g:textField name="inventoryLevels[${i}].maxQuantity" value="${inventoryLevelInstance?.maxQuantity }" size="10" class="text"/>--%>
                        ${inventoryLevelInstance?.maxQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.preferred }
                    </td>
                    <td class="center">
                        <g:formatDate date="${inventoryLevelInstance?.lastUpdated }"/>
                    </td>

                    <td>
                        <%--
                        <g:link controller="inventoryLevel" action="edit" id="${inventoryLevelInstance?.id}">
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'pencil.png')}"/></g:link>
                        --%>
                        <div class="button-group">

                            <a href="javascript:void(0);" class="open-dialog create button icon edit" dialog-id="inventory-level-${inventoryLevelInstance?.id}-dialog">
                                ${warehouse.message(code:'default.button.edit.label')}</a>

                            <g:link controller="inventoryLevel" action="clone" class="button icon settings" id="${inventoryLevelInstance?.id}">
                                ${warehouse.message(code:'default.button.clone.label')}</g:link>

                            <g:link controller="inventoryLevel" action="delete" class="button icon remove" id="${inventoryLevelInstance?.id}">
                                ${warehouse.message(code:'default.button.delete.label')}</g:link>

                        </div>
                    </td>
                </tr>
            </g:each>
            <g:unless test="${productInstance?.inventoryLevels}">
                <tr>
                    <td colspan="10" class="center">
                        <div class="empty center">
                            <warehouse:message code="product.hasNoInventoryLevels.label" default="There are no stock levels"/>
                        </div>
                    </td>
                </tr>
            </g:unless>
            </tbody>
            <tfoot>
            <tr class="prop">
                <td colspan="10" class="center">

                    <a href="javascript:void(0);" class="open-dialog create button icon add" dialog-id="inventory-level-dialog">
                        ${warehouse.message(code:'inventoryLevel.create.label', default: 'Create stock level')}</a>

                    <g:link class="button icon log" controller="inventoryLevel" action="export" id="${productInstance?.id}">
                        ${warehouse.message(code:'inventoryLevel.export.label', default: 'Export stock levels')}
                    </g:link>

                </td>
            </tr>
            </tfoot>
        </table>
    </div>
</g:form>
