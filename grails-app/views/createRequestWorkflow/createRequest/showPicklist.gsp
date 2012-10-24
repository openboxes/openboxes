<%@page import="java.text.Format"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="${params?.print?'print':'custom' }" />
<title><warehouse:message code="request.showPicklist.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		
		<g:if test="${!params?.print }">
			<div class="right">
				<g:link action="createRequest" event="showPicklist" params="[print:true]" target="_blank">
					<img src="${createLinkTo(dir:'images/icons/silk',file: 'printer.png')}"/>
					<warehouse:message code="createRequestWorkflow.showPicklist.label" default="Print picklist"/>
				</g:link>
			</div>
		</g:if>

		<div class="dialog">
			<g:render template="/requisition/summary" model="[requestInstance:requestInstance]"/>
			<g:if test="${!params?.print }">
			<g:render template="header" model="['state':'showPicklist']"/>
			</g:if>
			<div>
				<table border="${params?.print?'1':'0' }">
					<thead>
						<tr class="odd">
							<th></th>
							<th>${warehouse.message(code:'default.item.label')}</th>
							<th>${warehouse.message(code:'inventoryItem.lotNumber.label')}</th>
							<th>${warehouse.message(code:'inventoryItem.expirationDate.label')}</th>
							<th class="center">${warehouse.message(code:'inventory.quantity.label')}</th>
							<th class="center">${warehouse.message(code:'picklist.quantityToPick.label')}</th>
							<g:if test="${params?.print }">
							<th width="20%">Comments</th>
							</g:if>
						</tr>
					</thead>
					<tbody>
						<g:each var="picklistItem" in="${picklist?.picklistItems}" status="i">
							<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
								<g:hiddenField name="picklistItems[${i }].id" value="${picklistItem?.id }" size="5"/>
								<td>
									<g:if test="${picklistItem?.inventoryItem?.product?.images }">
										<g:each var="document" in="${picklistItem?.inventoryItem?.product?.images}" status="j">
											<g:if test="${j==0 }">
												<a class="open-dialog" href="javascript:openDialog('#dialog-${document.id }', '#img-${document.id }');">
													<img src="${createLink(controller:'product', action:'viewThumbnail', id:document.id)}" 
														class="middle" style="padding: 2px; margin: 2px; border: 1px solid lightgrey;" />		
												</a>
												
												<div id="dialog-${document.id }" title="${document.filename }" style="display:none;" class="dialog center">
													<div>
														<img id="img-${document.id }" src="${createLink(controller:'product', action:'viewImage', id:document.id, params:['width':'300','height':'300'])}" 
								           							class="middle image" style="border: 1px solid lightgrey" />
													</div>
													<g:link controller="document" action="download" id="${document.id}">Download</g:link>
												</div>		
											</g:if>				
										</g:each>								
									</g:if>
								</td>
								<td>
									<g:link controller="inventoryItem" action="showStockCard" id="${picklistItem?.inventoryItem?.product?.id }" target="_blank">
										${picklistItem?.inventoryItem?.product}
									</g:link>									
								</td>
								<td class="center">
									${picklistItem?.inventoryItem?.lotNumber?.toUpperCase()}
								
								</td>
								<td class="center">
									<g:formatDate date="${picklistItem?.inventoryItem?.expirationDate}" format="MMMMM yyyy"/>
								
								</td>
								<td class="center">
									${picklistItem?.inventoryItem?.quantityOnHand}
									${picklistItem?.inventoryItem?.product?.unitOfMeasure}
								</td>
								<td class="center">
									${picklistItem?.quantity}
									${picklistItem?.inventoryItem?.product?.unitOfMeasure}
								</td>
								<g:if test="${params?.print }">
									<td>
										<g:textArea name="fake" cols="30" rows="5"></g:textArea>
									</td>
								</g:if>							
							</tr>
						</g:each>
					</tbody>								
				</table>
			</div>
			
			<g:if test="${!params?.print }">
				<g:form action="createRequest" autocomplete="false">
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
						<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
						<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</div>
				</g:form>
			</g:if>
				

		</div>

	</div>
	

        
<script>
	function openDialog(dialogId, imgId) { 
		$(dialogId).dialog({autoOpen: true, modal: true, width: 500, height: 360});
	}

	
</script>

<g:if test="${params?.print }">
<script>
function init() {
	window.print();
	/*
	var objBrowse = window.navigator;
	if (objBrowse.appName == “Opera” || objBrowse.appName == “Netscape”) {
		setTimeout(‘window.print()’, 1000);
	} else {
		window.print();
	}
	*/
}
window.onload = init;
</script>
</g:if>

        

</body>
</html>