

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title>Shipment Complete</title>        
</head>

<body>    
	<div class="body">
	
			<fieldset>
				
				<g:render template="../shipment/summary" />
			
				<div class="dialog">	
					<table>
						<tbody>
							<tr>
								<td style="text-align: center; ">Your shipment had been marked as shipped.</td>
							</tr>
						</tbody>
					
					</table>		
				</div>
				<div class="buttons">
					<table>
							<tr>
								<td style="text-align: center;">
									<g:form action="createShipment" method="post">
										<g:submitButton name="done" value="Done"></g:submitButton>				
									</g:form>
								</td>
							</tr>
					</table>
			    </div>
			</fieldset>		
	</div>
</body>
</html>