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
			table { -fs-table-paginate: paginate; }
			.filter { padding-right: 30px; border-right: 1px solid lightgrey; }
        	.title { text-align: center; padding: 5px; font-size: 3em; }
        	.subtitle { text-align: center; padding: 15px; font-size: 2em; }
        	.underline { border-bottom: 1px dashed black; }
        	.label { width: 15%; text-align: left; vertical-align: bottom; }
        	.value { font-weight: bold; width: 30%; }
        	.spacer { width: 10%; }
        	th { border-bottom: 1px solid black; }
        	td { padding: 5px; }
        	table { margin-left: auto; margin-right: auto; }

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
			<div class="form box" >
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
			<div>
				<table>
					<tr>
						<td class="left">
							<img src="${createLinkTo(dir:'images/icons/logos/',file:'pih_logo.jpg')}"  width="34" height="50"/>
						</td>
						<td class="center">
							<div class="title">
								<warehouse:message code="report.shippingReport.heading"/>
							</div>
							<div class="subtitle">
								<div style="line-height: 24px">
									${session?.warehouse?.name }
								</div>
								<div style="line-height: 24px">
									<warehouse:message code="report.shippingReport.title"/>
								</div>
							</div>
						</td>
						<td class="right">
							<img src="${createLinkTo(dir:'images/icons/logos/',file:'pih_logo.jpg')}" width="34" height="50" />
						</td>
					</tr>
				</table>
				<hr/>

				<table>
					<tr>
						<td class="label">
							<label>
								<warehouse:message code="report.containerNumber.label"/>
							</label>
						</td>
						<td class="value underline">
							<span class="value">
								${command?.shipment?.name }
							</span>
						</td>
						<td class="spacer">

						</td>
						<td class="label">
							<label>
								<warehouse:message code="report.plate.label"/>
							</label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.getReferenceNumber('License Plate Number')?.identifier }</span>
						</td>
					</tr>
					<tr>
						<td class="label">
							<label>
								<warehouse:message code="report.origin.label"/>
							</label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.origin?.name }</span>
						</td>
						<td class="spacer">

						</td>
						<td class="label">
							<label>
								<warehouse:message code="report.destination.label"/>
							</label>
						</td>
						<td class="value underline">
							<span class="value">${command?.shipment?.destination?.name }</span>
						</td>
					</tr>
					<tr>
						<td colspan="5">
							<div class="list">
					   			<g:set var="status" value="${0 }"/>
					    		<table border="1">
					    			<thead>
					    				<tr>
					    					<th rowspan="2" class="center bottom">
					    						<warehouse:message code="report.number.label"/><!-- No., Number -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
						    					<warehouse:message code="shipping.container.label"/>
					    					</th>
					    					<th rowspan="2" class="bottom">
					    						<warehouse:message code="report.productDescription.label"/><!-- Description produit, Product description -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
						    					<warehouse:message code="report.lotNumber.label"/><!-- No lot, Lot Number -->
					    					</th>
					    					<th rowspan="2" class="center bottom">
					    						<warehouse:message code="report.expirationDate.label"/><!-- Exp, Expiration date -->
					    					</th>
					    					<th colspan="2" class="center bottom">
												<warehouse:message code="report.quantityDelivered.label"/><!-- Livré, Delivered -->
											</th>
					    					<th colspan="2" class="center bottom">
						    					<warehouse:message code="report.quantityReceived.label"/><!-- Reçu, Received -->
					    					</th>
					    				</tr>
					    				<tr>
					    					<th class="center">
						    					<warehouse:message code="report.quantityPerBox.label"/><!-- Qté caisse, Qty per box -->
					 						</th>
					    					<th class="center">
						    					<warehouse:message code="report.quantityTotal.label"/><!-- Qté, Qty -->
					 						</th>
					    					<th class="center">
						    					<warehouse:message code="report.quantityPerBox.label"/><!-- Qté caisse, Qty per box -->
					 						</th>
					    					<th class="center">
						    					<warehouse:message code="report.quantityTotal.label"/><!-- Qté, Qty -->
					 						</th>
					    				</tr>
					    			</thead>

					    			<tbody>

									    <g:set var="previousContainer"/>
										<g:set var="shipmentItemsByContainer" value="${command?.checklistReportEntryList?.groupBy { it?.shipmentItem?.container } }"/>
								    	<g:each var="checklistEntry" in="${command?.checklistReportEntryList }" status="i">
										    <g:set var="rowspan" value="${shipmentItemsByContainer[checklistEntry?.shipmentItem?.container]}"/>
											<tr class="noborder">
												<td class="center">
													${i+1 }
												</td>
												<td class="center" rowspan="${rowspan }">
													<g:if test="${checklistEntry?.shipmentItem?.container != previousContainer }">
														<g:if test="${checklistEntry?.shipmentItem?.container }">
															${checklistEntry?.shipmentItem?.container?.name}
														</g:if>
														<g:else>
															<warehouse:message code="shipping.unpacked.label"/>
														</g:else>
													</g:if>
												</td>
												<td>
													<format:product product="${checklistEntry?.shipmentItem?.product}"/>
												</td>
												<td>
													${checklistEntry?.shipmentItem?.lotNumber }
												</td>
												<td>
													<format:expirationDate obj="${checklistEntry?.shipmentItem?.expirationDate }"/>

												</td>
												<td>

												</td>
												<td class="center">
													${checklistEntry?.shipmentItem?.quantity }
												</td>
												<td>

												</td>
												<td class="center">
													${checklistEntry?.shipmentItem?.quantity }
												</td>
											</tr>
								    		<g:set var="previousContainer" value="${checklistEntry?.shipmentItem?.container}"/>
										</g:each>
									</tbody>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.preparedBy.label"/></label><!--  Préparé par, Prepared by -->
						</td>
						<td class="value underline">

						</td>
						<td class="spacer">

						</td>
						<td class="label">
							<label><warehouse:message code="report.receivedBy.label"/></label><!-- Reçu par, Received by -->
						</td>
						<td class="value underline">

						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.deliveredBy.label"/></label><!-- Livré par, Delivered/supplied by -->
						</td>
						<td class="value underline">

						</td>
						<td class="spacer">

						</td>
						<td class="label">
							<label><warehouse:message code="report.receivedOn.label"/></label><!-- Reçu le, Received on -->
						</td>
						<td class="value underline">

						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.deliveredOn.label"/></label><!-- Livré le, Delivered/supplied on -->
						</td>
						<td class="value underline">

						</td>
						<td class="spacer">

						</td>
						<td class="label">
							<label><warehouse:message code="report.verifiedOn.label"/></label><!-- Vérifié le, Verified on -->
						</td>
						<td class="value underline">

						</td>
					</tr>
					<tr>
						<td class="label">
							<label><warehouse:message code="report.transportedBy.label"/></label><!-- Transporté, Carrier -->
						</td>
						<td class="value underline">

						</td>
						<td class="spacer">

						</td>
						<td class="label">
							<label><warehouse:message code="report.verifiedBy.label"/></label><!-- Vérifié par, Verified by -->
						</td>
						<td class="value underline">

						</td>
					</tr>

				</table>
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
