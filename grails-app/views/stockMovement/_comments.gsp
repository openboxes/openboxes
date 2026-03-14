<section id="comments-tab" class="box" aria-label="Comments">
    <style>
        #comments-tab table td,
        #comments-tab table th {
            padding: 8px 10px;
            line-height: 1.4;
        }
    </style>
    <h2><warehouse:message code="comments.label"/></h2>
    <g:set var="comments" value="${stockMovement?.requisition?.comments ?: stockMovement?.shipment?.comments}" />
    <g:if test="${comments && !stockMovement.isReturn}">
        <table>
            <tr>
                <th><warehouse:message code="default.to.label" /></th>
                <th><warehouse:message code="default.from.label" /></th>
                <th><warehouse:message code="default.comment.label" /></th>
                <th><warehouse:message code="default.date.label" /></th>
                <th><warehouse:message code="default.actions.label" /></th>
            </tr>
            <g:each var="comment" in="${comments?.sort()}" status="status">
                <tr class="${status % 2 ? 'even' : 'odd'}">
                    <td nowrap="nowrap">
                        <g:if test="${comment?.recipient?.name}">
                            ${comment?.recipient?.name}
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label" default="None"/></span>
                        </g:else>
                    </td>
                    <td nowrap="nowrap">
                        <g:if test="${comment?.sender?.name}">
                            ${comment?.sender?.name}
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label" default="None"/></span>
                        </g:else>
                    </td>
                    <td class="left">
                        <g:if test="${comment?.comment}">
                            ${comment?.comment}
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.empty.label" default="Empty"/></span>
                        </g:else>
                    </td>
                    <td class="center" nowrap="nowrap">
                        ${comment?.lastUpdated}
                    </td>
                    <td align="right" nowrap="nowrap">
                        <g:if test="${comment?.sender?.id == session?.user?.id}">
                            <g:link action="editComment" id="${comment.id}" params="['stockMovementId': stockMovement?.id]">
                                <img tile="edit" src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                            </g:link>

                            <g:link
                                    action="deleteComment"
                                    id="${comment.id}"
                                    params="['stockMovementId': stockMovement?.id]"
                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                            >
                                <img  title="delete" src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noComments.label" /></div>
    </g:else>
</section>
