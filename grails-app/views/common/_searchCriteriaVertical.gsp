<div id="searchCriteria"  style="border: 1px solid lightgrey; padding: 1px;">
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
					&nbsp;
					<label>Filters</label> 
					&nbsp; 
 					<g:link action="clearCategoryFilters">clear all</g:link>
 				</td>
			</tr>
			<tr>
				<td>
					<g:each var="categoryFilter" in="${categoryFilters }">
						<div style="width: 100%; float: left; border: 0px solid lightgrey; height: 30px; margin: 1px; background-color: #fcfcfc;">
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
				<td><hr/></td>
			</tr>
			<tr>
				<td style="text-align: center;">
					<g:form action="addCategoryFilter">
						<select id="categoryFilter" name="categoryId">
							<option value="">Search by category</option>
							<g:render template="../category/selectOptions" model="[category:rootCategory, selected:null, level: 0]"/>								
						</select>										
					</g:form>
				</td>
			</tr>							
		</table>
	</div>
</div>						
