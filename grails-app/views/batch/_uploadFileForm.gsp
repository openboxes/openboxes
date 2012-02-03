<fieldset>
	<g:uploadForm controller="batch" action="importData">
		<table>
			<tbody>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="inventory.label"/></label>
					</td>
					<td class="value">
						${session?.warehouse?.name }
						<input name="location.id" type="hidden" value="${session.warehouse.id }"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="default.type.label"/></label>
					</td>
					<td class="value">
						<g:radio name="type" value="inventory" checked="${params?.type=='inventory'}"/> <label>Inventory</label>						
						<g:radio name="type" value="product" checked="${params?.type=='product'}"/> <label>Product</label>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="inventory.uploadAFileToImport.label"/></label>
					</td>
					<td class="value">
						<input name="xlsFile" type="file" />
					</td>
				<tr class="prop">
					<td class="name"></td>
					<td class="value">						
						<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="upload" /> 
							${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}</button>
					</td>
				</tr>
			</tbody>						
		</table>
	</g:uploadForm>
</fieldset>	
