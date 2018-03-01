
<%@ page import="org.pih.warehouse.product.ProductGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'productGroup.label', default: 'ProductGroup')}" />
<title><warehouse:message code="default.edit.label"
		args="[entityName]" /></title>
<!-- Specify content to overload like global navigation links, page titles, etc. -->
<content tag="pageTitle"> <warehouse:message
	code="default.edit.label" args="[entityName]" /></content>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${productGroupInstance}">
			<div class="errors">
				<g:renderErrors bean="${productGroupInstance}" as="list" />
			</div>
		</g:hasErrors>

        <div class="summary">
            <p class="title">${productGroupInstance?.category?.name} &rsaquo; ${productGroupInstance.name}</p>
        </div>

		<div class="buttonBar">            	
            <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'productGroups.label')]"/></g:link>
            <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'productGroup.label')]"/></g:link>
		</div>
            <g:form method="post" action="update">
				<g:hiddenField name="id" value="${productGroupInstance?.id}" />
				<g:hiddenField name="version" value="${productGroupInstance?.version}" />
				<div class="box">
                    <h2><warehouse:message code="productGroup.label" default="Product group"/></h2>
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label for="name"><warehouse:message
											code="productGroup.name.label" default="Name" /></label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: productGroupInstance, field: 'name', 'errors')}">
									<g:textField name="name" class="text large" size="100"
										value="${productGroupInstance?.name}" />
								</td>
							</tr>


							<tr class="prop">
								<td valign="top" class="name"><label for="category.id"><warehouse:message
											code="productGroup.category.label" default="Category" /></label></td>
								<td valign="top" class="value">
                                    <g:selectCategory name="category.id" class="chzn-select-deselect" noSelection="['null':'']"
                                                      value="${productGroupInstance?.category?.id}" />

								</td>									
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label for="description"><warehouse:message
										code="productGroup.description.label" default="Description" /></label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: productGroupInstance, field: 'description', 'errors')}">
									<g:textArea name="description" class="text" style="width: 100%" rows="5">${productGroupInstance?.description}</g:textArea>
								</td>
							</tr>


							<tr class="prop">
								<td valign="top" colspan="2">
									<div class="buttons">
										<g:actionSubmit class="button icon approve" action="update"
											value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
										<g:actionSubmit class="button icon delete" action="delete"
											value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}"
											onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
            </g:form>
            <div class="box">
                <h2><warehouse:message code="productGroup.products.label" default="Products"/></h2>
                <g:render template="products" model="[productGroup: productGroupInstance, products:productGroupInstance?.products]"/>
            </div>

	</div>
<script>
	$(document).ready(function() {			
		$("#category").change(function() {
		    $(this).closest("form").submit();
		});
	});		
</script>	
	
</body>
</html>
