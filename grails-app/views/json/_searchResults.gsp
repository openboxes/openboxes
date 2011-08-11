
<table>
	<g:each var="product" in="${searchResults}">
		<tr>
			<td>${product?.name }
		</tr>	
	</g:each>
</table>