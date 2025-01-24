
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="category.productCategories.label" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message" role="status" aria-label="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${categoryInstance}">
            <div class="errors" role="alert" aria-label="error-message"><g:renderErrors bean="${categoryInstance}" as="list" /></div>
        </g:hasErrors>

        <div class="buttonBar">
            <g:link class="button" controller="category" action="tree"><warehouse:message code="default.list.label" args="[warehouse.message(code: 'category.label')]"/></g:link>
            <g:isUserAdmin>
                <g:link class="button" controller="category" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code: 'category.label')]"/></g:link>
            </g:isUserAdmin>
        </div>

        <div class="yui-ga">
            <div class="yui-u first">

                <g:form action="saveCategory">
                    <g:hiddenField name="id" value="${categoryInstance?.id }"/>
                    <div class="box">
                        <h2><format:category category="${categoryInstance}"/></h2>
                        <table>
                            <tbody>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="category.parent.label"/></label>
                                    </td>
                                    <td class="value">
                                        <g:selectCategory name="parentCategory.id" class="chzn-select-deselect" value="${categoryInstance?.parentCategory?.id}" noSelection="['null':'']"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="default.name.label"/></label>
                                    </td>
                                    <td class="value">
                                        <g:textField name="name" value="${categoryInstance?.name }" class="text" size="60"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="category.isRoot.label" default="Is root node?"/></label>
                                    </td>
                                    <td class="value">
                                        <g:checkBox name="isRoot" value="${categoryInstance?.isRoot }"/>

                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="default.sortOrder.label" default="Sort order"/></label>
                                    </td>
                                    <td class="value">
                                        <g:textField name="sortOrder" value="${categoryInstance?.sortOrder }" class="text" size="10"/>

                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="category.children.label"/></label>
                                    </td>
                                    <td class="value">
                                        <g:if test="${categoryInstance?.categories }">
                                            <table>
                                                <g:each var="child" in="${categoryInstance?.categories }" status="status">
                                                    <tr>
                                                        <td>
                                                            <g:link action="tree" id="${child.id }"><format:category category="${child}"/></g:link>
                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </table>
                                        </g:if>
                                        <g:else>
                                            <warehouse:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="category.products.label"/></label>
                                    </td>
                                    <td class="value">
                                        <g:if test="${categoryInstance?.products }">
                                            <table>
                                                <tr>
                                                    <th><warehouse:message code="product.productCode.label"/></th>
                                                    <th><warehouse:message code="product.label"/></th>
                                                </tr>
                                                <g:each var="product" in="${categoryInstance?.products }" status="status">
                                                    <tr>
                                                        <td>
                                                            <label>${product?.productCode}</label>
                                                        </td>
                                                        <td>
                                                            <g:link controller="product" action="edit" id="${product?.id}" target="_blank"><format:product product="${product}"/></g:link>
                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </table>
                                        </g:if>
                                        <g:else>
                                            <warehouse:message code="default.none.label"/>
                                        </g:else>

                                    </td>
                                </tr>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td></td>
                                    <td>

                                        <button type="submit" name="save" class="button">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/>&nbsp;
                                            ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                                        </button>
                                        &nbsp;
                                        <g:link action="tree">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </g:form>

            </div>
        </div>
    </body>
</html>
