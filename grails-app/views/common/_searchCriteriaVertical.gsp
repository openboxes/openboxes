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
			<tr class="odd">
				<td colspan="2">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" style="vertical-align:middle"/>
					<label>Filters</label> 
 				</td>
			</tr>
			<tr>
				<td colspan="3">				
					<label>Category </label>
				</td>
			</tr>
			<tr>
				<td>
					<table>
						<g:each var="categoryFilter" in="${categoryFilters }" status="status">
							<tr class="prop ${status%2?'even':'odd' }">
								<td style="text-align: left;">
									<g:if test="${status!=0 }"><label>OR</label></g:if>
									<g:else><label>=</label></g:else>
									${categoryFilter?.name }
								</td>
								<td style="text-align: right">
									<g:link action="removeCategoryFilter" params="[categoryId:categoryFilter.id]">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}" style="vertical-align:middle"/>
									</g:link>
								</td>
							</tr>
						</g:each>
						<tr class="prop">
							<td colspan="2">
								<g:form action="addCategoryFilter">
									<select id="categoryFilter" name="categoryId" >
										<option value="">Search by category</option>
										<g:render template="../category/selectOptions" model="[category:rootCategory, selected:null, level: 0]"/>								
									</select>										
								</g:form>
							</td>
						</tr>
						<tr class="prop">
							<td style="text-align: right;">
								<label></label>
								<span class="fade">Returned ${productInstanceList?.size() } product(s)</span>			
							</td>
							<td>
								<g:if test="${categoryFilters.size() >= 2 }">
									<g:link action="clearCategoryFilters">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bin.png' )}" style="vertical-align:middle"/>
									</g:link>							
								</g:if>
							</td>
						</tr>						
						
					</table>
				</td>
			</tr>
			<g:if test="${categoryFilters }">
				
			</g:if>
		</table>
	</div>
</div>						
