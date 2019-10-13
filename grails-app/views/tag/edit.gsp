
<%@ page import="org.pih.warehouse.core.Tag" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'tag.label', default: 'Tag')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    <link rel="stylesheet" href="${resource(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${tagInstance}">
            <div class="errors">
                <g:renderErrors bean="${tagInstance}" as="list" />
            </div>
        </g:hasErrors>
        <g:render template="summary"/>

        <div class="dialog box">
            <div id="tag-tabs" class="tabs">
                <ul>
                    <li><a href="#tag-details-tab"><g:message code="tag.label"/></a></li>
                    <li><a href="#tag-products-tab"><g:message code="products.label"/></a></li>
                </ul>
                <div id="tag-details-tab">
                    <div class="box">
                        <h2>
                            <g:message code="default.edit.label" args="[g.message(code: 'tag.label')]" />
                        </h2>

                        <g:form method="post">
                            <g:hiddenField name="id" value="${tagInstance?.id}" />
                            <g:hiddenField name="version" value="${tagInstance?.version}" />
                            <table>
                                <tbody>

                                    <tr class="prop">
                                        <td valign="top" class="name">
                                          <label for="tag"><warehouse:message code="tag.tag.label" default="Tag" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'tag', 'errors')}">
                                            <g:textField name="tag" cols="40" rows="5" value="${tagInstance?.tag}" class="text" size="60" />
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="product.properties.label" default="Properties"/></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'active', 'errors')}">
                                            <div>
                                                <g:checkBox name="isActive" value="${tagInstance?.isActive}" />
                                                <warehouse:message
                                                        code="default.isActive.label" default="Is active?" />
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <td></td>
                                        <td >
                                            <g:actionSubmit class="button icon accept" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                            <g:actionSubmit class="button icon trash" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                            <g:link class="button" controller="tag" action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                        </td>
                                    </tr>
                                </tfoot>
                            </table>
                        </g:form>
                    </div>
                </div>
                <div id="tag-products-tab">
                    <div class="box">
                        <h2>
                            <warehouse:message code="tag.products.label" default="Products" />
                            (${tagInstance?.products?.size()})
                        </h2>

                        <g:form method="post" action="addToProducts">
                            <g:hiddenField name="id" value="${tagInstance?.id}" />
                            <g:hiddenField name="version" value="${tagInstance?.version}" />
                            <table>
                                <tbody>
                                    <tr>
                                        <td>
                                            <g:textArea id="productCodesInput" name="productCodesToBeAdded" />
                                        </td>
                                        <td width="1%">
                                            <button class="button">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                                                ${warehouse.message(code:'tag.addToProducts.label', default: 'Add to products')}
                                            </button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </g:form>

                        <g:form method="post" action="removeFromProducts">
                            <g:hiddenField name="id" value="${tagInstance?.id}" />
                            <g:hiddenField name="version" value="${tagInstance?.version}" />
                            <table>
                                <tbody>
                                    <tr class="prop">
                                        <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'products', 'errors')}">
                                            <table id="products">
                                                <tr>
                                                    <th class="center"><g:checkBox id="selectAllProducts" name="selectAllProducts"/></th>
                                                    <th class="center">${warehouse.message(code:'product.productCode.label')}</th>
                                                    <th>${warehouse.message(code:'product.name.label')}</th>
                                                    <th></th>
                                                </tr>
                                                <g:each in="${tagInstance.products.sort { it.name } }" var="p" status="i">
                                                    <tr class="${i%2?'odd':'even'}">
                                                        <td class="middle center"><g:checkBox name="product.id" value="${p.id}" checked="${false}" class="select-product"/></td>
                                                        <td class="center">${p.productCode }</td>
                                                        <td>
                                                            <g:link controller="inventoryItem" action="showStockCard" id="${p.id}">
                                                                ${p?.name}
                                                            </g:link>
                                                        </td>
                                                        <td class="right">
                                                            <g:link controller="tag" action="removeFromProducts" id="${tagInstance?.id}" params="['product.id':p?.id]" class="button">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                                                                ${warehouse.message(code:'default.button.delete.label')}
                                                            </g:link>
                                                        </td>
                                                    </tr>
                                                </g:each>
                                                <g:unless test="${tagInstance?.products}">
                                                    <tr>
                                                        <td class="fade center empty" colspan="4">
                                                            <g:message code="default.noItems.label"/>
                                                        </td>
                                                    </tr>
                                                </g:unless>
                                            </table>
                                        </td>
                                    </tr>
                                </tbody>
                                <g:if test="${tagInstance?.products}">
                                    <tfoot>
                                        <tr>
                                            <td>
                                                <button class="button">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                                                    ${warehouse.message(code:'tag.removeSelectedProducts.label', default: 'Remove selected products')}
                                                </button>
                                            </td>
                                        </tr>
                                    </tfoot>
                                </g:if>
                            </table>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="${resource(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
    <script>
        $(document).ready(function() {

            $(".tabs").tabs({cookie: { expires: 1 }});
            $("#selectAllProducts").click(function(event) {
                var checked = ($(this).attr("checked") == 'checked');
                $("input.select-product[type='checkbox']").attr("checked", checked);
            });

            $('#productCodesInput').tagsInput({
                'defaultText': '',
                'autocomplete_url':'${createLink(controller: 'json', action: 'findProductCodes')}',
                'width': 'auto',
                'height': 'auto',
                'removeWithBackspace' : true
            });
        });
    </script>
</body>
</html>
