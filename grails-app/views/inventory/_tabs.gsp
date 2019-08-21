<style>
	.tab-container { overflow: auto; }
</style>

<div style="padding-top:0px;" class="tab-container">
	<g:form method="GET" controller="inventory" action="browse">
		<table style="width:100%; border-collapse: collapse; border-color: black;">
			<tr>
				<g:each var="quickCategory" in="${quickCategories}">
					<td class="filterTab filterRow paddingRow"></td>
					<td class="<g:if test="${commandInstance?.categoryInstance == quickCategory}">filterSelected </g:if>filterTab filterRow">
						<a href="?categoryId=${quickCategory?.id}&resetSearch=true">
							<span style="text-transform: lowercase"><format:category category="${quickCategory}"/></span>
						</a>
					</td>		
				</g:each>
				<td class="filterTab filterRow paddingRow" style="width:100%">&nbsp;</td>
			</tr>
		</table>
	</g:form>
</div>
