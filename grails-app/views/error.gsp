<html>
  <head>
	  <title>General Error</title>
	  <meta name="layout" content="custom" />	  
	  <style type="text/css">
	  		.message {
	  			border: 1px solid black;
	  			padding: 5px;
	  			background-color:#E9E9E9;
	  		}
	  		.stack {
	  			border: 1px solid black;
	  			padding: 5px;
	  			overflow:auto;
	  			height: 300px;
	  		}
	  		.snippet {
	  			padding: 5px;
	  			background-color:white;
	  			border:1px solid black;
	  			margin:3px;
	  			font-family:courier;
	  		}
	  		.dialog { 
	  			display: none;
	  		}
	  </style>
  </head>

  <body>
  
  	<div class="" style="padding: 10px;">
		<button class="open-dialog">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bug.png')}" style="vertical-align: middle" />&nbsp;
			<warehouse:message code="default.reportAsBug.label"/>
			&nbsp;
		</button>
		&nbsp;
		<button class="go-back">
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'reload.png')}" style="vertical-align: middle" />&nbsp;
			<warehouse:message code="default.ignoreError.label"/>
			&nbsp;
		</button>	
	</div>  
  
  
    <h2>Error Details</h2>
  	<div class="message">
		<strong>Error ${request?.'javax.servlet.error.status_code'}:</strong> 
		${request?.'javax.servlet.error.message'?.encodeAsHTML()}<br/>
		<strong>Servlet:</strong> ${request?.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request?.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
	  		<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
	  		<strong>Class:</strong> ${exception.className} <br />
	  		<strong>At Line:</strong> [${exception.lineNumber}] <br />
	  		<strong>Code Snippet:</strong><br />
	  		<div class="snippet">
	  			<g:each var="cs" in="${exception.codeSnippet}">
	  				${cs?.encodeAsHTML()}<br />
	  			</g:each>
	  		</div>
		</g:if>
  	</div>
	<g:if test="${exception}">
	    <h2>Stack Trace</h2>
	    <div class="stack">
	      <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
	    </div>
	</g:if>
	<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + (request.queryString?'?':'') + (request.queryString?:'') }"/>
	<div id="error-dialog" class="dialog" title="Error Report">
		<g:form controller="errors" action="processError">
			<g:hiddenField id="dom" name="dom" value=""/>
			<g:hiddenField name="reportedBy" value="${session?.user?.username}"/>
			<g:hiddenField name="targetUri" value="${targetUri}"/>
			<g:hiddenField name="request.statusCode" value="${request?.'javax.servlet.error.status_code'}"/>
			<g:hiddenField name="request.errorMessage" value="${request?.'javax.servlet.error.message'?.encodeAsHTML()}"/>
			<g:hiddenField name="exception.message" value="${exception?.message?.encodeAsHTML()}"/>
			<g:hiddenField name="exception.class" value="${exception?.className}"/>
			<g:hiddenField name="exception.date" value="${new Date() }"/>
			<g:set var="absoluteTargetUri" value="${g.createLinkTo(url: targetUri, absolute: true) }"/>
			<g:hiddenField name="absoluteTargetUri" value="${absoluteTargetUri}"/>
			
			
			<g:set var="summary" value="${exception.className?.replace('Controller','').replace('Service', '')} | ${exception?.message?.encodeAsHTML()} (${exception?.cause?.class?.name})"/>
			
			
			
			<table>
				<%-- 
				<tr class="prop">
					<td class="name">
						<label>To</label>
					</td>
					<td class="value">
						jmiranda@pih.org
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label>Cc</label>
					</td>
					<td class="value">
						<g:textField size="100" name="cc" value="${session?.user?.email }"/>
						<span class="fade">
							<warehouse:message code="default.separateMultipleAddresses.message"/>
						</span>
					</td>
				</tr>
				--%>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="default.reportedBy.label"/></label>
					</td>
					<td class="value">
						${session?.user?.name }
						<span class="fade">${session?.user?.email }</span>
					</td>
				</tr>
				<%-- 
				<tr class="prop">
					<td class="name"> 
						<label><warehouse:message code="error.class.label"/></label>
					</td>
					<td>
						${exception?.className}:${exception.lineNumber }				
					</td>
				</tr>
				--%>		
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="error.summary.label"/></label>
					</td>
					<td class="value">
						<%-- 
						<g:if test="${request?.'javax.servlet.error.message'}">						 
							${request?.'javax.servlet.error.message'?.encodeAsHTML()}
						</g:if>
						<g:else>
							${exception?.message?.encodeAsHTML()}
						</g:else>
						--%>
						<g:textField name="summary" class="text" size="60" 
							value="${summary }"
							placeholder="${warehouse.message(code:'error.summary.message') }"/>
					</td>
				</tr>
				
				
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="error.details.label"/></label>
					</td>
					<td class="value">
						<g:textArea name="comments" cols="60" rows="5" 
							placeholder="${warehouse.message(code:'error.details.message')}"></g:textArea>						
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label>Stacktrace</label>			
					
					</td>
					<td>
						<g:if test="${exception}">	    
							<g:textArea name="stacktrace" cols="120" rows="10" readonly="readonly"><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}</g:each></g:textArea>
						</g:if>
					</td>
				</tr>
				
				
				<tr class="prop">
				
					<td class="name">
					
					</td>
					<td class="value">
						<div>
							<g:checkBox name="ccMe" value="${true }" />&nbsp;
							<warehouse:message code="default.reportCcMe.label" />						
						
						</div>
					</td>
				</tr>
				<tr class="prop">
					<td class="name"></td>			
					<td class="value">
						<button>							
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'email_go.png')}" style="vertical-align: middle" />
							<warehouse:message code="default.button.submit.label"/>
						</button>	
						&nbsp;
						<button class="close-dialog">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'decline.png')}" style="vertical-align: middle" />
							<warehouse:message code="default.button.close.label"/>
						</button>
					</td>
				</tr>
			</table>
		</g:form>
	</div>
	
	<script>

		$(".go-back").click(function() { 
			parent.history.back();
	        return false;
		});
		$(".open-dialog").click(function() {
			var dom = document.getElementsByTagName('html')[0].innerHTML;
			dom = "<html>" + dom + "</html>"
			$("#dom").val(dom);
			
			$("#error-dialog").dialog({ 
				autoOpen: true, 
				modal: true, 
				width: '1000px'
			});
		});
		$(".close-dialog").click(function(event) {
			event.preventDefault(); 
			$("#error-dialog").dialog("close"); 
		});

	</script>
  </body>
</html>