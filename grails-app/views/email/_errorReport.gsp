<%@ page contentType="text/html"%>
<g:applyLayout name="email">	
<pre>
{code}
${params.remove("stacktrace") }
{code}
</pre>

<pre><g:each var="entry" in="${params }">
* ${entry.key }: ${entry.value ?: warehouse.message(code: 'default.none.label') }</g:each></pre>	
</g:applyLayout>
