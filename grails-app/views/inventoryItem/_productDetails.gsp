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
				<td></td>
			</tr>
			<tr class="even">	
				<td style="text-align: left;">
					<span class="name">Category</span>
				</td>
				<td>
					<span class="value">${productInstance?.category?.name }
				</td>
				<td></td>
			</tr>
			<tr class="odd">	
				<td style="text-align: left;">
					<span class="name">Product Code</span>
				</td>
				<td>
					<span class="value">${productInstance?.productCode?:'<span class="fade">none</span>' }</span>
				</td>
				<td></td>
			</tr>
			
			<tr class="even">	
				<td style="text-align: left;">
					<span class="name">Cold Chain</span>
				</td>
				<td>
					<span class="value">${productInstance?.coldChain?'Yes':'No' }</span>
				</td>
				<td></td>
			</tr>
			<g:each var="productAttribute" in="${productInstance?.attributes}" status="status">
				<tr class="${status%2==0?'odd':'even' }">
					<td style="text-align: left;">
						<span class="name">${productAttribute?.attribute.name }</span>
					</td>
					<td>
						<span class="value">${productAttribute.value }</span>
					</td>
					<td></td>
				</tr>													
			</g:each>
			<tr class="odd" style="border-top: 1px solid lightgrey;">
				<td style="text-align: left;">
					<span class="name">Supported</span>
				</td>
				<td>
				
					<script>
						$(document).ready(function() {
							$('#toggleSupportedImage').click(function() {	
								var image = $('#toggleSupportedImage');
								var currImageSrc = image.attr("src");
								var playImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'control_play.png' )}";							
								var stopImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'control_stop.png' )}";							
								var imageSrc = (currImageSrc == playImageSrc)?stopImageSrc:playImageSrc;							
								image.attr("src",imageSrc);								
							});
						});
					</script>
								
					<span id="supported" class="value">
						<span id="supportedValue">
							<g:if test="${inventoryLevelInstance}">
								<g:if test="${inventoryLevelInstance?.supported}">
									Yes  
								</g:if>			
								<g:else>										
									No 
								</g:else>
							</g:if>
							<g:else>
								<span class="fade">N/A</span>
							</g:else>
						</span>
					</span>
				</td>
				<td>
					<g:remoteLink action="toggleSupported" params="['product.id':productInstance.id, 'inventory.id':inventoryInstance?.id]"
					update="[success:'supportedValue',failure:'supportedValue']">
					
						<g:if test="${inventoryLevelInstance?.supported }">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'control_stop.png' )}" id="toggleSupportedImage"/>
						</g:if>
						<g:else>
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'control_play.png' )}" id="toggleSupportedImage"/>
						</g:else>					
						
					</g:remoteLink>
				</td>
			</tr>				
			<tr class="even">
				<td style="text-align: left;">
					<span class="name">Min Level</span>
				</td>
				<td>
					<script>

						function onCompleteCallback(id) { 
							$(id).click();
						}
						
						$(document).ready(function() {
							$("#minQuantityTextField").hide();
							$('.minQuantityToggle').click(function() {
								$('#minQuantityTextValue').toggle();
								$('#minQuantityTextField').toggle();
								$('#minQuantity').focus();
							});

							$("#reorderQuantityTextField").hide();
							$('.reorderQuantityToggle').click(function() {
								$('#reorderQuantityTextValue').toggle();
								$('#reorderQuantityTextField').toggle();
								$('#reorderQuantity').focus();
							});							
						
							$('#minQuantityImage').click(function() {	
								var image = $('#minQuantityImage');
								var currImageSrc = image.attr("src");
								var onImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png' )}";							
								var offImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}";							
								var imageSrc = (currImageSrc == onImageSrc)?offImageSrc:onImageSrc;							
								image.attr("src",imageSrc);								
							});
							$('#reorderQuantityImage').click(function() {	
								var image = $('#reorderQuantityImage');
								var currImageSrc = image.attr("src");
								var onImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png' )}";							
								var offImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}";							
								var imageSrc = (currImageSrc == onImageSrc)?offImageSrc:onImageSrc;							
								image.attr("src",imageSrc);								
							});
						});
					</script>
				
					<span id="minQuantityTextValue" class="value">
						<span id="minQuantityValue">
							<g:if test="${inventoryLevelInstance?.minQuantity}">
								${inventoryLevelInstance?.minQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade">N/A</span>
							</g:else>
						</span>
						&nbsp;
					</span>
					<span id="minQuantityTextField" class="value">
						<g:formRemote url="[controller:'inventoryItem',action:'updateQuantity']" 							
							update="[success:'minQuantityValue',failure:'minQuantityValue']" 
							onComplete="onCompleteCallback('#minQuantityImage');"
							id="minQuantityForm" name="minQuantityForm">
							
							<input type="hidden" name="product.id" value="${productInstance?.id }" />
							<input type="hidden" name="inventory.id" value="${inventoryInstance?.id }" />
							<g:textField id="minQuantity" name="minQuantity" size="3"/>
							<input class="button  " type="image" border="0"  src="${createLinkTo(dir: 'images/icons/silk', file: 'disk.png' )}" alt="Submit button">
						</g:formRemote >					
					</span>					
				</td>
				<td>
					<a class="minQuantityToggle" href="#">
						<img id="minQuantityImage" src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png' )}"/>
					</a>
				</td>				
				
			</tr>
			<tr class="odd">
				<td style="text-align: left;">
					<span class="name">Reorder Level</span>
				</td>
				<td>
					<span id="reorderQuantityTextValue" class="value">
						<span id="reorderQuantityValue">
							<g:if test="${inventoryLevelInstance?.reorderQuantity}">
								${inventoryLevelInstance?.reorderQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade">N/A</span>
							</g:else>
						</span>
					</span>
					<span id="reorderQuantityTextField" class="value">
						<g:formRemote url="[controller:'inventoryItem',action:'updateQuantity']" 
							update="[success:'reorderQuantityValue',failure:'reorderQuantityValue']" 
							onComplete="onCompleteCallback('#reorderQuantityImage');"
							id="reorderQuantityForm" name="reorderQuantityForm">
							
							<input type="hidden" name="product.id" value="${productInstance?.id }" />
							<input type="hidden" name="inventory.id" value="${inventoryInstance?.id }" />
							<g:textField id="reorderQuantity" name="reorderQuantity" size="3"/>
							<input class="button" type="image" border="0"  src="${createLinkTo(dir: 'images/icons/silk', file: 'disk.png' )}" alt="Submit button">
						</g:formRemote >					
					</span>	
				</td>
				<td>
					<a class="reorderQuantityToggle" href="#">
						<img id="reorderQuantityImage" src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png' )}"/>
					</a>
				</td>
			</tr>				
			<%-- 						
			<tr class="odd">
				<td></td>
				<td>
					<div style="text-align: right;">			
						<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
						<a href="#" id="configureWarningLevelsLink">Configure</a>
					</div>													
				</td>
			</tr>
			--%>
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