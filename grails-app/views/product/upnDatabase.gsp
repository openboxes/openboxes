<style>
	table, tr, td { font-size: 9px; }

</style>

${rows.size() }
<table border="1">
	<tr>
		<th>upn</th>
		<th>supplier</th>
		<th>division</th>
		<th>tradeName</th>
		<th>description</th>
		<th>uom</th>
		<th>qty</th>
		<th>partno</th>
		<th>saleable</th>
		<th>upnQualifier</th>
		<th>srcCode</th>
		<th>trackingRequired</th>
		<th>upnCreateDate</th>
		<th>upnEdithate</th>
		<th>statusCode</th>
		<th>actionCode</th>
		<th>reference</th>
		<th>referenceQualifier</th>
	</tr>

	<g:each var="row" in="${rows }">
		<tr>
			<td>${row.upn }</td>
			<td>${row.supplier }</td>
			<td>${row.division }</td>
			<td>${row.tradeName }</td>
			<td>${row.description }</td>
			<td>${row.uom }</td>
			<td>${row.qty }</td>
			<td>${row.partno }</td>
			<td>${row.saleable }</td>
			<td>${row.upnQualifier }</td>
			<td>${row.srcCode }</td>
			<td>${row.trackingRequired }</td>
			<td>${row.upnCreateDate }</td>
			<td>${row.upnEditDate }</td>
			<td>${row.statusCode }</td>
			<td>${row.actionCode }</td>
			<td>${row.reference }</td>
			<td>${row.referenceQualifier }</td>
		</tr>
	</g:each>

</table>