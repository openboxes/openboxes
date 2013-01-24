
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom"/>
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
	            		<g:link class="list" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'product.label').toLowerCase()]"/></g:link>
	            	</span>
                <g:isUserAdmin>
                  <span class="linkButton">
                    <g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'product.label').toLowerCase()]"/></g:link>
                  </span>
	              </g:isUserAdmin>	
            	</div>
            	
	            <div class="dialog box">
					<g:form action="list" method="get">
						<label><warehouse:message code="product.search.label"/></label>            
						<g:textField name="q" size="60" value="${params.q }" class="text"/>					
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
                        	<th></th>                        
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />
                            <g:sortableColumn property="category" title="${warehouse.message(code: 'category.label')}" />
                            <g:sortableColumn property="manufacturer" title="${warehouse.message(code: 'product.manufacturer.label')}" />  
                            <g:sortableColumn property="manufacturerCode" title="${warehouse.message(code: 'product.manufacturerCode.label')}" />  
                            <g:sortableColumn property="vendor" title="${warehouse.message(code: 'product.manufacturer.label')}" />  
                            <g:sortableColumn property="vendorCode" title="${warehouse.message(code: 'product.manufacturerCode.label')}" />  
                            <g:sortableColumn property="createdBy" title="${warehouse.message(code: 'default.createdBy.label')}" />
                            <g:sortableColumn property="updatedBy" title="${warehouse.message(code: 'default.updatedBy.label')}" />
                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'default.dateCreated.label')}" />
                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'default.lastUpdated.label')}" />
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${productInstanceList}" status="i" var="productInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
								<td align="center">
									<span title="${productInstance?.description }">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'information.png')}" class="middle" alt="Information" />
									</span>
								
								</td>
								<td align="center">
									<g:link action="edit" id="${productInstance.id}">
										<format:product product="${productInstance}"/>
									</g:link>
								</td>
								<td align="center">
									<format:category category="${productInstance?.category }"/>
								</td>
								<td align="center">
									${productInstance?.manufacturer }
								</td>
								<td align="center">
									${productInstance?.manufacturerCode }
								</td>
								<td align="center">
									${productInstance?.vendor }
								</td>
								<td align="center">
									${productInstance?.vendorCode }
								</td>
								<td align="center">
									${productInstance?.createdBy }
								</td>
								<td align="center">
									${productInstance?.updatedBy }
								</td>
								<td align="center">
									${productInstance?.dateCreated }
								</td>
								<td align="center">
									${productInstance?.lastUpdated }
								</td>
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
