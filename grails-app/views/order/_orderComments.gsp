<div class="box">
    <h2><warehouse:message code="comments.label"/></h2>
    <g:if test="${orderInstance?.comments }">
        <table>
            <thead>
            <tr class="odd">
                <th><warehouse:message code="default.to.label" /></th>
                <th><warehouse:message code="default.from.label" /></th>
                <th><warehouse:message code="default.comment.label" /></th>
                <th><warehouse:message code="default.date.label" /></th>
                <th><warehouse:message code="default.actions.label" /></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="commentInstance" in="${orderInstance?.comments}">
                <tr>
                    <td>
                        ${commentInstance?.recipient?.name}
                    </td>
                    <td>
                        ${commentInstance?.sender?.name}
                    </td>
                    <td>
                        ${commentInstance?.comment}
                    </td>
                    <td>
                        ${commentInstance?.lastUpdated}
                    </td>
                    <td align="right">
                        <g:link action="editComment" id="${commentInstance.id}" params="['order.id':orderInstance?.id]">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                        </g:link>

                        <g:link action="deleteComment" id="${commentInstance.id}" params="['order.id':orderInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                            <img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noComments.label" /></div>
    </g:else>
</div>
