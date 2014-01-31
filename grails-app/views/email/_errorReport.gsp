<pre>
Stacktrace:
{code}
${stacktrace}
{code}

Clickstream:
{code}
${clickstream}
{code}

Properties:
* Session ID: ${session?.id}
* Clickstream: ${clickstreamUrl}
* User: ${session?.user?.name} ${session?.user?.email} (${session?.user?.username})
* Location: ${session?.warehouse?.name} (${session?.warehouse?.id})
<g:each var="entry" in="${params }">
* ${entry.key }: ${entry.value ?: warehouse.message(code: 'default.none.label') }
</g:each>
</pre>
