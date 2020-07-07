<div class="box">
    <h2><warehouse:message code="documents.label"/></h2>
    <g:if test="${orderInstance?.documents }">
        <table>
            <thead>
            <tr class="odd">
                <th><warehouse:message code="document.filename.label" /></th>
                <th><warehouse:message code="document.type.label" /></th>
                <th><warehouse:message code="default.description.label" /></th>
                <th><warehouse:message code="document.size.label" /></th>
                <th><warehouse:message code="default.lastUpdated.label" /></th>
                <th><warehouse:message code="default.actions.label" /></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="documentInstance" in="${orderInstance?.documents}">
                <tr>
                    <td>${documentInstance?.filename} (<g:link controller="document" action="download" id="${documentInstance.id}">download</g:link>)</td>
                    <td><format:metadata obj="${documentInstance?.documentType}"/></td>
                    <td>${documentInstance?.name}</td>
                    <td>${documentInstance?.size} bytes</td>
                    <td>${documentInstance?.lastUpdated}</td>
                    <td align="right">
                        <g:link action="editDocument" id="${documentInstance.id}" params="['order.id':orderInstance?.id]">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                        </g:link>

                        <g:link action="deleteDocument" id="${documentInstance.id}" params="['order.id':orderInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                            <img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noDocuments.label" /></div>
    </g:else>
</div>
