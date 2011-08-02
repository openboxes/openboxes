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
		
		<div clss="dialog">
			<g:uploadForm controller="product" action="uploadProducts">
				<fieldset>		
					<legend>Select file with products to import</legend>	
					<table>
						<tbody>
							<tr class="prop">
								<td class="name"><label>File</label></td>
								<td class="value"><input name="csvFile" type="file" /></td>
							</tr>
							<tr class="prop">
								<td class="name"></td>
								<td class="value">
									<span class="buttons">
										<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="upload" /> 
											${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}</button>
										<a href="${createLink(controller: "product", action: "browse")}" id="edit-origin-link" class="negative"> 
											<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
									</span>						
								</td>					
							</tr>
						</tbody>						
					</table>
				</fieldset>
			</g:uploadForm>	
		</div>

	</div>
</body>
</html>
