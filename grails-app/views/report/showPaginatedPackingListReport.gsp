<%@ page defaultCodec="html" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showShippingReport.label" /></title>
        <style media="print">
        	body, td, th, div { font-family: 'Times New Roman'; }
        </style>
        <style>
        	.filter { padding-right: 30px; border-right: 1px solid lightgrey; }
        	.title { text-align: center; padding: 5px; font-size: 3em; }
        	.subtitle { text-align: center; padding: 15px; font-size: 2em; }
        	.underline { border-bottom: 1px dashed black; }
        	.label { width: 15%; text-align: left; vertical-align: bottom; }
        	.value { font-weight: bold; width: 30%; }
        	.spacer { width: 10%; }
        	th { border-bottom: 1px solid black; }
        	td { padding: 10px; }
        	table { -fs-table-paginate: paginate; }

        </style>
    </head>
    <body>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>
	   	<g:if test="${!params.print}">
			<div class="dialog box" >
				<g:form controller="report" action="showShippingReport" method="GET">
					<table>
						<tr>
							<td>
								<label>
									<warehouse:message code="shipment.label"/>
								</label>
								<g:selectShipment class="filter" name="shipment.id" noSelection="['null':'']" value="${command?.shipment?.id}"/>
							</td>
						</tr>
				    	<tr class="prop">
				    		<td>
								<label><warehouse:message code="report.exportAs.label"/></label>
								<g:if test="${command?.shipment }">
						   			<g:link target="_blank" controller="report" action="showShippingReport" params="[print:'true','shipment.id':command?.shipment?.id]">
						   				<warehouse:message code="report.exportAs.html.label"/>
						   			</g:link>
						   			|
						   			<g:link target="_blank" controller="report" action="downloadShippingReport" params="[format:'pdf',url:request.forwardURI,'shipment.id':command?.shipment?.id]">
						   				<warehouse:message code="report.exportAs.pdf.label"/>
						   			</g:link>
						   		</g:if>
						   		<g:else>
						   			<warehouse:message code="report.selectShipment.label"/>
						   		</g:else>

					   		</td>
						</tr>
					</table>

				</g:form>
			</div>
		</g:if>
		<g:else>
		</g:else>

		<g:if test="${command?.shipment }">
			<div class="dialog">
				<table border="0">
					<tr>
						<td class="left">
							<img src="${createLinkTo(dir:'images/icons/logos/',file:'pih_logo.jpg')}"  width="34" height="50"/>
						</td>
						<td class="center">
							<div class="title">
								<warehouse:message code="report.shippingReport.heading"/>
							</div>
							<div class="subtitle">
								${session?.warehouse?.name }
							</div>
							<div class="subtitle">
								${command?.shipment?.name?.encodeAsHTML() }
								--
								<warehouse:message code="report.shippingReport.title"/>
							</div>
						</td>
						<td class="right">
							<img src="${createLinkTo(dir:'images/icons/logos/',file:'pih_logo.jpg')}" width="34" height="50" />
						</td>
					</tr>
				</table>
				<hr/>
				<div class="dialog">
		   			<g:set var="status" value="${0 }"/>
			    	<g:set var="packingListByContainer" value="${command?.checklistReportEntryList?.groupBy { it?.shipmentItem?.container } }"/>
		   			<g:each var="packingListEntry" in="${packingListByContainer}">

		   				<div class="page" style="page-break-after: always;">

				    		<table border="1" style="width: 99%;" class="fs-repeat-header">
				    			<thead>
				    				<tr>
				    					<th colspan="7" style="text-align: center; margin: 0px; padding: 0px;">
							   				<h2>
							   					<b>${packingListEntry?.key?.encodeAsHTML() }</b>
							   				</h2>
				    					</th>
				    				</tr>
				    				<tr>
				    					<th rowspan="2" class="center bottom">
				    						<warehouse:message code="report.number.label"/>
				    					</th>
				    					<th rowspan="2" class="bottom">
				    						<warehouse:message code="product.description.label"/>
				    					</th>
				    					<th rowspan="2" class="center bottom">
					    					<warehouse:message code="inventoryItem.lotNumber.label"/>
				    					</th>
				    					<th rowspan="2" class="center bottom">
				    						<warehouse:message code="inventoryItem.expirationDate.label"/>
				    					</th>
				    					<th rowspan="2" class="center bottom">
				    						<warehouse:message code="shipping.recipient.label"/>
				    					</th>
				    					<th colspan="2" class="center bottom">
											<warehouse:message code="shipping.shipped.label"/>
										</th>
				    				</tr>
				    				<tr>
				    					<th class="center">
					    					<warehouse:message code="report.quantityPerBox.label"/>
				 						</th>
				    					<th class="center">
					    					<warehouse:message code="report.quantityTotal.label"/>
				 						</th>
				    				</tr>
				    			</thead>

				    			<tbody>
				    				<g:each var="checklistEntry" in="${packingListEntry?.value }" status="i">
										<tr>
											<td>
												${i+1 }
											</td>
											<td>
												<format:product product="${checklistEntry?.shipmentItem?.product}"/>
											</td>
											<td>
												${checklistEntry?.shipmentItem?.lotNumber?.encodeAsHTML()  }
											</td>
											<td>
												<format:expirationDate obj="${checklistEntry?.shipmentItem?.expirationDate }"/>
											</td>
											<td>
												<g:if test="${checklistEntry?.shipmentItem?.recipient }">
													${checklistEntry?.shipmentItem?.recipient?.name?.encodeAsHTML()  }
												</g:if>
												<g:elseif test="${checklistEntry?.shipmentItem?.container?.recipient }">
													${checklistEntry?.shipmentItem?.container?.recipient?.name?.encodeAsHTML()  }
												</g:elseif>
												<g:elseif test="${checklistEntry?.shipmentItem?.shipment?.recipient }">
													${checklistEntry?.shipmentItem?.shipment?.recipient?.name?.encodeAsHTML()  }
												</g:elseif>
											</td>
											<td>

											</td>
											<td class="center">
												${checklistEntry?.shipmentItem?.quantity }
											</td>
										</tr>
									</g:each>
								</tbody>
							</table>
							<div style="margin: 10px;">
								<label class="block">
									<warehouse:message code="default.comments.label"/>
								</label>
								<div style="border: 1px solid black; width: 100%; height: 200px;">
									&nbsp;
								</div>
							</div>
						</div>

					</g:each>
				</div>
			</div>
		</g:if>

	    <script>
			$(document).ready(function() {
				$(".filter").change(function() {
					$(this).closest("form").submit();
				});
			});
	    </script>
    </body>
</html>
