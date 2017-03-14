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
                    <%--
                    <div>
                        <g:radio name="type" value="product" checked="${params.type=='product'}"/>
                        <label><warehouse:message code="import.product.label" default="Products"/></label>
                        <g:link controller="batch" action="downloadTemplate" params="[type:'product']">
                            <warehouse:message code="default.template.label" default="Download template"/>
                        </g:link>
                    </div>
                    --%>
                    <div>
                        <g:radio name="type" id="inventory" value="inventory" checked="${params.type=='inventory'}"/>
                        <label for="inventory"><warehouse:message code="import.inventory.label" default="Inventory"/></label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'inventory.xls']">
                            <warehouse:message code="default.template.label" default="Download template"/>
                        </g:link>
                    </div>
                    <div>
                        <g:radio name="type" id="inventoryLevel" value="inventoryLevel" checked="${params.type=='inventoryLevel'}"/>
                        <label for="inventoryLevel"><warehouse:message code="import.inventoryLevel.label" default="Inventory levels"/></label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'inventoryLevel.xls']">
                            <warehouse:message code="default.template.label" default="Download template"/>
                        </g:link>
                    </div>
                    <%--
                    <div>
                        <g:radio name="type" value="productPrice" checked="${params.type=='productPrice'}" disabled="true"/>
                        <label><warehouse:message code="import.productPrice.label" default="Product pricing"/></label>
                        <warehouse:message code="default.comingSoon.label" default="Coming soon!"/>
                    </div>
                    <div>
                        <g:radio name="type" value="product" checked="${params.type=='shipment'}" disabled="true"/>
                        <label><warehouse:message code="import.shipment.label" default="Shipments"/></label>
                        <warehouse:message code="default.comingSoon.label" default="Coming soon!"/>
                    </div>
                    <div>
                        <g:radio name="type" value="product" checked="${params.type=='purchaseOrder'}" disabled="true"/>
                        <label><warehouse:message code="import.purchaseOrder.label" default="Purchase orders"/></label>
                        <warehouse:message code="default.comingSoon.label" default="Coming soon!"/>
                    </div>
                    --%>
                    <%--
                    <div class="chzn-container">
                        <g:select name="type" class="chzn-select-deselect"
                                  from="['product':'Product','inventory':'Inventory','inventoryLevel':'Inventory levels','productPrice':'Product price']"
                                  optionKey="${{it.key}}" optionValue="${{it.value}}" noSelection="['':'']" value="${params?.type}"></g:select>
                    </div>
                    --%>
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
                    <div>
                        <g:jqueryDatePicker id="date" name="date" class="datepicker" value="${new Date()}" placeholder=""/>

                    </div>
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

