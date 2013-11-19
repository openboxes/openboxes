<g:uploadForm controller="batch" action="importData">
    <table>
        <tbody>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="inventory.label"/></label>
                </td>
                <td class="value">
                    ${session?.warehouse?.name }
                    <g:hiddenField name="location.id" value="${session.warehouse.id }"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="default.type.label"/></label>
                </td>
                <td class="value">

                    <g:select name="type"
                              from="['product':'Product','inventory':'Inventory','inventoryLevel':'Inventory levels','productPrice':'Product price']"
                              optionKey="${{it.key}}" optionValue="${{it.value}}" noSelection="['':'']" value="${command?.type}"></g:select>

                    <%--
                    <div>
                        <g:radio name="type" value="product" checked="${params?.type=='product'}"/>
                        <warehouse:message code="import.product.label" default="Product"/>
                    </div>
                    <div>
                        <g:radio name="type" value="inventory" checked="${params?.type=='inventory'||!params?.type}"/>
                        <warehouse:message code="import.inventory.label" default="Inventory"/>
                    </div>
                    <div>
                        <g:radio name="type" value="inventoryLevel" checked="${params?.type=='inventoryLevel'}"/>
                        <warehouse:message code="import.inventoryLevel.label" default="Inventory level"/>
                    </div>
                    <div>
                        <g:radio name="type" value="productPrice" checked="${params?.type=='productPrice'}"/>
                        <warehouse:message code="import.productPrice.label" default="Product pricing"/>
                    </div>
                    --%>
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
                <td class="name"></td>
                <td class="value">
                    <button type="submit" class="button icon approve">
                        ${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}</button>
                </td>
            </tr>
        </tbody>
    </table>
</g:uploadForm>

