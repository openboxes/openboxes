<%@ page import="org.pih.warehouse.core.RoleType" %>
<!-- Block which includes the logo and login banner -->
<div id="header" class="yui-b">
	<div class="yui-gf">				
		<div id="banner">
		    <div id="bannerLeft" class="yui-u first" >
				<div class="logo" >
                    <g:if test="${session?.warehouse?.logo }">
                        <a href="${createLink(uri: '/dashboard/index')}">
                            <img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:session?.warehouse?.id)}" class="middle" />
                        </a>
                    </g:if>
                    <g:else>
                        <img src="${createLinkTo(dir:'images/icons/',file:'logo24.png')}" title="${warehouse.message(code:'default.tagline.label') }" class="middle"/>
                        <a href="${createLink(uri: '/dashboard/index')}">
                            <span class="middle"><warehouse:message code="default.openboxes.label"/></span></a>
                    </g:else>
				</div>
		    </div>
		    
		    <div id="bannerRight" class="yui-u">
		    	<div id="loggedIn" style="vertical-align:middle;" >
					<ul>


                        <g:isUserAdmin>

                            <li>
                                <g:if test="${session._showTime}">
                                    <span>
                                        Data load took ${request.actionDuration/1000}s
                                        Page load took ${request.viewDuration/1000}s
                                    </span>
                                    <g:form controller="${controllerName}" action="${actionName}" id="${params.id}" style="display: inline">
                                        <g:hiddenField name="showTime" value="off"/>
                                        <button class="button icon remove">Disable</button>
                                    </g:form>
                                </g:if>
                                <g:else>
                                    <g:form controller="${controllerName}" action="${actionName}" id="${params.id}" style="display: inline">
                                        <g:hiddenField name="showTime" value="on"/>
                                        <button class="button icon clock">Show time</button>
                                    </g:form>

                                </g:else>
                            </li>
                            <li>
                                <g:form controller="dashboard" action="flushCache" style="display: inline">
                                    <button class="button icon reload">${warehouse.message(code:'cache.flush.label', default: 'Flush cache')}</button>
                                </g:form>

                            </li>
                        </g:isUserAdmin>

					    <g:if test="${session.user}">
                            <%--
                            <li>
                                <span>
                                    <warehouse:message code="header.welcome.label" default="Welcome"/>,
                                </span>
                                <g:link controller="user" action="show" id="${session.user.id}" class="button icon user">
                                    <span id="username">${session?.user?.name}</span>
                                    <span id="userrole">[<g:userRole user="${session.user}"/>]</span></g:link>

                            </li>
                            --%>
                            <li>
                                <span class="action-menu" >
                                    <button class="action-hover-btn button icon user">
                                        <span id="username">${session?.user?.name}</span>
                                        <span id="userrole">[<g:userRole user="${session.user}"/>]</span>

                                    </button>
                                    <ul class="actions" style="text-align:left;">
                                        <%--
                                        <li class="action-menu-item">
                                            <g:link controller="inventory" action="browse" params="['resetSearch':'true']" style="color: #666;">
                                                <warehouse:message code="inventory.browse.label"/>
                                            </g:link>
                                        </li>
                                        --%>

                                        <%--
                                        <li>
                                            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'cart.png')}" style="vertical-align: middle" />
                                            <g:link controller="cart" action="list">Cart <span style="color: orange; font-weight: bold;">${session?.cart ? session?.cart?.items?.size() : '0'}</span></g:link>

                                        </li>
                                        --%>
                                        <li class="action-menu-item">
                                            <g:link controller="user" action="show" id="${session.user.id }" style="color: #666;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'user.png')}"/>
                                                <warehouse:message code="myProfile.label" default="My profile"/>
                                            </g:link>
                                        </li>
                                        <li class="action-menu-item">
                                            <g:link  controller="dashboard" action="index" style="color: #666;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'application_view_tile.png')}"/>
                                                <warehouse:message code="dashboard.label" default="Dashboard"/>
                                            </g:link>
                                        </li>
                                        <g:if test="${session?.warehouse}">
                                            <%--
                                            <li class="action-menu-item">
                                                <g:link controller="dashboard" action="chooseLocation" style="color: #666">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}"/>
                                                    <warehouse:message code="dashboard.changeLocation.label" default="Change location"/>
                                                </g:link>
                                            </li>
                                            --%>
                                            <li class="action-menu-item">
                                                <a href="javascript:void(0);" class="warehouse-switch" style="color: #666">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}"/>
                                                    <warehouse:message code="dashboard.changeLocation.label" default="Change location"/>
                                                </a>
                                                <span id="warehouseMenu" title="${warehouse.message(code:'warehouse.chooseLocationToManage.message')}" style="display: none; padding: 10px;">
                                                    <%--
                                                    <g:isUserNotInRole roles="[RoleType.ROLE_ADMIN,RoleType.ROLE_MANAGER]">
                                                        <div class="error">
                                                            ${warehouse.message(code:'auth.needAdminRoleToChangeLocation.message')}
                                                        </div>
                                                    </g:isUserNotInRole>
                                                    --%>

                                                    <%--<g:isUserInRole roles="[RoleType.ROLE_ADMIN,RoleType.ROLE_MANAGER]">--%>
                                                        <div style="height: 300px; overflow: auto;">
                                                            <table>
                                                                <g:set var="count" value="${0 }"/>
                                                                <g:each var="entry" in="${session.loginLocationsMap}" status="i">
                                                                    <tr class="odd">
                                                                        <td>
                                                                            <g:if test="${!entry?.key }">
                                                                                <label>${warehouse.message(code: 'default.others.label', default: 'Others')}</label>
                                                                            </g:if>
                                                                            <g:else>
                                                                                <label>${entry.key }</label>
                                                                            </g:else>
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>
                                                                            <div class="button-group">
                                                                                <g:each var="warehouse" in="${entry.value.sort() }">
                                                                                    <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
                                                                                    <a class="button" href='${createLink(controller: "dashboard", action:"chooseLocation", id: warehouse.id, params:['targetUri':targetUri])}'>
                                                                                        ${warehouse.name}
                                                                                    </a>
                                                                                </g:each>
                                                                            </div>
                                                                        </td>
                                                                    </tr>
                                                                </g:each>
                                                            </table>
                                                        <%--
                                                        <div class="prop">
                                                            <g:checkBox name="rememberLastLocation" value="${session.user.rememberLastLocation}"/>
                                                            Remember my location and log me in automatically.

                                                            ${session.user.rememberLastLocation}
                                                            ${session.user.warehouse }
                                                        </div>
                                                        --%>
                                                            <g:unless test="${session.loginLocationsMap }">
                                                                <div style="background-color: black; color: white;" class="warehouse button">
                                                                    <warehouse:message code="dashboard.noWarehouse.message"/>
                                                                </div>
                                                            </g:unless>
                                                        </div>
                                                    <%--</g:isUserInRole>--%>
                                                </span>
                                            </li>
                                        </g:if>
                                        <li class="action-menu-item">
                                            <hr/>
                                        </li>
                                        <li class="action-menu-item">
                                            <g:link class="list" controller="auth" action="logout" style="color:#666">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'door.png')}" class="middle"/>
                                                <warehouse:message code="default.logout.label"/>
                                            </g:link>
                                        </li>

                                        <!--
										 <li><g:link class="list" controller="user" action="preferences"><warehouse:message code="default.preferences.label"  default="Preferences"/></g:link></li>
										 -->
                                        <!--
										 <li><input type="text" value="search" name="q" style="color: #aaa; font-weight: bold;" disabled=disabled /></li>
										 -->
                                    </ul>
                                </span>
                            </li>
                            <g:if test="${session?.warehouse}">
                                <li>
                                    <button class="warehouse-switch button icon pin">
                                        ${session?.warehouse?.name }</button>
                                </li>

                            </g:if>

                        </g:if>
						<li>
							<g:globalSearch id="globalSearch" cssClass="globalSearch" name="searchTerms"
								jsonUrl="${request.contextPath }/json/globalSearch"></g:globalSearch>
						</li>
					</ul>
				</div>
		    </div>
		</div>
	</div>
</div>
