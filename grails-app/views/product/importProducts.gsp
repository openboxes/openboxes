<%@ page import="org.pih.warehouse.product.Product"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<content tag="pageTitle">Import Products</content>
<title>Import Products</title>
</head>
<body>
	<div class="body">
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if> 
		
		<g:hasErrors bean="${productInstance}">
			<div class="errors">
				<g:renderErrors bean="${productInstance}" as="list" />
			</div>
		</g:hasErrors> 
		

		
		<div class="dialog">
			<fieldset>		
				<legend>Review products to import</legend>	
				<table>
					<tbody>
						<tr class="prop">
							<td class="name"><label>Products</label></td>
							<td class="value">
								<table width="100%">
									<thead>
										<tr>         
											<th width="5%"></th>               
											<g:sortableColumn property="id" title="${message(code: 'product.id.label', default: 'ID')}"  width="5%" />
											<g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" width="10%" />
											<g:sortableColumn property="description" title="${message(code: 'product.description.label', default: 'Description')}" />
											<g:sortableColumn property="productType" title="${message(code: 'product.productType.label', default: 'Product Type')}" />
										</tr>
									</thead>
									<tbody>
										<g:each var="productInstance" in="${products}" status="i">
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">         
												<td><input type="checkbox" checked="checked" disabled="disabled"/></td>   
												<td align="center"><g:link action="show" id="${productInstance.id}">${fieldValue(bean: productInstance, field: "id")}</g:link></td>
												<td align="center">${fieldValue(bean: productInstance, field: "name")}</td>
												<td align="center">${fieldValue(bean: productInstance, field: "description")}</td>
												<td>${fieldValue(bean: productInstance, field: "productType.name")}</td>
											</tr>
										</g:each>		                    
									</tbody>
								</table>						
							</td>
						</tr>
						<tr class="prop">
							<td class="name">&nbsp;</td>
							<td class="value">
								<div class="buttonBar">
									<g:form action="importProducts" method="post">
										<span class="buttons">
											<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="upload" /> 
												${message(code: 'default.button.import.label', default: 'Import')}</button>
											<a href="${createLink(controller: "product", action: "importProducts")}" id="" class="negative"> 
												<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
										</span>						
									
									</g:form>			
								</div>							
							</td>
						</tr>
				
					</tbody>
				</table>						    
			</fieldset>
		</div>
	</div>
</body>
</html>
