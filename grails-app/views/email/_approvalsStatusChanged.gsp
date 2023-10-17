<%@ page contentType="text/html"%>
<style>
    label {
        font-weight: bold;
    }
</style>
<div>
    <g:set var="event" value="${requisition.mostRecentEvent}" />
    <g:set var="status" value="${event.eventType}" />
    <g:set var="comment" value="${event.comment}" />
    <div class="header">
        <g:render template="/email/header"/>
    </div>
    <div>
        <g:message code="email.statusChange.message" args="[format.metadata(obj:status), g.createLink(uri: '/', absolute: true)]" />
    </div>
    <div>
        <g:message code="email.withComment.message" />:
        <g:if test="${comment}">
            "${comment.sender.name}, ${g.formatDate(date:comment.dateCreated)}: “${comment.comment}”"
        </g:if>
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
