<div id="${dialogId }" class="dialog hidden" title="${packageInstance?.id?warehouse.message(code:'package.edit.label'):warehouse.message(code:'package.add.label') }">
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
					    <label for="name">Name on package</label>
					</td>
					<td valign="top" class="value ">
						<g:textField name="name" size="60" class="medium text" value="${packageInstance?.name }" />
				    </td>
				</tr>										
				
				<tr class="prop">
					<td class="name">
						<label>
							<warehouse:message code="package.gtin.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="gtin" value="${packageInstance?.gtin }" class="medium text"/>
					</td>
				</tr>		
				<%-- 
				<tr class="prop">
					<td class="name">
						<label>
							<warehouse:message code="package.quantity.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="quantity" value="${packageInstance?.quantity }" size="5" class="medium text"/>
					</td>
				</tr>
				--%>	
				<tr class="prop">
					<td class="name">
						<label>
							<warehouse:message code="package.uom.label"/>
						</label>
					</td>
					<td class="value middle">
						<div class="middle">
							1 
							<g:select name="uom.id" from="${org.pih.warehouse.core.UnitOfMeasure.list() }" 
								value="${packageInstance?.uom?.id }"
								optionValue="name" optionKey="id" noSelection="['null':'']"></g:select>
							= 
							<g:textField name="quantity" value="${packageInstance?.quantity }" size="10" class="medium text"/>
							${productInstance?.unitOfMeasure?:warehouse.message(code: 'default.each.label') }	
						</div>
					</td>
				</tr>		
				<%-- 										
				<tr class="prop">
					<td class="name">
						<label>
							<warehouse:message code="package.name.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="name" value="${packageInstance?.name }" class="medium text"/>
					</td>
				</tr>	
				<tr class="prop">
					<td class="name">
						<label>
							<warehouse:message code="package.description.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="description" value="${packageInstance?.description }" class="medium text"/>
					</td>
				</tr>	
				--%>
				
				
																								
			</tbody>
		</table>
		<div class="buttons">
			<button type="submit" class="button icon approve"> 
				${warehouse.message(code: 'default.button.save.label', default: 'Save')}
			</button>
			&nbsp;
			
			<a href="#" class="close-dialog" dialog-id="package-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
		</div>
	</g:form>
</div>				