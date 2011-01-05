
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'category.label', default: 'Category')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
			<div class="nav">            	
				<g:render template="nav"/>
           	</div>
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">        
            
            
	           
						        <table>
						        	<tr>
						        		<td>
						           			<div>
									           	<style>
									           		.categoryTree li { padding-left: 25px;padding-top: 5px; padding-bottom: 5px; }
									           	</style>
												<ul class="categoryTree">
													<g:render template="tree" model="[category:rootCategory, level: 0]"/>
												</ul>													
								            </div>
										</td>
										<td>
											<g:if test="${categoryInstance }">
												<g:form action="saveCategory">
													<g:hiddenField name="id" value="${categoryInstance?.id }"/>
												
								           			<fieldset>
								           				<table>
								           					<tr class="prop">
								           						<td class="value">
								           							<label>Name</label><br/>
											           				<g:textField name="name" value="${categoryInstance?.name }"/>
								           						</td>
								           					</tr>
								           					<tr class="prop">
								           						<td class="value">
								           							<label>Parent</label>
							           								<select name="parentCategory.id">
							           									<option value="">no parent</option>
							           									<g:render template="selectOptions" model="[category:rootCategory, selected:categoryInstance?.parentCategory, level: 1]"/>
							           								</select>
								           						</td>
								           						<td class="value">
								           						</td>
								           					</tr>
								           					<tr class="prop">
								           						<td class="value">
								           							<label>Children</label>
								           							<table>			           							
									           							<g:each var="child" in="${categoryInstance?.categories }" status="status">
									           								<tr>
												           						<td>${child?.name }</td>
									           								</tr>
												           				</g:each>
											           				</table>
								           						</td>
								           					</tr>
								           					<tr class="prop">
								           						<td class="value">																	
																	<g:submitButton name="submit" value="Submit"/>
																	&nbsp;
																	<g:link action="tree">cancel</g:link>
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
										                    <table>
										                        <tbody>
										                            <tr class="prop">
										                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'name', 'errors')}">
										                                    <label for="name"><g:message code="category.name.label" default="Name" /></label><br/>
										                                    <g:textField name="name" value="${categoryInstance?.name}" />
										                                </td>
										                            </tr>
										                            <tr class="prop">
										                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'parentCategory', 'errors')}">
										                                    <label for="parentCategory"><g:message code="category.parentCategory.label" default="Parent" /></label><br/>
										                                    <%--<g:select name="parentCategory.id" from="${org.pih.warehouse.product.Category.list()}" optionKey="id" value="${categoryInstance?.parentCategory?.id}" noSelection="['null': '']" /> --%>
																			<select name="parentCategory.id">
																				<option value="">no parent</option>
																				<g:render template="selectOptions" model="[category:rootCategory, level: 1, selected: categoryInstance]"/>
																			</select>						                                    
										                                </td>
										                            </tr>
											                        <tr class="prop">
											                        	<td valign="top">
															                   <g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
															                   <g:link action="tree">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
											                        	</td>
											                        </tr>
										                        </tbody>
										                    </table>
									                    </fieldset>

										            </g:form>	
										    	</g:if>	
										    	<g:else>
													<span class="menuButton">
									            		<g:link class="new" controller="category" action="tree" params="[addCategory:'addCategory']"><g:message code="default.add.label" args="['category']"/></g:link>
									            	</span>										    	
										    	</g:else>
										    	
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
										<g:link class="new" action="create" params="['parentCategory.id':category.id]"><g:message code="default.add.label" args="['category']"/></g:link>
										| 
										<g:link class="new" action="delete" params="['category.id':category.id]"><g:message code="default.delete.label" args="['category']"/></g:link>
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
											<g:link class="new" action="create" params="['parentCategory.id':childCategory.id]"><g:message code="default.add.label" args="['category']"/></g:link>
											| 
											<g:link class="new" action="delete" params="['category.id':childCategory.id]"><g:message code="default.delete.label" args="['category']"/></g:link>
											
											
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
