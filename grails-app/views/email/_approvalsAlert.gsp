<%@ page import="org.pih.warehouse.core.Constants" contentType="text/html"%>

<style>
    label {
        font-weight: bold;
    }
</style>
<div>
    <div class="header">
        <g:render template="/email/header"/>
    </div>
    <div>
        <g:message
                code="email.requestReceived.message"
                args="[requisition.destination]"
        />
        %{--
             If there is a desired delivery date we want to add a comma at the end of this message, because there is a
             second part of the sentence. If there is no delivery date, we just finish the sentence after requestCreatedBy
             message. It cannot be included in HTML alone because there will be additional space around the punctuation mark.
         --}%
        <g:set var="punctuationMark" value="${requisition.dateDeliveryRequested ? ',' : '.'}" />
        <g:message
                code="email.requestCreatedBy.message"
                args="[
                        requisition.requestedBy,
                        punctuationMark,
                ]"
        />
        <g:if test="${requisition.dateDeliveryRequested}">
            <g:message
                    code="email.requestDesiredDateOfDelivery.message"
                    args="[requisition.dateDeliveryRequested?.format(Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)]"
            />
        </g:if>
        <g:set var="redirect" value="${g.createLink(uri: redirectUrl, absolute: true)}" />
        <g:message
                code="email.requestRedirect.message"
                args="[redirect]"
        />
    </div>
    &nbsp;
</div>
<div>
    <table>
        <thead style="text-align: left">
        <tr>
            <th>
                <g:message code="product.productCode.label" />
            </th>
            <th>
                <g:message code="product.title.label" />
            </th>
            <th>
                <g:message code="product.uom.label" />
            </th>
            <th>
                <g:message code="requisition.quantity.label" />
            </th>
        </tr>
        </thead>
        <g:if test="${requisition.requisitionItems}">
            <g:each var="item" in="${requisition.requisitionItems}">
                <tr>
                    <td>
                        ${item?.product?.productCode}
                    </td>
                    <td>
                        ${item?.product?.name}
                    </td>
                    <td>
                        ${item?.product?.unitOfMeasure}
                    </td>
                    <td style="text-align: center">
                        ${item?.quantity}
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>
