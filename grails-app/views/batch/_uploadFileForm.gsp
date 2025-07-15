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
                    <g:datePicker name="date" value="none" precision="minute" relativeYears="[-20..0]" noSelection="['':'']"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="inventory.uploadAFileToImport.label"/></label>
                </td>
                <td class="value">
                    <input name="importFile" type="file" />
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
                                    <g:radio name="importType" value="category" checked="${params.importType=='category'}"/>
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
                                    <g:radio name="importType" value="inventory" checked="${params.importType=='inventory'}"/>
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
                                    <g:radio name="importType" value="inventoryLevel" checked="${params.importType=='inventoryLevel'}"/>
                                    <warehouse:message code="inventoryLevel.label" default="Inventory levels"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'inventoryLevels.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'InventoryLevel']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="importType" value="location" checked="${params.importType=='location'}"/>
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
                                    <g:radio name="importType" value="person" checked="${params.importType=='person'}"/>
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
                                    <g:radio name="importType" value="productAttribute" checked="${params.importType=='productAttribute'}"/>
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
                                    <g:radio name="importType" value="productCatalog" checked="${params.importType=='productCatalog'}"/>
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
                                    <g:radio name="importType" value="productCatalogItem" checked="${params.importType=='productCatalogItem'}"/>
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
                                    <g:radio name="importType" value="productSupplier" checked="${params.importType=='productSupplier'}"/>
                                    ${g.message(code:'productSuppliers.label')}
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'ProductSuppliers.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                                 <g:link controller="productSupplier" action="export" params="[format:'xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="importType" value="productSupplierPreference" checked="${params.importType=='productSupplierPreference'}"/>
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
                                    <g:radio name="importType" value="productSupplierAttribute" checked="${params.importType=='productSupplierAttribute'}"/>
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
                                    <g:radio name="importType" value="productPackage" checked="${params.importType=='productPackage'}"/>
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
                                    <g:radio name="importType" value="productAssociation" checked="${params.importType=='productAssociation'}"/>
                                    <warehouse:message code="productAssociations.label" default="Product Associations"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'productAssociations.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'ProductAssociation']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="importType" value="productSynonym" checked="${params.importType=='productSynonym'}"/>
                                    <warehouse:message code="synonym.productSynonyms.label" default="Product Synonyms"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="product" action="exportSynonymTemplate">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadExcel" params="[type:'Synonym']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.data.label')]"/>
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="importType" value="outboundStockMovement" checked="${params.importType=='outboundStockMovement'}"/>
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
                                    <g:radio name="importType" value="purchaseOrderActualReadyDate" checked="${params.importType=='purchaseOrderActualReadyDate'}"/>
                                    <warehouse:message code="import.purchaseOrderActualReadyDate.label" default="PO Actual Ready Date"/>
                                </label>
                            </td>
                            <td>
                                <g:link controller="batch" action="downloadTemplate" params="[template:'purchaseOrderActualReadyDate.xls']">
                                    <warehouse:message code="default.download.label" args="[g.message(code:'default.template.label')]"/>
                                </g:link>
                            </td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label>
                                    <g:radio name="importType" value="tag" checked="${params.importType=='tag'}"/>
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
                                    <g:radio name="importType" value="user" checked="${params.importType=='user'}"/>
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
                                    <g:radio name="importType" value="userLocation" checked="${params.importType=='userLocation'}"/>
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
                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />&nbsp;
                        ${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}</button>
                </td>
            </tr>

        </tfoot>
    </table>
</g:uploadForm>

