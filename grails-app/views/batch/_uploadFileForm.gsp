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
                        <g:radio name="type" value="inventory" checked="${params.type=='inventory'}"/>
                        <label><warehouse:message code="import.inventory.label" default="Inventory"/></label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'inventory.xls']">
                            <warehouse:message code="default.template.label" default="Download template"/>
                        </g:link>
                    </div>
                    <div>
                        <g:radio name="type" value="inventoryLevel" checked="${params.type=='inventoryLevel'}"/>
                        <label><warehouse:message code="import.inventoryLevel.label" default="Inventory levels"/></label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'inventoryLevel.xls']">
                            <warehouse:message code="default.template.label" default="Download template"/>
                        </g:link>
                    </div>
                    <div>
                        <g:radio name="type" value="productPrice" checked="${params.type=='productPrice'}" disabled="true"/>
                        <label><warehouse:message code="import.productPrice.label" default="Product pricing"/></label>
                        <warehouse:message code="default.comingSoon.label" default="Coming soon!"/>
                    </div>
                    <div>
                        <g:radio name="type" value="user" checked="${params.type=='user'}"/>
                        <label><warehouse:message code="default.users.label" default="Users"/></label>
                        <g:link controller="batch" action="downloadTemplate" params="[template:'users.xls']">
                            <warehouse:message code="default.template.label" default="Download template"/>
                        </g:link>
                    </div>
                    %{--<div class="chzn-container">--}%
                        %{--<g:select name="type" class="chzn-select-deselect"--}%
                                  %{--from="['product':'Product','inventory':'Inventory','inventoryLevel':'Inventory levels','productPrice':'Product price']"--}%
                                  %{--optionKey="${{it.key}}" optionValue="${{it.value}}" noSelection="['':'']" value="${params?.type}"></g:select>--}%
                    %{--</div>--}%
                </td>
            </tr>
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

