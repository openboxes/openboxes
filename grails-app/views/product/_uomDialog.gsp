<g:form controller="unitOfMeasure" action="save" method="post">
    <table>
        <tr class="prop">
            <td class="name">
                <label for="uomClass.id">Uom Class</label>
            </td>
            <td class="value ">
                <g:select name="uomClass.id" from="${org.pih.warehouse.core.UnitOfMeasureClass.list() }"
                          optionValue="name" optionKey="id" value="${pacakageInstance?.uom }" noSelection="['null':'']" class="chzn-select-deselect"></g:select>
                <span class="linkButton">
                    <a href="javascript:void(0);" class="open-dialog create" dialog-id="uom-class-dialog">Add UoM Class</a>
                </span>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label for="code">Code</label>
            </td>
            <td class="value ">
                <g:textField name="code" size="40" class="medium text" placeholder="e.g. BX, CS, EA"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label for="name">Name</label>
            </td>
            <td class="value ">
                <g:textField name="name" size="40" class="medium text" placeholder="e.g. Box, Case, Each" />
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="description">Description</label>
            </td>
            <td class="value ">
                <g:textField name="description" size="40" class="medium text" placeholder="Describe the unit of measure" />
            </td>
        </tr>
        <tfoot>
        <tr>
            <td></td>
            <td>
                <div>
                    <button type="submit" name="create" class="button icon approve" value="Create" id="create">${warehouse.message(code: 'default.button.create.label', default: 'Create')}</button>
                    <a href="#" class="btn-close-dialog" data-target="uom-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
                </div>
            </td>
        </tr>
        </tfoot>
    </table>

</g:form>
