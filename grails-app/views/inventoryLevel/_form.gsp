<%@ page import="org.pih.warehouse.core.Location; org.pih.warehouse.core.ActivityCode; org.pih.warehouse.inventory.Inventory;" %>
<g:set var="productInstance" value="${inventoryLevelInstance?.product?:productInstance}"/>
<g:set var="locationInstance" value="${inventoryLevelInstance?.inventory?.warehouse ?: session?.warehouse}"/>
<g:form method="post" controller="inventoryLevel" action="save" autocomplete="off">

    <g:hiddenField name="id" value="${inventoryLevelInstance?.id}" />
    <g:hiddenField name="version" value="${inventoryLevelInstance?.version}" />
    <g:hiddenField name="redirectUrl" value="${params.redirectUrl}"/>

    <g:if test="${productInstance}">
        <g:render template="../product/summaryDialog" model="[productInstance:productInstance]"/>
    </g:if>

    <div class="tabs">
        <ul>
            <li><a href="#tabs-1"><g:message code="default.target.label" default="Target"/></a></li>
            <li><a href="#tabs-2"><g:message code="default.replenishment.label" default="Replenishment"/></a></li>
            <li><a href="#tabs-3"><g:message code="default.receiving.label" default="Receiving"/></a></li>
            <li><a href="#tabs-4"><g:message code="default.forecasting.label" default="Forecasting"/></a></li>
        </ul>
        <div id="tabs-1">
            <table>
                <tbody>
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
                        <label for="product.id"><warehouse:message code="product.label" /></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                        <g:if test="${!productInstance}">
                            <g:select name="product.id" class="chzn-select-deselect"
                                      from="${org.pih.warehouse.product.Product.list()}"
                                      optionKey="id" value="${productInstance}"/>
                        </g:if>
                        <g:else>
                            <g:hiddenField name="product.id" value="${productInstance?.id}"/>
                            <g:select name="displayProduct.id" class="chzn-select-deselect"
                                      from="${[productInstance]}"
                                      optionKey="id" value="${productInstance}" disabled="true"/>
                        </g:else>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="location.id"><warehouse:message code="location.label" /></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'inventory', 'errors')}">
                        <g:hiddenField name="location.id" value="${locationInstance?.id}"/>
                        <g:select name="displayLocation.id" from="${[locationInstance]}"
                            optionKey="id" optionValue="${{format.metadata(obj:it)}}" class="chzn-select-deselect"
                                                  value="${locationInstance.id}" disabled="${true}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="internalLocation"><warehouse:message code="location.binLocation.label" default="Bin Location"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'internalLocation', 'errors')}">
                        <g:selectBinLocationByLocation name="internalLocation" id="${locationInstance?.id}"
                                                       value="${inventoryLevelInstance?.internalLocation?.id}"
                                                       noSelection="['':'Optional']" class="chzn-select-deselect"/>
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
                        <g:textArea name="comments" class="text large" rows="5">${inventoryLevelInstance?.comments }</g:textArea>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>
        <div id="tabs-2">
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="minQuantity">
                            <warehouse:message code="inventoryLevel.minQuantity.label" default="Min Quantity" />
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'minQuantity', 'errors')}">
                        <g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" class="text large"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="reorderQuantity">
                            <warehouse:message code="inventoryLevel.reorderQuantity.label" default="Reorder Quantity" />
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'reorderQuantity', 'errors')}">
                        <g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" class="text large"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="maxQuantity">
                            <warehouse:message code="inventoryLevel.maximumQuantity.label" default="Max Quantity" />
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'maxQuantity', 'errors')}">
                        <g:textField name="maxQuantity" value="${inventoryLevelInstance?.maxQuantity }" class="text large"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="expectedLeadTimeDays">
                            <warehouse:message code="inventoryLevel.expectedLeadTimeDays.label" default="Expected Lead Time Days"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'expectedLeadTimeDays', 'errors')}">
                        <g:textField name="expectedLeadTimeDays" value="${inventoryLevelInstance?.expectedLeadTimeDays }" class="text large"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="expectedOrderPeriodDays">
                            <warehouse:message code="inventoryLevel.replenishmentPeriodDays.label" default="Replenishment Period Days"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'replenishmentPeriodDays', 'errors')}">
                        <g:textField name="replenishmentPeriodDays" value="${inventoryLevelInstance?.replenishmentPeriodDays }" class="text large"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="replenishmentLocation">
                            <warehouse:message code="inventoryLevel.replenishmentLocation.label" default="Default Replenishment Source"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'binLocation', 'errors')}">
                        <g:selectBinLocationByLocation name="replenishmentLocation" id="${locationInstance?.id}"
                                             value="${inventoryLevelInstance?.replenishmentLocation?.id}"
                                             noSelection="['':'']" class="chzn-select-deselect"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div id="tabs-3">
        <table>
            <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="preferredBinLocation">
                            <warehouse:message code="inventoryLevel.preferredBinLocation.label" default="Default Putaway Location"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'binLocation', 'errors')}">
                        <g:selectBinLocationByLocation name="preferredBinLocation" id="${locationInstance?.id}"
                                                       value="${inventoryLevelInstance?.preferredBinLocation?.id}"
                                                       noSelection="['':'']" class="chzn-select-deselect"/>
                    </td>
                </tr>
            </tbody>
        </table>
        </div>
        <div id="tabs-4">
            <table>
                <tbody>
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
        <table>
            <tfoot>
            <tr>
                <td colspan="2">
                    <div class="buttons">
                        <g:if test="${!inventoryLevelInstance?.id}">
                            <button class="button" name="_action_save">
                                <img src="${createLinkTo(dir:'images/icons/silk', file: 'add.png')}"/>&nbsp;
                                ${warehouse.message(code: 'default.button.create.label', default: 'Create')}
                            </button>
                        </g:if>
                        <g:else>
                            <button class="button" name="_action_update">
                                <img src="${createLinkTo(dir:'images/icons/silk', file: 'accept.png')}"/>&nbsp;
                                ${warehouse.message(code: 'default.button.update.label', default: 'Update')}
                            </button>
                            <g:link controller="inventoryLevel" action="delete" class="button right" id="${inventoryLevelInstance?.id}"
                               onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${createLinkTo(dir:'images/icons/silk', file: 'delete.png')}"/>&nbsp;
                                ${warehouse.message(code:'default.button.delete.label')}</g:link>
                        </g:else>
                    </div>
                </td>
            </tr>
            </tfoot>
        </table>
    </div>
</g:form>
<g:javascript>

function toggleDisabledProperty(select, otherSelect) {
    var selectedOption = $(select).children("option:selected").val();
    if (selectedOption) {
        otherSelect.prop("disabled", true).trigger("chosen:updated");
    } else {
        otherSelect.prop("disabled", false).trigger("chosen:updated");
    }
}

$(document).ready(function() {

    // Initialize tabs
    $(".tabs").tabs();

    // Event handler to detect changes to internal location dropdown
    $("select[name=internalLocation]").change(function(event) {
      toggleDisabledProperty($(this), $("select[name=preferredBinLocation]"));
    });

    // We want the preferred bin location field to be disabled on load if an internal location has been selected
    toggleDisabledProperty($("select[name=internalLocation]"), $("select[name=preferredBinLocation]"))
});


</g:javascript>
