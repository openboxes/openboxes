<g:form controller="requisition" action="reassignPicker">
    <g:hiddenField name="id" value="${requisition?.id}"/>

    <div class="message">
        Select a user to reassign this order's picker to.
    </div>

    <table>
        <tr class="prop">
            <td class="name">
                <label>Reassign to</label>
            </td>
            <td class="value">
                <g:selectPerson id="reassignAssigneeId" name="assigneeId" noSelection="['null':'']" size="30" class="chzn-select-deselect"/>
            </td>
        </tr>
    </table>

    <div class="buttons">
        <button class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'user_add.png')}" />&nbsp;
            Reassign picker
        </button>
    </div>
</g:form>
