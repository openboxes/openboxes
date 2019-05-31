<%@ page defaultCodec="html" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showTransactionReport.label" /></title>
        <style>
        	.title { text-align: center; padding: 15px; }
        	.total { border-right: 2px solid lightgrey; }
        	.parameters { width:30%; margin-left: auto; margin-right: auto;  }
        	.filter { padding-right: 15px; }
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

		<table>
			<tr>
				<g:if test="${!params.print}">
					<td style="width: 250px;">
						<%--
						<div class="">
							<span style="border: 1px solid lightgrey; background-color: #f7f7f7; padding: 5px; margin: 5px; width: 100px;">
								<a id="hideParameters" class="toggleParameters" href="javascript:void(0);">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'section_expanded.png')}" class="middle"/>
								</a>
								<a id="showParameters" class="toggleParameters" href="javascript:void(0);">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'section_collapsed.png')}" class="middle"/>
								</a>
							</span>
						</div>
						--%>
						<div id="parameters" class="box">
							<g:form controller="report" action="generateTransactionReport" method="GET">
								<table>
									<g:if test="${command?.product }">
										<tr>
											<td>
												<label>
													<warehouse:message code="report.product.label"/>
												</label>
											</td>
										</tr>
										<tr>
											<td>
												<format:product product="${command?.product }"/>
												<g:hiddenField name="product.id" value="${command?.product?.id }"/>
											</td>
										</tr>
									</g:if>
									<tr>
										<td>
											<label>
												<warehouse:message code="report.location.label"/>
											</label>
										</td>
									</tr>
									<tr>
										<td>
											<g:selectLocation class="chzn-select-deselect" name="location.id"
                                                              noSelection="['null':'']" maxChars="75" groupBy="locationType" value="${command?.location?.id}"/>
										</td>
									</tr>
									<tr>
										<td>
											<label>
												<warehouse:message code="report.category.label"/>
											</label>
										</td>
									</tr>
									<tr>
										<td>
											<g:selectCategory id="category.id" name="category.id"
                                                              class="chzn-select-deselect" value="${command?.category?.id}" noSelection="['':'']"/>

										</td>
									</tr>
									<tr>
										<td>
											<label>
												<warehouse:message code="report.dateRange.label"/>
											</label>
										</td>
									</tr>
									<tr>
										<td>
											<g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy" autocomplete="off"/>
										</td>
									</tr>
									<tr>
										<td>
											<g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy" autocomplete="off"/>

										</td>
									</tr>
									<tr>
										<td>
											<div>
												<label>
													<g:checkBox name="hideInactiveProducts" value="${command?.hideInactiveProducts}" />
													<warehouse:message code="report.hideInactiveProducts.label"/>
												</label>
											</div>
											<div>
												<label>
													<g:checkBox name="showTransferBreakdown" value="${command?.showTransferBreakdown}" />
													<warehouse:message code="report.showTransferBreakdown.label"/>
												</label>
											</div>
											<div>
												<label class="fade">
													<g:checkBox name="includeChildren" value="${command?.includeChildren }" disabled="true" />
													<warehouse:message code="report.includeChildren.label"/> (disabled for performance reasons)

												</label>
											</div>
											<div>
												<label>
													<g:checkBox name="insertPageBreakBetweenCategories" value="${command?.insertPageBreakBetweenCategories }"/>
													<warehouse:message code="report.insertPageBreakBetweenCategories.label"/>
												</label>
											</div>

										</td>
									</tr>
									<tr>
										<td class="left">
											<button type="submit" class="btn">
												<warehouse:message code="report.runReport.label"/>
											</button>

										</td>
									</tr>
								</table>
							</g:form>
						</div>
						<g:if test="${!command?.product }">
					    	<div style="padding: 5px;">
								<label><warehouse:message code="report.exportAs.label"/></label>
					   			<g:link target="_blank" controller="report" action="generateTransactionReport" params="[print:'true','location.id':command.location?.id,'category.id':command?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':command.showTransferBreakdown,'includeChildren':command?.includeChildren,'hideInactiveProducts':command?.hideInactiveProducts]">
					   				<warehouse:message code="report.exportAs.html.label"/>
					   			</g:link>
					   			&nbsp;|&nbsp;
					   			<g:link target="_blank" controller="report" action="downloadTransactionReport" params="[url:request.forwardURI,'location.id':command.location?.id,'category.id':command?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':command.showTransferBreakdown,'includeChildren':command?.includeChildren,'hideInactiveProducts':command?.hideInactiveProducts,'insertPageBreakBetweenCategories':command?.insertPageBreakBetweenCategories]">
					   				<warehouse:message code="report.exportAs.pdf.label"/>
					   			</g:link>
							</div>
						</g:if>
					</td>
				</g:if>


			<g:else>
				<div class="title">
					<warehouse:message code="report.transactionReport.title"/>
				</div>
				<table>
					<tr>
						<td class="right">
							<label>
								<warehouse:message code="report.location.label"/>
							</label>
						</td>
						<td>
							${command?.location?.name }
						</td>
					</tr>
					<tr>
						<td class="right">
							<label>
								<warehouse:message code="report.category.label"/>
							</label>
						</td>
						<td>
							<format:category category="${command?.category}"/>
						</td>
					</tr>
					<tr>
						<td class="right">
							<label>
								<warehouse:message code="report.dateRange.label"/>
							</label>
						</td>
						<td>
							${format.date(obj:command?.startDate)} <warehouse:message code="default.to.label"/> ${format.date(obj:command?.endDate)}
						</td>
					</tr>
				</table>
			</g:else>



				<td>
					<g:if test="${command.product }">
						<style>
							.debit:before { content: '-'; }
							.debit:after { content: ''; }
							.credit:before { content: '+'; }
							.credit:after { content: ''; }
							.product_inventory { font-weight: bold; }
							.inventory { font-weight: bold; }
						</style>
						<g:set var="i" value='${0 }'/>
						<g:each var="entry" in="${command?.entries }">
							<g:if test="${command?.product == entry?.value?.product}">

                                <div class="box">

                                    <div class="header">
                                        <table>
                                            <tr>
                                                <td>
                                                    <h1 style="display:inline;">
                                                        ${entry.key }&nbsp;
                                                        <span class="circle">${entry.value.entries*.value.quantityRunning.sum() }</span>
                                                    </h1>
                                                    <g:link controller="report" action="generateTransactionReport" params="['location.id':command?.location?.id,'category.id':command?.category.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':params.includeChildren]" style="display: inline">
                                                        <warehouse:message code="report.backToInventoryReport.label"/>
                                                    </g:link>
                                                    &nbsp;|&nbsp;
                                                    <g:link controller="report" action="generateTransactionReport" params="['product.id':command?.product?.id,'category.id':command?.category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate,showEntireHistory:true,'includeChildren':params.includeChildren]">
                                                        <warehouse:message code="report.showEntireHistory.label"/>
                                                    </g:link>
                                                    &nbsp;|&nbsp;
                                                    <g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">
                                                        <warehouse:message code="report.showStockCard.label"/>
                                                    </g:link>
                                                </td>
                                            </tr>
                                        </table>


                                    </div>
                                    <br/>
                                    <g:each var="itemEntry" in="${entry.value.entries}">
                                        <h2>
                                            ${itemEntry.key.lotNumber ?: 'EMPTY'}&nbsp;
                                            <span class="circle">${itemEntry?.value?.quantityRunning}</span>
                                        </h2>

                                        <table style="border: 1px solid lightgrey" class="report">
                                            <thead>
                                                <tr class="${i++%2?'odd':'even' }">
                                                    <th>
                                                        <warehouse:message code="report.transactionDate.label"/>
                                                    </th>
                                                    <th>
                                                        <warehouse:message code="report.transactionType.label"/>
                                                    </th>
                                                    <th class="center">
                                                        <warehouse:message code="report.quantityChange.label"/>
                                                    </th>
                                                    <th class="center">
                                                        <warehouse:message code="report.quantityBalance.label"/>
                                                    </th>
                                                </tr>
                                            </thead>
                                            <g:if test="${itemEntry.value.transactionEntries}">
                                                <tbody>
                                                    <tr>
                                                        <td><format:date obj="${command?.startDate}"/></td>
                                                        <td><warehouse:message code="report.initialQuantity.label"/></td>
                                                        <td></td>
                                                        <td class="center">${itemEntry?.value?.quantityInitial }</td>
                                                    </tr>
                                                    <g:each var="row" in="${itemEntry.value.transactionEntries}">
                                                        <g:set var="transactionTypeCode" value="${row?.transactionEntry?.transaction?.transactionType?.transactionCode?.toString()?.toLowerCase()}"/>
                                                        <tr class="${i++%2?'odd':'even' }">
                                                            <td>
                                                                <format:date obj="${row.transactionEntry?.transaction?.transactionDate}"/>
                                                            </td>
                                                            <td>
                                                                <g:link controller="inventory" action="showTransaction" id="${row?.transactionEntry?.transaction?.id }">
                                                                    <format:metadata obj="${row.transactionEntry?.transaction?.transactionType}"/>
                                                                </g:link>
                                                            </td>
                                                            <td class="center">
                                                                <span class="${transactionTypeCode}">${row.transactionEntry?.quantity }</span>
                                                            </td>
                                                            <td class="center">${row.balance }</td>
                                                        </tr>
                                                    </g:each>

                                                </tbody>
                                                <tfoot>
                                                    <tr>
                                                        <td><format:date obj="${command?.endDate}"/></td>
                                                        <td><warehouse:message code="report.finalQuantity.label"/></td>
                                                        <td></td>
                                                        <td class="center">${itemEntry?.value?.quantityFinal }</td>
                                                    </tr>
                                                </tfoot>
                                            </g:if>
                                            <g:else>
                                                <tbody>
                                                    <tr>
                                                        <td colspan="5">
                                                            <warehouse:message code="transaction.noTransactions.label"/>
                                                        </td>
                                                    </tr>
                                                </tbody>
                                            </g:else>
                                        </table>
                                        <br/>
                                    </g:each>
                                </div>
							</g:if>
						</g:each>
					</g:if>
					<g:else>
				    	<div class="box">
				   			<g:set var="status" value="${0 }"/>
					    	<g:each var="productEntry" in="${command?.productsByCategory }" status="i">
					    		<g:set var="category" value="${productEntry.key }"/>
                                <h2 class="middle">
                                    <table>
                                        <tr>
                                            <td class="left">

                                                <h1 style="display:inline;">
                                                    <format:category category="${category}"/>
                                                </h1>
                                                <g:if test="${!params.print}">
                                                    &nbsp;
                                                    <g:link controller="report" action="generateTransactionReport" params="['location.id':command?.location?.id,'category.id':category?.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':false,'insertPageBreakBetweenCategories':command?.insertPageBreakBetweenCategories,'showTransferBreakdown':command?.showTransferBreakdown,'hideInactiveProducts':command?.hideInactiveProducts]" style="display: inline">
                                                        <warehouse:message code="report.showThisCategoryOnly.label"/>
                                                    </g:link>
                                                </g:if>
                                            </td>
                                            <td class="right">
                                                <span class="fade">
                                                    ${format.date(obj:command?.startDate, format: 'MMM dd, yyyy')} - ${format.date(obj:command?.endDate, format: 'MMM dd, yyyy')}
                                                </span>
                                            </td>
                                        </tr>
                                    </table>
                                </h2>

    							<div class="list">
							    	<table class="report">
							    		<thead>
							    			<tr style="border-top: 1px solid lightgrey;">
							    				<th rowspan="2" class="bottom">
													<warehouse:message code="report.productDescription.label"/>
							    				</th>
							    				<th rowspan="2" class="center bottom total start">
													<warehouse:message code="report.initialQuantity.label"/>
							    				</th>
							    				<td colspan="${(command.showTransferBreakdown) ? 3 + (transferInLocations?.size?:0) : 3}" class="center total">
							    					<label>
							    						<warehouse:message code="report.incomingQuantity.label"/>
							    					</label>
							    				</td>
							    				<td colspan="${(command.showTransferBreakdown) ? 6 + (transferOutLocations?.size?:0) : 6}" class="center total">
								    				<label>
								    					<warehouse:message code="report.outgoingQuantity.label"/>
								    				</label>
												</td>
												<th rowspan="2" class="center bottom total end">
													<warehouse:message code="report.finalQuantity.label"/>
												</th>
							    			</tr>

								    		<tr style="border-top: 1px solid lightgrey;">
												<th class="right">
													<warehouse:message code="report.incomingTransferQuantity.label"/>
												</th>
												<g:if test="${command.showTransferBreakdown }">
													<g:each var="location" in="${transferInLocations }">
														<th class="right bottom">${location.name.substring(0,3) }</th>
													</g:each>
												</g:if>
												<th class="right nowrap">
													<warehouse:message code="report.adjustedInQuantity.label"/>
												</th>
												<th class="right total">
													<warehouse:message code="report.incomingTotalQuantity.label"/>
												</th>
												<th class="right">
													<warehouse:message code="report.outgoingTransferQuantity.label"/>
												</th>
												<g:if test="${command.showTransferBreakdown }">
													<g:each var="location" in="${transferOutLocations }">
														<th class="right">${location.name.substring(0,3) }</th>
													</g:each>
												</g:if>
												<th class="right">
													<warehouse:message code="report.expiredQuantity.label"/>
												</th>
												<th class="right">
													<warehouse:message code="report.consumedQuantity.label"/>
												</th>
												<th class="right">
													<warehouse:message code="report.damagedQuantity.label"/>
												</th>
												<th class="right">
													<warehouse:message code="report.adjustedOutQuantity.label"/>
												</th>
												<th class="right total">
													<warehouse:message code="report.outgoingTotalQuantity.label"/>
												</th>
								    		</tr>
								    	</thead>
								    	<tbody>
									    	<g:each var="product" in="${productEntry.value }" status="j">
									    		<g:set var="entry" value="${command.entries[product].totals }"/>
												<tr class="${j%2 ? 'even' : 'odd' }">
													<td class="left" style="width: 35%">
														<g:if test="${!params.print }">
															<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">
																<format:product product="${product }"/>
												    		</g:link>

															<g:link controller="report" action="generateTransactionReport" params="['product.id':product?.id,'category.id':category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate,includeChildren:command?.includeChildren,pageBreak:params.pageBreak]">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" class="middle"/>
															</g:link>

											    		</g:if>
											    		<g:else>
															<format:product product="${product }"/>
											    		</g:else>
										    		</td>
													<td class="right total start nowrap">
											    		<span class="${(entry?.quantityInitial>=0)?'credit':'debit'}">
											    			${entry?.quantityInitial ?: 0}
											    		</span>
										    		</td>
										    		<td class="right nowrap">
											    		<span class="${(entry?.quantityTransferredIn>=0)?'credit':'debit'}">
												    		${entry?.quantityTransferredIn ?: 0}
												    	</span>
												    	<g:if test="${command.showTransferBreakdown && !params.print }">
												    		<img src="${createLinkTo(dir:'images/icons/silk',file:'magnifier.png')}" class="show-details middle"/>
												    		<div class="hidden details">
												    			<g:if test="${entry.quantityTransferredInByLocation }">
												    				<h2>
													    				<table>
													    					<tr class="unhighlight">
													    						<td class="">
												    								<format:product product="${product }"/>
													    						</td>
													    						<td class="right middle">
															    					<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" class="close-details middle"/>
													    						</td>
													    					</tr>
													    				</table>
												    				</h2>
													    			<table>
													    				<thead>
													    					<tr>
													    						<th>
													    							<warehouse:message code="default.from.label"/>
													    						</th>
													    						<th class="right">
													    							<warehouse:message code="default.qty.label"/>
													    						</th>
													    					</tr>
													    				</thead>
													    				<tbody>
															    			<g:each var="xferInMapEntry" in="${entry.quantityTransferredInByLocation.sort { it.key } }" status="xinStatus">
															    				<tr class="${xinStatus%2?'even':'odd' }">
															    					<td>
																	    				${xferInMapEntry.key }

																	    			</td>
																	    			<td class="right">
																		    			${xferInMapEntry.value }
																	    			</td>
																	    		</tr>
															    			</g:each>
														    			</tbody>
														    			<tfoot>
														    				<tr>
														    					<td>

														    					</td>
														    					<td class="right">
														    						${entry?.quantityTransferredIn?:0}
														    					</td>
														    				</tr>
														    			</tfoot>
													    			</table>
													    		</g:if>
												    			<g:unless test="${entry.quantityTransferredInByLocation }">
												    				<warehouse:message code="default.none.label"/>
												    			</g:unless>
												    		</div>
												    	</g:if>

													</td>
													<td class="right nowrap">
											    		<span class="${(entry?.quantityFound>=0)?'credit':'debit'}">${entry?.quantityFound ?: 0}</span>
													</td>
													<td class="right total nowrap">
											    		<span class="${(entry?.quantityTotalIn>=0)?'credit':'debit'}">
											    			${entry?.quantityTotalIn ?: 0}
											    		</span>
													</td>

										    		<g:if test="${command.showTransferBreakdown }">
														<g:each var="location" in="${transferOutLocations }">
															<td class="center nowrap">
																<span class="${(entry?.quantityTransferredOutByLocation[location]>0)?'debit':'credit'}">${entry.quantityTransferredOutByLocation[location]?:0}</span>
															</td>
														</g:each>
													</g:if>
													<td class="right nowrap">
														<span class="${(entry?.quantityTransferredOut>0)?'debit':'credit'}">${entry?.quantityTransferredOut?:0}</span>
												    	<g:if test="${command.showTransferBreakdown && !params.print }">
												    		<img src="${createLinkTo(dir:'images/icons/silk',file:'magnifier.png')}" class="show-details middle"/>
												    		<div class="hidden details">
												    			<g:if test="${entry.quantityTransferredOutByLocation }">
												    				<h2>
													    				<table>
													    					<tr class="unhighlight">
													    						<td class="left middle">
													    							<label>
														    							<format:product product="${product }"/>
														    						</label>
													    						</td>
													    						<td class="right middle">
															    					<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" class="close-details middle"/>
													    						</td>
													    					</tr>
													    				</table>
												    				</h2>
													    			<table>
													    				<thead>
													    					<tr>
													    						<th>
													    							<warehouse:message code="default.to.label"/>
													    						</th>
													    						<th class="right">
													    							<warehouse:message code="default.qty.label"/>
													    						</th>
													    					</tr>
													    				</thead>
													    				<tbody>
															    			<g:each var="xferOutMapEntry" in="${entry.quantityTransferredOutByLocation.sort { it.key } }" status="xoutStatus">
															    				<tr class="${xoutStatus%2?'even':'odd' }">
															    					<td>
																	    				${xferOutMapEntry.key }

																	    			</td>
																	    			<td class="right">
																		    			${xferOutMapEntry.value }
																	    			</td>
																	    		</tr>
															    			</g:each>
														    			</tbody>
														    			<tfoot>
														    				<tr>
														    					<td>
														    					</td>
														    					<td class="right">
														    						${entry?.quantityTransferredOut?:0}
														    					</td>
														    				</tr>
														    			</tfoot>
													    			</table>
													    		</g:if>
												    			<g:unless test="${entry.quantityTransferredOutByLocation }">
												    				<warehouse:message code="default.none.label"/>
												    			</g:unless>
												    		</div>
												    	</g:if>
													</td>
													<td class="right nowrap">
											    		<span class="${(entry?.quantityExpired>0)?'debit':'credit'}">${entry?.quantityExpired ?: 0}</span>
													</td>
													<td class="right nowrap">
											    		<span class="${(entry?.quantityConsumed>0)?'debit':'credit'}">${entry?.quantityConsumed ?: 0}</span>
													</td>
													<td class="right nowrap">
											    		<span class="${(entry?.quantityDamaged>0)?'debit':'credit'}">${entry?.quantityDamaged ?: 0}</span>
													</td>
													<td class="right nowrap">
											    		<span class="${(entry?.quantityLost>=0)?'credit':'debit'}">${entry?.quantityLost ?: 0}</span>
													</td>
													<td class="right total nowrap">
														<span class="${(entry?.quantityTotalOut>0)?'debit':'credit'}">${entry?.quantityTotalOut ?: 0 }</span>
													</td>
													<td class="right total end nowrap">
														<span class="${(entry?.quantityFinal>=0)?'credit':'debit'}">${entry?.quantityFinal ?: 0}</span>
													</td>
										    	</tr>
									    	</g:each>
										</tbody>
									</table>
								</div>
								<br/>
							</g:each>
				    	</div>

				    </g:else>
				</td>
			</tr>
		</table>
	    <script>

		    function showDetails() {
				//$(this).children(".actions").show();
			}

			function hideDetails() {
				$(this).parent().children(".details").hide();
			}

			$(document).ready(function() {

				/*
				$(".details").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 5,   // number = milliseconds for onMouseOver polling interval
					over: showActions,     // function = onMouseOver callback (required)
					timeout: 100,   // number = milliseconds delay before onMouseOut
					out: hideActions       // function = onMouseOut callback (required)
				});
				*/
				/*
				$(".show-details").mouseout(function() {
					$(this).parent().children(".details").toggle();
				});
				$(".show-details").click(function() {
					$(this).parent().children(".details").show();
				});
				*/

				$(".close-details").click(function(event) {
					$(".details").hide();
				});


				$(".show-details").click(function(event) {
					//show the menu directly over the placeholder
					var details = $(this).parent().children(".details");

					// Need to toggle before setting the position
					details.toggle();

					// Set the position for the actions menu
				    details.position({
						my: "left top",
						at: "left bottom",
						of: $(this).closest(".show-details"),
						//offset: "0 0"
						collision: "flip"
					});

					// To prevent the action button from POST'ing to the server
					event.preventDefault();
				});


				$(".details").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 5,   // number = milliseconds for onMouseOver polling interval
					over: showDetails,     // function = onMouseOver callback (required)
					timeout: 100,   // number = milliseconds delay before onMouseOut
					out: hideDetails       // function = onMouseOut callback (required)
				});

			    // Open / Close Panel According to Cookie //
			    /*
			    if ($.cookie('panel') == 'open'){
			        $('#parameters').slideDown('fast'); // Show on Page Load / Refresh with Animation
			        $('#parameters').show(); // Show on Page Load / Refresh without Animation
			        $("#hideParameters").show();
			        $("#showParameters").hide();
			    } else {
			        $('#parameters').slideUp('fast'); // Hide on Page Load / Refresh with Animation
			        $('#parameters').hide(); // Hide on Page Load / Refresh without Animation
			        $("#hideParameters").hide();
			        $("#showParameters").show();
			    }
				*/
				function toggleParameters() {
					$('#parameters').slideToggle('fast', function(){
			            if ($(this).is(':hidden')) {
			                $.cookie('panel', 'closed');
			            } else {
			                $.cookie('panel', 'open');
			            }
			        });
				}


			    // Toggle Panel and Set Cookie //
			    $('.toggleParameters').click(function(){
			        toggleParameters();
			        $(".toggleParameters").toggle();

			    });

			});

	    </script>

    </body>
</html>
