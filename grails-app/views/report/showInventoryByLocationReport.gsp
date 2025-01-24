<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="report.inventoryByLocationReport.label" /></title>
</head>

<body>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message" role="status" aria-label="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${command}">
        <div class="errors" role="alert" aria-label="error-message">
            <g:renderErrors bean="${command}" as="list" />
        </div>
    </g:hasErrors>

    <div class="button-bar">
        <g:link controller="dashboard" action="index" class="button"><g:message code="default.button.backTo.label" args="['Dashboard']"/></g:link>
    </div>

    <div class="yui-gf">
        <div class="box p-2">
            <g:message
                    code="report.inventoryByLocation.instructions.label"
                    default="In the Inventory by Location report, you find information about the inventory across multiple depot locations. Use this summary and filters to find quantities available in stock of a specific product or group of products in multiple locations."
            />
        </div>
        <div class="yui-u first">

            <div class="box">
                <h2 class="middle"><g:message code="report.parameters.label"/></h2>
                <g:form controller="report" action="showInventoryByLocationReport" method="GET">
                    <div class="filters">

                        <div class="prop">
                            <label>${warehouse.message(code:'locations.label')}</label>
                            <div>
                                <g:selectLocation name="locations" multiple="true"
                                                  class="chzn-select-deselect"
                                                  value="${command.locations?.collect { it.id }}"
                                                  noSelection="['null':'']"
                                                  data-placeholder=" "
                                                  activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"/>
                            </div>
                            <label><warehouse:message code="category.label"/></label>
                            <p>
                                <g:selectCategory
                                        id="category" multiple="true"
                                        class="chzn-select-deselect filter"
                                        data-placeholder="${g.message(code: 'category.selectCategory.label', default: 'Select a category')}"
                                        name="categories"
                                        noSelection="['':'']"
                                        value="${params?.list('categories')}"
                                />
                            </p>
                            <p>
                                <label>
                                    <g:checkBox name="includeSubcategories" value="${command.includeSubcategories}"/>
                                    ${warehouse.message(
                                            code:'report.search.includeCategoryChildren.label',
                                            default: 'Include all products in all subcategories',
                                    )}
                                </label>
                            </p>
                        </div>
                        <div class="prop">
                            <div class="center">
                                <button name="actionButton" value="run" class="button"><g:message code="default.button.run.label"/></button>
                                <button name="actionButton" value="download" class="button"><g:message code="default.button.download.label"/></button>
                            </div>
                        </div>
                    </div>
                </g:form>
            </div>
        </div>
        <div class="yui-u">
            <div class="box">
                <h2>
                    ${warehouse.message(code:'report.inventoryByLocationReport.label')} <small>(${command.entries?.keySet()?.size()} results)</small>
                </h2>

                <div >
                    <table class="dataTable">
                        <thead>

                        <tr>
                            <th><warehouse:message code="product.productCode.label" default="Code"/></th>
                            <th><warehouse:message code="product.label" default="Product"/></th>
                            <th><warehouse:message code="product.productFamily.label" default="Product Family"/></th>
                            <th><warehouse:message code="product.primaryCategory.label" default="Category"/></th>
                            <th><warehouse:message code="catalogs.label" default="Formularies"/></th>
                            <th><warehouse:message code="product.tags.label" default="Tags"/></th>
                            <g:each var="location" in="${command.locations}">
                                <th class="center"><warehouse:message code="product.QoH.label" default="QoH"/> ${location?.name}</th>
                            </g:each>
                            <th class="center"><warehouse:message code="product.totalQoH.label" default="QoH Total"/></th>
                            <th class="center"><warehouse:message code="product.totalAvailableToPromise.label" default="Quantity Available Total"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <g:each var="entry" in="${command.entries}">
                            <g:set var="row" value="${entry.value}"/>
                            <g:set var="product" value="${entry.key}"/>
                            <g:set var="totalQuantity" value="${row?.values()?.quantityOnHand?.sum()}"/>
                            <g:set var="totalQuantityAvailableToPromise" value="${row?.values()?.quantityAvailableToPromise?.sum()}"/>
                            <g:set var="form" value='${product?.getProductCatalogs()?.collect{ it.name }?.join(",")}'/>

                            <tr>
                                <td>
                                    ${product?.productCode}
                                </td>
                                <td>
                                    ${product?.name}  <g:renderHandlingIcons product="${product}" />
                                </td>
                                <td>
                                    ${product?.productFamily?.name}
                                </td>
                                <td>
                                    ${product?.category?.name}
                                </td>
                                <td>
                                    ${form}
                                </td>
                                <td>
                                    ${product?.tagsToString()}
                                </td>

                                <g:each var="location" in="${command.locations}">
                                    <td class="center">
                                        ${row[location?.id]?.quantityOnHand}
                                    </td>
                                </g:each>
                                <td class="center">
                                    ${totalQuantity}
                                </td>
                                <td class="center">
                                    ${totalQuantityAvailableToPromise}
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
  $(document).ready(function() {

    $(".dataTable").dataTable({
      "bJQueryUI": true,
      "sPaginationType": "full_numbers",
      "iDisplayLength": 25
    });

  });
</script>

</body>
</html>
