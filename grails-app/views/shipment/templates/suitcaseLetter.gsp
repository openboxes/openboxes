<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="" />
	<title><warehouse:message code="shipment.create.label" default="Review Suitcase Letter" /></title>        
</head>

<body>    

	<script>
		
	
	</script>

	<div class="body">
	
		<g:form action="suitcase">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
	
			<div class="">
				<g:link action="showDetails" params="${[id:shipmentInstance.id]}">Back</g:link>
				<a href="javascript:window.print()">Print</a>
			</div>	
		
			<div style="width: 8in; height: 11in; border: 0px solid #ccc; padding-top: 1in; padding-bottom: 1in; padding-right: .5in; padding-left: .5in; font-family: 'Times New Roman'; font-size: 12pt;">
				<p><g:formatDate date="${new Date()}" format="MMMMM dd, yyyy"/></p>

				<p>To Whom It May Concern:</p>

				<p>This letter is to certify that the luggage carried by ${shipmentInstance?.carrier?.name} contains humanitarian donations for Haiti. 
				<b>${shipmentInstance?.carrier?.name}</b> is an employee of Partners In Health. The items <b>${shipmentInstance?.carrier?.name}</b> is carrying constitute a charitable donation 
				from Partners In Health to Haiti.</p>			

				<p>The contents of <b>${shipmentInstance?.carrier?.name}</b>'s luggage include:</p>

				<div style="padding-left: 50px;">					
					<g:each var="itemInstance" in="${shipmentInstance?.allShipmentItems}">
						<format:product product="${itemInstance?.product}"/><br/>
					</g:each>
				</div>

				<p>The value of these items is US <b>$ ${shipmentInstance?.totalValue ? formatNumber(number: shipmentInstance.totalValue, format: '#,##0.00') : 0.00 }</b>, and is provided for customs use only.  These products will not be resold, but will be donated and distributed free of charge, under the supervision of Partners In Health.</p>

				<p>Thank you in advance for your assistance in ensuring that these materials reach the clinics as quickly as possible.</p>

				<p>Sincerely,</p>

				<!--  note that we are scaling the image to width=200 -->
				<img src="${createLinkTo(dir: 'images/signatures', file: 'joiamukherjee.jpg') }" border="0" width="200"/>   

				<p>Joia Mukherjee<br/>
				Medical Director, Partners In Health</p>

				<p>Enclosures<br/>
				Sworn to and subscribed before me on <g:formatDate date="${new Date()}" format="MMMMM dd, yyyy"/></p>
				
				<!--  note that we are scaling the image to width=200 -->
				<img src="${createLinkTo(dir: 'images/signatures', file: 'kathrynkempton.jpg') }" border="0" width="200"/>   

				<p>Kathryn G. Kempton Amaral, Notary Public<br/>
				My commission expires: 27 February 2015.</p>
			</div>
		</div>
	</g:form>
</body>
</html>
