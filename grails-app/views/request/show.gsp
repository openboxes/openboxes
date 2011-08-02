
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'request.label', default: 'Request')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
            	<fieldset>
            		<g:render template="summary" model="[requestInstance:requestInstance]"/>
            	
            	
	                <table>
	                    <tbody>
	                        <tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='description'><warehouse:message code="request.description.label" default="Description" /></label>
	                            </td>
	                            
	                            <td valign="top" class="value">${fieldValue(bean: requestInstance, field: "description")}</td>
	                            
	                        </tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Request from</label></td>
								<td valign='top' class='value'>
									${requestInstance?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Request for</label></td>
								<td valign='top' class='value'>
									${requestInstance?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='requestedBy'>Requested by</label></td>
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
	                        <tr class="prop">
	                            <td valign="top" class="name"><label for="comments"><warehouse:message code="request.comments.label" default="Comments" /></label></td>
	                            <td valign="top" class="value">
									<g:if test="${requestInstance?.comments }">
										<table>
											<thead>
												<tr class="odd">
													<th>To</th>
													<th>From</th>
													<th>Comment</th>
													<th>Date</th>
													<th>Actions</th>
												</tr>
											</thead>									
											<tbody>
												<g:each var="commentInstance" in="${requestInstance?.comments}">
													<tr>
														<td>	
															${commentInstance?.recipient?.name}
														</td>
														<td>	
															${commentInstance?.sender?.name}
														</td>
														<td>
															${commentInstance?.comment}
														</td>
														<td>	
															${commentInstance?.lastUpdated}
														</td>
														<td align="right">
															<g:link action="editComment" id="${commentInstance.id}" params="['request.id':requestInstance?.id]">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
															</g:link>
														
															<g:link action="deleteComment" id="${commentInstance.id}" params="['request.id':requestInstance?.id]" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
																<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
															</g:link>
														</td>
													</tr>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade">No comments</span>
									</g:else>
	                            
	                            </td>
	                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for="comments"><warehouse:message code="request.documents.label" default="Documents" /></label>
	                            </td>                            
	                            <td valign="top" class="value">
									<g:if test="${requestInstance?.documents }">
										<table>
											<thead>
												<tr class="odd">
													<th>Filename</th>
													<th>Type</th>
													<th>Description</th>
													<th>Size</th>
													<th>Last Updated</th>
													<th>Actions</th>
												</tr>
											</thead>									
											<tbody>
												<g:each var="documentInstance" in="${requestInstance?.documents}">
													<tr>
														<td>
															<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Edit" />
															${documentInstance?.filename} (<g:link controller="document" action="download" id="${documentInstance.id}">download</g:link>)
														</td>
														<td>${documentInstance?.documentType?.name}</td>
														<td>${documentInstance?.name}</td>
														<td>${documentInstance?.size} bytes</td>
														<td>${documentInstance?.lastUpdated}</td>
														<td align="right">
															<g:link action="editDocument" id="${documentInstance.id}" params="['request.id':requestInstance?.id]">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
															</g:link>
														
															<g:link action="deleteDocument" id="${documentInstance.id}" params="['request.id':requestInstance?.id]" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
																<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
															</g:link>
														</td>
													</tr>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade">No documents</span>
									</g:else>
	                            
	                            </td>
	                        </tr>
							<tr class="prop">
	                            <td valign="top" class="name"><label><warehouse:message code="request.items.label" default="Items" /></label></td>
	                            <td valign="top" class="value">
									<g:if test="${requestInstance?.requestItems }">
										<table>
											<thead>
												<tr class="odd">
													<th>Type</th>
													<th>Product</th>
													<th>Qty Requested</th>
													<th>Qty Fulfilled</th>
												</tr>
											</thead>									
											<tbody>
												<g:each var="requestItem" in="${requestInstance?.requestItems}" status="i">
													<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
														<td>
															<g:if test="${requestItem?.product }">
																Product
															</g:if>
															<g:elseif test="${requestItem?.category }">
																Category
															</g:elseif>							
															<g:else>
																Unclassified											
															</g:else>
														</td>
														<td>
															<g:if test="${requestItem?.product }">
																${requestItem?.product?.name}
															</g:if>
															<g:else>
																${requestItem?.description }
															</g:else>
														</td>
														<td>
															${requestItem?.quantity}
														</td>
														<td>
															${requestItem?.quantityFulfilled()}
														</td>
													</tr>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade">No items</span>
									</g:else>
	                            </td>
	                        </tr>	                        
	                    
	                    </tbody>
	                </table>
               </fieldset>
            </div>
        </div>
    </body>
</html>
