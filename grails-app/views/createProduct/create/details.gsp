<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter Product Details</title>
</head>
<body>
	<div class="body">

		<g:render template="header" model="['currentState':'verify']"/>

		<g:form action="create">
			<div class="dialog box">
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label for="name">Name:</label>
							</td>
							<td valign="top"><input type="text" id="name" name="name"
								class="text medium" value="${product.name}" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="lastName">Description:</label>
							</td>
							<td valign="top"><input type="text" id="description"
								name="description" class="text medium"
								value="${product.description}" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="brandName">Brand
									name:</label></td>
							<td valign="top"><input type="text" id="brandName"
								name="brandName" class="text large" value="" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="manufacturer">Manufacturer:</label>
							</td>
							<td valign="top"><input type="text" id="manufacturer"
								name="manufacturer" class="text large"
								value="${product.manufacturer}" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="manufacturerCode">Manufacturer
									Code:</label></td>
							<td valign="top"><input type="text" id="manufacturerCode"
								name="manufacturerCode" class="text large"
								value="${product.manufacturerCode}" /></td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="buttons center">
				<g:submitButton class="back" name="back" value="Back" />
				<g:submitButton class="next" name="next" value="Next" />
				<g:submitButton class="cancel" name="cancel" value="Cancel" />
			</div>
		</g:form>
	</div>
</body>
</html>