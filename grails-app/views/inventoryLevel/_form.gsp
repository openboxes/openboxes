
<g:form method="post" controller="inventoryLevel" action="save" autocomplete="off">
    <div>
        <g:hiddenField name="id" value="${inventoryLevelInstance?.id}" />
        <g:hiddenField name="version" value="${inventoryLevelInstance?.version}" />

        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="product.id"><warehouse:message code="inventoryLevel.product.label" default="Product" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                    <g:if test="${inventoryLevelInstance?.product}">
                        <g:hiddenField name="product.id" value="${inventoryLevelInstance?.product?.id}"/>
                        <format:product product="${inventoryLevelInstance?.product}"/>
                    </g:if>
                    <g:elseif test="${productInstance}">
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
                    <label for="inventory.id"><warehouse:message code="inventoryLevel.inventory.label" default="Inventory" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'inventory', 'errors')}">
                    <g:select name="inventory.id" from="${org.pih.warehouse.inventory.Inventory.list()}" optionKey="id" value="${inventoryLevelInstance?.inventory?.id}"  />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="product.id"><warehouse:message code="inventoryLevel.status.label" default="Status" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                    <g:select name="status"
                              from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
                              optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance?.status}"
                              noSelection="['':warehouse.message(code:'inventoryLevel.chooseStatus.label')]" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="inventory.id"><warehouse:message code="inventoryLevel.binLocation.label" default="Bin location" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'binLocation', 'errors')}">
                    <g:textField name="binLocation" value="${inventoryLevelInstance?.binLocation }" size="20" class="text"/>
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
            </tbody>
        </table>
    </div>
    <div class="buttons">

        <g:if test="${!inventoryLevelInstance?.id}">
            <g:actionSubmit class="button" action="save" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
        </g:if>
        <g:else>
            <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
            <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </g:else>
    </div>


</g:form>