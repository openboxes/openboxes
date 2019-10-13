
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.enterOrderDetails.label"/></title>
</head>
<body>
	<div class="nav">
		<span class="linkButton"><a href="${resource(dir:'')}"><warehouse:message code="default.home.label"/></a></span>
	</div>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
		<g:each var="orderItem" in="${orderItems}" status="i">
			<g:hasErrors bean="${orderItem}">
				<div class="errors">
					<g:renderErrors bean="${orderItem}" as="list" />
				</div>
			</g:hasErrors>
		</g:each>
		<g:form action="purchaseOrder" method="post">

            <g:hiddenField name="orderTypeCode" value="PURCHASE_ORDER"/>
			<div class="dialog">
                <g:render template="/order/summary" model="[orderInstance:order,currentState:'editOrder']"/>
                <div class="box">
                    <h2><warehouse:message code="order.enterOrderDetails.label" /></h2>
                    <table>
                        <tbody>
                            <tr class='prop'>
                                <td class='name top'><label for='name'>
                                    <warehouse:message code="default.name.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'name','errors')}'>
                                    <g:textField type="text" id="name" name='name'
                                                 placeholder="${warehouse.message(code:'order.description.placeholder')}" size="100" class="text large" value="${order?.name?.encodeAsHTML()}"/>

                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='name top' class='name middle'>
                                    <label for='orderNumber'><warehouse:message code="order.orderNumber.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'orderNumber','errors')}'>
                                    <input type="text" id="orderNumber" name='orderNumber'
                                           value="${order?.orderNumber?.encodeAsHTML()}" size="50" class="text"
                                           placeholder="${warehouse.message(code:'order.orderNumber.placeholder')}"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td class='name top'><label for='status'>
                                    <warehouse:message code="order.status.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'status','errors')}'>
                                    <div style="width:300px">
                                        <g:select name="status"
                                                  from="${org.pih.warehouse.order.OrderStatus.list()}" class="chzn-select-deselect"
                                                  optionValue="${{format.metadata(obj:it)}}" value="${order?.status}"
                                                  noSelection="['':warehouse.message(code:'')]" />

                                    </div>

                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'><label for='dateOrdered'><warehouse:message code="order.orderedOn.label"/></label></td>
                                <td valign='top'
                                    class='value ${hasErrors(bean:order,field:'dateOrdered','errors')}'>
                                    <g:jqueryDatePicker
                                            id="dateOrdered"
                                            name="dateOrdered"
                                            value="${order?.dateOrdered?:new Date() }"
                                            format="MM/dd/yyyy"
                                            size="30"
                                            showTrigger="false" />
                                </td>
                            </tr>


                            <tr class='prop'>
                                <td valign='top' class='name middle'><label for='origin.id'>
                                    <warehouse:message code="order.orderFrom.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'origin','errors')}'>
                                    <div style="width: 300px;">
                                        <g:selectOrderSupplier name="origin.id" class="chzn-select-deselect" style="width:350px;"
                                                               optionKey="id" value="${order?.origin?.id}" noSelection="['null':'']"/>
                                    </div>

                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'>
                                    <label for="destination.id"><warehouse:message code="order.orderFor.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'destination','errors')}'>
                                    <g:if test="${order?.destination }">
                                        ${order?.destination?.name }
                                        <g:hiddenField name="destination.id" value="${order?.destination?.id}"/>
                                    </g:if>
                                    <g:else>
                                        ${session?.warehouse?.name }
                                        <g:hiddenField name="destination.id" value="${session?.warehouse?.id}"/>
                                    </g:else>

                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'><label for='orderedBy.id'><warehouse:message code="order.orderedBy.label"/></label></td>
                                <td valign='top'
                                    class='value ${hasErrors(bean:order,field:'orderedBy','errors')}'>
                                    <div style="width: 300px;">
                                        <g:select class="chzn-select-deselect" name="orderedBy.id" from="${org.pih.warehouse.core.Person.list().sort()}"
                                                  optionKey="id" value="${order?.orderedBy?.id}" noSelection="['null':'']" />
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <g:submitButton name="next" value="${warehouse.message(code:'default.button.save.label')}" class="button"></g:submitButton>
                    <g:link action="purchaseOrder" event="cancel" class="button"><warehouse:message code="default.button.back.label"/></g:link>
                </div>
            </div>
		</g:form>
	</div>
</body>
</html>
