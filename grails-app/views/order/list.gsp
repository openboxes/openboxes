<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'orders.label', default: 'Purchase orders').toLowerCase()}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>    
    <body>
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <div class="box">

            <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
         	<table>
            	<thead>
					<tr>
						<th>${warehouse.message(code: 'default.actions.label')}</th>
						<th>${warehouse.message(code: 'default.status.label')}</th>
						<th>${warehouse.message(code: 'order.orderNumber.label')}</th>
                        <th>${warehouse.message(code: 'default.description.label')}</th>
						<th>${warehouse.message(code: 'default.origin.label')}</th>
						<th>${warehouse.message(code: 'order.dateOrdered.label')}</th>
						<th class="right">${warehouse.message(code: 'order.totalPrice.label', default: 'Total price')}</th>
						<th></th>
					</tr>
				</thead>		            
				<tbody>
            		<tr class="prop odd">            
			            <g:form id="listForm" action="list" method="post">
							<td>

							</td>
                            <td class="filter-list-item middle" width="10%">
                                <g:select id="status" name="status"
                                          from="${org.pih.warehouse.order.OrderStatus.list()}" class="chzn-select-deselect"
                                          optionValue="${{format.metadata(obj:it)}}" value="${status}"
                                          noSelection="['':warehouse.message(code:'default.all.label')]" />
                            </td>
							<td>
								<g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" readonly="readonly" size="10" onclick="alert('This filter is not currently supported.');"/>

							</td>
							<td>
                                <g:textField class="text" id="description" name="description" value="${params.description}" size="30"/>
			            	</td>
				           	<td class="filter-list-item middle">
								<div style="width: 300px">
									<g:select id="origin" name="origin" class="chzn-select-deselect"
											  from="${suppliers}"
											  optionKey="id" optionValue="name" value="${origin}"
											  noSelection="['':warehouse.message(code:'default.all.label')]" />
								</div>
							</td>
							<td class="filter-list-item middle" nowrap="nowrap">
								<g:jqueryDatePicker id="statusStartDate" name="statusStartDate"  placeholder="Start date"
													value="${statusStartDate}" format="MM/dd/yyyy"/>

								<g:jqueryDatePicker id="statusEndDate" name="statusEndDate" placeholder="End date"
													value="${statusEndDate}" format="MM/dd/yyyy"/>


							</td>
							<td class="right">
								<g:textField class="text" id="totalPrice" name="totalPrice" value="${params.totalPrice}" readonly="readonly" size="10" onclick="alert('This filter is not currently supported.')"/>
							</td>
							<td class="filter-list-item center middle">
								<button name="filter" class="button icon edit">
									<warehouse:message code="default.button.filter.label"/>
								</button>
								<button href="javascript:void(0);" class="clear-all button icon trash"><warehouse:message code="default.clear.label"/></button>
							</td>
			            </g:form>
					</tr>					
									
				
				
		            <g:unless test="${orders}">
		            	<tr class="prop">
		            		<td colspan="8">
                                <div class="empty fade center">
    			           			<warehouse:message code="orders.none.message"/>
                                </div>
				           	</td>
						</tr>     
		           	</g:unless>
            		<g:set var="totalPrice" value="${0.00}"/>
					<g:each var="orderInstance" in="${orders}" status="i">
						<g:set var="totalPrice" value="${totalPrice + (orderInstance.totalPrice()?:0)}"/>
						<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
							<td class="middle">
								<div class="action-menu">
									<g:render template="/order/actions" model="[orderInstance:orderInstance,hideDelete:true]"/>
								</div>
							</td>

							<td class="middle">
                                <format:metadata obj="${orderInstance?.status}"/>
                            </td>
							<td class="middle">
								<g:link action="show" id="${orderInstance.id}">
									${fieldValue(bean: orderInstance, field: "orderNumber")}
								</g:link>
							</td>
							<td class="middle">
								<g:link action="show" id="${orderInstance.id}">
									${fieldValue(bean: orderInstance, field: "description")}
								</g:link>				
							</td>
							<td class="middle">
								${fieldValue(bean: orderInstance, field: "origin.name")}
							</td>
							<td class="middle">
								<format:date obj="${orderInstance?.dateOrdered}"/>
							</td>
							<td class="middle right">
								<g:formatNumber number="${orderInstance?.totalPrice()}" type="currency" currencyCode="USD"/>
							</td>
							<td>

							</td>

						</tr>
					</g:each>                    		
				</tbody>
				<tfoot>
					<tr class="odd">
						<th colspan="6"><label>${warehouse.message(code:'default.total.label')}</label></th>
						<th colspan="1" class="right">
							<div class="text large">

								<g:formatNumber number="${totalPrice}" type="currency" currencyCode="USD"/>

							</div>
						</th>
						<th>

						</th>
					</tr>

				</tfoot>
			</table>
        </div>
		<script type="text/javascript">
			$(document).ready(function() {
				$(".clear-all").click(function() {
					$('#statusStartDate-datepicker').val('');					
					$('#statusEndDate-datepicker').val('');
					$('#statusStartDate').val('');					
					$('#statusEndDate').val('');
					$('#totalPrice').val('');
					$('#description').val('');
					$("#origin").val('').trigger("chosen:updated");
					$("#status").val('').trigger("chosen:updated");
				});
			});
        </script>
    </body>
</html>