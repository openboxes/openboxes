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
		
		<div>	
			<g:uploadForm controller="product" action="uploadProducts">
				<label>File</label>
				<input name="csvFile" type="file" />				
				
				<span class="buttons">
					<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="upload" /> 
						${message(code: 'default.button.upload.label', default: 'Upload')}</button>
					<a href="${createLink(controller: "product", action: "browse")}" id="edit-origin-link" class="negative"> 
						<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
				</span>				
				
			</g:uploadForm>	
		</div>

	</div>
</body>
</html>
