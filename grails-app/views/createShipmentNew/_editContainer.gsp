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
			<g:hiddenField name="container.id" value="${containerInstance?.id }"/>
		</g:if>
		<g:else>
			<g:hiddenField name="type" value="${type}"/>
		</g:else>
		<table>
			<tbody>
				<g:render template="containerFields" model="['containerInstance':containerInstance]"/>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveContainer" value="Save ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
							<g:if test="${containerInstance}">
								<g:submitButton name="deleteContainer" value="Remove ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
							</g:if>
							<g:submitButton name="cancelDialog" value="Cancel"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addBoxToContainer" value="Add a Box to this ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addItemToContainer" value="Add an Item to this ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

