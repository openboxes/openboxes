<div id="${dialogId }" class="dialog hidden" title="${packageInstance?.id?warehouse.message(code:'package.edit.label'):warehouse.message(code:'package.add.label') }">
	<g:form controller="product" action="savePackage" method="post">
		<g:hiddenField name="product.id" value="${productInstance?.id}"/>							
		<g:hiddenField name="id" value="${packageInstance?.id}"/>							
		<table>
			<tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name">${warehouse.message(code:'default.name.label')}</label>
                    </td>
                    <td valign="top" class="value ">
                        <g:textField name="name" size="60" class="medium text" value="${packageInstance?.name }" />
                    </td>
                </tr>
			    <tr class="prop">
                    <td class="name">
                        <label>
                            <warehouse:message code="package.type.label"/>
                        </label>
                    </td>
                    <td class="value middle">
                        <div class="middle">
                            <span style="width: 100px">
                                <g:select name="uom.id" class="chzn-select-deselect"
                                          from="${org.pih.warehouse.core.UnitOfMeasure.list() }"
                                          value="${packageInstance?.uom?.id }"
                                          optionKey="id" optionValue="name"
                                          noSelection="['null':'']"></g:select>
                            </span>
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
                        <div class="middle">
                            <g:textField name="quantity" value="${packageInstance?.quantity }" size="10" class="medium text"/>
                            ${productInstance?.unitOfMeasure?:warehouse.message(code: 'default.each.label') }
                        </div>
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
                            <g:textField name="price" value="${packageInstance?.price }" size="10" class="medium text"/>
							${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
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
			
			<a href="#" class="close-dialog" dialog-id="package-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
		</div>
	</g:form>
</div>				