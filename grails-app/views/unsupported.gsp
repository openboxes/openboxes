<html>
  <head>
	  <title>Unsupported Operation Error</title>
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
				font-family: ui-monospace, monospace, sans-serif;
	  		}
	  </style>
  </head>

  <body>

    <h2>Unsupported Operation</h2>

  	<div class="message" role="status" aria-label="message">
		<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'}<br/>
		<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong>Exception Message:</strong> ${exception.message} <br />
	  		<strong>Caused by:</strong> ${exception.cause?.message} <br />
	  		<strong>Class:</strong> ${exception.className} <br />
	  		<strong>At Line:</strong> [${exception.lineNumber}] <br />
	  		<strong>Code Snippet:</strong><br />
	  		<div class="snippet">
	  			<g:each var="cs" in="${exception.codeSnippet}">
	  				${cs}<br />
	  			</g:each>
	  		</div>
		</g:if>
  	</div>
	<g:if test="${exception}">
	    <h2>Stack Trace</h2>
	    <div class="stack">
	      <pre><g:each in="${exception.stackTraceLines}">${it}<br/></g:each></pre>
	    </div>
	</g:if>
  </body>
</html>
