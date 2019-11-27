
<%@ page import="org.pih.warehouse.product.ProductCatalog" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${productCatalogInstance}">
            <div class="errors">
                <g:renderErrors bean="${productCatalogInstance}" as="list" />
            </div>
        </g:hasErrors>
        <g:hasErrors bean="${command}">
            <div class="errors">
                <g:renderErrors bean="${command}" as="list" />
            </div>
        </g:hasErrors>

        <g:render template="summary" model="[productInstance:productInstance]"/>

        <div class="button-bar">
            <g:link class="button" action="list">
                <img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
                <warehouse:message code="default.list.label" args="[entityName]"/>
            </g:link>
            <g:isSuperuser>
                <g:link class="button" action="create">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.create.label" args="[entityName]"/>
                </g:link>
                <a href="#" class="button button-dialog" data-dialog="#dialog-import-data" action="importCsv">
                    <img src="${resource(dir: 'images/icons/silk', file: 'table_add.png')}" />&nbsp;
                    <warehouse:message code="default.import.label" args="[warehouse.message(code: 'productCatalogItems.label', default: 'Product Catalog Items')]"/>
                </a>
            </g:isSuperuser>
            <g:link class="button" action="exportProductCatalog" id="${productCatalogInstance?.id}">
                <img src="${resource(dir: 'images/icons/silk', file: 'table_go.png')}" />&nbsp;
                <warehouse:message code="default.export.label" args="[entityName]"/>
            </g:link>

        </div>

        <div class="tabs">
            <ul>
                <li>
                    <a href="#edit-product-catalog">
                        <warehouse:message code="productCatalog.label" default="Product Catalog"/>
                    </a>
                </li>
                <li>
                    <a href="#edit-product-catalog-items">
                        <warehouse:message code="productCatalogItems.label" default="Product Catalog Items"/>
                    </a>
                </li>
            </ul>
            <div id="edit-product-catalog">
                <g:form method="post" >
                    <g:hiddenField name="id" value="${productCatalogInstance?.id}" />
                    <g:hiddenField name="version" value="${productCatalogInstance?.version}" />
                    <div class="box">
                        <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                        <table>
                            <tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active"><warehouse:message
                                            code="productCatalog.active.label"
                                            default="Active"/></label>
                                </td>
                                <td valign="top"
                                    class="value ${hasErrors(bean: productCatalogInstance, field: 'active', 'errors')}">
                                    <g:checkBox name="active"
                                                value="${productCatalogInstance?.active}"/>
                                </td>
                            </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="code"><warehouse:message code="productCatalog.code.label" default="Code" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'code', 'errors')}">
                                        <g:textField class="text large" size="80" name="code" value="${productCatalogInstance?.code}" />
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="name"><warehouse:message code="productCatalog.name.label" default="Name" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'name', 'errors')}">
                                        <g:textField class="text large" size="80" name="name" value="${productCatalogInstance?.name}" />
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="description"><warehouse:message code="productCatalog.description.label" default="Description" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'description', 'errors')}">
                                        <g:textArea class="text large" name="description" value="${productCatalogInstance?.description}" />
                                    </td>
                                </tr>



                            <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="color"><warehouse:message
                                                code="productCatalog.color.label"/></label>
                                    </td>
                                <td valign="top"
                                    class="value ${hasErrors(bean: productCatalogInstance, field: 'color', 'errors')}">
                                    <g:textField name="color"
                                                 value="${productCatalogInstance?.color}"
                                                 class="text large colorpicker"/>
                                </td>
                            </tr>

                            </tbody>
                            <tfoot>
                                <tr class="prop">
                                    <td valign="top"></td>
                                    <td valign="top left">
                                        <div class="buttons left">
                                            <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                            <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                        </div>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </g:form>
            </div>
            <div id="edit-product-catalog-items">
                <div class="box">
                    <h2><warehouse:message code="productCatalog.productCatalogItems.label" default="Product Catalog Items"/></h2>
                    <g:render template="productCatalogItems" model="[productCatalogInstance:productCatalogInstance]"/>
                </div>

            </div>
        </div>
    </div>
    <div id="dialog-import-data" class="dialog" style="display: none;" title="<warehouse:message code="default.import.label" args="[warehouse.message(code: 'productCatalogItems.label', default: 'Product Catalog Items')]"/>">
        <g:form controller="productCatalog" action="importProductCatalog" method="post" enctype="multipart/form-data">
            <label>${warehouse.message(code:'importDataCommand.importFile.label')}</label>
            <input type="file" name="importFile" />
            <g:hiddenField name="id" value="${productCatalogInstance?.id}"/>
            <g:hiddenField name="type" value="productCatalog"/>
            <g:hiddenField name="location.id" value="${session?.warehouse?.id}"/>
            <g:submitButton name="importData" class="button" value="${warehouse.message(code: 'default.button.import.label', default: 'Import')}" />
        </g:form>

    </div>

    <script>
        function onSuccess() {
            $("#product-suggest").focus();
        }

        function onComplete() {
            $("#product-suggest").focus();
        }

        $(document).ready(function() {
            $(".tabs").tabs({});

            $(".button-dialog").click(function (event) {
                var dialog = $(this).data('dialog');
                console.log(dialog);
                $(dialog).dialog({autoOpen: true, modal: true});
            });

            $('.dataTable').livequery(function () {
                $(this).dataTable({
                    "bJQueryUI": true,
                    "sPaginationType": "full_numbers"
                });

            });
        });

    </script>
    </body>
</html>
