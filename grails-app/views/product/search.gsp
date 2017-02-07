
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom"/>
        <g:set var="entityName" value="${warehouse.message(code: 'products.label')}" />
        <title><warehouse:message code="default.search.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

			<div class="buttonBar">
				<span class="linkButton">
					<g:link class="list" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'product.label').toLowerCase()]"/></g:link>
				</span>
				<span class="linkButton">
					<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'product.label').toLowerCase()]"/></g:link>
				</span>
			</div>

			<div class="yui-gf">
				<div class="yui-u first">
					<div class="dialog box">
						<h2><g:message code="default.filters.label"/></h2>
						<g:form action="search" method="get">
							<div class="filter-list-item">
								<label><warehouse:message code="product.search.label"/></label>
								<g:textField name="q" size="45" value="${params.q }" class="text"/>
							</div>
							<div class="buttons">
								<button type="submit" class="button">
									${warehouse.message(code: 'default.button.find.label')}
								</button>
								<g:link action="search" class="button">${g.message(code:'default.button.reset.label')}</g:link>

							</div>
						</g:form>
					</div>
				</div>
				<div class="yui-u">
					<div class="box">
						<h2><warehouse:message code="default.search.label" args="[entityName]" /></h2>
						<table>
							<thead>
							<tr>
								<th>Barcode</th>
								<th>Image</th>
								<g:sortableColumn property="manufacturer" title="${warehouse.message(code: 'product.manufacturer.label')}" />
								<g:sortableColumn property="description" title="${warehouse.message(code: 'default.description.label')}" />
								<g:sortableColumn property="upc" title="${warehouse.message(code: 'product.upc.label')}" />
								<g:sortableColumn property="category" title="${warehouse.message(code: 'category.label')}" />
							</tr>
							</thead>
							<tbody>
							<g:each in="${products}" status="i" var="product">
								<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
									<td class="center">
										<g:if test="${product.productCode }">
											<img src="${createLink(controller:'product',action:'barcode',params:[data:product.productCode,width:100,height:50,barcodeFormat:'CODE_128']) }"/>
										</g:if>
									<%--
										Example URL
										/openboxes/product/barcode?data=00048001270688&width=100&height=10&format=CODE_128
									--%>
									</td>

									<td align="center">
										${product?.manufacturer }
									</td>
									<td>
										<b>${product.name }</b><br/>

										${product.description }
									</td>
									<td align="center">
										<format:category category="${product?.category }"/>
									</td>
								</tr>
							</g:each>
							</tbody>
						</table>
					</div>
				</div>
			</div>



            
        </div>
    </body>
</html>
