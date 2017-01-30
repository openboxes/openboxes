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
		<link rel="stylesheet" href="${resource(dir:'js/jquery',file:'jquery.colorpicker.css')}" type="text/css" media="screen, projection" />
		<script src="${resource(dir:'js/jquery/', file:'jquery.colorpicker.js')}" type="text/javascript" ></script>
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
					<div id="tabs-transactions" class="dialog">

                        <div class="box">
                            <h2><g:message code="transactions.label"/></h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><g:message code="default.actions.label"/></th>
                                        <th><g:message code="default.id.label"/></th>
                                        <th><g:message code="transaction.date.label"/></th>
                                        <th><g:message code="transaction.type.label"/></th>
                                        <th><g:message code="transaction.inventory.label"/></th>
                                        <th><g:message code="transaction.source.label"/></th>
                                        <th><g:message code="transaction.destination.label"/></th>
                                    </tr>
                                </thead>
                                <g:each var="transaction" in="${locationInstance?.transactions }" status="i">
                                    <tr class="${i%2?'odd':'even' }">
                                        <td>
                                            <g:render template="../transaction/actions" model="[transactionInstance:transaction]"/>
                                        </td>
                                        <td>${transaction.id }</td>
                                        <td>${transaction.transactionDate }</td>
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
					</div>
					<div id="tabs-shipments" class="dialog">
                        <div class="box">
                        <h2><g:message code="shipments.label"/></h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><g:message code="default.actions.label"/></th>
                                        <th><g:message code="default.id.label"/></th>
                                        <th><g:message code="shipping.name.label"/></th>
                                        <th><g:message code="shipping.shipmentType.label"/></th>
                                        <th><g:message code="shipping.status.label"/></th>
                                        <th><g:message code="shipping.origin.label"/></th>
                                        <th><g:message code="shipping.destination.label"/></th>
                                    </tr>
                                </thead>
                                <g:each var="shipment" in="${locationInstance?.shipments }" status="i">
                                    <tr class="${i%2?'odd':'even' }">
                                        <td>
                                            <g:render template="../shipment/actions" model="[shipmentInstance:shipment]"/>
                                        </td>
                                        <td>${shipment.id }</td>
                                        <td>${shipment.shipmentType?.name }</td>
                                        <td>
                                            <format:metadata obj="${shipment?.status?.code}"/>
                                            <g:prettyDateFormat date="${shipment?.status?.date}" />
                                        </td>
                                        <td>
                                            ${shipment?.status?.date}
                                        </td>
                                        <td>${shipment.origin }</td>
                                        <td>${shipment.destination }</td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
					</div>
					<div id="tabs-events" class="dialog">
                        <div class="box">
                            <h2><g:message code="events.label"/></h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><g:message code="default.id.label"/></th>
                                        <th><g:message code="default.name.label"/></th>
                                        <th><g:message code="default.actions.label"/></th>
                                    </tr>
                                </thead>
                                <g:each var="event" in="${locationInstance?.events }" status="i">
                                    <tr class="${i%2?'odd':'even' }">
                                        <td>${event.id }</td>
                                        <td>${event }</td>
                                        <td>
                                            <g:link controller="event" action="show" id="${event.id }" class="button"><g:message code="default.button.show.label"/></g:link>
                                            <g:link controller="event" action="edit" id="${event.id }" class="button"><g:message code="default.button.edit.label"/></g:link>
                                            <g:link controller="location" action="deleteEvent" id="${event.id }" class="button"
                                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                                    params="['location.id':locationInstance?.id]" fragment="tabs-events"><g:message code="default.button.delete.label"/></g:link>
                                        </td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
					</div>
					<div id="tabs-requests" class="dialog">
                        <div class="box">
                            <h2><g:message code="requisitions.label"/></h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><g:message code="default.id.label"/></th>
                                        <th><g:message code="default.name.label"/></th>
                                        <th><g:message code="default.actions.label"/></th>
                                    </tr>
                                </thead>
                                <g:each var="requestInstance" in="${locationInstance?.requests }" status="i">
                                    <tr class="${i%2?'odd':'even' }">
                                        <td>${requestInstance.id }</td>
                                        <td>${requestInstance.name }</td>
                                        <td>
                                            <g:link controller="requisition" action="show" id="${requestInstance.id }" class="button"><g:message code="default.button.show.label"/></g:link>
                                            <g:link controller="requisition" action="edit" id="${requestInstance.id }" class="button"><g:message code="default.button.edit.label"/></g:link>
                                            <g:link controller="location" action="deleteRequest" id="${requestInstance.id }" class="button"
                                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                                    params="['location.id':locationInstance?.id]" fragment="tabs-requests"><g:message code="default.button.delete.label"/></g:link>
                                        </td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
					</div>
					<div id="tabs-orders" class="dialog">
						<div class="box">
                            <h2><g:message code="orders.label"/></h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><g:message code="default.id.label"/></th>
                                        <th><g:message code="default.name.label"/></th>
                                        <th><g:message code="default.actions.label"/></th>
                                    </tr>
                                </thead>
                                <g:each var="order" in="${locationInstance?.orders }" status="i">
                                    <tr class="${i%2?'odd':'even' }">
                                        <td>${order.id }</td>
                                        <td>${order.description }</td>
                                        <td>
                                            <g:link controller="order" action="show" id="${order.id }" class="button"><g:message code="default.button.show.label"/></g:link>
                                            <g:link controller="order" action="edit" id="${order.id }" class="button"><g:message code="default.button.edit.label"/></g:link>
                                            <g:link controller="location" action="deleteOrder" id="${order.id }" class="button"
                                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                                    params="['location.id':locationInstance?.id]" fragment="tabs-orders"><g:message code="default.button.delete.label"/></g:link>
                                        </td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
					</div>
					<div id="tabs-users" class="dialog">
                        <div class="box">
                            <h2><g:message code="orders.label"/></h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><g:message code="default.id.label"/></th>
                                        <th><g:message code="default.name.label"/></th>
                                        <th><g:message code="default.actions.label"/></th>
                                    </tr>
                                </thead>
                                <g:each var="user" in="${locationInstance?.users }" status="i">
                                    <tr class="${i%2?'odd':'even' }">
                                        <td>${user.id }</td>
                                        <td>${user.name }</td>
                                        <td>
                                            <g:link controller="user" action="show" id="${user.id }" class="button"><g:message code="default.button.show.label"/></g:link>
                                            <g:link controller="user" action="edit" id="${user.id }" class="button"><g:message code="default.button.edit.label"/></g:link>
                                            <g:link controller="location" action="deleteUser" id="${user.id }" class="button"
                                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                                    params="['location.id':locationInstance?.id]" fragment="tabs-users"><g:message code="default.button.delete.label"/></g:link></td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
					</div>
				</g:isUserInRole>
				<div id="tabs-details" class="dialog">
		            <g:form method="post" action="update">
		                <g:hiddenField name="id" value="${locationInstance?.id}" />
		                <g:hiddenField name="version" value="${locationInstance?.version}" />

                        <div class="box">
                            <h2><g:message code="location.label"/></h2>
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
											<label for="type"><warehouse:message code="location.locationType.label" /></label>
			
		                                </td>
		                                <td valign="top" class="value">
		                                	${locationInstance?.locationType?.name }
		                                		
		                                	
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
											<label for="locationGroup"><warehouse:message code="location.locationGroup.label" /></label>
		                                </td>
		                                <td valign="top" class="value">
											${locationInstance?.locationGroup?.name?:warehouse.message(code:'default.none.label') }
		                                </td>
		                            </tr>	         
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                	<label for="manager"><warehouse:message code="warehouse.manager.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'manager', 'errors')}">
											${locationInstance?.manager?:warehouse.message(code:'default.none.label')}
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
			                                <label for="active"><warehouse:message code="warehouse.active.label" /></label> 
		                                </td>
		                                <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
											${locationInstance?.active}
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
			                                <label for="local"><warehouse:message code="warehouse.local.label" /></label>
		                                </td>
		                                <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'local', 'errors')}">
											${locationInstance?.local}
		                                </td>
		                            </tr>
		                            
		                            <tr class="prop">
		                                <td valign="top" class="name">
											<label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
		                                </td>
		                                <td valign="top" class="value">
		                                
		                                	<g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
	                                		<g:set var="locationActivityList" value="${locationInstance?.supportedActivities?:locationInstance?.locationType?.supportedActivities}"/>
		                                	
		                                	<table>
			                                	<g:each var="activity" in="${activityList }" status="status">
													<tr class="${status%2?'even':'odd' }">
														<td>
															<g:if test="${locationInstance?.supports(activity) }">
																<img class="middle" src="${resource(dir:'images/icons/silk',file:'tick.png')}" alt="${warehouse.message(code: 'default.yes.label') }" title="${warehouse.message(code: 'default.yes.label') }"/>
							                            	</g:if>
							                            	<g:else>
																<img class="middle" src="${resource(dir:'images/icons/silk',file:'cross.png')}" alt="${warehouse.message(code: 'default.no.label') }" title="${warehouse.message(code: 'default.no.label') }"/>
							                            	</g:else>
							                            	&nbsp;
															${format.metadata(obj:activity)}
														</td>
													</tr>
												</g:each>
											</table>
		                                	
		                                </td>
		                            </tr>
									<tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="bgColor"><warehouse:message code="warehouse.logo.label"/></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'logo', 'errors')}">
		                              		      
											<g:if test="${locationInstance?.logo }">
												<img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:locationInstance.id)}" style="vertical-align: bottom" />		            				
											</g:if>				                                   
		                                   	
		                                </td>
		                            </tr>		                            
		                            
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="bgColor"><warehouse:message code="warehouse.bgColor.label"/></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'bgColor', 'errors')}">
		                                
		                                	<table>
		                                		<tr>
		                                			<td>
					                                    <span class="fade">${locationInstance?.bgColor }</span>
		                                			</td>
		                                				                                			
		                                		</tr>
		                                	</table>
			                                   
		                                   	
		                                </td>
		                            </tr>
		                            
		                            
					                            
		                            
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="fgColor"><warehouse:message code="warehouse.fgColor.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'fgColor', 'errors')}">
											<table>
		                                		<tr>
		                                			<td>
					                                    <span class="fade">${locationInstance?.fgColor }</span>
		                                			</td>
		                                			
		                                		</tr>
		                                	</table>		                                    
		                                   	
		                                </td>
		                            </tr>
		                            
		                            <%-- 
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="parentLocation"><warehouse:message code="warehouse.parentLocation.label" default="Parent Location" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'parentLocation', 'errors')}">
											<g:select name="parentLocation.id" from="${org.pih.warehouse.core.Location.list()}" 
												optionKey="id" optionValue="name" value="" noSelection="['null': '']" />							
		                                </td>
		                            </tr>
		                            --%>
		                            <%-- 
		                            <tr class="prop">
		                            
		                            	<td valign="top" class="name">
		                            	
		                            	</td>
		                            	<td class="value">
											<div class="buttons left">
							                   <button type="submit">								
													<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
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
                        </div>
                    </g:form>
                </div>
            </div>
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
