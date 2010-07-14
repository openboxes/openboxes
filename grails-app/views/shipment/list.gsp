
<%@ page import="org.pih.warehouse.shipping.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>		
<%-- 
		<g:javascript library="prototype" />
		<g:javascript>
			function clearShipment(e) { $('shipmentContent').value=''; }	
			function showSpinner(visible) { $('spinner').style.display = visible ? "inline" : "none"; }
		</g:javascript>
--%>

    </head>
    <body>
        <div class="body" width="90%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

			<div class="search" style="height: 100px; border: 1px solid black; background-color: #eee; padding: 10px;">
				<%-- 	
					<div>	
		            	<ul>
		            		<li class="first active"><g:link class="browse" action="list" id="all">Show all shipments (${shipmentInstanceTotal})</g:link></li>
		            		<li><g:link class="browse" action="list" id="incoming">Show all shipments FROM ${session.warehouse.name} (${incomingShipmentCount})</g:link></li>
		            		<li><g:link class="browse" action="list" id="outgoing">Show all shipments TO ${session.warehouse.name} (${outgoingShipmentCount})</g:link></li>
		            	</ul>
		            </div>
				--%>				
				<ul>
					<g:each in="${shipmentListByStatus}" var="shipmentList">      
						<li>
							<g:link class="browse" action="list" id="${shipmentList.key}">${shipmentList.key} Shipments</g:link> 
							<g:if test="${shipmentList.value.objectList}">(${shipmentList.value.objectList.size})</g:if>
						</li>
					</g:each>
				</ul>
			</div>


            <div class="list">
				
				<g:each in="${shipmentListByStatus}" var="shipmentList">      
				
					<div style="padding-top: 20px">      
						<span style="color: ${shipmentList.value.color}; font-size: 1.5em; font-weight: bold;">
							${shipmentList.key} Shipments 
							<g:if test="${shipmentList.value.objectList}">(${shipmentList.value.objectList.size})</g:if>
						</span>
						<table>
		                    <thead>
		                        <tr style="height:20px">                        
		                            <g:sortableColumn property="identifier" title="${message(code: 'shipment.identifier.label', default: 'Identifier')}" />                            
		                            <g:sortableColumn property="shipmentStatus" title="${message(code: 'shipment.status.label', default: 'Status')}" />                            
		                            <g:sortableColumn property="shippingDate" title="${message(code: 'shipment.shippingDate.label', default: 'Date')}" />
		                            <g:sortableColumn property="name" title="${message(code: 'shipment.name.label', default: 'Name')}" />
		                            <g:sortableColumn property="origin" title="${message(code: 'shipment.origin.label', default: 'From')}" />
		                            <g:sortableColumn property="destination" title="${message(code: 'shipment.destination.label', default: 'To')}" />
		                            <th><g:message code="shipment.document.label" default="Documents" /></th>		                            
<%-- 		                            		                            
		                            <g:sortableColumn property="identifier" title="${message(code: 'shipment.identifier.label', default: 'Identifier')}" />                        
		                            <g:sortableColumn property="trackingNumber" title="${message(code: 'shipment.trackingNumber.label', default: 'Tracking Number')}" />                        
--%>	                            
		                        </tr>
		                    </thead>
		                    <tbody>				
								<g:each in="${shipmentList.value.objectList}" var="shipmentInstance" status="i">							
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			                            <td style="vertical-align: top; text-align: center">											
			                            	<g:link action="show" id="${shipmentInstance.id}" alt="show"><span style="color:#aaa">${fieldValue(bean: shipmentInstance, field: "identifier")}</span></g:link>
										</td>
			                            <td style="vertical-align: top; text-align: center;" nowrap="true" width="5%">
		                            		<g:if test="${shipmentInstance?.shipmentStatus}">${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}</g:if>
		                            		<g:else>No status</g:else>
			                            </td>
			                            <td style="vertical-align: top; text-align: center;" nowrap="true" width="5%">
			                            	<g:if test="${shipmentInstance.actualShippingDate}"><g:formatDate date="${shipmentInstance.actualShippingDate}" format="dd MMM yyyy" /><br/></g:if>
			                            	<i><g:formatDate date="${shipmentInstance.expectedShippingDate}" format="dd MMM yyyy" /></i>
			                            </td>
			                            <td style="vertical-align: top; text-align: left">
			                            	${fieldValue(bean: shipmentInstance, field: "name")}
										</td>
			                            <td style="vertical-align: top; text-align: center;" nowrap="true" width="10%">
			                            	<g:if test="${session?.warehouse?.id != shipmentInstance?.origin?.id}">
				                            	<g:set var="cssClass" >font-weight: bold;</g:set>	
			                            	</g:if>
			                            	<g:else>
			                            		<g:set var="cssClass" >color: #ddd;</g:set>	
			                            	</g:else>
			                            	<span style="${cssClass}">
				                            	${fieldValue(bean: shipmentInstance, field: "origin")}
			                            	</span>
			                            </td>
			                            <td style="vertical-align: top; text-align: center;" nowrap="true" width="10%">
			                            	<g:if test="${session?.warehouse?.id != shipmentInstance?.destination?.id}">
				                            	<g:set var="cssClass" >font-weight: bold;</g:set>	
			                            	</g:if>
			                            	<g:else>
			                            		<g:set var="cssClass" >color: #ddd;</g:set>	
			                            	</g:else>
			                            	<span style="${cssClass}">${fieldValue(bean: shipmentInstance, field: "destination")}</span>
			                            </td>
			                            <td style="vertical-align: top; text-align: center;" width="2%">
			                            	<g:if test="${shipmentInstance.documents}">			                            					                            	
			                            		<a href="#" id="preview-documents-link-${shipmentInstance.id}"><img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="" valign="middle"/></a>	
											</g:if>
											<script type="text/javascript">
												jQuery(document).ready(function($){		
													$('#preview-documents-dialog-${shipmentInstance.id}').dialog({ autoOpen: false, width: 600, modal: true });										
													$('#preview-documents-link-${shipmentInstance.id}').click(function(){
														$('#preview-documents-dialog-${shipmentInstance.id}').dialog('open');
														return false;
													});		
												});
											</script>	
											<div id="preview-documents-dialog-${shipmentInstance.id}" title="Preview Documents" style="display:none; text-align: left;">
												<h1>${shipmentInstance.name} ${shipmentInstance.identifier}</h1>												
				                            	
			                            		<div>
			                            			<ul>
						                            	<g:each in="${shipmentInstance.documents}" var="document" status="j">		
															<li>				                            			
							                            		<img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="" style="vertical-align: top"/>
																<g:link controller="document" action="download" id="${document.id}">
																	${document.filename}
																</g:link> -- 
																<span style="color: #aaa;">${document.size} bytes</span>														
															</li>
						                            	</g:each>
					                            	</ul>
												</div>										
											</div>
			                            </td>
<%-- 
			                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>
			                            
--%>		                            



			                        </tr>							
	    						</g:each>
							</tbody>            
			            </table>
					</div>
			            
				</g:each>
				<%-- 
				<h2>All Shipments</h2>				
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${message(code: 'shipment.name.label', default: 'Name')}" />                            
                            <g:sortableColumn property="identifier" title="${message(code: 'shipment.identifier.label', default: 'Identifier')}" />                        
                            <g:sortableColumn property="shipmentStatus" title="${message(code: 'shipment.status.label', default: 'Status')}" />                            
                            <g:sortableColumn property="expectedShippingDate" title="${message(code: 'shipment.expectedShippingDate.label', default: 'Shipping Date')}" />
                            <g:sortableColumn property="trackingNumber" title="${message(code: 'shipment.trackingNumber.label', default: 'Tracking Number')}" />                        
                            <th><g:message code="shipment.origin.label" default="Origin" /></th>                   	    
                            <th><g:message code="shipment.destination.label" default="Destination" /></th>                   	    
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${shipmentInstanceList}" status="i" var="shipmentInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 50px;">
	                            <td style="vertical-align: middle; text-align: center;">
	                            	<g:link action="show" id="${shipmentInstance.id}" alt="show">
	                            		${fieldValue(bean: shipmentInstance, field: "name")}
									</g:link>
								</td>
	                            <td style="vertical-align: middle; text-align: center;">
	                            	${fieldValue(bean: shipmentInstance, field: "identifier")}
	                            </td>
	                            <td style="vertical-align: middle; text-align: center;" nowrap="true">
                            		<g:if test="${shipmentInstance?.shipmentStatus}">${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}</g:if>
                            		<g:else>No status</g:else>
	                            </td>
	                            <td style="vertical-align: middle; text-align: center;" nowrap="true"><g:formatDate date="${shipmentInstance.expectedShippingDate}" format="dd MMM yyyy" /></td>
	                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>
	                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "origin")}</td>
	                            <td style="vertical-align: middle; text-align: center;">${fieldValue(bean: shipmentInstance, field: "destination")}</td>
	                        </tr>
	                    </g:each>
                    </tbody>
                </table>
                --%>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${shipmentInstanceTotal}" />
            </div>				
				
        </div>




    </body>
</html>
