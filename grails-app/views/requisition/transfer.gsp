<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'request.label', default: 'Requisition')}" />
        <title><warehouse:message code="default.fulfill.label" default="Fulfill {0}" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${requisition}">
				<div class="errors">
					<g:renderErrors bean="${requisition}" as="list" />
				</div>
			</g:hasErrors>


			<g:render template="summary" model="[requisition:requisition]"/>

            <g:if test="${requisition.status == RequisitionStatus.ISSUED || requisition.status == RequisitionStatus.CANCELED}">
                <div class="notice">
                    <warehouse:message code="requisition.hasAlreadyBeenCompleted.message" default="This requisition has already been issued."/>
                </div>
            </g:if>

			<div class="yui-gf">
				<div class="yui-u first">
                    <g:render template="header" model="[requisition:requisition]"/>

                </div>
                <div class="yui-u">

                    <g:form controller="requisition" action="complete" method="POST">
                        <g:hiddenField name="id" value="${requisition?.id }"/>
                        <div class="box">
                            <h2>
                                ${warehouse.message(code:'requisition.issue.label')}
                            </h2>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="requisition.issuedBy.label"/></label>
                                    </td>
                                    <td class="value">
                                        <g:autoSuggest id="issuedBy" name="issuedBy" jsonUrl="${request.contextPath }/json/findPersonByName"
                                                       valueId="${requisition?.issuedBy?.id?:session?.user?.id}"
                                                       valueName="${requisition?.issuedBy?.name?:session?.user?.name}"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="requisition.deliveredBy.label" default="Delivered By"/></label>
                                    </td>
                                    <td class="value">
                                        <g:autoSuggest id="deliveredBy" name="deliveredBy" jsonUrl="${request.contextPath }/json/findPersonByName"
                                                       valueId="${requisition?.deliveredBy?.id}"
                                                       valueName="${requisition?.deliveredBy?.name}"/>
                                    </td>
                                </tr>
                                <tr class="prop">

                                    <td class="name">
                                        <label>Comments</label>
                                    </td>
                                    <td class="value">
                                        <g:textArea name="comments" cols="80" rows="6"></g:textArea>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="box dialog">

                            <table class="requisition">
                                <thead>
                                <tr class="odd">
                                    <th></th>
                                    <th><warehouse:message code="product.label"/></th>
                                    <th><warehouse:message code="location.binLocation.label"/></th>
                                    <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                                    <th><warehouse:message code="default.quantity.label"/></th>
                                    <th><warehouse:message code="product.unitOfMeasure.label"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each var="picklistItem" in="${picklist?.picklistItems }" status="status">
                                    <tr class="${status%2?'odd':'even' }">
                                        <td>
                                            ${status+1 }
                                        </td>
                                        <td>
                                            ${picklistItem?.inventoryItem?.product?.productCode }
                                            ${picklistItem?.inventoryItem?.product }
                                        </td>
                                        <td>
                                            ${picklistItem?.binLocation?.name }
                                        </td>
                                        <td>
                                            ${picklistItem?.inventoryItem?.lotNumber }
                                        </td>
                                        <td>
                                            ${picklistItem?.quantity }
                                        </td>
                                        <td>
                                            ${picklistItem?.inventoryItem?.product?.unitOfMeasure?:"EA" }
                                        </td>
                                    </tr>
                                </g:each>
                                <g:unless test="${picklist?.picklistItems }">
                                    <tr>
                                        <td colspan="5">
                                            <div class="empty center">
                                                <warehouse:message code="picklistItems.empty.label" default="Picklist is empty"/>
                                            </div>
                                        </td>
                                    </tr>
                                </g:unless>
                                </tbody>
                                <tfoot>
                                <tr>
                                    <td colspan="6">
                                        <div class="buttons center">
                                            <g:link controller="requisition" action="confirm" id="${requisition.id }" class="button">
                                                <warehouse:message code="default.button.back.label"/>
                                            </g:link>
                                            <g:if test="${requisition.status == RequisitionStatus.ISSUED || requisition.status == RequisitionStatus.CANCELED}">

                                            </g:if>
                                            <g:else>
                                                <g:submitButton name="finish" value="Finish" class="button">
                                                    <warehouse:message code="default.button.finish.label"/>
                                                </g:submitButton>
                                            </g:else>
                                        </div>
                                    </td>
                                </tr>
                                </tfoot>
                            </table>
                        </div>
                    </g:form>
				</div>
            </div>
        </div>
    </body>
</html>
