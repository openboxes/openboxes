<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<div id="location-configuration-tab">
    <div class="box">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
            <warehouse:message code="default.configuration.label" default="Configuration"/>
        </h2>
        <table>
            <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label>
                            <warehouse:message code="location.expectedLeadTimeDays.label" default="Expected Lead Time Days"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'expectedLeadTimeDays', 'errors')}">
                        <g:textField name="expectedLeadTimeDays" value="${inventoryLevelInstance?.expectedLeadTimeDays }" size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label>
                            <warehouse:message code="location.replenishmentPeriodDays.label" default="Replenishment Period Days"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'replenishmentPeriodDays', 'errors')}">
                        <g:textField name="replenishmentPeriodDays" value="${inventoryLevelInstance?.replenishmentPeriodDays }" size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label>
                            <warehouse:message code="location.demandTimePeriodDays.label" default="Demand Time Period Days"/>
                        </label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'demandTimePeriodDays', 'errors')}">
                        <g:textField name="demandTimePeriodDays" value="${inventoryLevelInstance?.demandTimePeriodDays }" size="80"/>
                    </td>
                </tr>
            </tbody>
            <tfoot>
            <tr>
                <td>
                </td>
                <td>
                    <div class="buttons left">
                        <g:actionSubmit class="button icon approve" action="updateForecastingConfiguration" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
                        &nbsp;
                        <g:link action="list">
                            ${warehouse.message(code: 'default.button.cancel.label')}
                        </g:link>
                    </div>

                </td>
            </tr>
            </tfoot>

        </table>
    </div>
</div>
</body>
</html>
