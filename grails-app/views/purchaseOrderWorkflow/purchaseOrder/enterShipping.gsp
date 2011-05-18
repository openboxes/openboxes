
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter Shipping Details</title>
</head>
<body>
	<div class="body">
		<h1>Enter Shipping Details</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${address}">
			<div class="errors">
				<g:renderErrors bean="${address}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="purchaseOrder" method="post">
			<div class="dialog">
				<table>
					<tbody>
						<tr class='prop'>
							<td valign='top' class='name'><label for='address'>Address:</label>
							</td>
							<td valign='top'
								class='value ${hasErrors(bean:address,field:'address','errors')}'><input
								type="text" name='address'
								value="${address?.address?.encodeAsHTML()}" />
							</td>
						</tr>
						<tr class='prop'>
							<td valign='top' class='name'><label for='address'>City:</label>
							</td>
							<td valign='top'
								class='value ${hasErrors(bean:address,field:'city','errors')}'><input
								type="text" name='city'
								value="${address?.city?.encodeAsHTML()}" />
							</td>
						</tr>
						<tr class='prop'>
							<td valign='top' class='name'><label for='country'>Country:</label>
							</td>
							<td valign='top'
								class='value ${hasErrors(bean:address,field:'country','errors')}'><input
								type="text" name='country'
								value="${address?.country?.encodeAsHTML()}" />
							</td>
						</tr>
						<tr class='prop'>
							<td valign='top' class='name'><label for='postalCode'>Postal Code:</label>
							</td>
							<td valign='top'
								class='value ${hasErrors(bean:address,field:'postalCode','errors')}'><input
								type="text" name='postalCode'
								value="${address?.postalCode?.encodeAsHTML()}" />
							</td>
						</tr>

					</tbody>
				</table>
			</div>
			<div class="buttons">
				<span class="formButton"> <g:submitButton name="back"
						value="Back"></g:submitButton> <g:submitButton name="submit"
						value="Next"></g:submitButton> </span>
			</div>
		</g:form>
	</div>
</body>
</html>