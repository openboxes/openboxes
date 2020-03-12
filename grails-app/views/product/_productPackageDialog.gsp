<g:form controller="product" action="savePackage" method="post">
	<g:hiddenField name="product.id" value="${productInstance?.id}"/>
	<g:hiddenField name="id" value="${packageInstance?.id}"/>
	<table>
		<tbody>
			<g:if test="${productInstance }">
				<tr class="prop">
					<td class="name"><label><warehouse:message code="product.label"/></label></td>
					<td class="value">
						<format:product product="${productInstance}" />
					</td>
				</tr>
			</g:if>
			<tr class="prop">
				<td valign="top" class="name">
					<label for="name">${warehouse.message(code:'default.name.label')}</label>
				</td>
				<td valign="top" class="value ">
					<g:textField name="name" size="60" class="medium text" value="${packageInstance?.name }" />
					<div class="fade">Descriptive package name i.e. Bottle of 100 or BTL/100</div>
				</td>
			</tr>

			<tr class="prop">
				<td class="name">
					<label>
						<warehouse:message code="package.uom.label"/>
					</label>
				</td>
				<td class="value middle">
					<div class="middle">
						<g:select name="uom.id"
								  from="${org.pih.warehouse.core.UnitOfMeasure.list() }"
								  value="${packageInstance?.uom?.id }"
								  optionKey="id" optionValue="name"
								  class="chzn-select-deselect"
								  noSelection="['null':'']"></g:select>
					</div>
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label>
						<warehouse:message code="package.quantity.label"/>
					</label>
				</td>
				<td class="value middle">
					<g:textField name="quantity" value="${packageInstance?.quantity }" size="10" class="medium text"/>
					${productInstance?.unitOfMeasure?:warehouse.message(code: 'default.each.label') }
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label>
						<warehouse:message code="package.gtin.label"/>
					</label>
				</td>
				<td class="value">
					<g:textField name="gtin" value="${packageInstance?.gtin }" size="60" class="medium text"/>
				</td>
			</tr>


			<tr class="prop">
				<td class="name">
					<label>
						<warehouse:message code="package.price.label"/>
					</label>
				</td>
				<td class="value">
					<div class="middle">
						<g:hasRoleFinance onAccessDenied="${g.message(code:'errors.userNotGrantedPermission.message', args: [session.user.username])}">
							<g:textField name="price" value="${packageInstance?.price }" size="10" class="medium text"/>
							${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
						</g:hasRoleFinance>
					</div>
				</td>
			</tr>
		</tbody>
	</table>
	<hr/>
	<div class="buttons">
		<button type="submit" class="button icon approve">
			${warehouse.message(code: 'default.button.save.label', default: 'Save')}
		</button>
		&nbsp;

		<a href="#" class="btn-close-dialog" data-target="package-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
	</div>
</g:form>
