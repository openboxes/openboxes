<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
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
                <th><warehouse:message code="location.label"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Class"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.preferredBinLocation.label" default="Putaway Location"/></th>
                <th class="center border-right"><warehouse:message code="inventoryLevel.replenishmentLocation.label" default="Replenishment Location"/></th>
                <th class="center"><warehouse:message code="default.minimum.label" default="Minimum"/></th>
                <th class="center"><warehouse:message code="default.reorder.label" default="Reorder"/></th>
                <th class="center border-right"><warehouse:message code="default.maximum.label" default="Maximum"/></th>
                <th class="center"><warehouse:message code="inventoryLevel.forecastQuantity.label"/></th>
                <th class="center border-right"><warehouse:message code="inventoryLevel.forecastPeriodDays.label"/></th>
                <th class="center"><warehouse:message code="default.comments.label"/></th>
                <th class="center"><warehouse:message code="default.lastUpdated.label"/></th>
                <th><warehouse:message code="default.actions.label"/></th>
            </tr>
            </thead>
            <tbody>

            <g:each var="inventoryLevelInstance" in="${productInstance?.inventoryLevels.sort { it?.inventory?.warehouse?.name }}" status="i">

                <tr class="prop ${i%2?'even':'odd'}">
                    <td class="center middle">
                        <g:if test="${inventoryLevelInstance?.status in org.pih.warehouse.inventory.InventoryStatus.listEnabled()}">
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'accept.png')}" title="${inventoryLevelInstance?.status}" />
                        </g:if>
                        <g:elseif test="${inventoryLevelInstance?.status in org.pih.warehouse.inventory.InventoryStatus.listDisabled()}">
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'delete.png')}" title="${inventoryLevelInstance?.status}" />
                        </g:elseif>
                        <g:else>
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'error.png')}" title="${inventoryLevelInstance?.status}"/>
                        </g:else>
                    </td>
                    <td class="middle">
                        ${inventoryLevelInstance?.inventory?.warehouse?.name }
                        <g:if test="${inventoryLevelInstance?.internalLocation}">/
                            ${inventoryLevelInstance?.internalLocation?.name }
                        </g:if>
                    </td>
                    <td class="center middle">
                        ${inventoryLevelInstance?.abcClass}
                    </td>
                    <td class="center middle">
                        ${inventoryLevelInstance?.preferredBinLocation?.name}
                    </td>
                    <td class="center middle border-right">
                        ${inventoryLevelInstance?.replenishmentLocation?.name}
                    </td>
                    <td class="center middle">
                        ${inventoryLevelInstance?.minQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center middle">
                        ${inventoryLevelInstance?.reorderQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center middle border-right">
                        ${inventoryLevelInstance?.maxQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center middle">
                        ${inventoryLevelInstance?.forecastQuantity?:0 }
                        ${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="center middle border-right">
                        ${inventoryLevelInstance?.forecastPeriodDays?:0 }
                        ${warehouse.message(code:'default.days.label')}
                    </td>
                    <td class="center middle">
                        ${inventoryLevelInstance?.comments }
                    </td>
                    <td class="center middle">
                        <g:formatDate date="${inventoryLevelInstance?.lastUpdated }" format="d MMM yyyy"/>
                    </td>
                    <td>
                        <a href="javascript:void(0);" class="button btn-show-dialog"  data-width="900" data-height="500"
                           data-title="${warehouse.message(code:'inventoryLevel.edit.label', default: 'Edit stock level')}"
                           data-url="${request.contextPath}/inventoryLevel/dialog/${inventoryLevelInstance?.id}">
                            <img src="${createLinkTo(dir:'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                            ${warehouse.message(code:'default.button.edit.label')}</a>
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
                <td colspan="14" class="center">

                    <a href="javascript:void(0);" class="button btn-show-dialog"
                       data-title="${warehouse.message(code:'inventoryLevel.create.label', default: 'Create stock level')}" data-width="900" data-height="500"
                       data-url="${request.contextPath}/inventoryLevel/dialog/${inventoryLevelInstance?.id}?productId=${productInstance?.id}">
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
