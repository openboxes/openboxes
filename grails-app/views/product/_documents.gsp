<div class="box">
    <h2>
        <warehouse:message code="product.documents.label" default="Product documents"/>
    </h2>
<!-- process an upload or save depending on whether we are adding a new doc or modifying a previous one -->
    <g:uploadForm controller="product" action="upload">
        <g:hiddenField name="product.id" value="${productInstance?.id}" />
        <g:hiddenField name="document.id" value="${documentInstance?.id}" />
        <table>
            <tr class="prop">
                <td class="name"><label><warehouse:message
                        code="document.selectUrl.label" default="Select URL" /></label>
                </td>
                <td class="value">
                    <g:textField name="url" value="${params.url }" placeholder="http://www.example.com/images/image.gif" class="text medium" size="80"/>
                    &nbsp;
                    <!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
                    <button type="submit" class="button icon approve">
                        ${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label><warehouse:message
                        code="document.selectFile.label" /></label>
                </td>
                <td class="value">
                    <input name="fileContents" type="file" />
                    &nbsp;
                    <!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
                    <button type="submit" class="button icon approve">
                        ${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
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
                        <g:each var="document" in="${productInstance?.documents }" status="i">
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
                                <td>
                                    <g:link controller="product" action="downloadDocument" id="${document?.id}" params="['product.id':productInstance?.id]" target="_blank">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Download" />
                                    </g:link>
                                    <g:link controller="product" action="deleteDocument" id="${document?.id}" params="['product.id':productInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" />
                                    </g:link>

                                </td>
                            </tr>
                        </g:each>
                        <g:unless test="${productInstance?.documents }">
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
        </table>
    </g:uploadForm>
</div>