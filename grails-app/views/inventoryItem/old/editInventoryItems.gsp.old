<%--

	NOTE:  This is just a fragment moved from the showStockCard.gsp (it needs to be refactored in order to get it to work) 
	
--%>

<div id="inventoryForm">
	<g:form action="saveInventoryLot" autocomplete="off">
		<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
		<g:hiddenField name="product.id" value="${productInstance?.id}"/>
		<g:hiddenField name="active" value="true"/>
		<g:hiddenField name="createdBy" value="${session?.user?.id }"/>		
		<g:hiddenField name="initialQuantity" value="0"/>							
		<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
			
		<g:hasErrors bean="${inventoryLotInstance}">
            <div class="errors">
                <g:renderErrors bean="${inventoryLotInstance}" as="list" />
            </div>
           </g:hasErrors>									
			
			<table border="1" style="border:1px solid #f5f5f5">
				<thead>
					<tr>
						<th>Lot Number</th>
						<th>Expires</th>
						<th>&nbsp;</th>
					</tr>											
				</thead>
				<tbody>
					<g:each var="itemInstance" in="${inventoryItemList }" status="i">				
						<tr class="${(i%2 == 0)?'odd':'even' }">
							<td>${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }</td>
							<td><format:expirationDate obj="${itemInstance?.expirationDate}"/></td>
							<td style="text-align:center;">
								<g:link controller="inventoryItem" action="deleteInventoryItem" id="${itemInstance?.id }" params="['inventory.id':inventoryInstance?.id]">
									<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
								</g:link>
							</td>
						</tr>
					</g:each>
					
					<tr>
						<td>
							<g:textField name="lotNumber" size="10"/>
						</td>
						<td nowrap>
							<g:jqueryDatePicker id="expirationDate" name="expirationDate" value="" format="MM/dd/yyyy"
								showTrigger="false" />
						</td>
						<td>
						</td>
					</tr>
				</tbody>
			</table>
			<div class="buttonBar" >
				<g:submitButton name="save" value="Save"/>
				&nbsp;
				<a href="#" id="inventoryViewLink">
					<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
			</div>												
			
			
	</g:form>
</div>			