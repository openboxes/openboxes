<div id="searchCriteria"  style="border: 1px solid lightgrey; padding: 1px;">
	<script>
		$(function() {
			$("#categoryFilter").change(function () { 
				$(this).closest("form").submit();
			});

			$(".searchCriteriaToggle").click(function() { 
				$("#searchCriteriaView").toggle();
				$("#searchCriteriaForm").toggle();
				
			});
			
		});
	</script>
	
	<span id="searchCriteriaView">
		<table>
			<tr class="odd">
				<td colspan="2">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" style="vertical-align:middle"/>
					&nbsp;
					<label>Filters</label> &nbsp; 
				</td>
				<td style="text-align: right;">
				<!-- 
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png' )}" style="vertical-align:middle"/>
					<a class="searchCriteriaToggle" href="#">add filter</a> &nbsp;
				 -->
					<g:if test="${categoryFilters }">
						&nbsp;											
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png' )}" style="vertical-align:middle"/>
						<g:link action="clearCategoryFilters">clear all</g:link>	
					</g:if>												
				</td>										
			</tr>
			<tr>
				<td style="text-align: left; width: 160px;">
					Show products in any of the following categories:
				</td>
				<td style="text-align: left; border-left: 1px solid lightgrey;">
					<g:if test="${categoryFilters }">
						<g:each var="categoryFilter" in="${categoryFilters }">												
							<div style="width: 150px; float: left; border: 1px solid lightgrey; height: 30px; margin: 1px; height: 30px; margin: 1px; background-color: #fcfcfc;"">
								<table> 
									<tr>
										<td>
											${categoryFilter?.name }
										</td>
										<td style="text-align: right; vertical-align: top;">
											<g:link action="removeCategoryFilter" params="[categoryId:categoryFilter.id]">
											 &nbsp; x &nbsp;
											</g:link>
										</td>
									</tr>
								</table>
							</div>
						</g:each>
						<br clear="all"/>					
					</g:if>
					<g:else>
						<span class="fade" style="padding-left: 10px">No filters applied</span>
					</g:else>
				</td>
				<td style="text-align: right;">
					<g:form action="addCategoryFilter">
						<select id="categoryFilter" name="categoryId" style="display:inline;">
							<option value="">filter by category</option>
							<g:render template="../category/selectOptions" model="[category:rootCategory, selected:null, level: 0]"/>	
						</select>										
					</g:form>
				</td>			
			</tr>							
		</table>								
	</span>
	<span id="searchCriteriaForm" style="display: none;">
		<table>
			<tr class="odd">
				<td colspan="2">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" style="vertical-align:middle"/>
					&nbsp; <label>Filters</label>
				</td>
				<td style="text-align: right;">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'door.png' )}" style="vertical-align:middle"/>
					<a class="searchCriteriaToggle" href="#">close</a>
				</td>
			</tr>
			<tr>
				<td style="text-align: left; width: 160px;">
					Show products in any of the following categories:
				</td>

				<td style="text-align: left; border-left: 1px solid lightgrey;">
				
					<g:if test="${categoryFilters }">
						<g:each var="categoryFilter" in="${categoryFilters }">							
							<div style="width: 150px; float: left; border: 1px solid lightgrey; height: 30px; margin: 1px;">
								<table>
									<tr>
										<td>
											${categoryFilter?.name }
										</td>
										<td style="text-align: right">
											<g:link action="removeCategoryFilter" params="[categoryId:categoryFilter.id]">
												<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}" style="vertical-align:middle"/>
											</g:link>
										</td>
									</tr>
								</table>
							</div>		
						</g:each>
							<br clear="all"/>					
					</g:if>
				</td>
				<td style="text-align: right;">
					<g:form action="addCategoryFilter">
						<select id="categoryFilter" name="categoryId" style="display:inline;">
							<option value="">filter by category</option>
							<g:render template="../category/selectOptions" model="[category:rootCategory, selected:null, level: 0]"/>	
						</select>										
					</g:form>
				</td>											
			</tr>																	
		</table>
	</span>
</div>