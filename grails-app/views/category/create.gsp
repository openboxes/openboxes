
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'category.label', default: 'Category')}" />
    <title><warehouse:message code="category.productCategories.label" /></title>
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

    <div class="yui-ga">
        <div class="yui-u first">
            <g:form action="save" method="post" >
                <div class="dialog box">
                    <h2><warehouse:message code="default.create.label" args="[warehouse.message(code: 'category.label')]"/></h2>

                    <g:if test="${flash.message}">
                        <div class="message">${flash.message}</div>
                    </g:if>
                    <g:hasErrors bean="${categoryInstance}">
                        <div class="errors"><g:renderErrors bean="${categoryInstance}" as="list" /></div>
                    </g:hasErrors>


                    <table>
                        <tbody>
                        <tr class="prop">
                            <td valign="top" class="name ${hasErrors(bean: categoryInstance, field: 'name', 'errors')}">
                                <label for="name" class="desc"><warehouse:message code="default.name.label" default="Name" /></label>
                            </td>
                            <td class="value">
                                <g:textField name="name" class="text" size="80" value="${categoryInstance?.name}" />
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label for="name" class="desc"><warehouse:message code="category.parent.label" default="Parent" /></label>
                            </td>
                            <td class="value">
                                <g:selectCategory name="parentCategory.id" class="chzn-select-deselect"
                                                  noSelection="['null':'']"/>
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
                </div>
            </g:form>
        </div>
    </div>

</body>
</html>
