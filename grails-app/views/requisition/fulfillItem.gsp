<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="" />
<title>Fulfill request items</title>

</head>
<body>

	<div class="dialog">
		
		<div class="head" >
			<table>
				<tr>
					<td class="left">
						<a href="">&lsaquo; previous item </a>
					</td>
					<td class="right">
						<a href="">next item &rsaquo;</a>
					</td>			
				</tr>		
			</table>		
		</div>
		
		<div class="" style="border-bottom: 1px solid lightgrey; background-color: #fff; padding: 10px">
			<table>
				<tr class="prop">
					<td class="name">
						<label>Product</label>
					</td>
					<td class="value">
						<format:product product="${requestItem?.product}"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label>Category</label>
					</td>
					<td class="value">
						${requestItem.category}
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label>Quantity Requested</label>
					</td>
					<td class="value">
						${requestItem.quantity}
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label>Quantity Fulfilled</label>
					</td>
					<td class="value">
						
					</td>
				</tr>
			</table>
		</div>
		
		<br/><br/>

		<g:form action="fulfillRequest" autocomplete="false">
			<fieldset>
				<legend><format:metadata obj="${requestItem.displayName()}"/></legend>
				<div class="list">
					<table border="0">
						<tr class="odd">
							<td>Lot Number</td>
							<td>Expires</td>
							<td>On Hand Qty</td>
							<td>Fulfill Qty</td>
						</tr>
						<g:each var="entry" in="${inventoryItems}" status="i">
							<g:set var="quantity" value="${entry.value }"/>
							<g:set var="inventoryItem" value="${entry.key }"/>
							
							<tr class="${i%2?'odd':'even' }">	
								<td>
									${inventoryItem.lotNumber?:warehouse.message(code: 'default.none.label') }
								</td>
								<td>
									${inventoryItem.expirationDate?:warehouse.message(code: 'default.never.label')}
								</td>
								<td>
									${quantity }
								</td>
								<td>
									<g:textField name="quantity" size="5"/>
								</td>
							</tr>
						</g:each>
					</table>
				</div>
				<div class="center buttons">
					<g:submitButton name="fullRequestItemRemote" value="${warehouse.message(code: 'request.fulfill.label')}"></g:submitButton>
				</div>
			</fieldset>
		</g:form>
	</div>
</body>
</html>
