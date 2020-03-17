<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.enterOrderDetails.label"/></title>
</head>
<body>
	<div class="nav">
		<span class="linkButton"><a href="${createLinkTo(dir:'')}"><warehouse:message code="default.home.label"/></a></span>
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
            <g:hiddenField name="orderTypeCode" value="${org.pih.warehouse.order.OrderTypeCode.PURCHASE_ORDER}"/>
            <div class="dialog">
                <g:render template="/order/summary" model="[orderInstance:order,currentState:'editOrder']"/>
                <div class="box">
                    <h2><warehouse:message code="order.enterOrderDetails.label" /></h2>
                    <table>
                        <tbody>

                            <tr class='prop'>
                                <td valign='top' class='name middle'><label for='origin.id'>
                                    <warehouse:message code="order.orderedFrom.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'origin','errors')}'>
                                    <g:selectOrderSupplier name="origin.id" class="chzn-select-deselect"
                                                           optionKey="id" value="${order?.origin?.id}" noSelection="['null':'']"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td class='name top'><label for='name'>
                                    <warehouse:message code="order.description.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'name','errors')}'>
                                    <g:textField type="text" id="name" name='name'
                                                 placeholder="${warehouse.message(code:'order.description.placeholder')}" class="text large" value="${order?.name?.encodeAsHTML()}"/>

                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='name top' class='name middle'>
                                    <label for='orderNumber'><warehouse:message code="order.orderNumber.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'orderNumber','errors')}'>
                                    <input type="text" id="orderNumber" name='orderNumber'
                                           value="${order?.orderNumber?.encodeAsHTML()}" class="text large"
                                           placeholder="${warehouse.message(code:'order.orderNumber.placeholder')}"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'><label for='dateOrdered'><warehouse:message code="order.dateOrdered.label"/></label></td>
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
                                <td valign='top' class='name middle'>
                                    <label for="destination.id"><warehouse:message code="order.destination.label"/></label>
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
                                <td valign='top' class='name middle'><label for='currencyCode'><warehouse:message code="order.currencyCode.label"/></label></td>
                                <td valign='top'
                                    class='value ${hasErrors(bean:order,field:'currency','errors')}'>
                                    <g:selectCurrency name="currencyCode" class="chzn-select-deselect" value="${order?.currencyCode}" noSelection="['':'']"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'>
                                    <label for="paymentMethodType.id"><warehouse:message code="order.paymentMethodType.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'paymentMethodType','errors')}'>
                                    <g:selectPaymentMethodType name="paymentMethodType.id" value="${order?.paymentMethodType?.id}" class="chzn-select-deselect" noSelection="['':'']"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'>
                                    <label for="paymentTerm.id"><warehouse:message code="order.paymentTerm.label"/></label>
                                </td>
                                <td valign='top' class='value ${hasErrors(bean:order,field:'paymentTerm','errors')}'>
                                    <g:selectPaymentTerm name="paymentTerm.id" value="${order?.paymentTerm?.id}" class="chzn-select-deselect" noSelection="['':'']"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name middle'><label for='orderedBy.id'><warehouse:message code="order.createdBy.label"/></label></td>
                                <td valign='top'
                                    class='value ${hasErrors(bean:order,field:'orderedBy','errors')}'>
                                    <g:select class="chzn-select-deselect" name="orderedBy.id" from="${org.pih.warehouse.core.Person.list().sort()}"
                                              optionKey="id" value="${order?.orderedBy?.id}" noSelection="['null':'']" />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons right">

                    <button name="_eventId_next" class="button">
                        <warehouse:message code="default.button.next.label"/>
                        <img src="${resource(dir:'images/icons/silk', file: 'resultset_next.png')}">
                    </button>

                </div>
            </div>
		</g:form>
	</div>
</body>
</html>
