<%@ page import="org.pih.warehouse.order.OrderItemStatusCode; org.pih.warehouse.order.OrderTypeCode" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
		<g:set var="entityName" value="${warehouse.message(code: 'productMergeLogs.label', default: 'Product Merge Logs')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
   	</head>
	<body>
		<div class="body">
			<div class="yui-gf">
				<div class="yui-u first">
					<g:render template="productMergeLogsFilters" model="[]"/>
				</div>
				<div class="yui-u">

					<div class="box">
						<h2>
							<warehouse:message code="default.list.label" args="[entityName]" />
						</h2>
						<table>
							<thead>
								<tr>
									<th>${warehouse.message(code: 'productMerge.id.label', default: "Log ID")}</th>
									<th>${warehouse.message(code: 'productMerge.primaryProduct.label', default: "Primary Product")}</th>
									<th>${warehouse.message(code: 'productMerge.obsoleteProduct.label', default: "Obsolete Product")}</th>
									<th>${warehouse.message(code: 'productMerge.relatedObjectId.label', default: "Related Object ID")}</th>
									<th>${warehouse.message(code: 'productMerge.relatedObjectClassName.label', default: "Related Object class name")}</th>
									<th>${warehouse.message(code: 'productMerge.dateMerged.label', default: "Date Merged")}</th>
									<th>${warehouse.message(code: 'productMerge.createdBy.label', default: "Created By")}</th>
								</tr>
							</thead>
							<tbody>
								<g:unless test="${productMergeLogs}">
									<tr class="prop">
										<td colspan="15">
											<div class="empty fade center">
												<warehouse:message code="productMerge.none.message" default="None"/>
											</div>
										</td>
									</tr>
								</g:unless>

								<g:each var="productMergeLog" in="${productMergeLogs}" status="i">

									<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
										<td class="middle">
											${fieldValue(bean: productMergeLog, field: "id")}
										</td>
										<td class="middle">
											${fieldValue(bean: productMergeLog.primaryProduct, field: "productCode")}
										</td>
										<td class="middle">
											${fieldValue(bean: productMergeLog.obsoleteProduct, field: "productCode")}
										</td>
										<td class="middle">
											${fieldValue(bean: productMergeLog, field: "relatedObjectId")}
										</td>
										<td class="middle">
											${fieldValue(bean: productMergeLog, field: "relatedObjectClassName")}
										</td>
										<td class="middle">
											${fieldValue(bean: productMergeLog, field: "dateMerged")}
										</td>
										<td class="middle">
											${fieldValue(bean: productMergeLog.createdBy, field: "name")}
										</td>
									</tr>
								</g:each>
							</tbody>
						</table>
						<div class="paginateButtons">
							<g:set var="pageParams" value="${pageScope.variables['params']}"/>
							<g:paginate total="${productMergeLogs?.totalCount?:0}" params="${params}"/>
						</div>
					</div>
				</div>

			</div>
		</div>
    </body>
</html>
