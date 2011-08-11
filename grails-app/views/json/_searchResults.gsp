
<table>
	<g:each var="product" in="${searchResults}">
		<tr>
			<td><format:product product="${product}" /></td>
		</tr>	
	</g:each>
</table>