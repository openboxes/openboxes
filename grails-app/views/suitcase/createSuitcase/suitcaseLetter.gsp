<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></title>        
	<content tag="pageTitle"><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></content>
	<style>
	</style>
</head>

<body>    

	<div class="body">
	
		<g:form action="createSuitcase">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
	
			<div class="">
				<g:submitButton name="refresh" value="Refresh"></g:submitButton>
				<g:submitButton name="back" value="Back"></g:submitButton>
				<g:link action="createSuitcase" event="cancel" id="${shipmentInstance?.id}">Cancel</g:link>
			</div>	
		
			<div style="width: 8in; height: 11in; border: 1px solid #ccc; padding-top: 1in; padding-bottom: 1in; padding-right: .5in; padding-left: .5in; font-family: 'Times New Roman'; font-size: 12pt;">
				<p><g:formatDate date="${new Date()}" format="MMMMM dd, yyyy"/></p>
				<br/>
				<p>To Whom It May Concern:</p>
				<br/>
				<p>This letter is to certify that the luggage carried by ${session.user.name} contains humanitarian donations for Haiti. 
				<b>${session.user.name}</b> is an employee of Partners In Health. The items <b>${session.user.name}</b> is carrying constitute a charitable donation 
				from Partners In Health to Haiti.</p>			
				<br/>
				<p>The contents of <b>${session.user.name}</b>'s luggage include:</p>
				<br/>
				<g:each var="containerInstance" in="${shipmentInstance?.containers}">
					<g:each var="itemInstance" in="${containerInstance?.shipmentItems}">
						${itemInstance.quantity} ${itemInstance?.product?.name}<br/>
					</g:each>
				</g:each>
				<br/>
				<p>The value of these items is US <b>$${shipmentInstance?.totalValue ? shipmentInstance.totalValue : 0.00 }</b>, and is provided for customs use only.  These products will not be resold, but will be donated and distributed free of charge, under the supervision of Partners In Health.</p>
				<br/>
				<p>Thank you in advance for your assistance in ensuring that these materials reach the clinics as quickly as possible.</p>
				<br/>
				<p>Sincerely,</p>
				<br/>
				<img src="${createLinkTo(dir: 'images/', file: 'signature.jpg') }" border="0"/>
				<br/>
				<p>Joia Mukherjee<br/>
				Medical Director, Partners In Health</p>
				<br/>
				<p>Enclosures<br/>
				Sworn to and subscribed before me on <g:formatDate date="${new Date()}" format="MMMMM dd, yyyy"/></p>
				<br/>
				<p>Kathryn G. Kempton Amaral, Notary Public<br/>
				My commission expires: 27 February 2015.</p>
			</div>
		</div>
	</g:form>
</body>
</html>