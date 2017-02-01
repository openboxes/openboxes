
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">

            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                </ul>
            </div>

            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${inventoryLevelInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog box">
                    <h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
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
                                    <g:else>
                                        <g:select name="product.id" class="chzn-select"
                                                  from="${org.pih.warehouse.product.Product.list()}" optionKey="id"
                                                  value="${inventoryLevelInstance?.product?.id}" noSelection="['':'']"/>
                                    </g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="product.id"><warehouse:message code="inventoryLevel.status.label" default="Status" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'product', 'errors')}">
                                    <g:select name="status" class="chzn-select-deselect"
                                              from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
                                              optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance?.status}"
                                              noSelection="['':warehouse.message(code:'inventoryLevel.chooseStatus.label')]" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="inventory.id"><warehouse:message code="inventoryLevel.inventory.label" default="Inventory" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'inventory', 'errors')}">
                                    <g:select name="inventory.id" class="chzn-select-deselect" noSelection="['':'']"
                                              from="${org.pih.warehouse.inventory.Inventory.list()}" optionKey="id"
                                              value="${inventoryLevelInstance?.inventory?.id}"  />
                                </td>
                            </tr>
                            <%--
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="supported"><warehouse:message code="inventoryLevel.supported.label" default="Supported" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'supported', 'errors')}">
                                    <g:checkBox name="supported" value="${inventoryLevelInstance?.supported}" />
                                </td>
                            </tr>
                            --%>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="minQuantity"><warehouse:message code="inventoryLevel.minQuantity.label" default="Min Quantity" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'minQuantity', 'errors')}">
                                    <g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="10" class="text"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="reorderQuantity"><warehouse:message code="inventoryLevel.reorderQuantity.label" default="Reorder Quantity" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'reorderQuantity', 'errors')}">
                                    <g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="10" class="text"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="maxQuantity"><warehouse:message code="inventoryLevel.maxQuantity.label" default="Max Quantity" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'maxQuantity', 'errors')}">
                                    <g:textField name="maxQuantity" value="${inventoryLevelInstance?.maxQuantity }" size="10" class="text"/>
                                </td>
                            </tr>

                            <%--
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated"><warehouse:message code="inventoryLevel.dateCreated.label" default="Date Created" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'dateCreated', 'errors')}">
                                    <g:datePicker name="dateCreated" precision="day" value="${inventoryLevelInstance?.dateCreated}"  />
                                </td>
                            </tr>


                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated"><warehouse:message code="inventoryLevel.lastUpdated.label" default="Last Updated" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryLevelInstance, field: 'lastUpdated', 'errors')}">
                                    <g:datePicker name="lastUpdated" precision="day" value="${inventoryLevelInstance?.lastUpdated}"  />
                                </td>
                            </tr>
                            --%>

                            <tr class="prop">
                                <td valign="top"></td>
                                <td valign="top">
                                    <div class="buttons left">
                                       <g:submitButton name="create" class="button save" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

                                       <g:link action="list" class="button">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

                                    </div>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
            </g:form>
        </div>

    </body>
</html>
