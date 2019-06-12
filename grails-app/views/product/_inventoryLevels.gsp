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
                <th><warehouse:message code="default.actions.label"/></th>
                <th><warehouse:message code="inventoryLevel.status.label"/></th>
                <th><warehouse:message code="inventory.label"/></th>
                <th><warehouse:message code="inventoryLevel.binLocation.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Class"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.minQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.reorderQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.maxQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.forecastQuantity.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.forecastPeriodDays.label"/></th>
                <th class="center"><warehouse:message code="default.comments.label"/></th>
                <th class="center"><warehouse:message code="default.lastUpdated.label"/></th>
            </tr>
            </thead>
            <tbody>

            <g:each var="inventoryLevelInstance" in="${productInstance?.inventoryLevels}" status="i">

                <tr class="prop ${i%2?'even':'odd'}">
                    <td>

                        <div class="action-menu">
                            <button class="action-btn">
                                <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
                            </button>
                            <div class="actions">
                                <div class="action-menu-item">

                                    <a href="javascript:void(0);" class="open-dialog create" dialog-id="inventory-level-${inventoryLevelInstance?.id}-dialog">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                                        ${warehouse.message(code:'default.button.edit.label')}</a>

                                </div>
                                <div class="action-menu-item">
                                    <g:link controller="inventoryLevel" action="delete" class="" id="${inventoryLevelInstance?.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'delete.png')}"/>&nbsp;
                                        ${warehouse.message(code:'default.button.delete.label')}</g:link>
                                </div>
                            </div>
                        </div>
                    </td>
                    <td>
                        ${inventoryLevelInstance?.status}
                    </td>
                    <td>
                        ${inventoryLevelInstance?.inventory?.warehouse?.name }
                    </td>
                    <td>
                        ${inventoryLevelInstance?.binLocation}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.abcClass?:warehouse.message(code:'default.none.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.minQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.reorderQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.maxQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.forecastQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.forecastPeriodDays?:0 }
                        ${warehouse.message(code:'default.days.label')}
                    </td>
                    <td class="center">
                        ${inventoryLevelInstance?.comments }
                    </td>
                    <td class="center">
                        <g:formatDate date="${inventoryLevelInstance?.lastUpdated }"/>
                    </td>

                </tr>
            </g:each>
            <g:unless test="${productInstance?.inventoryLevels}">
                <tr>
                    <td colspan="12" class="center">
                        <div class="empty center">
                            <warehouse:message code="product.hasNoInventoryLevels.label" default="There are no stock levels"/>
                        </div>
                    </td>
                </tr>
            </g:unless>
            </tbody>
            <tfoot>
            <tr class="prop">
                <td colspan="12" class="center">

                    <a href="javascript:void(0);" class="open-dialog create button" dialog-id="inventory-level-dialog">
                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'add.png')}"/>&nbsp;
                        ${warehouse.message(code:'inventoryLevel.create.label', default: 'Create stock level')}</a>

                    <g:link class="button" controller="inventoryLevel" action="export" id="${productInstance?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'page_excel.png')}"/>&nbsp;
                        ${warehouse.message(code:'inventoryLevel.export.label', default: 'Export stock levels')}
                    </g:link>

                </td>
            </tr>
            </tfoot>
        </table>
    </div>
</g:form>
