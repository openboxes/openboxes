<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgMoveItem").dialog({ autoOpen: true, modal: true, width: '600px', });				
	});
</script>

<div id="dlgMoveItem" title="Move Item" style="padding: 10px; display: none;" >

	<g:if test="${itemToMove}">
		<g:form name="moveItem" action="createShipment">
			<table>
				<tbody>
					<g:render template="itemToMoveFields" model="['item':itemToMove]"/>
				</tbody>
			</table>
		</g:form>														
	</g:if>
</div>		
		
