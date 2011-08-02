<% import grails.persistence.Event %>
<%=packageName%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="\${warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
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
            <g:form action="save" method="post" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        <%  excludedProps = Event.allEvents.toList() << 'version' << 'id'
	                            props = domainClass.properties.findAll { !excludedProps.contains(it.name) }
	                            Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
	                            props.each { p ->
	                                if (!Collection.class.isAssignableFrom(p.type)) {
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
	                        <%  }   }   } %>
	                        
		                        <tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">
						                <div class="buttons">
						                   <g:submitButton name="create" class="save" value="\${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
						                   
						                   <g:link action="list">\${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
						                   
						                </div>                        	
		                        	</td>
		                        </tr>
		                        
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
