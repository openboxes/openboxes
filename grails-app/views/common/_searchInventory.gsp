					


<div id="searchCriteria"  style="border-right: 0px solid lightgrey; padding: 0px;">
	<script>
		$(function() {
			$("#categoryFilter").change(function () { 
				$(this).closest("form").submit();
			});
		});
	</script>
	<div>
		<table>
			<tr>
				<td colspan="2" style="text-align: left;">
					<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
						<g:textField name="searchTerms" value="${params.searchTerms }" size="18"/>
						<button type="submit" class="" name="submitSearch">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" class="middle"/>
							&nbsp;Find&nbsp;</button>
					</g:form>	
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<g:form action="addCategoryFilter">
						<select id="categoryFilter" name="categoryId" >
							<option value="">Filter by category</option>
							<g:render template="../category/selectOptions" model="[category:rootCategory, selected:null, level: 0]"/>								
						</select>										
					</g:form>
				</td>
			
			</tr>
		</table>
			
		
		<g:if test="${categoryFilters }">
			<table>
				<tr class="">
					<td>				
						<label>Filters </label>
					</td>
					<td class="right">
						<g:if test="${categoryFilters.size() >= 2 }">
							<g:link action="clearCategoryFilters">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bin.png' )}" style="vertical-align:middle"/>
							</g:link>							
						</g:if>
					</td>				
				</tr>
				<g:each var="categoryFilter" in="${categoryFilters }" status="status">
					<tr class="${status%2?'even':'odd' }">
						<td style="text-align: left;">									
							${categoryFilter?.name }
						</td>
						<td style="text-align: right">
							<g:link action="removeCategoryFilter" params="[categoryId:categoryFilter.id]">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}" style="vertical-align:middle"/>
							</g:link>
						</td>
					</tr>
				</g:each>
			
				<%-- 
				<tr class="prop">
					<td colspan="2">				
						<label>Attributes</label>
					</td>
				</tr>
				<tr>
					<td>
						<table>
							<g:each var="attributeFilter" in="${commandInstance?.attributeMap }" status="status">
								<tr class="prop ${status%2?'even':'odd' }">				
									<td>${attributeFilter }</td>
								</tr>
							</g:each>
						</table>				
					
					</td>
				</tr>
				--%>
				<%-- 
				<g:each var="attribute" in="${org.pih.warehouse.product.Attribute.list()}" status="status">
					<tr class="prop">
						<td colspan="2">
							<label>${attribute.name }</label>	
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<table>
								<g:if test="${attribute.options }">
									<tr>
										<g:each var="attributeOption" in="${attribute?.options }" status="status2">
											<tr class="${status2%2?'even':'odd' }">
												<td style="text-align: left;">									
													<a href="${createLink(action:'addAttributeFilter',params:["attributeId":attribute.id,value:attributeOption])}">${(attributeOption)?:'none' }</a>
												</td>
												<td style="text-align: right">
													<g:link action="removeAttributeFilter" params="[attributeId:attribute.id,value:attributeOption]">
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}" style="vertical-align:middle"/>
													</g:link>
												</td>
											</tr>
												
												
											</li>
										</g:each>
									</tr>
									
								</g:if>
							</table>
						</td>
					</tr>
				</g:each>	
				--%>
				
			</table>
		</g:if>
	</div>
</div>						
