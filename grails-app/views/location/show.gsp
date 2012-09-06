<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'warehouse.label', default: 'Location')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery',file:'jquery.colorpicker.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.colorpicker.js')}" type="text/javascript" ></script>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationInstance}" as="list" />
	            </div>
            </g:hasErrors>
                        
			<g:render template="summary"/>			  
			
			
			<div class="tabs">
				<ul>
					<li>
						<a href="#tabs-details">
							<warehouse:message code="location.label"/>
						</a>
					</li>
					<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
						<li>
							<a href="#tabs-transactions">
								<warehouse:message code="transactions.label"/>
							</a>
						</li>
						<li>
							<a href="#tabs-shipments">
								<warehouse:message code="shipments.label"/>
							</a>
						</li>
						<li>
							<a href="#tabs-requests">
								<warehouse:message code="requests.label"/>																	
							</a>
						</li>
						<li>
							<a href="#tabs-orders">
								<warehouse:message code="orders.label"/>																	
							</a>
						</li>
						<li>
							<a href="#tabs-events">
								<warehouse:message code="events.label"/>
							</a>
						</li>
						<li>
							<a href="#tabs-users">
								<warehouse:message code="users.label"/>																	
							</a>
						</li>
					</g:isUserInRole>
				</ul>		
				<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
					<div id="tabs-transactions">	
						<table>
							<tr>
								<th>Actions</th>							
								<th>Id</th>
								<th>Type</th>
								<th>Inventory</th>
								<th>Source</th>
								<th>Destination</th>
							</tr>
							<g:each var="transaction" in="${locationInstance?.transactions }" status="i">
								<tr class="${i%2?'odd':'even' }">
									<td>
										<g:render template="../transaction/actions" model="[transactionInstance:transaction]"/>
									</td>
									<td>${transaction.id }</td>
									<td>${transaction.transactionType?.name }</td>
									<td>${transaction.inventory }</td>
									<td>${transaction.source }</td>
									<td>${transaction.destination }</td>
									<%-- 
									<td><g:link controller="inventory" action="showTransaction" id="${transaction.id }">show</g:link></td>
									<td><g:link controller="inventory" action="editTransaction" id="${transaction.id }">edit</g:link></td>
									<td><g:link controller="location" action="deleteTransaction" id="${transaction.id }" 
										onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
										params="['location.id':locationInstance?.id]" fragment="tabs-transactions">delete</g:link></td>
										--%>
								</tr>
							</g:each>
						</table>
					</div>
					<div id="tabs-shipments">	
						<table>
							<tr>
								<th>Actions</th>							
								<th>Id</th>
								<th>Type</th>
								<th>Source</th>
								<th>Destination</th>
							</tr>						
							<g:each var="shipment" in="${locationInstance?.shipments }" status="i">
								<tr class="${i%2?'odd':'even' }">
									<td>
										<g:render template="../shipment/actions" model="[shipmentInstance:shipment]"/>
									</td>
									<td>${shipment.id }</td>
									<td>${shipment.shipmentType?.name }</td>
									<td>${shipment.origin }</td>
									<td>${shipment.destination }</td>
								</tr>
							</g:each>
						</table>
					</div>
					<div id="tabs-events">	
						<table>
							<g:each var="event" in="${locationInstance?.events }">
								<tr>
									<td>${event.id }</td>
									<td>${event }</td>
									<td><g:link controller="event" action="show" id="${event.id }">show</g:link></td>
									<td><g:link controller="event" action="edit" id="${event.id }">edit</g:link></td>
									<td>
										<g:link controller="location" action="deleteEvent" id="${event.id }" 
											onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
											params="['location.id':locationInstance?.id]" fragment="tabs-events">delete</g:link>
									</td>
								</tr>
							</g:each>
						</table>
					</div>
					<div id="tabs-requests">	
						<table>
							<g:each var="requestInstance" in="${locationInstance?.requests }">
								<tr>
									<td>${requestInstance.id }</td>
									<td>${requestInstance }</td>
									<td><g:link controller="request" action="show" id="${requestInstance.id }">show</g:link></td>
									<td><g:link controller="request" action="edit" id="${requestInstance.id }">edit</g:link></td>
									<td><g:link controller="location" action="deleteRequest" id="${requestInstance.id }" 
										onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
										params="['location.id':locationInstance?.id]" fragment="tabs-requests">delete</g:link></td>
								</tr>
							</g:each>
						</table>
					</div>
					<div id="tabs-orders">	
						<table>
							<g:each var="order" in="${locationInstance?.orders }">
								<tr>
									<td>${order.id }</td>
									<td>${order }</td>
									<td><g:link controller="order" action="show" id="${order.id }">show</g:link></td>
									<td><g:link controller="order" action="edit" id="${order.id }">edit</g:link></td>
									<td><g:link controller="location" action="deleteOrder" id="${order.id }" 
										onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
										params="['location.id':locationInstance?.id]" fragment="tabs-orders">delete</g:link></td>
								</tr>
							</g:each>
						</table>
					</div>
					<div id="tabs-users">	
						<table>
							<g:each var="user" in="${locationInstance?.users }">
								<tr>
									<td>${user.id }</td>
									<td>${user }</td>
									<td><g:link controller="user" action="show" id="${user.id }">show</g:link></td>
									<td><g:link controller="user" action="edit" id="${user.id }">edit</g:link></td>
									<td><g:link controller="location" action="deleteUser" id="${user.id }" 
										onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
										params="['location.id':locationInstance?.id]" fragment="tabs-users">delete</g:link></td>
								</tr>
							</g:each>
						</table>
	
					</div>				
				</g:isUserInRole>
				<div id="tabs-details">			   			
		            <g:form method="post" action="update">
		                <g:hiddenField name="id" value="${locationInstance?.id}" />
		                <g:hiddenField name="version" value="${locationInstance?.version}" />
		                
		                    <table>
		                        <tbody>
		                            <tr class="prop">
		                                <td valign="top" class="name">
											<label for="name"><warehouse:message code="default.name.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
											${locationInstance?.name}
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
											<label for="name"><warehouse:message code="location.locationType.label" /></label>
			
		                                </td>
		                                <td valign="top" class="value">
		                                	${locationInstance?.locationType?.name }
		                                		
		                                	
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
											<label for="name"><warehouse:message code="location.locationGroup.label" /></label>
		                                </td>
		                                <td valign="top" class="value">
											${locationInstance?.locationGroup?.name?:"none" }
		                                </td>
		                            </tr>	         
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                	<label for="manager"><warehouse:message code="warehouse.manager.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'manager', 'errors')}">
											${locationInstance?.manager}
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
			                                <label for="manager"><warehouse:message code="warehouse.properties.label" /></label>
		                                </td>
		                                <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
											<div>										
												<label><warehouse:message code="warehouse.active.label" /></label> ${locationInstance?.active}
											</div>										
											<div>										
												<label><warehouse:message code="warehouse.local.label" /></label> ${locationInstance?.local}
											</div>
		                                </td>
		                            </tr>
		                            
		                            <tr class="prop">
		                                <td valign="top" class="name">
											<label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
		                                </td>
		                                <td valign="top" class="value">
		                                	<g:each var="activity" in="${locationInstance?.supportedActivities?:locationInstance?.locationType?.supportedActivities}">
												<span class="box">${format.metadata(obj:activity)}</span>
											</g:each>
		                                	
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="bgColor"><warehouse:message code="warehouse.bgColor.label"/></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'bgColor', 'errors')}">
		                                    <span class="fade">${locationInstance?.bgColor }</span>
		                                    <span style="float: left">
		                                    	<g:select name="bgColor" class="colorpicker" 
			                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
			                                    	value="${locationInstance?.bgColor}" />
		                                   </span>
			                                   
		                                   	
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="fgColor"><warehouse:message code="warehouse.fgColor.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'fgColor', 'errors')}">
		                                    <span class="fade">${locationInstance?.fgColor }</span>
		                                    <span style="float: left">
			                                    <g:select name="fgColor" class="colorpicker" 
			                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
			                                    	value="${locationInstance?.fgColor}" />
		                                    </span>
		                                   	
		                                </td>
		                            </tr>
		                            
		                            <!--  
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="parentLocation"><warehouse:message code="warehouse.parentLocation.label" default="Parent Location" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'parentLocation', 'errors')}">
											<g:select name="parentLocation.id" from="${org.pih.warehouse.core.Location.list()}" 
												optionKey="id" optionValue="name" value="" noSelection="['null': '']" />							
		                                </td>
		                            </tr>
		                            -->
		                            <%-- 
		                            <tr class="prop">
		                            
		                            	<td valign="top" class="name">
		                            	
		                            	</td>
		                            	<td class="value">
											<div class="buttons left">
							                   <button type="submit">								
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
													<warehouse:message code="default.button.save.label"/>
												</button>
												&nbsp;
												<g:link action="list">
													${warehouse.message(code: 'default.button.cancel.label')}						
												</g:link>			
											</div>
										</td>
									</tr>
		                            --%>
		                        </tbody>
		                    </table>
		                
		               
		            </g:form>
        </div>
	    <script type="text/javascript">

	        function selectCombo(comboBoxElem, value) {
		        alert(comboBoxElem + " " + value)
				if (comboBoxElem != null) {
					if (comboBoxElem.options) { 
						for (var i = 0; i < comboBoxElem.options.length; i++) {
				        	if (comboBoxElem.options[i].value == value &&
				                comboBoxElem.options[i].value != "") { //empty string is for "noSelection handling as "" == 0 in js
				                comboBoxElem.options[i].selected = true;
				                break
				        	}
						}
					}
				}
			}						
	    
	        $(document).ready(function() {
	        	// store cookie for a day, without, it would be a session cookie
	        	$(".tabs").tabs({cookie: { expires: 1 } }); 
	            $('#bgColor').colorpicker({
	                size: 20,
	                label: '',
	                hide: true
	            });

	            $('#fgColor').colorpicker({
	                size: 20,
	                label: '',
	                hide: true
	            });
			
	        });
	    </script>        
    </body>
</html>
