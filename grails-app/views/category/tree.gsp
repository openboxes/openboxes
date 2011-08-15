
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="category.productCategories.label" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${categoryInstance}">
				<div class="errors"><g:renderErrors bean="${categoryInstance}" as="list" /></div>
			</g:hasErrors>            
            
            <div class="dialog" >        
		        <table>
		        	<tr>
		        		<td>
		        		
		        		<fieldset>
		        			<legend><warehouse:message code="category.categories.label"/></legend>
		        		
		        		
							<div style="padding:10px; text-align: right">
								<span class="menuButton">
				            		<g:link class="new" controller="category" action="tree" params="[addCategory:'addCategory']"><warehouse:message code="default.add.label" args="[entityName]"/></g:link>
				            	</span>										    	
							</div>						
		        		
		           			<div>
								<style>
									.category-tree ul { margin-left: 2em; } 
									.category-tree li { background-color: #f7f7f7;
									border: 1px dashed lightgrey; padding: .5em; margin: .5em;}
								</style>
								
								<%-- Display the category tree from the ROOT node --%>
								<g:render template="tree" model="[category:rootCategory, level: 0]"/>
							
							
								<script>
									$(function() {
										//$( ".draggable" ).draggable();
							
										$('li.draggable').draggable(
												{
													revert		: true,
													autoSize		: false,
													ghosting			: false,
													onStop		: function()
													{
														$('li.droppable').each(
															function()
															{
																this.expanded = false;
															}
														);
													}
												}
											);
							
										$('li.droppable').droppable(
												{
													accept: 'li.draggable',
													tolerance: 'intersect',
													over: function(event, ui) { 
														$( this ).addClass( "ui-state-highlight" );
													},
													out: function(event, ui) { 
														$( this ).removeClass( "ui-state-highlight" );
													},
													drop: function( event, ui ) {
														ui.draggable.hide();
														$( this ).removeClass( "ui-state-highlight" );
														var child = ui.draggable.attr("id");
														var parent = $(this).attr("id");
														var url = "/warehouse/category/move?child=" + child + "&newParent=" + parent;
														window.location.replace(url);
													}
												}
											);
										});
								</script>
				            </div>
				            
				            </fieldset>
				            
						</td>
						<td>
							<g:if test="${categoryInstance }">
								<g:form action="saveCategory">
									<g:hiddenField name="id" value="${categoryInstance?.id }"/>
				           			<fieldset>
				           				<legend><warehouse:message code="category.editCategory.label"/></legend>
				           				<table>
				           					<tr class="prop odd">
				           						<td class="value">
				           							<label><warehouse:message code="category.parent.label"/></label>
			           								<select name="parentCategory.id">
			           									<option value="null"><warehouse:message code="category.chooseACategory.label"/></option>
			           									<g:render template="selectOptions" model="[category:rootCategory, selected:categoryInstance?.parentCategory, level: 0]"/>
			           								</select>
			           							</td>
			           						</tr>
			           						<tr class="prop even">
			           							<td class="value">
			           								<label><warehouse:message code="default.name.label"/></label>
							           				<g:textField name="name" value="${categoryInstance?.name }"/>
				           						</td>
				           					</tr>
				           					<tr class="prop odd">
				           						<td class="value">
				           							<label><warehouse:message code="category.children.label"/></label>
						           					<g:if test="${categoryInstance?.categories }">
					           							<table>			           							
						           							<g:each var="child" in="${categoryInstance?.categories }" status="status">
						           								<tr>
									           						<td>
									           							<g:link action="tree" id="${child.id }"><format:category category="${child}"/></g:link>
									           						</td>
						           								</tr>
									           				</g:each>
								           				</table>
													</g:if>
				           						</td>
				           					</tr>
				           					<tr class="prop even">
				           						<td class="value">
					           						<label><warehouse:message code="category.products.label"/></label>
						           					<g:if test="${categoryInstance?.products }">
					           							<table>			           							
						           							<g:each var="product" in="${categoryInstance?.products }" status="status">
						           								<tr>
																	<td>
																		<g:link controller="product" action="edit" id="${product?.id}" target="_blank"><format:product product="${product}"/></g:link>
																	</td>
						           								</tr>
									           				</g:each>
								           				</table>
								           			</g:if>
				           						</td>
				           					</tr>
				           					
				           					<tr class="prop">
				           						<td colspan="2" style="text-align:center">		
				           						
					           						<button type="submit" name="save" class="save">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</button>															
													&nbsp;
													<g:link action="tree">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
				           						</td>
				           					</tr>
				           				</table>
				           			</fieldset>
		           				</g:form>	
		           			</g:if>									
		           			<g:else>
		           				<g:if test="${params.addCategory=='addCategory' }">					           			
									<g:form action="save" method="post" >
						            	<fieldset>
					           				<legend><warehouse:message code="category.createCategory.label"/></legend>
						                    <table>
						                        <tbody>
													<tr class="prop">
														<td>
															<label for="name" class="desc"><warehouse:message code="category.parent.label" default="Parent" /></label>
															<select name="parentCategory.id" style="display: inline">
																<option value="null"></option>
																<g:render template="selectOptions" model="[category:rootCategory, level: 1, selected: categoryInstance]"/>
															</select>						                                    
														</td>
													</tr>
													<tr class="prop">
														<td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'name', 'errors')}">
															<label for="name" class="desc"><warehouse:message code="default.name.label" default="Name" /></label>
															<g:textField name="name" value="${categoryInstance?.name}" />
														</td>
													</tr>
							                        <tr class="prop">
				           								<td colspan="2" style="text-align:center">		
											                   <button type="submit" name="create" class="save">${warehouse.message(code: 'default.button.create.label', default: 'Create')}</button>
											                   &nbsp;
											                   <g:link action="tree">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
							                        	</td>
							                        </tr>
						                        </tbody>
						                    </table>
					                    </fieldset>

						            </g:form>	
						    	</g:if>	
						    	
		           			</g:else>
						
						</td>
		        	</tr>
		        </table>								            
				
				<%-- 
	           	<table>
		           	<g:set var="counter" value="${1 }" /> 
					<g:each var="category" in="${org.pih.warehouse.product.Category.list().sort() { it?.categories?.size() }.reverse() }" status="status">
						<g:if test="${!category.parentCategory }">
							<tr>
								<td>
									<div style="padding-left: 25px;">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_white.png')}" alt="Bullet" /> &nbsp;										 
										<g:if test="${!category.parentCategory }"><b><format:category category="${category}"/></b></g:if> 
										<g:else><format:category category="${category}"/></g:else>
										<g:link class="new" action="create" params="['parentCategory.id':category.id]"><warehouse:message code="default.add.label" args="[entityName]"/></g:link>
										| 
										<g:link class="new" action="delete" params="['category.id':category.id]"><warehouse:message code="default.delete.label" args="[entityName]"/></g:link>
									</div>
								</td>
							</tr>
							<g:each var="childCategory" in="${category.categories}">
								<tr>
									<td>							
										<div style="padding-left: 50px;">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_white.png')}" alt="Bullet" /> &nbsp;
											
											<g:if test="${!childCategory.parentCategory }">
												<b><format:category category="${childCategory}"/></b>
											</g:if>
											<g:else>
												<format:category category="${childCategory}"/>
											</g:else>
											<g:link class="new" action="create" params="['parentCategory.id':childCategory.id]"><warehouse:message code="default.add.label" args="[entityName]"/></g:link>
											| 
											<g:link class="new" action="delete" params="['category.id':childCategory.id]"><warehouse:message code="default.delete.label" args="[entityName]"/></g:link>
											
											
										</div>
									</td>
								</tr>						
							</g:each>
						</g:if>
					</g:each>
				</table>
					--%>
			</div>         
        </div>
    </body>
</html>
