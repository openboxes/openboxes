<g:uploadForm controller="batch" action="importData">
    <table>
        <tbody>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="inventory.uploadAFileToImport.label"/></label>
                </td>
                <td class="value">
                    <input name="xlsFile" type="file" />
                </td>
            </tr>

            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="default.type.label"/></label>
                </td>
                <td class="value">
                    <div>
                        <label>
                            <g:radio name="type" value="category" checked="${params.type=='category'}"/>
                            <warehouse:message code="import.category.label" default="Category"/>
                        </label>
                        <g:link controller="batch" action="downloadExcel" params="[type:'Category']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="inventory" checked="${params.type=='inventory'}"/>
                            <warehouse:message code="import.inventory.label" default="Inventory"/>
                        </label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'inventory.xls']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="inventoryLevel" checked="${params.type=='inventoryLevel'}"/>
                            <warehouse:message code="import.inventoryLevel.label" default="Inventory levels"/>
                        </label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'inventoryLevels.xls']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="location" checked="${params.type=='location'}"/>
                            <warehouse:message code="locations.label" default="Locations"/>
                        </label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'locations.xls']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="person" checked="${params.type=='person'}"/>
                            <warehouse:message code="persons.label" default="People"/>
                        </label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'persons.xls']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="productCatalog" checked="${params.type=='productCatalog'}"/>
                            ${g.message(code:'productCatalog.label')}
                        </label>
                        <g:link controller="batch" action="downloadExcel" params="[type:'ProductCatalog']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="productCatalogItem" checked="${params.type=='productCatalogItem'}"/>
                            ${g.message(code:'productCatalogItem.label')}
                        </label>
                        <g:link controller="batch" action="downloadExcel" params="[type:'ProductCatalogItem']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="productSupplier" checked="${params.type=='productSupplier'}"/>
                            ${g.message(code:'productSuppliers.label')}
                        </label>
                        <g:link controller="batch" action="downloadExcel" params="[type:'ProductSupplier']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="tag" checked="${params.type=='tag'}"/>
                            <warehouse:message code="import.tag.label" default="Tag"/>
                        </label>
                        <g:link controller="batch" action="downloadExcel" params="[type:'Tag']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="user" checked="${params.type=='user'}"/>
                            <warehouse:message code="users.label" default="Users"/>
                        </label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'users.xls']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                    <div>
                        <label>
                            <g:radio name="type" value="userLocation" checked="${params.type=='userLocation'}"/>
                            <warehouse:message code="userLocations.label" default="User Locations"/>
                        </label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'userLocations.xls']">
                            <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                        </g:link>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="location.label"/></label>
                </td>
                <td class="value">
                    ${session?.warehouse?.name }
                    <g:hiddenField name="location.id" value="${session.warehouse.id }"/>
                </td>
            </tr>

            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="default.date.label"/></label>
                </td>
                <td class="value">
                    <g:jqueryDatePicker id="date" name="date" value="${new Date()}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"></td>
                <td class="value">
                    <button type="submit" class="button icon approve">
                        ${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}</button>
                </td>
            </tr>
        </tbody>
    </table>
</g:uploadForm>

