<g:form controller="unitOfMeasureClass" action="save" method="post">
    <table>
        <tr class="prop">
            <td class="name">
                <label for="name">Name</label>
            </td>
            <td class="value ">
                <g:textField name="name" size="10" class="medium text" placeholder="e.g. Quantity, Area, Currency, Volume"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label for="code">Type</label>
            </td>
            <td class="value ">
                <g:select name="type" from="${org.pih.warehouse.core.UnitOfMeasureType.list() }"
                          value="${packageInstance?.uom }" noSelection="['null':'']" class="chzn-select-deselect"></g:select>
                <span class="fade"></span>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="code">Code</label>
            </td>
            <td class="value ">
                <g:textField name="code" size="10" class="medium text" placeholder="e.g. QTY"/>
                <span class="fade"></span>
            </td>
        </tr>
        <tfoot>
        <tr>
            <td></td>
            <td>
                <div>
                    <button type="submit" name="create" class="button icon approve" value="Create" id="create">${warehouse.message(code: 'default.button.create.label', default: 'Create')}</button>
                    <a href="#" class="btn-close-dialog button icon remove" data-target="uom-class-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
                </div>
            </td>
        </tr>
        </tfoot>

    </table>

</g:form>
