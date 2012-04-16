<fieldset>
	<g:uploadForm controller="batch" action="importData">
		<table>
			<tbody>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="inventory.uploadAFileToImport.label"/></label>
					</td>
					<td class="value">
						<input name="xlsFile" type="file" />
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="default.type.label"/></label>
					</td>
					<td class="value">
						<div>
							<g:radio name="type" value="inventory" checked="${params?.type=='inventory'}"/> 
							<warehouse:message code="inventory.label"/>				
						</div>
						<div>
							<g:radio name="type" value="product" checked="${params?.type=='product'}"/> 
							<warehouse:message code="product.label"/>
						</div>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label><warehouse:message code="inventory.label"/></label>
					</td>
					<td class="value">
						${session?.warehouse?.name }
						<g:hiddenField name="location.id" value="${session.warehouse.id }"/>
					</td>
				</tr>
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
