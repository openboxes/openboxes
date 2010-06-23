<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.home.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle">${message(code: 'default.home.label', default: 'Home')}</content>		
		<content tag="pageTitle">${message(code: 'default.home.label', default: 'Home')}</content>
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>
    </head>
    <body>        
		<div class="body">		
	    	<div id="dashboard">				
				<g:if test="${!session.user}">
					<p>Welcome! Please <a class="home" href="${createLink(uri: '/auth/login')}">login</a> to gain access</p>
				</g:if>
				<g:else>
					<p class="large" align="justify">				
						<span class="greeting">Welcome, <b>${session.user.firstName} ${session.user.lastName}</b>!</span>
						You are logged into the <b>${session.warehouse?.name}</b> warehouse as a <b>${session.user.role}</b></b>.  
						This page will be the future home of the system's role-based dashboard.  
						For now, please click on one of the actions below.	
					</p>
					<br/><br/>
					<table class="withoutBorder menu">
						<tbody>
							<tr>
								<td width="50%">
									<span class="heading">Manage Shipments</span>
									<ul>
										<li><span class="menuButton"><g:link class="list" controller="shipment" action="list"><g:message code="default.list.label"  args="['Shipment']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="create" controller="shipment" action="create"><g:message code="default.create.label" args="['a new Shipment']" default="Create a new shipment" /></g:link></span></li>						
									</ul>
								</td>							
								<td>
									<span class="heading">Manage Shipment Metadata</span>
									<ul>
										<li><span class="menuButton"><g:link class="list" controller="containerType" action="list"><g:message code="default.list.label"  args="['Container Type']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="referenceNumberType" action="list"><g:message code="default.list.label"  args="['Reference Number Type']"/></g:link></span></li>		
									</ul>
								</td>				
							</tr>
							<tr>
								<td width="50%">
									<span class="heading">Manage Products</span>
									<ul>
										<li><span class="menuButton"><g:link class="browse" controller="product" action="browse"><g:message code="default.browse.label"  args="['Product']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="create" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Create a new Product" /></g:link></span></li>						
										<li><span class="menuButton"><g:link class="create" controller="drugProduct" action="create"><g:message code="default.create.label" args="['Drug Product']" default="Create a new Drug Product" /></g:link></span></li>						
										<li><span class="menuButton"><g:link class="create" controller="consumableProduct" action="create"><g:message code="default.create.label" args="['Consumable Product']" default="Create a new Consumable Product" /></g:link></span></li>
										<li><span class="menuButton"><g:link class="create" controller="durableProduct" action="create"><g:message code="default.create.label" args="['Durable Product']" default="Create a new Durable Product" /></g:link></span></li>
									</ul>								
								</td>
								<td>
									<span class="heading">Manage Product Metadata</span>
									<ul>
										<li><span class="menuButton"><g:link class="list" controller="genericType" action="list"><g:message code="default.list.label"  args="['Generic Type']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="productType" action="list"><g:message code="default.list.label"  args="['Product Type']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="category" action="list"><g:message code="default.list.label"  args="['Category']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="conditionType" action="list"><g:message code="default.list.label"  args="['Medical Condition']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="drugRouteType" action="list"><g:message code="default.list.label"  args="['Administration Route']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="packageType" action="list"><g:message code="default.list.label"  args="['Drug Package']"/></g:link></span></li>		
										<li><span class="menuButton"><g:link class="list" controller="drugClass" action="list"><g:message code="default.list.label"  args="['Drug Class']"/></g:link></span></li>		
									</ul>
								</td>			
						
						</tbody>
					
					</table>
					


				</g:else>
	    	</div>
		</div>
    </body>
</html>

