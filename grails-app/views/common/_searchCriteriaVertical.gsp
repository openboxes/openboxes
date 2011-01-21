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
				<td>
					<g:each var="categoryFilter" in="${categoryFilters }">
						<div style="width: 100%; float: left; border: 0px solid lightgrey; margin: 1px; background-color: #fcfcfc;">
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
				</td>
			</tr>
			<tr>
				<td style="text-align: center;">
					<g:form action="addCategoryFilter">
						<select id="categoryFilter" name="categoryId" >
							<option value="">Search by category</option>
							<g:render template="../category/selectOptions" model="[category:rootCategory, selected:null, level: 0]"/>								
						</select>										
					</g:form>
				</td>
			</tr>
			<g:if test="${categoryFilters }">
				<tr>
					<td style="text-align: left; border-top: 1px solid lightgrey;">
						<span class="fade">Returned ${productInstanceList?.size() } product(s)</span>			
						<g:if test="${categoryFilters.size() >= 2 }">
							<g:link action="clearCategoryFilters">clear all</g:link>							
						</g:if>
					</td>
				</tr>
			</g:if>
		</table>
	</div>
</div>						
