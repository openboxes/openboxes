<%@ page defaultCodec="html" %>
<html>
<head>
    <title>OpenBoxes Bug Report</title>
</head>
<body>

<h1>OpenBoxes Bug Report</h1>
<h3>Stacktrace</h3>
<pre>
    ${stacktrace}
</pre>

<h3>Properties</h3>
<table>
    <tr>
        <th>session</th>
        <td>${session?.id}</td>
    </tr>
    <tr>
        <th>user</th>
        <td>${session?.user?.name} ${session?.user?.email} (${session?.user?.username})</td>
    </tr>
    <tr>
        <th>location</th>
        <td>${session?.warehouse?.name} (${session?.warehouse?.id})</td>
    </tr>
    <g:each var="entry" in="${params }">
        <tr>
            <th>${entry.key }</th>
            <td>${entry.value ?: warehouse.message(code: 'default.none.label') }</td>
        </tr>
    </g:each>
</table>
</body>
</html>
