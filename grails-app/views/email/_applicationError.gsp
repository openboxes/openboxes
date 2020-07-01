<%@ page contentType="text/html"%>

<h2>Location</h2>
<div class="location">
	 ${location.name}
</div>
<h2>Error Details</h2>
<div class="message">
	<g:if test="${exception}">
		<strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
		<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
		<strong>Class:</strong> ${exception.className} <br />
	</g:if>
</div>
<div class="stacktrace">
	<h2>Stacktrace</h2>
	<g:if test="${exception}">
		<div class="stack">
<pre>
<g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each>
</pre>
		</div>
	</g:if>
</div>
