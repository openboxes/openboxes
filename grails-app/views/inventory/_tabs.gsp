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


<%-- 
<div>	
	<ul class="megamenu">
		<g:each var="quickCategory" in="${quickCategories}">
			<li>
				<a class="menu-heading" href="?categoryId=${quickCategory?.id}&resetSearch=true">
					<format:category category="${quickCategory}"/>
				</a>
				<div class="menu-section">
					<g:if test="${quickCategory.categories}">
						<table>							
							<tr>
								<g:each var="childCategory" in="${quickCategory.categories}">
									<td>
										<a href="?categoryId=${childCategory?.id}&resetSearch=true">
											<b><format:category category="${childCategory}"/></b>
										</a>
										<table>
											<g:each var="subchildCategory" in="${childCategory?.categories }">
												<tr>
													<td>
														<a href="?categoryId=${subchildCategory?.id}&resetSearch=true">
															<format:category category="${subchildCategory}"/>
														</a>
													</td>
												</tr>
											</g:each>					
										</table>
									</td>
									
								</g:each>	
							</tr>
						</table>
					</g:if>
					<g:else>
						<warehouse:message code="default.none.label"/>
					</g:else>
				</div>	
			</li>
		</g:each>
		<li>
			${categoryInstance }
		</li>
	</ul>
</div>					        	
--%>
