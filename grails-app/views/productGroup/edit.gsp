
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
			<h1 class="title">
				${productGroupInstance?.name}
			</h1>
        </div>

		<div class="buttonBar">
            <g:link class="button" action="list">
				<img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}"/>&nbsp;
				<warehouse:message code="default.list.label" args="[warehouse.message(code:'productGroups.label')]"/>
			</g:link>
            <g:link class="button" action="create">
				<img src="${resource(dir:'images/icons/silk',file:'add.png')}"/>&nbsp;
				<warehouse:message code="default.add.label" args="[warehouse.message(code:'productGroup.label')]"/>
			</g:link>
		</div>
		<div class="dialog box">
            <div id="product-group-tabs" class="tabs">
                <ul>
                    <li><a href="#product-group-tab"><g:message code="productGroup.label"/></a></li>
                    <li><a href="#product-group-products-tab"><g:message code="productGroup.products.label"/></a></li>
                    <li><a href="#product-group-siblings-tab"><g:message code="productGroup.siblings.label"/></a></li>
                </ul>
                <div id="product-group-tab">
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
				</div>
				<div id="product-group-products-tab">
					<div class="box">
						<h2><warehouse:message code="productGroup.products.label" default="Products"/></h2>
						<g:render template="products" model="[productGroup: productGroupInstance, products: productGroupInstance?.products, isProductFamily: false]"/>
					</div>
				</div>
				<div id="product-group-siblings-tab">
					<div class="box">
						<h2><warehouse:message code="productGroup.products.label" default="Products"/></h2>
						<g:render template="products" model="[productGroup: productGroupInstance, products: productGroupInstance?.siblings, isProductFamily: true]"/>
					</div>
				</div>
			</div>
		</div>
	</div>
<script>
	$(document).ready(function() {
		$("#category").change(function() {
		    $(this).closest("form").submit();
		});
		// Do not set cookie here, because the isProductFamily param is then
		// wrongly recognized by the Products and Siblings tabs
		$(".tabs").tabs({});
	});
</script>

</body>
</html>
