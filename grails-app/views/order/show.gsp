
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
            	<fieldset>
            		<g:render template="header" model="[orderInstance:orderInstance]"/>
            	
            	
	                <table>
	                    <tbody>
	                    
	                        <tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='orderNumber'><g:message code="order.orderNumber.label" default="Order Number" /></label>
	                            </td>
	                            
	                            <td valign="top" class="value">${fieldValue(bean: orderInstance, field: "orderNumber")}</td>
	                            
	                        </tr>
	
	                        <tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='description'><g:message code="order.description.label" default="Description" /></label>
	                            </td>
	                            
	                            <td valign="top" class="value">${fieldValue(bean: orderInstance, field: "description")}</td>
	                            
	                        </tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Order from</label></td>
								<td valign='top' class='value'>
									${orderInstance?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Destination</label></td>
								<td valign='top' class='value'>
									${orderInstance?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'>Order date</label></td>
								<td valign='top' class='value'>								
									${orderInstance?.dateOrdered } 
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='orderedBy'>Ordered by</label></td>
								<td valign='top'class='value'>
									${orderInstance?.orderedBy?.name }
								</td>
							</tr>
	                        
	                        <tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="order.items.label" default="Items" /></label></td>
	                            <td valign="top" class="value">
									<g:if test="${orderInstance?.orderItems }">
										<table>
											<thead>
												<tr class="odd">
													<th>Type</th>
													<th>Product</th>
													<th>Quantity</th>										
												</tr>
											</thead>									
											<tbody>
												<g:each var="orderItem" in="${orderInstance?.orderItems}" status="i">
													<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
														<td>
															<g:if test="${orderItem?.product }">
																Product
															</g:if>
															<g:elseif test="${orderItem?.category }">
																Category
															</g:elseif>							
															<g:else>
																Unclassified											
															</g:else>
														</td>
														<td>
															<g:if test="${orderItem?.product }">
																${orderItem?.product?.name}
															</g:if>
															<g:else>
																${orderItem?.description }
															</g:else>
														</td>
														<td>
															${orderItem?.quantity}
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
	                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><label for="comments"><g:message code="order.comments.label" default="Comments" /></label></td>
	                            <td valign="top" class="value">
									<g:if test="${orderInstance?.comments }">
										<table>
											<thead>
												<tr>
													<th>Comment</th>
												</tr>
											</thead>									
											<tbody>
												<g:each in="${orderInstance?.comments}">
													<tr>
														<td>
															${it?.comment}
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
	                            	<label for="comments"><g:message code="order.documents.label" default="Documents" /></label>
	                            </td>                            
	                            <td valign="top" class="value">
									<g:if test="${orderInstance?.documents }">
										<table>
											<thead>
												<tr>
													<th>Document</th>
												</tr>
											</thead>									
											<tbody>
												<g:each in="${orderInstance?.documents}">
													<tr>
														<td>
															${it?.name}
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
	                            <td valign="top" class="name">
	                            	<label for="dateCreated"><g:message code="order.dateCreated.label" default="Date Created" /></label>
	                            </td>
	                            <td valign="top" class="value"><g:formatDate date="${orderInstance?.dateCreated}" /></td>
	                            
	                        </tr>
	                    
	                        <tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for="lastUpdated"><g:message code="order.lastUpdated.label" default="Last Updated" /></label>
	                            
	                            </td>
	                            
	                            <td valign="top" class="value"><g:formatDate date="${orderInstance?.lastUpdated}" /></td>
	                            
	                        </tr>
	                    
	                    
	                    </tbody>
	                </table>
               </fieldset>
            </div>
        </div>
    </body>
</html>
