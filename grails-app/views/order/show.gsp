
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
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="order.orderNumber.label" default="Order Number" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: orderInstance, field: "orderNumber")}</td>
                            
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="order.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: orderInstance, field: "description")}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="order.items.label" default="Items" /></td>
                            <td valign="top" class="value">
								<g:if test="${orderInstance?.orderItems }">
									<table>
										<thead>
											<tr>
												<th>Type</th>
												<th>Product</th>
												<th>Quantity</th>										
											</tr>
										</thead>									
										<tbody>
											<g:each in="${orderInstance?.orderItems}">
												<tr>
													<td>
														${it?.product?.category?.name}
													</td>
													<td>
														${it?.product?.name}
													</td>
													<td>
														${it?.quantity}
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
                            <td valign="top" class="name"><g:message code="order.comments.label" default="Comments" /></td>
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
                            <td valign="top" class="name"><g:message code="order.documents.label" default="Documents" /></td>
                            
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
                            <td valign="top" class="name"><g:message code="order.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${orderInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="order.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${orderInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons">
					                <g:form>
					                    <g:hiddenField name="id" value="${orderInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
