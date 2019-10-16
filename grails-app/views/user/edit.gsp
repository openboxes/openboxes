<%@ page import="org.pih.warehouse.core.*" %>
<g:set var="adminAndBrowser" value="${[Role.browser(), Role.assistant(), Role.manager(), Role.admin(), Role.superuser()]}" />
<g:set var="allRoles" value="${[Role.admin(), Role.browser(), Role.manager()]}" />
<g:set var="locationRolePairs" value="${userInstance?.locationRolePairs()}" />
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'user.label', default: 'User')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
<div class="body">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <g:hasErrors bean="${userInstance}">
        <div class="errors">
            <g:renderErrors bean="${userInstance}" as="list" />
        </div>
    </g:hasErrors>

    <div class="dialog">
        <g:render template="summary"/>
        <div class="yui-ga">
            <div class="yui-u first">
                <div id="user-tabs" class="tabs">
                    <ul>
                        <li><a href="#details-tab"><warehouse:message code="user.details.label" default="User Details"/></a></li>
                        <li><a href="#password-tab"><warehouse:message code="user.changePassword.label" default="Change Password"/></a></li>
                        <li><a href="#authorization-tab"><warehouse:message code="user.authorization.label" default="Authorization"/></a></li>
                    </ul>
                    <div id="details-tab">

                        <div class="box">
                            <h2><warehouse:message code="user.details.label" default="Details"/></h2>

                            <g:form method="post" >
                                <g:hiddenField name="id" value="${userInstance?.id}" />
                                <g:hiddenField name="version" value="${userInstance?.version}" />

                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="active"><warehouse:message code="user.active.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'active', 'errors')}">
                                                <g:checkBox name="active" value="${userInstance?.active}" />
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="email"><warehouse:message code="user.email.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
                                                <g:textField name="email" value="${userInstance?.email}" class="text" size="40" />
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="username"><warehouse:message code="user.username.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                                                <g:textField name="username" value="${userInstance?.username}" class="text" size="40" />
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="firstName"><warehouse:message code="user.firstName.label"/></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
                                                <g:textField name="firstName" value="${userInstance?.firstName}" class="text" size="40" />
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="lastName"><warehouse:message code="user.lastName.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
                                                <g:textField name="lastName" value="${userInstance?.lastName}" class="text" size="40" />
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                              <label for="locale"><warehouse:message code="default.locale.label"/></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'locale', 'errors')}">
                                                <g:select name="locale" from="${ grailsApplication.config.openboxes.locale.supportedLocales.collect{ new Locale(it) } }"
                                                          optionValue="displayName" value="${userInstance?.locale}" noSelection="['':'']" class="chzn-select-deselect"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="locale"><warehouse:message
                                                    code="default.timezone.label" default="Timezone" /></label></td>
                                            <td valign="top" class="value">
                                                <g:selectTimezone id="timezone" name="timezone" value="${userInstance?.timezone}"
                                                                  noSelection="['':'']"/>
                                            </td>
                                        </tr>
                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td>

                                        </td>
                                        <td>
                                            <div class="buttons left">
                                                <g:actionSubmit class="button icon approve" action="update" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
                                                &nbsp;
                                                <g:link class="cancel" action="show" id="${userInstance?.id }">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                            </div>
                                        </td>
                                    </tr>
                                    </tfoot>
                                </table>

                            </g:form>
                        </div>
                    </div>
                    <div id="password-tab">
                        <div class="box">
                            <h2><g:message code="user.changePassword.label" default="Change Password"/></h2>

                            <g:form method="post">
                                <g:hiddenField name="id" value="${userInstance?.id}" />
                                <g:hiddenField name="version" value="${userInstance?.version}" />
                                <g:hiddenField name="changePassword" value="${true}"/>
                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="password"><warehouse:message code="user.password.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">

                                                <input type="password" style="display:none"/>
                                                <g:passwordField name="password" value="${userInstance?.password}" class="text" size="40"/>
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="password"><warehouse:message code="user.confirmPassword.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
                                                <g:passwordField name="passwordConfirm" value="" class="text" size="40"/>
                                            </td>
                                        </tr>

                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td>

                                        </td>
                                        <td>
                                            <div class="buttons left">
                                                <g:actionSubmit class="button icon approve" action="update" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
                                                &nbsp;
                                                <g:link class="cancel" action="show" id="${userInstance?.id }">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                            </div>
                                        </td>
                                    </tr>
                                    </tfoot>
                                </table>


                            </g:form>
                        </div>
                    </div>
                    <div id="authorization-tab">
                        <div class="box">
                            <h2><warehouse:message code="user.authorization.label" default="Authorization"/></h2>

                            <g:form method="post">
                                <g:hiddenField name="id" value="${userInstance?.id}" />
                                <g:hiddenField name="version" value="${userInstance?.version}" />
                                <g:hiddenField name="updateRoles" value="${true}" />

                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="email"><warehouse:message code="user.defaultLocation.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'warehouse', 'errors')}">
                                                <g:select name="warehouse.id" from="${locations}"
                                                          optionKey="id" value="${userInstance?.warehouse?.id}" noSelection="['null':'']" class="chzn-select-deselect"/>

                                                <div class="fade">
                                                    <g:checkBox name="rememberLastLocation" value="${userInstance?.rememberLastLocation}" />
                                                    <warehouse:message code="user.rememberLastLocation.label" />
                                                </div>

                                            </td>
                                        </tr>
                                        <g:isUserAdmin>
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="roles"><warehouse:message code="user.roles.label" /></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'roles', 'errors')}">
                                                    <g:set var="noAccessLabel" value="${warehouse.message(code: 'no.access.label')}" />
                                                    <g:select name="roles" from="${org.pih.warehouse.core.Role.list()?.sort({it.description})}"
                                                              optionKey="id" value="${userInstance?.roles}"
                                                              noSelection="${['null': noAccessLabel]}" multiple="true" class="chzn-select-deselect"/>
                                                    <span class="fade"><g:message code="user.clearDefaultRole.message"/></span>
                                                </td>
                                            </tr>
                                            <tr class="prop" id="locationRoles">
                                                <td valign="top" class="name">
                                                    <label><warehouse:message code="user.locationRoles.label"/></label>
                                                </td>
                                                <td valign="top" class="value" style="padding: 0px">
                                                    <table>
                                                        <thead>
                                                            <tr class="odd">
                                                                <th><warehouse:message code="location.label"/></th>
                                                                <th><warehouse:message code="location.locationGroup.label"/></th>
                                                                <th><warehouse:message code="location.locationType.label"/></th>
                                                                <th><warehouse:message code="user.role.label"/></th>
                                                                <th><g:message code="default.actions.label"/></th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <g:set var="locationRolesByLocation" value="${userInstance?.locationRoles?.groupBy{it.location}}"/>
                                                            <g:each var="locationRoleEntry" in="${locationRolesByLocation}" status="status">
                                                                <g:set var="location" value="${locationRoleEntry.key}"/>
                                                                <g:each var="locationRole" in="${locationRoleEntry.value.sort {it.role.roleType}}" status="innerStatus">
                                                                    <g:set var="inactive" value="${!(locationRole?.role in locationRole?.user?.getHighestRole(locationRole.location))}"/>
                                                                    <tr class="${status%2==0?'even':'odd'} ${inactive?'fade':''}">
                                                                        <td>
                                                                            <g:if test="${innerStatus==0}">
                                                                            ${locationRole?.location?.name}
                                                                            </g:if>
                                                                        </td>
                                                                        <td>
                                                                            <g:if test="${innerStatus==0}">
                                                                            ${locationRole?.location?.locationGroup?.name}
                                                                            </g:if>
                                                                        </td>
                                                                        <td>
                                                                            <g:if test="${innerStatus==0}">
                                                                            <format:metadata obj="${locationRole?.location?.locationType}"/>
                                                                            </g:if>
                                                                        </td>
                                                                        <td>
                                                                            <g:if test="${inactive}">
                                                                                <g:set var="title">Roles that are faded may not have any impact on user permissions unless they are secondary roles like notification roles.</g:set>
                                                                            </g:if>
                                                                            <div title="${title}">
                                                                            ${locationRole?.role}
                                                                            </div>
                                                                        </td>
                                                                        <td>
                                                                            <div class="button-group">
                                                                                <%-- FIXME Temporarily disabled until I can figure out how to edit without causing list index to break --%>
                                                                                <%--<a href="javascript:void(0);"
                                                                                   class="button btn-show-dialog"
                                                                                   data-title="${g.message(code:'default.edit.label', args: [g.message(code: 'user.locationRole.label')])}"
                                                                                   data-url="${request.contextPath}/user/editLocationRole?id=${locationRole?.id}">
                                                                                    <g:message code="default.button.edit.label" />
                                                                                </a>--%>
                                                                                <g:link controller="user" action="deleteLocationRole" id="${locationRole?.id}">
                                                                                    <g:message code="default.button.delete.label" />
                                                                                </g:link>
                                                                            </div>
                                                                        </td>
                                                                    </tr>
                                                                </g:each>
                                                            </g:each>
                                                            <g:unless test="${userInstance.locationRoles}">
                                                                <tr>
                                                                    <td colspan="5" class="fade center">
                                                                        <g:message code="default.results.message"
                                                                                   args="[userInstance?.locationRoles?.size()?:'no', g.message(code:'user.locationRoles.label').toLowerCase()]"/>
                                                                    </td>
                                                                </tr>
                                                            </g:unless>
                                                            <tr class="prop">
                                                                <td colspan="5" class="buttons right">
                                                                    <a href="javascript:void(0);"
                                                                       class="button btn-show-dialog"
                                                                       data-title="${g.message(code:'default.add.label', args: [g.message(code: 'user.locationRoles.label')])}"
                                                                       data-url="${request.contextPath}/user/createLocationRoles?user.id=${userInstance?.id}">
                                                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}"/>&nbsp;
                                                                        <g:message code="default.add.label" args="[g.message(code: 'user.locationRoles.label')]"/>
                                                                    </a>
                                                                </td>
                                                            </tr>
                                                        </tfoot>
                                                    </table>
                                                </td>
                                            </tr>
                                        </g:isUserAdmin>
                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td>

                                        </td>
                                        <td>
                                            <div class="buttons left">
                                                <g:actionSubmit class="button icon approve" action="update"
                                                                value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
                                                &nbsp;
                                                <g:link class="cancel" action="show" id="${userInstance?.id }">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                            </div>
                                        </td>
                                    </tr>
                                    </tfoot>
                                </table>
                            </g:form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("select[name='defaultRole']").change(function(){
            if($(this).val() == "${Role.admin().id}")
                $("#locationRoles").hide();
            else
                $("#locationRoles").show();
        });

        $(".tabs").tabs({cookie: { expires: 1 } });
     });
</script>
</body>
</html>
