<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditContainer-${containerInstance?.id}").click(function() { $("#dlgEditContainer-${containerInstance?.id}").dialog('open'); });									
		$("#dlgEditContainer-${containerInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	});
</script>	   
<div id="dlgEditContainer-${containerInstance?.id}" title="Edit ${containerInstance?.containerType?.name}" style="padding: 10px; display: none;" >
	<g:form action="createShipment">
		<g:hiddenField name="id" value="${containerInstance?.id }"/>
		<table>
			<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label>Name</label></td>                            
					<td valign="top" class="value">
						<g:textField id="name" name="name" size="15" value="${containerInstance?.name}"/> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label>Height</label></td>                            
					<td valign="top" class="value">
						<g:textField id="height" name="height" size="15" value="${containerInstance?.height}"/> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label>Width</label></td>                            
					<td valign="top" class="value">
						<g:textField id="height" name="witdth" size="15" value="${containerInstance?.width}"/> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label>Length</label></td>                            
					<td valign="top" class="value">
						<g:textField id="height" name="length" size="15" value="${containerInstance?.length}"/> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label>Weight</label></td>                            
					<td valign="top" class="value">
						<g:textField id="height" name="weight" size="15" value="${containerInstance?.weight}"/> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="editContainer" value="Save ${containerInstance?.containerType?.name}"></g:submitButton>
							<g:submitButton name="deleteContainer" value="Delete ${containerInstance?.containerType?.name}"></g:submitButton>
							<g:submitButton name="cancelContainer" value="Cancel"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addBox" value="Add a Box to this ${containerInstance?.containerType?.name}"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

