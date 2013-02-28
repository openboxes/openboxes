<%@ page
	import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
</head>
<body>

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${requisition}">
		<div class="errors">
			<g:renderErrors bean="${requisition}" as="list" />
		</div>
	</g:hasErrors>	

	

	<g:render template="summary" model="[requisition:requisition]"/>
	
	
	<div class="yui-ga">
		<div class="yui-u first">
		                	
	
			<g:form name="requisitionForm" method="post" action="saveHeader">
			
				<g:hiddenField name="id" value="${requisition?.id }"/>
			
				<div id="requisition-header-details" class="dialog ui-validation expandable">
					<div class="yui-g">
						<div class="yui-u first">
						
							<table id="requisition-header-details-table" class="header-summary-table">
								
								<tbody>	
								
									<g:isUserAdmin>
										<tr class="prop">
											<td class="name"><label><warehouse:message
														code="requisition.isTemplate.label" /></label>
											</td>
											<td class="value">
												<g:checkBox name="isTemplate" value="${requisition?.isTemplate }"/>											
											</td>
										</tr>
										<tr class="prop">
											<td class="name"><label><warehouse:message
														code="requisition.isPublished.label" /></label>
											</td>
											<td class="value">
												<g:checkBox name="isPublished" value="${requisition?.isPublished }"/>			
											</td>
										</tr>
									</g:isUserAdmin>
											
											
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.type.label" /></label></td>
										<td class="value">
											
											<g:select name="type" value="${requisition?.type }" noSelection="['null':'']"
												from="${org.pih.warehouse.requisition.RequisitionType.list() }"/>
											
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.commodityClass.label" /></label></td>
										<td class="value">
											<g:select name="type" value="${requisition?.commodityClass }" noSelection="['null':'']"
												from="${org.pih.warehouse.requisition.CommodityClass.list() }"/>
										</td>
									</tr>
									<tr class="prop">
										<td class="name">
											<label for="origin.id"> <g:if
													test="${requisition.isWardRequisition()}">
													<warehouse:message code="requisition.requestingWard.label" />
												</g:if> <g:else>
													<warehouse:message code="requisition.requestingDepot.label" />
												</g:else>
											</label>
										</td>
										<td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
											
											<g:select name="origin.id"
												from="${locations}" id="depot"
												
												optionKey="id" optionValue="name" class='required' value="${requisition?.origin?.id }"
												noSelection="['null':'']" />
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.requestedBy.label" /></label></td>
										<td class="value">
											<g:autoSuggest name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
												width="200" valueId="${requisition?.requestedBy?.id }" valueName="${requisition?.requestedBy?.name }" styleClass="text"/>
										</td>
									</tr>
									<tr class="prop">
										<td class="name">
											<label><warehouse:message
													code="requisition.dateRequested.label" /></label></td>
										<td class="value">
											<g:jqueryDatePicker id="dateRequested" name="dateRequested" 
												value="${requisition?.dateRequested}" format="MM/dd/yyyy"/>
										</td>
									</tr>
																
									
								</tbody>
							</table>
												
						
						</div>
					
						<div class="yui-u">
						
						
							<table class="">
								<tbody>
									<tr class="prop">
										<td class="name">
											<label for="destination.id"> 
												<warehouse:message code="requisition.destination.label" />
											</label>
										</td>
										<td class="value">	
											${session?.warehouse?.name }
										</td>
									</tr>
									<tr class="prop">
										<td class="name">
											<label><warehouse:message
													code="requisition.processedBy.label" /></label>
										</td>
										<td class="value">
											${requisition?.createdBy?.name }
										</td>
										
										
									</tr>
									<g:if test="${requisition.isDepotRequisition()}">
										<tr>
											<td class="name"><label><warehouse:message
														code="requisition.program.label" /></label></td>
											<td class="value">
												<input id="recipientProgram"
													name="recipientProgram" class="autocomplete text" size="60"
													placeholder="${warehouse.message(code:'requisition.program.label')}"
													data-bind="autocomplete: {source: '${request.contextPath }/json/findPrograms'}, value: requisition.recipientProgram" />
											</td>
										</tr>
									</g:if>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label" /></label></td>
										<td class="value">
										
											<g:jqueryDatePicker id="requestedDeliveryDate" name="requestedDeliveryDate" 
													value="${requisition?.requestedDeliveryDate}" format="MM/dd/yyyy"/>											
										</td>
									</tr>		
									<tr class="prop">							
										<td class="name">
											<label for="description"> 
												<warehouse:message code="default.description.label" />
											</label>
										</td>
									
										<td class="value">	
										
											<g:textArea name="description" cols="80" rows="5"
												placeholder="${warehouse.message(code:'requisition.description.message')}"
												class="text">${requisition.description }</g:textArea>
										</td>
									</tr>	
															
								</tbody>
							</table>							
						</div>
					</div>
					
				</div>				
					
				<div class="buttons">
			
					<button class="button" name="saveAndContinue">${warehouse.message(code:'default.button.saveAndContinue.label', default: 'Save & Continue') }</button>									
					
					&nbsp;
					<g:link controller="requisition" action="edit" id="${requisition?.id }">
						<warehouse:message code="default.button.cancel.label"/>
					</g:link>
					

				</div>					
			</g:form>
		</div>
	</div>
</body>
</html>
