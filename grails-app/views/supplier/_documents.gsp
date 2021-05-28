<div>
    <g:if test="${documents?.findAll { !it?.fileUri } }">
        <div class="box">
            <h2><warehouse:message code="documents.label"/></h2>
            <table>
                <thead>
                <tr class="odd">
                    <th><warehouse:message code="purchaseOrder.orderNumber.label" default="PO Number"/></th>
                    <th><warehouse:message code="purchaseOrder.description.label" default="PO Description"/></th>
                    <th><warehouse:message code="default.origin.label"/></th>
                    <th><warehouse:message code="default.destination.label"/></th>
                    <th><warehouse:message code="document.type.label"/></th>
                    <th><warehouse:message code="document.name.label"/></th>
                    <th><warehouse:message code="document.contentType.label"/></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <g:each var="documentInstance" in="${documents?.findAll { !it?.fileUri }}">
                    <tr>
                        <td>${documentInstance?.orderNumber}</td>
                        <td>${documentInstance?.orderDescription}</td>
                        <td>${documentInstance?.origin}</td>
                        <td>${documentInstance?.destination}</td>
                        <td><format:metadata obj="${documentInstance?.documentType}"/></td>
                        <td>${documentInstance?.documentName}</td>
                        <td>${documentInstance?.fileType}</td>
                        <td><g:link controller="document" action="download" id="${documentInstance?.documentId}">download</g:link></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    <g:if test="${documents?.findAll { it?.fileUri } }">
        <div class="box">
            <h2><warehouse:message code="links.label" default="Links"/></h2>
            <table>
                <thead>
                <tr class="odd">
                    <th><g:message code="default.name.label"/></th>
                    <th><warehouse:message code="document.url.label" default="URL"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each var="documentInstance" in="${documents?.findAll { it?.fileUri }}">
                    <tr>
                        <td>
                            <a href="${documentInstance?.fileUri}" target="_blank">
                                ${documentInstance?.documentName}
                            </a>
                        </td>
                        <td style="width: 80%;">
                            <a href="${documentInstance?.fileUri}" target="_blank" style="word-break:break-all;">
                                ${documentInstance?.fileUri}
                            </a>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    <g:if test="${!documents}">
        <div class="fade center empty"><warehouse:message code="default.noDocuments.label" /></div>
    </g:if>
</div>
