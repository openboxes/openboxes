<style>
span.name {   } 
span.value { font-size: 1.0em; } 
span.name:after { content: ": " }
th { color: lightgrey; } 
fieldset table td { padding: 6px; } 
</style>
	
<div id="productDetails">
	<fieldset>
		<legend class="fade">Product Details</legend>
		<table>
			<tr class="odd">	
				<td style="text-align: left;">
					<span class="name">Description</span>
				</td>
				<td>
					<span class="value">${productInstance?.name }</span>
				</td>
			</tr>
			<tr class="even">	
				<td style="text-align: left;">
					<span class="name">Category</span>
				</td>
				<td>
					<%-- 
					<g:if test="${productInstance?.category?.parentCategory }">
						<span class="value">${productInstance?.category?.parentCategory?.name } &rsaquo;</span>
					</g:if>
					--%>
					<span class="value">${productInstance?.category?.name }
				</td>
			</tr>
			<tr class="odd">	
				<td style="text-align: left;">
					<span class="name">Product Code</span>
				</td>
				<td>
					<span class="value">${productInstance?.productCode?:'<span class="fade">none</span>' }</span>
				</td>
			</tr>
			
			<tr class="even">	
				<td style="text-align: left;">
					<span class="name">Cold Chain</span>
				</td>
				<td>
					<span class="value">${productInstance?.coldChain?'Yes':'No' }</span>
				</td>
			</tr>
			<g:each var="productAttribute" in="${productInstance?.attributes}" status="status">
				<tr class="${status%2==0?'odd':'even' }">
					<td style="text-align: left;">
						<span class="name">${productAttribute?.attribute.name }</span>
					</td>
					<td>
						<span class="value">${productAttribute.value }</span>
					</td>
				</tr>													
			</g:each>
			<tr class="odd">
				<td style="text-align: left;">
					<span class="name">Min Qty</span>
				</td>
				<td>
					<span class="value">${inventoryLevelInstance?.minQuantity?:'<span class="fade">Not Configured</span>' }</span>
				</td>
			</tr>
			<tr class="even">
				<td style="text-align: left;">
					<span class="name">Reorder Qty</span>
				</td>
				<td>
					<span class="value">${inventoryLevelInstance?.reorderQuantity?:'<span class="fade">Not Configured</span>' }</span>
				</td>
			</tr>										
			<tr class="odd">
				<td></td>
				<td>
					<div style="text-align: right;">			
						<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
						<a href="#" id="configureWarningLevelsLink">Configure</a>
					</div>													
				</td>
			</tr>
		</table>
	</fieldset>
</div>



<%-- 								
											<script>
												$(document).ready(function() {
													$("#showWarningLevels").show();
													$("#configureWarningLevels").hide();
													$("#configureWarningLevelsLink").click(function() { 
														$("#showWarningLevels").hide(); 
														$("#configureWarningLevels").show(); 
													});
													$("#showWarningLevelsLink").click(function() { 
														$("#showWarningLevels").show(); 
														$("#configureWarningLevels").hide(); 
													});
			
												});
											</script>								

											<div id="showWarningLevels">
												<table>
													<tr class="prop">
														<td style="text-align: right"><label>Minimum Quantity </label></td>
														<td class="value">
															${inventoryLevelInstance?.minQuantity?:'Not Configured' }
														</td>
													</tr>
													<tr class="prop">
														<td style="text-align: right"><label>Reorder Quantity</label></td>
														<td class="value">
															${inventoryLevelInstance?.reorderQuantity?:'Not Configured' }
														</td>
													</tr>
												</table>	
												<div style="text-align: right">			
													<a href="#" id="configureWarningLevelsLink">
													<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
													configure</a>
												</div>
											</div>
										
											<div id="configureWarningLevels">
												<g:form>
													<g:hiddenField name="id" value="${inventoryLevelInstance?.id}"/>
													<g:hiddenField name="product.id" value="${productInstance?.id}"/>
													<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
														<table>
															<tr class="prop">
																<td class="name"><label>Minimum Quantity </label></td>
																<td class="value">
																	<g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="3"/>
																</td>
															</tr>
															<tr class="prop">
																<td class="name"><label>Reorder Quantity</label></td>
																<td class="value">
																	<g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="3"/>
																</td>
															</tr>
														</table>
														<div class="buttonBar" style="text-align: center;">
										                    <g:actionSubmit class="save" action="saveInventoryLevel" value="${message(code: 'default.button.save.label', default: 'Save')}" />
										                    &nbsp;
															<a href="#" id="showWarningLevelsLink">
																<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/> Cancel</a>
									                    </div>
												</g:form>								
											</div>		
											--%>		