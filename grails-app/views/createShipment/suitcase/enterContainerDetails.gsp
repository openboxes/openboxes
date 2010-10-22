                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Container Details</title>         
    </head>
    <body>
        <div class="body">
           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>
			<g:hasErrors bean="${containerInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${containerInstance}" as="list" />
	            </div>				
			</g:hasErrors>          
			 
			<g:form action="suitcase" method="post" >
               <div class="dialog">
	                <table>
	                    <tbody>
							<tr class="prop">
								<td valign="top" class="name"><label>Shipment</label></td>
								<td valign="top" class="value">${shipmentInstance?.name}</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label>Suitcases / Boxes</label>
								</td>
								<td valign="top" class="value">
									<table style="width: 0%; border: 1px solid black;">

					                	<g:set var="count" value="${0 }"/>	
										<g:each var="suitcaseInstance" in="${shipmentInstance?.containers}">			
											<tr class="${count++%2==0?'odd':'even' }">
												<td>
												
													<img src="${createLinkTo(dir:'images/icons/silk',file:'briefcase.png')}" alt="Add a box" style="vertical-align: middle"/>&nbsp;
													<b>${suitcaseInstance?.containerType?.name } ${suitcaseInstance?.name }</b>
												</td>

											</tr>
											<g:each in="${suitcaseInstance?.containers}" var="boxInstance">
												<tr class="${count++%2==0?'odd':'even' }">
													<td>
														
														<span style="padding-left: 32px;"> 
															<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Package" style="vertical-align: middle"/>
															&nbsp;
															${boxInstance?.containerType?.name} ${boxInstance?.name}
															&nbsp; <g:link action="suitcase" event="removeBox" id="${shipmentInstance?.id}" params="['suitcase.id':suitcaseInstance?.id,'box.id':boxInstance?.id]">remove</g:link>													
															
														</span>
													</td>
												</tr>
											</g:each>
											<tr class="${count++%2==0?'odd':'even' }">
												<td>
													<span style="padding-left: 32px;"> 
														<g:link action="suitcase" event="addBox" id="${shipmentInstance?.id}" params="['suitcase.id':suitcaseInstance?.id]">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>&nbsp;
															Add another box
														</g:link>
													</span>
												</td>
											</tr>												
										</g:each>
										<tr class="${count++%2==0?'odd':'even' }">
											<td>
												<g:link action="suitcase" event="addSuitcase" id="${shipmentInstance?.id}">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'briefcase.png')}" alt="Add another suitcase" style="vertical-align: middle"/>&nbsp;
														Add another suitcase</g:link>									
											</td>
										</tr>
									</table>
								</td>
							</tr>	                    
							<tr>
								<td valign="top" class="name"></td>
								<td valign="top" class="value">
					               <div class="buttons">
										<span class="formButton">
											<g:submitButton name="back" value="Back"></g:submitButton>								
											<g:submitButton name="submit" value="Next"></g:submitButton>
										</span>
					               </div>
								</td>		
							</tr>							
							
	                    </tbody>
	               </table>
               </div>
            </g:form>
        </div>
    </body>
</html>
