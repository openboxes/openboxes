
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'products.label')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="buttonBar">            	
	            	<span class="linkButton">
	            		<g:link class="list" action="list"><warehouse:message code="default.list.label" args="['products']"/></g:link>
	            	</span>
	            	<span class="linkButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['product']"/></g:link>
	            	</span>
            	</div>
            	
	            <div class="dialog box">
					<g:form action="list" method="get">
						<label><warehouse:message code="product.search.label"/></label>            
						<g:textField name="searchTerm" size="45"/>					
						<button type="submit" class="positive"><img
							src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" style="vertical-align: middle;"
							alt="Save" /> ${warehouse.message(code: 'default.button.find.label')}
						</button>		          
					</g:form>
				</div> 				
				<br/>
            
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />
                            <g:sortableColumn property="category" title="${warehouse.message(code: 'category.label')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productInstanceList}" status="i" var="productInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
							<td align="center">
								<g:link action="edit" id="${productInstance.id}">
									<format:product product="${productInstance}"/>
								</g:link>
							</td>
							<td align="center"><format:category category="${productInstance?.category }"/></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productInstanceTotal}" params="${params }" />
            </div>
        </div>
    </body>
</html>
