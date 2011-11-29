<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="locations.label" /></title>
        <style>
        	.vertical-text { 
				color:#333;
				border:0px solid red;
				writing-mode:tb-rl;
				-webkit-transform:rotate(-60deg);
				-moz-transform:rotate(-60deg);
				-o-transform: rotate(-60deg);
				white-space:nowrap;
				display:block;
				bottom:0;
				width:20px;
				height:20px;
				font-family: ‘Trebuchet MS’, Helvetica, sans-serif;
				font-weight:normal;
				
        	}
        	tr th { border-top: 0;}
        	
        </style>
        
    </head>
    <body>        
        <div class="body">
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
				<span class="linkButton">
           			<a href="${request.contextPath }/location/edit" class="new"><warehouse:message code="location.addLocation.label"/></a>
	           	</span>
           	</div>
              
            <div class="" style="width: 99%">
                <table>
                    <thead>
                        <tr style="height: 100px;">                        
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" class="bottom"/>
                            <th class="left bottom"><warehouse:message code="location.locationType.label" /></th>
                            <th class="left bottom"><warehouse:message code="location.locationGroup.label" /></th>
                            <th class="bottom"><span class="vertical-text"><warehouse:message code="warehouse.active.label" /></span></th>
                           	<g:each var="activity" in="${org.pih.warehouse.core.ActivityCode.list()}">
                           		<th class="bottom">
                           			<span class="vertical-text"><warehouse:message code="enum.ActivityCode.${activity}"/></span>
                           		</th>
                           	</g:each>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${locationInstanceList}" status="i" var="locationInstance">
						<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:link action="edit" id="${locationInstance.id}">${fieldValue(bean: locationInstance, field: "name")}</g:link>
							</td>
                            <td class="left"><format:metadata obj="${locationInstance?.locationType}"/></td>                            
                            <td class="left">${locationInstance?.locationGroup}</td>                            
                            <td class="left middle">
                            	<g:if test="${locationInstance.active }">
									<img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="${warehouse.message(code: 'default.yes.label') }" title="${warehouse.message(code: 'default.yes.label') }"/>               	
                            	</g:if>
                            	<g:else>
									<img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="${warehouse.message(code: 'default.no.label') }" title="${warehouse.message(code: 'default.no.label') }"/>               	
                            	</g:else>

                            </td>                            
                            <g:each var="activity" in="${org.pih.warehouse.core.ActivityCode.list()}">
                           		<td class="left middle">
									<g:if test="${locationInstance?.supports(activity) }">
										<img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="${warehouse.message(code: 'default.yes.label') }" title="${warehouse.message(code: 'default.yes.label') }"/>               	
	                            	</g:if>
	                            	<g:else>
										<img class="middle" src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="${warehouse.message(code: 'default.no.label') }" title="${warehouse.message(code: 'default.no.label') }"/>               	
	                            	</g:else>
                           			
                           		</td>
                           	</g:each>
						</tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <g:if test="${locationInstanceTotal >= params.max }">            
	            <div class="paginateButtons">
	                <g:paginate total="${locationInstanceTotal}" />
	            </div>
	        </g:if>
        </div>
    </body>
</html>
