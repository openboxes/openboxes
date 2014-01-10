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

