<g:form controller="product" action="save">
	<table>
		<tbody>
		
			<tr class="prop">
				<td valign="top" class="name"><label for="category.id"><warehouse:message
					code="product.category.label" default="Category" /></label></td>
				<td valign="top"
					class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
					<g:render template="../category/breadcrumb" model="[categoryInstance:categoryInstance]"/>
					<g:hiddenField name="category.id" value="${categoryInstance?.id }"/>
					<g:hiddenField name="categoryId" value="${categoryInstance?.id }"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="name"><warehouse:message
					code="product.name.label" default="Product Description" /></label></td>
				<td valign="top"
					class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
				<g:textField name="name" value="${productInstance?.name}" size="40" />
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"></td>
				<td>
					<div class="buttons">
						<button type="submit" class="positive"><img
							src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
							alt="Save" /> ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
						</button>
					</div>
				</td>
			</tr>
		</tbody>
	</table>
</g:form>
