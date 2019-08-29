<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="admin.upgrade.title" /></title>
        <style>
        	#progressbar { width: 400px; }
        </style>
    </head>
    <body>        
    
        <div class="body">      
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${command}">
	            <div class="errors">
	                <g:renderErrors bean="${command}" as="list" />
	            </div>
            </g:hasErrors>
			<div class="dialog">
				<g:form>
					<fieldset>
						<table>
							<tr class="">
								<td colspan="2">
									<h1><warehouse:message code="upgrade.step1.label"/></h1>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">					
									<label>
										<warehouse:message code="upgrade.remoteWebArchiveUrl.label"/>
									</label>
								</td>
								<td class="value">						
									<g:textField name="remoteWebArchiveUrl" value="${command?.remoteWebArchiveUrl }" size="80"/>
									<button type="submit" class="positive" name="_action_download">	
										<img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" class="middle"/>&nbsp;						
										<g:message code="upgrade.download.label"/> &nbsp;
									</button>												
									<br/>
									<span class="fade">(e.g. http://ci.pih-emr.org/downloads/openboxes.war)</span>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">					
									<label>
										<warehouse:message code="upgrade.progress.label"/>
									</label>
								</td>
								<td class="value">
									<div>
										<label>Downloading file:</label> <b>${command?.remoteWebArchiveUrl }</b> 
									</div>
									<div>
										<label>Last updated:</label> <b>${command?.remoteFileLastModifiedDate }</b>
									</div> 
									<div>
										<label>Remote file size:</label> <b>${command?.remoteFileSize }</b>
									</div> 
									<div>
										<label>Local file size:</label> <b>${command?.localWebArchive?.size() }</b>
									</div> 
									<script>
										$(function() {
											$( "#progressbar" ).progressbar({
												value: ${command?.progressPercentage}
											});
										});
									</script>
									<div>
										<div id="progressbar"></div>
									</div>
									<div>	
										${command?.progressPercentage}% complete
										
										<g:if test="${session?.command?.future?.isCancelled() }">
											Download was canceled.
										</g:if>
										<g:elseif test="${session?.command?.future?.isDone() }">
											Download has been completed!!!				
										</g:elseif>
									</div>									
								</td>
							</tr>
							<tr class="">
								<td colspan="2">
									<h1><warehouse:message code="upgrade.step2.label"/></h1>
								</td>
							</tr>						
							<tr class="prop">
								<td class="name">					
									<label>
										<warehouse:message code="upgrade.localWebArchive.label"/>
									</label>
								</td>
								<td class="value">
									${command?.localWebArchive?.absolutePath }
									- <b>${session?.command?.future?.done ? "Ready" : "Not ready" }</b>
								</td>
							</tr>
							
							<tr class="prop">
								<td class="name">					
									<label>
										<warehouse:message code="upgrade.localWebArchivePath.label"/>
									</label>
								</td>
								<td class="value">						
									<g:textField name="localWebArchivePath" value="${command?.localWebArchivePath }" size="80"/>
									
									<g:if test="${command?.localWebArchive?.absolutePath }">
										<g:if test="${session?.command?.future?.done}">
											<button type="submit" class="positive" name="_action_deploy">	
												<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_start.png')}" class="middle"/>						
												<g:message code="upgrade.deploy.label"/> &nbsp;
											</button>							
										</g:if>			
										<g:else>
											<button type="submit" class="" name="_action_deploy" disabled="disabled">	
												<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_stop.png')}" class="middle"/>						
												<g:message code="upgrade.deploy.label"/> &nbsp;
											</button>											
											(Please wait for download to complete)
										</g:else>		
									</g:if>									
									<br/>
									<span class="fade">(e.g. file:///var/lib/tomcat6/webapps/openboxes.war)</span>
								</td>
							</tr>
							<tr>
								<td></td>
								<td class="middle left">
								</td>
							</tr>
	
						</table>
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>