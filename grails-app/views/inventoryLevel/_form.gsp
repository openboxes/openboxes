<%@ page import="org.pih.warehouse.core.Location; org.pih.warehouse.core.ActivityCode; org.pih.warehouse.inventory.Inventory;" %>
<g:form method="post" controller="inventoryLevel" action="save" autocomplete="off">
    <div>
        <g:hiddenField name="id" value="${inventoryLevelInstance?.id}" />
        <g:hiddenField name="version" value="${inventoryLevelInstance?.version}" />
        <g:hiddenField name="redirectUrl" value="${params.redirectUrl}"/>
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="product.id"><warehouse:message code="inventoryLevel.product.label" default="Product" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                    <g:if test="${inventoryLevelInstance?.product}">
                        ${inventoryLevelInstance?.product?.productCode}
                        <g:hiddenField name="product.id" value="${inventoryLevelInstance?.product?.id}"/>
                        <format:product product="${inventoryLevelInstance?.product}"/>
                    </g:if>
                    <g:elseif test="${productInstance}">
                        ${productInstance?.productCode}
                        <g:hiddenField name="product.id" value="${productInstance?.id}"/>
                        <format:product product="${productInstance}"/>
                    </g:elseif>
                    <g:else>
                        <g:select name="product.id" class="chzn-select" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${inventoryLevelInstance?.product?.id}"/>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="location.id"><warehouse:message code="location.label" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'inventory', 'errors')}">
                    <g:selectLocation name="location.id"
                                      class="chzn-select-deselect"
                                      value="${inventoryLevelInstance?.inventory?.warehouse?.id ?: session?.warehouse?.id}"
                                      activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="product.id"><warehouse:message code="inventoryLevel.status.label" default="Status" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                    <g:select name="status"
                              class="chzn-select-deselect"
                              id="${inventoryLevelInstance?.id?'edit':'save'}-${inventoryLevelInstance?.id}-status"
                              from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
                              optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance?.status}"
                              noSelection="['null':warehouse.message(code:'inventoryLevel.chooseStatus.label',default:'Choose status')]" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="binLocation"><warehouse:message code="inventoryLevel.binLocation.label" default="Bin location" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'binLocation', 'errors')}">
                    <g:textField name="binLocation" value="${inventoryLevelInstance?.binLocation }" class="text large"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="abcClass"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Analysis Class" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'abcClass', 'errors')}">
                    <g:textField name="abcClass" value="${inventoryLevelInstance?.abcClass }" size="5" class="text large"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="comments"><warehouse:message code="inventoryLevel.comments.label" default="Comments" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'comments', 'errors')}">
                    <g:textField name="comments" value="${inventoryLevelInstance?.comments }" class="text large"/>
                </td>
            </tr>


            <tr class="prop">
                <td valign="top" class="name">
                    <label for="minQuantity"><warehouse:message code="inventoryLevel.minQuantity.label" default="Min Quantity" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'minQuantity', 'errors')}">
                    <g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="10" class="text"/>
                    ${inventoryLevelInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="reorderQuantity"><warehouse:message code="inventoryLevel.reorderQuantity.label" default="Reorder Quantity" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'reorderQuantity', 'errors')}">
                    <g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="10" class="text"/>
                    ${inventoryLevelInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="maxQuantity"><warehouse:message code="inventoryLevel.maximumQuantity.label" default="Max Quantity" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'maxQuantity', 'errors')}">
                    <g:textField name="maxQuantity" value="${inventoryLevelInstance?.maxQuantity }" size="10" class="text"/>
                    ${inventoryLevelInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="forecastQuantity"><warehouse:message code="inventoryLevel.forecastQuantity.label" default="Forecast Quantity" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'forecastQuantity', 'errors')}">
                    <g:textField name="forecastQuantity" value="${inventoryLevelInstance?.forecastQuantity }" size="10" class="text"/>
                    ${inventoryLevelInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="forecastPeriodDays"><warehouse:message code="inventoryLevel.forecastPeriodDays.label" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'forecastPeriodDays', 'errors')}">
                    <g:textField name="forecastPeriodDays" value="${inventoryLevelInstance?.forecastPeriodDays }" size="10" class="text"/>
                    ${warehouse.message(code:'default.days.label')}
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <hr/>
    <div class="buttons">

        <g:if test="${!inventoryLevelInstance?.id}">
            <g:actionSubmit class="button" action="save" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
        </g:if>
        <g:else>
            <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
        </g:else>


    </div>


</g:form>
