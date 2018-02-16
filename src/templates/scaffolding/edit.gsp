<% import grails.persistence.Event %>
<%=packageName%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="\${warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="\${flash.message}">
            	<div class="message">\${flash.message}</div>
            </g:if>
            <g:hasErrors bean="\${${propertyName}}">
	            <div class="errors">
	                <g:renderErrors bean="\${${propertyName}}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['${domainClass.propertyName}']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['${domainClass.propertyName}']"/></g:link>
			</div>

			<g:form method="post" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
				<g:hiddenField name="id" value="\${${propertyName}?.id}" />
				<g:hiddenField name="version" value="\${${propertyName}?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						<%  excludedProps = Event.allEvents.toList() << 'version' << 'id'
							props = domainClass.properties.findAll { !excludedProps.contains(it.name) }
							Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
							props.each { p ->
								cp = domainClass.constrainedProperties[p.name]
								display = (cp ? cp.display : true)
								if (display) { %>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="${p.name}"><warehouse:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></label>
								</td>
								<td valign="top" class="value \${hasErrors(bean: ${propertyName}, field: '${p.name}', 'errors')}">
									${renderEditor(p)}
								</td>
							</tr>
						<%  }   } %>
						</tbody>
                        <tfoot>
                            <tr class="prop">
                                <td valign="top"></td>
                                <td valign="top left">
                                    <div class="buttons left">
                                        <g:actionSubmit class="button" action="update" value="\${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                        <g:actionSubmit class="button" action="delete" value="\${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('\${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                    </div>
                                </td>
                            </tr>
                        </tfoot>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
