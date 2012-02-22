
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

				<div class="buttonBar">            	
	            	<span class="linkButton">
	            		<g:link class="list" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'persons.label').toLowerCase()]"/></g:link>
	            	</span>
	            	<span class="linkButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'person.label').toLowerCase()]"/></g:link>
	            	</span>
            	</div>
            
	            <div class="dialog box">
					<g:form action="list" method="get">
						<label><warehouse:message code="person.search.label"/></label>            
						<g:textField name="q" size="45" value="${params.q }"/>					
						<button type="submit"><img
							src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" style="vertical-align: middle;"
							alt="Save" /> ${warehouse.message(code: 'default.button.find.label')}
						</button>		          
					</g:form>
				</div> 				
				<br/>
                <table>
                    <thead>
                        <tr>                                                    
                            <g:sortableColumn property="type" title="${warehouse.message(code: 'person.type.label')}" />
                            
                            <g:sortableColumn property="lastName" title="${warehouse.message(code: 'person.name.label')}" />
                                                                        
                            <g:sortableColumn property="email" title="${warehouse.message(code: 'person.email.label')}" />
                        
                            <g:sortableColumn property="phoneNumber" title="${warehouse.message(code: 'person.phoneNumber.label')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
							<td>
								${warehouse.message(code: (personInstance.class.simpleName.toLowerCase() + '.label'))}
							</td>                           
                        
                            <td>
								<g:link action="edit" id="${personInstance.id}">
                            		${fieldValue(bean: personInstance, field: "firstName")}
                        			${fieldValue(bean: personInstance, field: "lastName")}
                        		</g:link>
                        	</td>
                        
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
