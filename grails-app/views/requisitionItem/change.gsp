
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${requisitionItemInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${requisitionItemInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:hiddenField name="id" value="${requisitionItemInstance?.id}" />
            <g:hiddenField name="version" value="${requisitionItemInstance?.version}" />
            
                
                <div class="dialog">
                
	                 <g:render template="../requisition/summary" model="[requisition:requisitionItemInstance?.requisition]"/>
                    <table>
                        <tbody>                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><warehouse:message code="requisitionItem.product.label" default="Product" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'product', 'errors')}">
	                                ${requisitionItemInstance?.product?.productCode} - ${requisitionItemInstance?.product?.name}
                                </td>
                            </tr>
                            <%-- 
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><warehouse:message code="product.unitOfMeasure.label" default="Product" /></label>
                                </td>
                                <td valign="top" class="value">
	                                ${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                                </td>
                            </tr>
                            --%>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantity"><warehouse:message code="requisitionItem.quantity.label" default="Quantity requested" /></label>
                                </td>
                                <td class="middle value ${hasErrors(bean: requisitionItemInstance, field: 'quantity', 'errors')}">
                                	${requisitionItemInstance?.quantity }
                                	<%-- 
                                    <g:textField name="quantity" class="text" size="10" value="${fieldValue(bean: requisitionItemInstance, field: 'quantity')}" />
	                                --%>
	                                ${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantity"><warehouse:message code="default.quantityOnHand.label" default="Quantity on Hand" /></label>
                                </td>
                                <td class="middle value">
                                    <div id="quantityOnHand">
                                    	${quantityOnHand }
                                    	${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                    </div>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantityCanceled"><warehouse:message code="requisitionItem.quantityCanceled.label" default="Quantity canceled" /></label>
                                </td>
                                <td class="middle value">
                                    <div id="quantityCanceled">
                                    	${requisitionItemInstance?.quantityCanceled?:0 }
                                    	${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                    </div>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantityCanceled"><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Cancel reason" /></label>
                                </td>
                                <td class="middle value">
                                	${requisitionItemInstance?.cancelReasonCode }
								</td>
							</tr>                            
                            <%--
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantity"><warehouse:message code="default.quantityAvailableToPromise.label" default="Quantity Available to Promise" /></label>
                                </td>
                                <td class="middle value">
                                    <div id="quantityAvailableToPromise">
                                    	${quantityAvailableToPromise }
                                    	${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                    </div>
                                </td>
                            </tr>
                             --%>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantity"><warehouse:message code="requisitionItem.substitutionsAndChanges.label" default="Substitutions & changes" /></label>
                                </td>
                                <td class="middle value">
                                    <div>
                                    
                                    
                                    	<g:if test="${!requisitionItemInstance?.quantityCanceled }">
	                                    	<div class="box" style="float:left;">
	                                    		<h3>${warehouse.message(code:'requisitionItem.changeQuantity.label') }</h3>
	                                    		<div class="dialog">
		                                    		<g:form controller="requisition" action="changeQuantity">
		                                    			<g:hiddenField name="id" value="${requisitionItemInstance?.requisition?.id }"/>
		                                    			<g:hiddenField name="requisitionItem.id" value="${requisitionItemInstance?.id }"/>
		                                    			<div class="prop">
														 	${requisitionItemInstance?.product?.name }
													 	</div>
													 	<div class="prop">
															<g:textField name="quantity" value="${requisitionItemInstance?.quantity}" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
														</div>
														<div class="prop">
								                            <g:select name="parentCancelReasonCode" from="['Stock out','Substituted','Damaged','Expired','Reserved',
																'Cancelled by requestor','Clinical adjustment', 'Other']" 
																noSelection="['null':'']" value="${requisitionItemInstance.cancelReasonCode }"/>
														
														</div>
														<div class="prop">
															<button class="button">
																${warehouse.message(code:'default.button.save.label') }
															</button>
														</div>
													</g:form>     
												</div>                               	
	                                    	</div>
	                                    
	                                    	<div class="box" style="float:left;">
	                                    		<h3>${warehouse.message(code:'requisitionItem.addSubstitution.label') }</h3>
												<div class="dialog">
		                                    		<g:form controller="requisition" action="addSubstitution">
		                                    			<g:hiddenField name="id" value="${requisitionItemInstance?.requisition?.id }"/>
		                                    			<g:hiddenField name="requisitionItem.id" value="${requisitionItemInstance?.id }"/>
		                                    			<div class="prop">
				                                    		<g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
																width="200" valueId="" valueName="" styleClass="text"/>
														</div>
														<div class="prop">
															<g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
														</div>																								
														<div class="prop">
								                            <g:select name="parentCancelReasonCode" from="['Stock out','Substituted','Damaged','Expired','Reserved',
																'Cancelled by requestor','Clinical adjustment', 'Other']" 
																noSelection="['null':'']" value="${requisitionItemInstance.cancelReasonCode }"/>
														
														</div>
														<div class="prop">
															<button class="button">
																${warehouse.message(code:'default.button.save.label') }
															</button>												
														</div>
													</g:form>                                    	
												</div>
	                                    	</div>
	                                    </g:if>
                                    	
										<div class="box" style="float:left;">
                                    		<h3>${warehouse.message(code:'requisitionItem.addAddition.label') }</h3>
											<div class="dialog">
	                                    		<g:form controller="requisition" action="addAddition">
	                                    			<g:hiddenField name="id" value="${requisitionItemInstance?.requisition?.id }"/>
	                                    			<g:hiddenField name="requisitionItem.id" value="${requisitionItemInstance?.id }"/>
	                                    			<div class="prop">
			                                    		<g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
															width="200" valueId="" valueName="" styleClass="text"/>
													</div>
													<div class="prop">
														<g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
													</div>
													<div class="prop">
														<button class="button">
															${warehouse.message(code:'default.button.save.label') }
														</button>												
													</div>
												</g:form>                                    	
											</div>
                                    	</div>                                    	
                                    
                                    	<table>
                                    		<thead>
                                    			<tr>
                                    				<th></th>
                                    				<th>${warehouse.message(code:'product.label') }</th>
                                    				<th>${warehouse.message(code:'product.unitOfMeasure.label') }</th>
                                    				<th>${warehouse.message(code:'requisitionItem.quantity.label') }</th>
                                    			</tr>
                                    		</thead>
                                    		<tbody>
	                                    		<g:each var="requisitionItem" in="${requisitionItemInstance?.requisitionItems }">
		                                    		<tr>
														<td>
														</td>	       
														<td>
															${requisitionItem?.product?.name }
														</td>	                             		
														<td>
															${requisitionItem?.product?.unitOfMeasure }
														</td>	                             		
														<td>
															${requisitionItem?.quantity}
														</td>	                             		
		                                    		</tr>
	                                    		</g:each>
	                                    		<g:unless test="${requisitionItemInstance?.requisitionItems }">
	                                    			<tr>
	                                    				<td colspan="4" class="center">
	                                    					<warehouse:message code="requisitionItem.noChanges.message"/>
	                                    				</td>
	                                    			</tr>
	                                    		</g:unless>
                                    		</tbody>
                                    	</table>

                                    </div>
                                </td>
                            </tr>
                        
                        	                    
                           	<tr class="prop">
	                        	<td valign="top"></td>
	                        	<td valign="top">                        	
					                <div class="buttons">
					                    <%-- 
					                    <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
					                    &nbsp;
					                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                    --%>
										<g:link controller="requisition" action="review" id="${requisitionItemInstance?.requisition?.id }">
											<warehouse:message code="default.button.back.label"/>
										</g:link>					                    
					                    
					                </div>
	    						</td>                    	
                        	</tr>	 
                        </tbody>
                    </table>
                </div>
           
        </div>
        
		<script>
			$(document).ready(function() {				
		    	

		    	//$("#quantityOnHand").load('${request.contextPath}/json/getQuantityOnHand?location.id=${session.warehouse.id}&product.id=${requisitionItem?.product?.id}');
		    	//$("#quantityAvailableToPromise").load('${request.contextPath}/json/getQuantityAvailableToPromise?location.id=${session.warehouse.id}&product.id=${requisitionItem?.product?.id}');
		    	
	    		/*
		    	$(".reloadQuantityOnHand").click(function(event) {
		    		//setInterval(refreshQuantity, 1000);
		    		refreshQuantity();
			    });

				refreshQuantity();
			    */
			});	
		</script>        
        
    </body>
</html>
