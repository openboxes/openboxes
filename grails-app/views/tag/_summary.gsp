<g:if test="${tagInstance}">
    <div class="summary">
        <table style="width:100%;">
            <tr>
                <td class="middle">
                    <span class="title">
                        <g:if test="${tagInstance?.id}">
                            ${fieldValue(bean: tagInstance, field: "tag")}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.create.label', args: [g.message(code: 'tag.label')])}
                        </g:else>
                    </span>
                </td>
                <td>
                    <div class="tag ${tagInstance?.id && tagInstance?.isActive ? 'tag-success' : 'tag-danger'} right">
                        ${tagInstance?.id && tagInstance?.isActive ? g.message(code:'default.active.label') : g.message(code:'default.inactive.label')}
                    </div>
                </td>
            </tr>
        </table>
    </div>
</g:if>

<div class="button-bar">
    <g:link class="button" action="list" controller="tag">
        <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
        <warehouse:message code="default.list.label" args="[g.message(code: 'tags.label')]" />
    </g:link>
    <g:link class="button" action="create" controller="tag">
        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
        <warehouse:message code="default.create.label" args="[g.message(code: 'tag.label')]" />
    </g:link>
    <g:if test="${tagInstance?.id && tagInstance?.products?.size() > 0}">
        <div class="right">
            <g:link class="button right" controller="productApi" action="list" params="[tagId: tagInstance?.id, format: 'csv', fileName: tagInstance?.tag]">
                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                <g:message code="default.export.label" args="[g.message(code: 'products.label')]"/>
                (${tagInstance?.products?.size()})
            </g:link>
        </div>
    </g:if>

</div>
