
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'request.requests.label', default: 'Requests').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
           		<g:render template="summary" model="[requestInstance:requestInstance]"/>
           		
           		
				<div class="tabs">
					<ul>
						<li>
							<a href="#tabs-details">
								<warehouse:message code="request.details.label"/>
							</a>
						</li>		   
						<li>
							<a href="#tabs-request-items">
								<warehouse:message code="request.requestItems.label"/>
							</a>
						</li>								
					</ul>             
						
						
					<div id="tabs-details">
						<table>
		                    <tbody>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='source'><warehouse:message code="request.status.label"/></label>
									</td>
									<td valign='top' class='value'>
										${requestInstance?.status?.encodeAsHTML()}
									</td>
								</tr>
		                        <tr class="prop">
		                            <td valign="top" class="name">
		                            	<label for='description'><warehouse:message code="default.description.label" default="Description" /></label>
		                            </td>
		                            
		                            <td valign="top" class="value">${fieldValue(bean: requestInstance, field: "name")}</td>
		                            
		                        </tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for='source'><warehouse:message code="request.requestFrom.label"/></label></td>
									<td valign='top' class='value'>
										${requestInstance?.origin?.name?.encodeAsHTML()}
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for="destination"><warehouse:message code="request.requestFor.label"/></label></td>
									<td valign='top' class='value'>
										${requestInstance?.destination?.name?.encodeAsHTML()}
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for="recipientProgram"><warehouse:message code="request.recipientProgram.label"/></label></td>
									<td valign='top' class='value'>
										${requestInstance?.recipientProgram }
									</td>
								</tr>
								
								<tr class='prop'>
									<td valign='top' class='name'><label for="recipient"><warehouse:message code="request.recipient.label"/></label></td>
									<td valign='top' class='value'>
										${requestInstance?.recipient?.name}
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'><label for='requestedBy'><warehouse:message code="request.requestedBy.label"/></label></td>
									<td valign='top'class='value'>
										${requestInstance?.requestedBy?.name }
									</td>
								</tr>
								
		                        <%--
								<tr class="prop">
		                            <td valign="top" class="name"><label><warehouse:message code="request.items.label" default="Shipments" /></label></td>
		                            <td valign="top" class="value">	    
		                            
										<g:if test="${requestInstance?.shipments() }">
											<table>
												<thead>
													<tr class="odd">
														<th>Type</th>
														<th>Name</th>
													</tr>
												</thead>									
												<tbody>      
													<g:each var="shipmentInstance" in="${requestInstance?.shipments()}" status="i">
														<tr>
															<td>
																${shipmentInstance?.shipmentType?.name }
															</td>
															<td>
																<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }">${shipmentInstance?.name }</g:link>
															</td>													
														</tr>
													</g:each>                                          
		                        				</tbody>
		                        			</table>
		                        		</g:if>
										<g:else>
											<span class="fade">No shipments</span>
										</g:else>
		                            	                        		
		                        	</td>
		                        </tr>	                        
		                     --%>
		                        
		                    </tbody>
		                </table>
		            </div>
		       
	                <div id="tabs-request-items">
	                
             			<g:if test="${requestInstance?.requisitionItems }">
							<table>
								<thead>
									<tr class="odd">
										<th><warehouse:message code="default.type.label"/></th>
										<th><warehouse:message code="product.label"/></th>
										<th><warehouse:message code="request.quantity.label"/></th>
										<th><warehouse:message code="picklist.quantity.label"/></th>
									</tr>
								</thead>									
								<tbody>
									<g:each var="requestItem" in="${requestInstance?.requisitionItems}" status="i">
										<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
											<td>
												<g:if test="${requestItem?.product }">
													<warehouse:message code="product.label"/>
												</g:if>
												<g:elseif test="${requestItem?.productGroup }">
													<warehouse:message code="productGroup.label"/>
												</g:elseif>							
												<g:elseif test="${requestItem?.category }">
													<warehouse:message code="category.label"/>
												</g:elseif>							
												<g:else>
													<warehouse:message code="default.unclassified.label"/>											
												</g:else>
											</td>
											<td>
												<format:metadata obj="${requestItem.displayName()}"/>
											</td>
											<td>
												${requestItem?.quantity}
											</td>
											<td>
												<g:set var="quantityPicked" value="${requestItem?.picklistItems?.sum { it.quantity }?:0 }"/>
												${quantityPicked }
												
											</td>
											<%-- 
											<td>
												${requestItem?.quantityFulfilled()}
											</td>
											--%>
										</tr>
									</g:each>
								</tbody>
							</table>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.noItems.label"/></span>
						</g:else>
	                </div>
	            </div>
	                
	                
            </div>
        </div>
        
	<script type="text/javascript">
    	$(document).ready(function() {
	    	$(".tabs").tabs(); 
	    });
       </script>		        
    </body>
</html>
