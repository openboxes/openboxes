<fieldset>
	<g:uploadForm controller="inventoryItem" action="importInventoryItems">
		<table>
			<tbody>
				<tr class="prop">
					<td class="name"><label>Upload a file to import</label></td>
					<td class="value" style="background-color: #f7f7f7;">
						<input name="xlsFile" type="file" />
				
						<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="upload" /> 
							${message(code: 'default.button.upload.label', default: 'Upload')}</button>
							&nbsp;
							<a href="${createLink(controller: "inventoryItem", action: "importInventoryItems")}" class="negative"> Clear </a>															
					</td>
				</tr>
			</tbody>						
		</table>
	</g:uploadForm>
</fieldset>	
