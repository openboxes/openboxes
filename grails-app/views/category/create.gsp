
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
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${categoryInstance}">
        <div class="errors"><g:renderErrors bean="${categoryInstance}" as="list" /></div>
    </g:hasErrors>


    <div class="buttonBar">
        <g:link class="button" controller="category" action="tree"><warehouse:message code="default.list.label" args="[warehouse.message(code: 'category.label')]"/></g:link>
        <g:isUserAdmin>
            <g:link class="button" controller="category" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code: 'category.label')]"/></g:link>
        </g:isUserAdmin>
    </div>

    <div class="yui-ga">
        <div class="yui-u first">
            <div class="box">
                <h2><format:category category="${categoryInstance}"/></h2>
                <g:form action="save" method="post" >
                    <table>
                        <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label for="name" class="desc"><warehouse:message code="category.parent.label" default="Parent" /></label>
                            </td>
                            <td class="value">

                                <g:selectCategory name="parentCategory.id"
                                                  class="chzn-select-deselect" noSelection="['':'']"
                                                  value="${categoryInstance?.parentCategory?.id}" />
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name ${hasErrors(bean: categoryInstance, field: 'name', 'errors')}">
                                <label for="name" class="desc"><warehouse:message code="default.name.label" default="Name" /></label>
                            </td>
                            <td class="value">
                                <g:textField name="name" class="text" size="80" value="${categoryInstance?.name}" />
                            </td>
                        </tr>
                        <tr class="prop">
                            <td colspan="2" style="text-align:center">
                                <button type="submit" name="create" class="button">${warehouse.message(code: 'default.button.create.label', default: 'Create')}</button>
                                &nbsp;
                                <g:link action="tree">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </g:form>
            </div>
        </div>
    </div>

</body>
</html>
