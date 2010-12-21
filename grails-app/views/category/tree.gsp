
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
            <%-- 
				<div>            	
	            	<span class="menuButton">
	            		<g:link class="new" action="create"><g:message code="default.add.label" args="['category']"/></g:link>
	            	</span>
	           	</div>
	           	--%>
	           	<table>
	           		<tr>
		           		<td valign="top">
				           	<fieldset>
				           		<legend>Categories</legend>
				           	
					           	<style>
					           		#categoryTree { 
					           			list-style-type: square;
					           		}
					           		#categoryTree li { 
					           			padding-left: 25px; 
					           			padding-top: 5px; 
					           			padding-bottom: 5px;
					           		}
					           	</style>
								<ul id="categoryTree">
									<g:render template="tree" model="[category:rootCategory, level: 0]"/>
								</ul>
							
							</fieldset>
		           		</td>
		           		<td >
		           			<g:if test="${categoryInstance}">
				           		<g:form action="saveCategory">
				           			<fieldset>
				           				<legend>Add a category</legend>
				           				<table>
				           					<tr class="prop">
				           						<td class="name">
				           							<label>Parent</label>
				           						</td>
				           						<td class="value">
				           							<g:if test="${categoryInstance }">
					           							${(categoryInstance?.parentCategory)?categoryInstance?.parentCategory?.name:'no parent' }
					           							<g:hiddenField name="parentCategory.id" value="${categoryInstance?.parentCategory?.id }"/>			           						
				           							</g:if>
				           							<g:else>
				           								<select name="parentCategory.id">
				           									<option value="0">no parent</option>
				           									<g:render template="optionTree" model="[category:rootCategory, level: 1]"/>
				           								</select>
				           							</g:else>
				           						</td>
				           					</tr>
				           					<tr class="prop">
				           						<td class="name">
				           							<label>Name</label>
				           						</td>
				           						<td class="value">
							           				<g:hiddenField name="id" value="${categoryInstance?.id }"/>
							           				<g:textField name="name" value="${categoryInstance?.name }"/>
				           						</td>
				           					</tr>
				           					<tr class="prop">
				           						<td class="name">
				           							<label>Children</label>
				           						</td>
				           						<td class="value">
				           							<table>			           							
					           							<g:each var="child" in="${categoryInstance?.categories }" status="status">
					           								<tr>
								           						<td>${child?.name }</td>
					           								</tr>
								           				</g:each>
							           				</table>
				           						</td>
				           					</tr>
				           					
				           					
				           					
				           					<%-- 
				           					<tr class="prop">
				           						<td class="name">
				           							<label>Class</label>
				           						</td>
				           						<td class="value">
													<g:select in="${org.pih.warehouse.product.ProductClass.list() }" value="${categoryInstance.productClass}"></g:select>
				           						</td>
				           					</tr>
				           					--%>
				           					<tr class="prop">
				           						<td class="name">
	
				           						</td>
				           						<td class="value">
													
													<g:submitButton name="submit" value="Submit"/>
				           						</td>
				           					</tr>
	
				           				</table>
			           				
			           				
				           			</fieldset>
		           				</g:form>
		           			</g:if>
		           			<g:else>
		           			
								<g:form action="save" method="post" >
					            	<fieldset>
					            		<legend>Create a new category</legend>
						                <div class="dialog">
						                    <table>
						                        <tbody>
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="parentCategory"><g:message code="category.parentCategory.label" default="Parent Category" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'parentCategory', 'errors')}">
						                                    <%--<g:select name="parentCategory.id" from="${org.pih.warehouse.product.Category.list()}" optionKey="id" value="${categoryInstance?.parentCategory?.id}" noSelection="['null': '']" /> --%>
															<select name="parentCategory.id">
																<option value="">no parent</option>
																<g:render template="optionTree" model="[category:rootCategory, level: 1]"/>
															</select>	                                    
						                                    
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="name"><g:message code="category.name.label" default="Name" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'name', 'errors')}">
						                                    <g:textField name="name" value="${categoryInstance?.name}" />
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="description"><g:message code="category.description.label" default="Description" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'description', 'errors')}">
						                                    <g:textField name="description" value="${categoryInstance?.description}" />
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="sortOrder"><g:message code="category.sortOrder.label" default="Sort Order" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'sortOrder', 'errors')}">
						                                    <g:textField name="sortOrder" value="${fieldValue(bean: categoryInstance, field: 'sortOrder')}" />
						                                </td>
						                            </tr>
						                        
						                        <%-- 
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="dateCreated"><g:message code="category.dateCreated.label" default="Date Created" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'dateCreated', 'errors')}">
						                                    <g:datePicker name="dateCreated" precision="day" value="${categoryInstance?.dateCreated}"  />
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="lastUpdated"><g:message code="category.lastUpdated.label" default="Last Updated" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'lastUpdated', 'errors')}">
						                                    <g:datePicker name="lastUpdated" precision="day" value="${categoryInstance?.lastUpdated}"  />
						                                </td>
						                            </tr>
						                        --%>
						                        
							                        <tr class="prop">
							                        	<td valign="top"></td>
							                        	<td valign="top">
											                <div class="buttons">
											                   <g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
											                   
											                   <g:link action="list">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
											                   
											                </div>                        	
							                        	</td>
							                        </tr>
							                        
						                        </tbody>
						                    </table>
						                </div>
					                </fieldset>
					            </g:form>		           			
		           			
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
