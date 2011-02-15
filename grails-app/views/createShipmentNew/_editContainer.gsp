<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditContainer-${containerInstance?.id}").click(function() { $("#dlgEditContainer-${containerInstance?.id}").dialog('open'); });									
		$("#dlgEditContainer-${containerInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });	
		
		$("#btnAddContainer-${type}").click(function() { $("#dlgAddContainer-${type}").dialog('open'); });									
		$("#dlgAddContainer-${type}").dialog({ autoOpen: false, modal: true, width: '600px' });	
					
	});
</script>	   
<g:if test="${containerInstance}">
	<div id="dlgEditContainer-${containerInstance?.id}" title="Edit ${containerInstance?.containerType?.name}" style="padding: 10px; display: none;" >
</g:if>
<g:else>
	<div id="dlgAddContainer-${type}" title="Add ${type}" style="padding: 10px; display: none;" >
</g:else>
	<g:form action="createShipment">
		<g:if test="${containerInstance}">
			<g:hiddenField name="id" value="${containerInstance?.id }"/>
		</g:if>
		<g:hiddenField name="type" value="${type}"/>
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
						<g:textField id="width" name="width" size="15" value="${containerInstance?.width}"/> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label>Length</label></td>                            
					<td valign="top" class="value">
						<g:textField id="length" name="length" size="15" value="${containerInstance?.length}"/> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label>Weight</label></td>                            
					<td valign="top" class="value">
						<g:textField id="weight" name="weight" size="15" value="${containerInstance?.weight}"/> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveContainer" value="Save ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
							<g:if test="${containerInstance}">
								<g:submitButton name="deleteContainer" value="Delete ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
							</g:if>
							<g:submitButton name="cancelContainer" value="Cancel"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addBox" value="Add a Box to this ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addItem" value="Add an Item to this ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

