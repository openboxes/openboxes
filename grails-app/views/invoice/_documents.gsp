<div>
    <g:if test="${(invoiceInstance?.orderDocuments + invoiceInstance?.documents).findAll { !it.fileUri }}">
        <div class="box">
            <h2><warehouse:message code="documents.label"/></h2>
            <table>
                <thead>
                <tr class="odd">
                    <th><warehouse:message code="document.filename.label" /></th>
                    <th><warehouse:message code="document.type.label" /></th>
                    <th><warehouse:message code="default.description.label" /></th>
                    <th><warehouse:message code="document.size.label" /></th>
                    <th><warehouse:message code="default.lastUpdated.label" /></th>
                    <th class="right"><warehouse:message code="default.actions.label" /></th>
                </tr>
                </thead>
                <tbody>
                <g:each var="documentInstance" in="${invoiceInstance?.documents?.findAll { !it.fileUri }}">
                    <tr>
                        <td>
                            ${documentInstance?.filename}
                            (<g:link controller="document" action="download" id="${documentInstance.id}">download</g:link>)
                            (<g:link controller="invoice" action="downloadTemplateDocument" params="['invoice.id': invoiceInstance?.id, 'document.id': documentInstance?.id]">download Template</g:link>)
                        </td>
                        <td><format:metadata obj="${documentInstance?.documentType}"/></td>
                        <td>${documentInstance?.name}</td>
                        <td>${documentInstance?.size} bytes</td>
                        <td>${documentInstance?.lastUpdated}</td>
                        <td class="right" align="right">
                            <g:link action="editDocument" id="${documentInstance.id}" params="['invoice.id':invoiceInstance?.id]">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                            </g:link>
                            <g:link action="deleteDocument" id="${documentInstance.id}" params="['invoice.id':invoiceInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                <g:each var="documentInstance" in="${invoiceInstance?.orderDocuments?.findAll { !it.fileUri }}">
                    <tr>
                        <td>${documentInstance?.filename} (<g:link controller="document" action="download" id="${documentInstance.id}">download</g:link>)</td>
                        <td><format:metadata obj="${documentInstance?.documentType}"/></td>
                        <td>${documentInstance?.name}</td>
                        <td>${documentInstance?.size} bytes</td>
                        <td>${documentInstance?.lastUpdated}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    <g:if test="${(invoiceInstance?.orderDocuments + invoiceInstance?.documents).findAll { it.fileUri }}">
        <div class="box">
            <h2><warehouse:message code="links.label" default="Links"/></h2>
            <table>
                <thead>
                <tr class="odd">
                    <th><g:message code="default.name.label"/></th>
                    <th><warehouse:message code="document.url.label" default="URL" /></th>
                    <th class="right"><warehouse:message code="default.actions.label" /></th>
                </tr>
                </thead>
                <tbody>
                <g:each var="documentInstance" in="${invoiceInstance?.documents?.findAll { it.fileUri }}">
                    <tr>
                        <td>
                            <a href="${documentInstance.fileUri}" target="_blank">
                                ${documentInstance.name}
                            </a>
                        </td>
                        <td style="width: 80%;">
                            <a href="${documentInstance.fileUri}" target="_blank" style="word-break:break-all;">
                                ${documentInstance.fileUri}
                            </a>
                        </td>
                        <td class="right">
                            <g:link action="editDocument" id="${documentInstance.id}" params="['invoice.id':invoiceInstance?.id]">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                            </g:link>

                            <g:link action="deleteDocument" id="${documentInstance.id}" params="['invoice.id':invoiceInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                <g:each var="documentInstance" in="${invoiceInstance?.orderDocuments?.findAll { it.fileUri }}">
                    <tr>
                        <td>
                            <a href="${documentInstance.fileUri}" target="_blank">
                                ${documentInstance.name}
                            </a>
                        </td>
                        <td style="width: 80%;">
                            <a href="${documentInstance.fileUri}" target="_blank" style="word-break:break-all;">
                                ${documentInstance.fileUri}
                            </a>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    <g:if test="${!(invoiceInstance?.orderDocuments + invoiceInstance?.documents)}">
        <div class="fade center empty"><warehouse:message code="default.noDocuments.label" /></div>
    </g:if>
</div>
