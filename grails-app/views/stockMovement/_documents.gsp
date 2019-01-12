<div id="documents-tab">

    <div class="box">
        <h2>
            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_stack.png')}" />
            <warehouse:message code="documents.label"/>
        </h2>
        <table class="zebra">
            <tr>
                <th></th>
                <th><g:message code="document.name.label"/></th>
                <th><g:message code="documentType.label"/></th>
                <th><g:message code="document.contentType.label"/></th>
                <th></th>
            </tr>
            <tbody>
                <g:each var="document" in="${stockMovement.documents}">
                    <g:if test ="${!document.hidden}">
                        <tr>
                            <td>
                                <g:set var="f" value="${document?.contentType}"/>
                                <g:if test="${f?.endsWith('jpg')||f?.endsWith('png')||f?.endsWith('gif') }">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'picture.png')}"/>
                                </g:if>
                                <g:elseif test="${f?.endsWith('pdf') }">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_acrobat.png')}"/>
                                </g:elseif>
                                <g:elseif test="${f?.endsWith('document')||f?.endsWith('msword') }">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}"/>
                                </g:elseif>
                                <g:elseif test="${f?.endsWith('excel')||f?.endsWith('sheet')||f?.endsWith('csv') }">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"/>
                                </g:elseif>
                                <g:elseif test="${f?.endsWith('html')}">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'html.png')}"/>
                                </g:elseif>
                                <g:elseif test="${f?.endsWith('gzip')||f?.endsWith('jar')||f?.endsWith('zip')||f?.endsWith('tar') }">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_compressed.png')}"/>
                                </g:elseif>
                                <g:else>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white.png')}"/>
                                </g:else>
                            </td>
                            <td>${document.name}</td>
                            <td>${document.documentType}</td>
                            <td>${document.contentType}</td>
                            <td>
                                <g:link url="${document.uri}" target="_blank" class="button">
                                    <warehouse:message code="default.button.download.label"/>
                                </g:link>
                            </td>
                        </tr>
                    </g:if>
                </g:each>
            </tbody>
        </table>
    </div>
</div>
