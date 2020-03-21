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
		  <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
		</div>
	</g:if>
</div>
<div class="status">
	<h2>Database Status</h2>
	<g:if test="${status}">
		<pre><g:each in="${status}">${it.encodeAsHTML()}</g:each></pre>
	</g:if>
	<g:else>
		None
	</g:else>
</div>
<div class="processes">
	<h2>Process List</h2>
	<g:if test="${processes}">
		<ul>
			<g:each in="${processes}"><li>${it.encodeAsHTML()}</li></g:each>
		</ul>
	</g:if>
	<g:else>
		None
	</g:else>
</div>
<div class="transactions">
	<h2>Transactions</h2>
	<g:if test="${transactions}">
		<ul>
			<g:each in="${transactions}"><li>${it.encodeAsHTML()}</li></g:each>
		</ul>
	</g:if>
	<g:else>
		None
	</g:else>
</div>
<div class="locks">
	<h2>Locks</h2>
	<g:if test="${locks}">
		<ul>
			<g:each in="${locks}"><li>${it.encodeAsHTML()}</li></g:each>
		</ul>
	</g:if>
	<g:else>
		None
	</g:else>
</div>
