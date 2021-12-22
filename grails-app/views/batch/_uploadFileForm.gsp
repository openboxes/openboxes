<%@ page import="org.pih.warehouse.core.EntityTypeCode" %>
<g:uploadForm controller="batch" action="importData">
    <table>
        <tbody>
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
                    <g:jqueryDatePicker id="date" name="date" value="${new Date()}" placeholder="Only required for Inventory imports"/>
                </td>
            </tr>
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
                    <label><warehouse:message code="default.choose.label" default="Choose {0}" args="[g.message(code: 'default.type.label')]"/></label>
                </td>
                <td class="value">
                    <table style="width:auto">
                        <thead>
                        <tr>
                            <th><warehouse:message code="default.type.label" /></th>
                            <th><warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/></th>
                            <th><warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/> </th>
                        </tr>
                        </thead>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="category" checked="${params.type=='category'}"/>
                                    <warehouse:message code="import.category.label" default="Category"/>
                                </label>
                            </td>
                            <td>

                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'Category']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="inventory" checked="${params.type=='inventory'}"/>
                                    <warehouse:message code="inventory.label" default="Inventory"/>
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="inventory" action="downloadTemplate">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="inventoryLevel" checked="${params.type=='inventoryLevel'}"/>
                                    <warehouse:message code="inventoryLevel.label" default="Inventory levels"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'inventoryLevels.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="location" checked="${params.type=='location'}"/>
                                    <warehouse:message code="locations.label" default="Locations"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'locations.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'Location']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="person" checked="${params.type=='person'}"/>
                                    <warehouse:message code="persons.label" default="People"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'persons.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productAttribute" checked="${params.type=='productAttribute'}"/>
                                    ${g.message(code:'productAttribute.label')}
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="productAttributeValue" action="exportProductAttribute">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productCatalog" checked="${params.type=='productCatalog'}"/>
                                    ${g.message(code:'productCatalog.label')}
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'ProductCatalog']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productCatalogItem" checked="${params.type=='productCatalogItem'}"/>
                                    ${g.message(code:'productCatalogItem.label')}
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'ProductCatalogItem']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productSupplier" checked="${params.type=='productSupplier'}"/>
                                    ${g.message(code:'productSuppliers.label')}
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'ProductSuppliers.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'ProductSupplier']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productSupplierPreference" checked="${params.type=='productSupplierPreference'}"/>
                                    ${g.message(code:'productSupplier.productSourcePreference.label')}
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'ProductSupplierPreference']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productSupplierAttribute" checked="${params.type=='productSupplierAttribute'}"/>
                                    ${g.message(code:'productSupplier.productSourceAttribute.label')}
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="productAttributeValue" action="exportProductAttribute" params="[entityTypeCode: EntityTypeCode.PRODUCT_SUPPLIER]">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="productPackage" checked="${params.type=='productPackage'}"/>
                                    ${g.message(code:'productPackages.label')}
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'ProductPackage']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="outboundStockMovement" checked="${params.type=='outboundStockMovement'}"/>
                                    <warehouse:message code="import.outboundStockMovement.label" default="Stock Movements (Outbound)"/>
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                %{--Cannot implement export yet --}%
                                %{--<g:link controller="batch" action="downloadExcel" params="[type:'outboundStockMovement']">--}%
                                %{--    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>--}%
                                %{--</g:link>--}%
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="tag" checked="${params.type=='tag'}"/>
                                    <warehouse:message code="import.tag.label" default="Tag"/>
                                </label>
                            </td>
                            <td>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'Tag']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="user" checked="${params.type=='user'}"/>
                                    <warehouse:message code="users.label" default="Users"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'users.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="type" value="userLocation" checked="${params.type=='userLocation'}"/>
                                    <warehouse:message code="userLocations.label" default="User Locations"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'userLocations.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </tbody>
        <tfoot>
            <tr class="">
                <td class="name"></td>
                <td class="value">
                    <button type="submit" class="button">
                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" />&nbsp;
                        ${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}</button>
                </td>
            </tr>

        </tfoot>
    </table>
</g:uploadForm>

