<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="locations.label" /></title>

    </head>
    <body>        
        <div class="body">
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
           	
           	<div>
				<div class="buttonBar">            	
                    <g:link class="button" action="list">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                        <warehouse:message code="default.list.label" args="[warehouse.message(code:'locations.label').toLowerCase()]"/>
                    </g:link>
                    <g:isUserAdmin>
                        <g:link class="button" action="edit">
                            <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                            <warehouse:message code="default.create.label"
                             args="[warehouse.message(code: 'location.label').toLowerCase()]"/></g:link>
                    </g:isUserAdmin>
                </div>

                <div class="yui-gf">
                    <div class="yui-u first">

                        <div class="dialog box">
                            <h2>Filters</h2>
                            <g:form action="list" method="get">
                                <div>
                                    <div class="filter-list-item">
                                            <label class="clear"><warehouse:message code="location.name.label"/></label>
                                            <g:textField name="q" value="${params.q }" class="text" style="width:100%"/>
                                    </div>
                                    <div class="filter-list-item">
                                            <label class="clear"><warehouse:message code="location.locationType.label"/></label>
                                            <g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}"
                                                      optionKey="id" optionValue="${{format.metadata(obj:it)}}" class="chzn-select-deselect"
                                                      value="${params?.locationType?.id}" noSelection="['null':'']" />
                                    </div>
                                    <div class="filter-list-item">

                                            <label class="clear"><warehouse:message code="location.locationGroup.label"/></label>
                                            <g:select name="locationGroup.id" from="${org.pih.warehouse.core.LocationGroup.list()}"
                                                      optionKey="id" optionValue="${{format.metadata(obj:it)}}" class="chzn-select-deselect"
                                                      value="${params?.locationGroup?.id}" noSelection="['null':'']" />
                                    </div>
                                    <hr/>
                                    <div class="filter-list-item center">
                                            <button type="submit" class="button icon search">
                                                ${warehouse.message(code: 'default.button.find.label')}
                                            </button>
                                    </div>
                                </div>
                            </g:form>
                        </div>

                    </div>
                    <div class="yui-u">

                        <div class="box">
                            <h2>
                                ${warehouse.message(code: 'default.showing.message', args: [locationInstanceList?.size()?:0]) }
                            </h2>
                            <table>
                                <thead>
                                    <tr style="height: 100px;">
                                        <th></th>
                                        <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" class="bottom"/>
                                        <g:sortableColumn property="locationType" title="${warehouse.message(code: 'location.locationType.label')}" class="bottom"/>
                                        <g:sortableColumn property="locationGroup" title="${warehouse.message(code: 'location.locationGroup.label')}" class="bottom"/>
                                        <th class="bottom"><span class="vertical-text"><warehouse:message code="warehouse.active.label" /></span></th>
                                        <g:each var="activity" in="${org.pih.warehouse.core.ActivityCode.list()}">
                                            <th class="bottom">
                                                <span class="vertical-text"><warehouse:message code="enum.ActivityCode.${activity}"/></span>
                                            </th>
                                        </g:each>
                                        <th class="left bottom"><warehouse:message code="default.color.label" /></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${locationInstanceList}" status="i" var="locationInstance">
                                        <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                                            <td class="middle">
                                                <g:render template="actions" model="[locationInstance:locationInstance]"/>
                                            </td>
                                            <td class="middle">
                                                <g:link action="edit" id="${locationInstance.id}">${fieldValue(bean: locationInstance, field: "name")}</g:link>
                                            </td>
                                            <td class="left middle"><format:metadata obj="${locationInstance?.locationType}"/></td>
                                            <td class="left middle">${locationInstance?.locationGroup?:warehouse.message(code:'default.none.label')}</td>
                                            <td class="left middle">
                                                <g:if test="${locationInstance.active }">
                                                    <img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="${warehouse.message(code: 'default.yes.label') }" title="${warehouse.message(code: 'default.yes.label') }"/>
                                                </g:if>
                                                <g:else>
                                                    <img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="${warehouse.message(code: 'default.no.label') }" title="${warehouse.message(code: 'default.no.label') }"/>
                                                </g:else>

                                            </td>
                                            <g:each var="activity" in="${org.pih.warehouse.core.ActivityCode.list()}">
                                                <td class="left middle">
                                                    <g:if test="${locationInstance?.supports(activity) }">
                                                        <img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="${warehouse.message(code: 'default.yes.label') }" title="${warehouse.message(code: 'default.yes.label') }"/>
                                                    </g:if>
                                                    <g:else>
                                                        <img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="${warehouse.message(code: 'default.no.label') }" title="${warehouse.message(code: 'default.no.label') }"/>
                                                    </g:else>

                                                </td>
                                            </g:each>
                                            <td class="center middle border-right">
                                                <div style="border: 1px solid lightgrey; color:${locationInstance?.fgColor?:'black' }; background-color: ${locationInstance?.bgColor?:'white' }; padding: 5px;">
                                                    ${locationInstance?.name }
                                                </div>
                                            </td>
                                        </tr>
                                    </g:each>
                                </tbody>
                            </table>

                            <g:set var="pageParams"
                                   value="${['locationType.id': params?.locationType?.id, 'locationGroup.id': params?.locationGroup?.id, q: params.q].findAll {it.value}}"/>
                            <g:if test="${locationInstanceTotal >= params.max }">
                                <div class="paginateButtons">
                                    <g:paginate total="${locationInstanceTotal}" max="${params.max}" offset="${params.offset}" params="${pageParams}"/>
                                </div>
                            </g:if>
                        </div>


                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
