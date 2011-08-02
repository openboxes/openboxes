
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'category.label', default: 'Category')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
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
		        			<legend>Categories</legend>
		        		
		        		
							<div style="padding:10px; text-align: right">
								<span class="menuButton">
				            		<g:link class="new" controller="category" action="tree" params="[addCategory:'addCategory']"><warehouse:message code="default.add.label" args="['category']"/></g:link>
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
				           				<legend>Edit Category</legend>
				           				<table>
				           					<tr class="prop odd">
				           						<td class="value">
				           							<label>Parent</label>
			           								<select name="parentCategory.id">
			           									<option value="null">Choose a category ... </option>
			           									<g:render template="selectOptions" model="[category:rootCategory, selected:categoryInstance?.parentCategory, level: 0]"/>
			           								</select>
			           							</td>
			           						</tr>
			           						<tr class="prop even">
			           							<td class="value">
			           								<label>Name</label>
							           				<g:textField name="name" value="${categoryInstance?.name }"/>
				           						</td>
				           					</tr>
				           					<tr class="prop odd">
				           						<td class="value">
				           							<label>Children</label>
						           					<g:if test="${categoryInstance?.categories }">
					           							<table>			           							
						           							<g:each var="child" in="${categoryInstance?.categories }" status="status">
						           								<tr>
									           						<td>
									           							<g:link action="tree" id="${child.id }">${child?.name }</g:link>
									           						</td>
						           								</tr>
									           				</g:each>
								           				</table>
													</g:if>
				           						</td>
				           					</tr>
				           					<tr class="prop even">
				           						<td class="value">
					           						<label>Products</label>
						           					<g:if test="${categoryInstance?.products }">
					           							<table>			           							
						           							<g:each var="product" in="${categoryInstance?.products }" status="status">
						           								<tr>
																	<td>
																		<g:link controller="product" action="edit" id="${product?.id}" target="_blank">${product?.name }</g:link>
																	</td>
						           								</tr>
									           				</g:each>
								           				</table>
								           			</g:if>
				           						</td>
				           					</tr>
				           					
				           					<tr class="prop">
				           						<td colspan="2" style="text-align:center">		
				           						
					           						<button type="submit" name="save" class="save">${message(code: 'default.button.save.label', default: 'Save')}</button>															
													&nbsp;
													<g:link action="tree">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
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
					           				<legend>Create Category</legend>
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
															<label for="name" class="desc"><warehouse:message code="category.name.label" default="Name" /></label>
															<g:textField name="name" value="${categoryInstance?.name}" />
														</td>
													</tr>
							                        <tr class="prop">
				           								<td colspan="2" style="text-align:center">		
											                   <button type="submit" name="create" class="save">${message(code: 'default.button.create.label', default: 'Create')}</button>
											                   &nbsp;
											                   <g:link action="tree">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
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
										<g:if test="${!category.parentCategory }"><b>${category.name }</b></g:if> 
										<g:else>${category.name }</g:else>
										<g:link class="new" action="create" params="['parentCategory.id':category.id]"><warehouse:message code="default.add.label" args="['category']"/></g:link>
										| 
										<g:link class="new" action="delete" params="['category.id':category.id]"><warehouse:message code="default.delete.label" args="['category']"/></g:link>
									</div>
								</td>
							</tr>
							<g:each var="childCategory" in="${category.categories}">
								<tr>
									<td>							
										<div style="padding-left: 50px;">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_white.png')}" alt="Bullet" /> &nbsp;
											
											<g:if test="${!childCategory.parentCategory }">
												<b>${childCategory.name }</b>
											</g:if>
											<g:else>
												${childCategory.name }
											</g:else>
											<g:link class="new" action="create" params="['parentCategory.id':childCategory.id]"><warehouse:message code="default.add.label" args="['category']"/></g:link>
											| 
											<g:link class="new" action="delete" params="['category.id':childCategory.id]"><warehouse:message code="default.delete.label" args="['category']"/></g:link>
											
											
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
