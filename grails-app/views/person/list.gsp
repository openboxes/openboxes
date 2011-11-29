
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="person.list.label"/></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div>            	
	            	<span class="linkButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'person.label').toLowerCase()]"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'default.id.label')}" />
                        
                            <g:sortableColumn property="type" title="${warehouse.message(code: 'person.type.label')}" />
                                                    
                            <g:sortableColumn property="firstName" title="${warehouse.message(code: 'person.firstName.label')}" />
                        
                            <g:sortableColumn property="lastName" title="${warehouse.message(code: 'person.lastName.label')}" />
                        
                            <g:sortableColumn property="email" title="${warehouse.message(code: 'person.email.label')}" />
                        
                            <g:sortableColumn property="phoneNumber" title="${warehouse.message(code: 'person.phoneNumber.label')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${personInstance.id}">${fieldValue(bean: personInstance, field: "id")}</g:link></td>
							<td>
								${warehouse.message(code: (personInstance.class.simpleName.toLowerCase() + '.label'))}
							</td>                           
                        
                            <td>${fieldValue(bean: personInstance, field: "firstName")}</td>
                        
                            <td>${fieldValue(bean: personInstance, field: "lastName")}</td>
                        
                            <td>${fieldValue(bean: personInstance, field: "email")}</td>
                        
                            <td>${fieldValue(bean: personInstance, field: "phoneNumber")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${personInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
