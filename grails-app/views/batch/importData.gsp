
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<title>
			<warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/>
		</title>
	</head>
	<body>
		<div class="body">
	
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:if test="${commandInstance?.data}">
				<div class="message">
					<warehouse:message code="inventory.thereAreRowsIn.message" args="[commandInstance.data.size(), commandInstance.filename]" />
				</div>
			</g:if>
			<g:hasErrors bean="${commandInstance}">
				<div class="errors"><g:renderErrors bean="${commandInstance}" as="list" /></div>
			</g:hasErrors>

			<div class="dialog">
				<g:render template="uploadFileForm"/>
			</div>
						
			<g:if test="${commandInstance?.data}">
				<div class="list">
					<fieldset>
						<div style="overflow: auto; height: 300px; ">
							<table >		
								<thead>
									<tr>
										<g:each var="column" in="${commandInstance?.columnMap?.columnMap }">
											<th><warehouse:message code="import.${column.value}.label"/></th>
										</g:each>
									</tr>							
								</thead>
								<tbody>							
									<g:each var="row" in="${commandInstance?.data }" status="status">
										<tr class="${status%2?'even':'odd' }">		
											<g:each var="column" in="${commandInstance?.columnMap?.columnMap }">
												<td>${row[column.value] }</td>
											</g:each>
											<g:each var="prompt" in="${row?.prompts }">
												<td>
													<select name="${prompt.key }">
														<g:each var="value" in="${prompt.value }">
															<option value="${value.id }">${value.name }</option>
														</g:each>
													</select>
												</td>
											</g:each>
										</tr>
									</g:each>
								</tbody>
							</table>
						</div>
					</fieldset>
					<g:if test="${!commandInstance.errors.hasErrors()}">
						<div style="text-align: center; display: inline">
							<g:form controller="batch" action="importData" method="POST"> 
								<button type="submit" name="importNow" value="true"><img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" alt="Import Now" /> 
									&nbsp;${warehouse.message(code: 'default.button.import.label', default: 'Import Now')}</button>
									
								&nbsp;
								<a href="${createLink(controller: "batch", action: "importData")}" class="negative"><warehouse:message code="default.button.clear.label"/></a>
									
							</g:form>			
						</div>
					</g:if>
				</div>
			</g:if>		
			<g:else>
				
			</g:else>
		</div>
	</body>
</html>
