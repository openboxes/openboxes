<div class="box">
    <h2>
        <warehouse:message code="product.documents.label" default="Product documents"/>
    </h2>
        <table>
            <tr class="prop">
                <td class="name"><label><warehouse:message
                        code="product.uploadADocument.label" default="Upload document" /></label>
                </td>
                <td>
                    <g:uploadForm controller="document" action="uploadDocument">
                        <g:hiddenField name="productId" value="${productInstance?.id}" />
                        <g:hiddenField name="documentId" value="${documentInstance?.id}" />
                        <table>
                            <tr>
                                <td style="display: inline-block;">
                                    <label><warehouse:message code="default.name.label" /></label>
                                </td>
                                <td class="${hasErrors(bean: documentInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${documentInstance?.name}" class="text" size="100" />
                                </td>
                            </tr>
                            <tr>
                                <td style="display: inline-block;">
                                    <label><warehouse:message code="document.file.label" default="File"/></label>
                                </td>
                                <td class="${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
                                    <input name="fileContents" type="file" />
                                </td>
                            </tr>
                            <tr>
                                <td style="display: inline-block;">
                                    <label><warehouse:message code="document.url.label" default="URL" /></label>
                                </td>
                                <td class="${hasErrors(bean: documentInstance, field: 'fileUri', 'errors')}">
                                    <g:textField class="text" size="100" name="fileUri" value="${documentInstance?.fileUri}" />
                                </td>
                            </tr>
                        </table>
                        <div class="center" size="100">
                            <button type="submit" class="button">${warehouse.message(code:'default.button.upload.label')}</button>
                        </div>
                    </g:uploadForm>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="documents"><warehouse:message
                        code="documents.label" /></label></td>
                <td class="value">

                    <table id="documents" class="box">
                        <thead>
                        <tr>
                            <th>
                                <!-- Delete -->
                            </th>
                            <th>
                                <warehouse:message code="document.filename.label"/>
                            </th>
                            <th>
                                <warehouse:message code="document.contentType.label"/>
                            </th>
                            <th>
                                <warehouse:message code="default.dateCreated.label"/>
                            </th>
                            <th>
                                <warehouse:message code="default.lastUpdated.label"/>
                            </th>
                            <th>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="document" in="${productInstance?.documents.findAll { !it.fileUri } }" status="i">
                            <tr class="prop ${i%2?'even':'odd' }" >
                                <td>
                                    <g:link controller="product" action="downloadDocument" id="${document?.id}" params="['product.id':productInstance?.id]" target="_blank">
                                        <img src="${createLink(controller:'product', action:'viewThumbnail', id:document.id)}"
                                             class="middle" style="padding: 2px; margin: 2px; border: 1px solid lightgrey;" />
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="product" action="downloadDocument" id="${document?.id}" params="['product.id':productInstance?.id]" target="_blank">
                                        ${document.filename }
                                    </g:link>
                                </td>
                                <td>
                                    ${document.contentType }
                                </td>
                                <td>
                                    ${document.dateCreated }
                                </td>
                                <td>
                                    ${document.lastUpdated }
                                </td>
                                <td class="right">
                                    <g:link controller="product" action="downloadDocument" id="${document?.id}" params="['product.id':productInstance?.id]" target="_blank">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Download" />
                                    </g:link>
                                    <g:link controller="product" action="deleteDocument" id="${document?.id}" params="['product.id':productInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" />
                                    </g:link>

                                </td>
                            </tr>
                        </g:each>
                        <g:unless test="${productInstance?.documents.find { !it.fileUri } }">
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
                </td>
            </tr>
            <g:if test="${productInstance.documents.find { it.fileUri } }">
                <tr class="prop">
                    <td class="name"><label for="links"><warehouse:message
                            code="links.label" /></label></td>
                    <td class="value">
                        <table id="links" class="box">
                            <thead>
                            <tr>
                                <th><g:message code="default.name.label"/></th>
                                <th><warehouse:message code="document.url.label" default="URL" /></th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each var="document" in="${productInstance?.documents.findAll { it.fileUri } }" status="i">
                                <tr class="prop ${i%2?'even':'odd' }" >
                                    <td>
                                        <a href="${document.fileUri}" target="_blank">
                                            ${document.name}
                                        </a>
                                    </td>
                                    <td>
                                        <a href="${document.fileUri}" target="_blank">
                                            ${document.fileUri}
                                        </a>
                                    </td>
                                    <td class="right">
                                        <g:link controller="product" action="deleteDocument" id="${document?.id}" params="['product.id':productInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" />
                                        </g:link>
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </g:if>
        </table>
</div>
