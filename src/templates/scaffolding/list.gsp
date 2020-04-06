<% import grails.persistence.Event %>
<%=packageName%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="\${warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="\${flash.message}">
            	<div class="message">\${flash.message}</div>
            </g:if>
            <div class="dialog">

				<div class="button-bar">
                    <g:link class="button" action="list">
                        <img src="\${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                        <warehouse:message code="default.list.label" args="[g.message(code:'${domainClass.propertyName}.label')]"/>
                    </g:link>
                    <g:link class="button" action="create">
                        <img src="\${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[g.message(code:'${domainClass.propertyName}.label')]"/>
                    </g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            <%  excludedProps = Event.allEvents.toList() << 'version'
                                props = domainClass.properties.findAll { !excludedProps.contains(it.name) && it.type != Set.class }
                                Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
                                props.eachWithIndex { p, i ->
                                    if (i < 6) {
                                        if (p.isAssociation()) { %>
                                <th><warehouse:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></th>
                            <%      } else { %>
                                <g:sortableColumn property="${p.name}" title="\${warehouse.message(code: '${domainClass.propertyName}.${p.name}.label', default: '${p.naturalName}')}" />
                            <%  }   }   } %>
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="\${${propertyName}List}" status="i" var="${propertyName}">
                            <tr class="\${(i % 2) == 0 ? 'odd' : 'even'}">
                            <%  props.eachWithIndex { p, i ->
                                    cp = domainClass.constrainedProperties[p.name]
                                    if (i == 0) { %>
                                <td><g:link action="edit" id="\${${propertyName}.id}">\${fieldValue(bean: ${propertyName}, field: "${p.name}")}</g:link></td>
                            <%      } else if (i < 6) {
                                        if (p.type == Boolean.class || p.type == boolean.class) { %>
                                <td><g:formatBoolean boolean="\${${propertyName}.${p.name}}" /></td>
                            <%          } else if (p.type == Date.class || p.type == java.sql.Date.class || p.type == java.sql.Time.class || p.type == Calendar.class) { %>
                                <td><format:date obj="\${${propertyName}.${p.name}}" /></td>
                            <%          } else { %>
                                <td>\${fieldValue(bean: ${propertyName}, field: "${p.name}")}</td>
                            <%  }   }   } %>
                            </tr>
                        </g:each>
                        <g:unless test="\${${propertyName}List}">
                            <tr>
                                <td colspan="${props.size()}" class="center fade empty">
                                    <g:message code="default.noResults.label"/>
                                </td>
                            </tr>
                        </g:unless>
                        </tbody>
                    </table>
                </div>
                <div class="paginateButtons">
                    <g:paginate total="\${${propertyName}Total}" />
                </div>
            </div>
        </div>
    </body>
</html>
