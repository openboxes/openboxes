<html>
<head>
    <title>OpenBoxes Bug Report</title>
</head>
<body>

<h1>OpenBoxes Bug Report</h1>
<h2>Stacktrace</h2>
<pre>
    ${stacktrace}
</pre>

<h3>Clickstream</h3>
<pre>${clickstream}</pre>

<h3>Properties</h3>
<dl>
    <dt>Session</dt>
    <dd>${session?.id}</dd>

    <dt>Clickstream</dt>
    <dd>${clickstreamUrl}</dd>

    <dt>User</dt>
    <dd>${session?.user?.name} ${session?.user?.email} (${session?.user?.username})</dd>

    <dt>Location</dt>
    <dd>${session?.warehouse?.name} (${session?.warehouse?.id})</dd>

    <g:each var="entry" in="${params }">
        <dt>${entry.key }</dt>
        <dd>${entry.value ?: warehouse.message(code: 'default.none.label') }</dd>
    </g:each>
</dl>
</body>
</html>
