
<%@ page import="org.pih.warehouse.core.Tag" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'tag.label', default: 'Tag')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
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


        <div class="yui-g">
            <div class="yui-u first">
                <g:form method="post" >
                    <g:hiddenField name="id" value="${tagInstance?.id}" />
                    <g:hiddenField name="version" value="${tagInstance?.version}" />
                    <div class="dialog box">
                        <h2><warehouse:message code="tag.edit.label" default="Edit tag" /></h2>
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

                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="updatedBy.id"><warehouse:message code="tag.updatedBy.label" default="Updated By" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'updatedBy', 'errors')}">
                                        <g:select name="updatedBy.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${tagInstance?.updatedBy?.id}" noSelection="['null': '']" />
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="createdBy.id"><warehouse:message code="tag.createdBy.label" default="Created By" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'createdBy', 'errors')}">
                                        <g:select name="createdBy.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${tagInstance?.createdBy?.id}" noSelection="['null': '']" />
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="dateCreated"><warehouse:message code="tag.dateCreated.label" default="Date Created" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'dateCreated', 'errors')}">
                                        <g:datePicker name="dateCreated" precision="minute" value="${tagInstance?.dateCreated}"  />
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                      <label for="lastUpdated"><warehouse:message code="tag.lastUpdated.label" default="Last Updated" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: tagInstance, field: 'lastUpdated', 'errors')}">
                                        <g:datePicker name="lastUpdated" precision="minute" value="${tagInstance?.lastUpdated}"  />
                                    </td>
                                </tr>



                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="2">
                                        <div class="center">
                                            <g:actionSubmit class="button icon accept" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                            <g:actionSubmit class="button icon trash" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                            &nbsp;
                                            <g:link controller="tag" action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                        </div>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </g:form>
            </div>
            <div class="yui-u">

                <div class="box">
                    <h2>
                        <warehouse:message code="tag.products.label" default="Products" />
                        (${tagInstance?.products?.size()})
                    </h2>

                    <g:form method="post" action="addToProducts">
                        <g:hiddenField name="id" value="${tagInstance?.id}" />
                        <g:hiddenField name="version" value="${tagInstance?.version}" />
                        <table>
                            <tr>
                                <td>
                                    <g:textField id="productCodesInput" name="productCodesToBeAdded" value=""/>
                                </td>
                            </tr>
                            <tr>
                                <td class="right">
                                    <button class="button icon add">
                                        ${warehouse.message(code:'tag.addToProducts.label', default: 'Add to products')}
                                    </button>

                                </td>
                            </tr>
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
                                                <th><g:checkBox id="selectAllProducts" name="selectAllProducts"/></th>
                                                <th>${warehouse.message(code:'product.productCode.label')}</th>
                                                <th>${warehouse.message(code:'product.name.label')}</th>
                                                <th></th>
                                            </tr>
                                            <g:each in="${tagInstance.products.sort { it.name } }" var="p" status="i">
                                                <tr class="${i%2?'odd':'even'}">
                                                    <td class="middle center"><g:checkBox name="product.id" value="${p.id}" checked="${false}" class="select-product"/></td>
                                                    <td class="center">${p.productCode }</td>
                                                    <td>
                                                        <g:link controller="inventoryItem" action="showStockCard" id="${p.id}">
                                                            ${p?.name?.encodeAsHTML()}
                                                        </g:link>
                                                    </td>
                                                    <td class="right">
                                                        <g:link controller="tag" action="removeFromProducts" id="${tagInstance?.id}" params="['product.id':p?.id]" class="button icon trash">
                                                            ${warehouse.message(code:'default.button.delete.label')}
                                                        </g:link>
                                                    </td>
                                                </tr>
                                            </g:each>
                                        </table>
                                    </td>
                                </tr>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="3" class="center">
                                        <button class="button icon trash">
                                            ${warehouse.message(code:'tag.removeSelectedProducts.label', default: 'Remove selected products')}
                                        </button>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </g:form>
                </div>

            </div>

        </div>
    </div>
    <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
    <script>
        $(document).ready(function() {
            $("#selectAllProducts").click(function(event) {
                var checked = ($(this).attr("checked") == 'checked');
                $("input.select-product[type='checkbox']").attr("checked", checked);
            });

            $('#productCodesInput').tagsInput({
                'autocomplete_url':'${createLink(controller: 'json', action: 'findProductCodes')}',
                'width': 'auto',
                'height': 'auto',
                'removeWithBackspace' : true
            });


        });
    </script>
</body>
</html>
