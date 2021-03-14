<div>
    <div class="box">
        <h2>
            <warehouse:message code="documents.label"/>
        </h2>
        <table class="zebra">
            <thead>
                <th><g:message code="document.name.label"/></th>
                <th><g:message code="documentType.label"/></th>
                <th><g:message code="document.contentType.label"/></th>
                <th></th>
            </thead>
            <tbody>
                <g:each var="document" in="${productInstance.documents.findAll { !it.fileUri } }">
                    <tr>
                        <td>${document?.filename} (<g:link controller="document" action="download" id="${document.id}">download</g:link>)</td>
                        <td><format:metadata obj="${document?.documentType}"/></td>
                        <td>${document.contentType}</td>
                        <td class="right">
                            <g:if test="${document.id}">
                                <g:link controller="product" action="deleteDocument" id="${document.id}" params="['product.id':productInstance?.id]"
                                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                        class="button">
                                    <warehouse:message code="default.button.delete.label"/>
                                </g:link>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                <g:unless test="${productInstance?.documents.findAll { !it.fileUri } }">
                    <tr>
                        <td colspan="6">
                            <div class="padded fade center">
                                <warehouse:message code="product.hasNoDocuments.message"/>
                            </div>
                        </td>
                    </tr>
                </g:unless>
            </tbody>
        </table>
    </div>
    <g:if test="${productInstance.documents.find { it.fileUri } }">
        <div class="box">
            <h2>
                <warehouse:message code="links.label" default="Links"/>
            </h2>
            <table class="zebra">
                <tr>
                    <th><g:message code="default.name.label"/></th>
                    <th><warehouse:message code="document.url.label" default="URL" /></th>
                    <th></th>
                </tr>
                <tbody>
                <g:each var="document" in="${productInstance.documents.findAll { it.fileUri } }">
                        <tr>
                            <td>
                                <a href="${document.fileUri}" target="_blank">
                                    ${document.name}
                                </a>
                            </td>
                            <td style="width: 80%;">
                                <a href="${document.fileUri}" target="_blank" style="word-break:break-all;">
                                    ${document.fileUri}
                                </a>
                            </td>
                            <td class="right">
                                <g:if test="${document.id}">
                                    <g:link controller="product" action="deleteDocument" id="${document.id}" params="['product.id':productInstance?.id]"
                                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                            class="button">
                                        <warehouse:message code="default.button.delete.label"/>
                                    </g:link>
                                </g:if>
                            </td>
                        </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
</div>
