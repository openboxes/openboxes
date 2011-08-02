
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
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['category']"/></g:link>
	            	</span>
	           	</div>
	           	--%>
	           	<table>
	           		<tr>
		           		<td valign="top">
		           		
	           				
								<g:form action="save" method="post" >
					            	<fieldset>
					            		<legend>Create a new category</legend>
						                <div class="dialog">
						                    <table>
						                        <tbody>
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="parentCategory"><warehouse:message code="category.parentCategory.label" default="Parent" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'parentCategory', 'errors')}">
						                                    <%--<g:select name="parentCategory.id" from="${org.pih.warehouse.product.Category.list()}" optionKey="id" value="${categoryInstance?.parentCategory?.id}" noSelection="['null': '']" /> --%>
															<select name="parentCategory.id">
																<option value="">no parent</option>
																<g:render template="selectOptions" model="[category:rootCategory, level: 1, selected: categoryInstance]"/>
															</select>						                                    
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="name"><warehouse:message code="category.name.label" default="Name" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'name', 'errors')}">
						                                    <g:textField name="name" value="${categoryInstance?.name}" />
						                                </td>
						                            </tr>
						                        <%-- 
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="description"><warehouse:message code="category.description.label" default="Description" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'description', 'errors')}">
						                                    <g:textField name="description" value="${categoryInstance?.description}" />
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="sortOrder"><warehouse:message code="category.sortOrder.label" default="Sort Order" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'sortOrder', 'errors')}">
						                                    <g:textField name="sortOrder" value="${fieldValue(bean: categoryInstance, field: 'sortOrder')}" />
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="dateCreated"><warehouse:message code="category.dateCreated.label" default="Date Created" /></label>
						                                </td>
						                                <td valign="top" class="value ${hasErrors(bean: categoryInstance, field: 'dateCreated', 'errors')}">
						                                    <g:datePicker name="dateCreated" precision="day" value="${categoryInstance?.dateCreated}"  />
						                                </td>
						                            </tr>
						                        
						                            <tr class="prop">
						                                <td valign="top" class="name">
						                                    <label for="lastUpdated"><warehouse:message code="category.lastUpdated.label" default="Last Updated" /></label>
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
