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
                                                          optionValue="displayName" value="${userInstance?.locale}" noSelection="['null':'']" class="chzn-select-deselect"/>
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
                                                </td>
                                            </tr>
                                        </g:isUserAdmin>
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
                                            <tr class="prop" id="locationRoles">
                                                <td valign="top" class="name">
                                                    <label><warehouse:message code="user.locationRoles.label"/></label>
                                                </td>
                                                <td valign="top">
                                                    <div id="location-roles" style="overflow-y:auto; max-height:300px;">
                                                        <table>
                                                            <thead>
                                                            <tr>
                                                                <th><warehouse:message code="location.locationGroup.label"/></th>
                                                                <th><warehouse:message code="location.label"/></th>
                                                                <th><warehouse:message code="location.locationType.label"/></th>
                                                                <th><warehouse:message code="user.role.label"/></th>
                                                            </tr>
                                                            </thead>
                                                            <tbody>
                                                            <g:each var="location" in="${locations}" status="status">
                                                                <tr class="${status % 2 ? 'even' : 'odd'}">
                                                                    <td>${location?.locationGroup?.name}</td>
                                                                    <td>${location.name}</td>
                                                                    <td><format:metadata obj="${location?.locationType}"/></td>
                                                                    <td>
                                                                        <g:set var="defaultLabel"
                                                                               value="${warehouse.message(code: 'default.label')}"/>
                                                                        <g:select name="locationRolePairs.${location.id}"
                                                                                  value="${locationRolePairs[location.id]}"
                                                                                  from="${adminAndBrowser}"
                                                                                class="chzn-select-deselect"
                                                                                  optionKey="id" noSelection="${['': defaultLabel]}"/>
                                                                    </td>
                                                                </tr>
                                                            </g:each>
                                                            </tbody>
                                                        </table>
                                                    </div>
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
