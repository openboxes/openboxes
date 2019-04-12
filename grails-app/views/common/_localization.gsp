<div id="localization-dialog" class="dialog" style="display: none;" title="Edit Translation">
    <div id="localization-form">
        <g:form controller="localization" action="save">
            <table>
                <tbody>
                <tr class="prop" data-bind="if: id">
                    <td class="name">
                        <label><warehouse:message code="default.id.label"/></label>
                    </td>
                    <td class="value">
                        <span data-bind="text: id"></span>
                        <input type="hidden" data-bind="value: id"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="default.locale.label"/></label>
                    </td>
                    <td class="prop">
                        <input type="hidden" data-bind="value: locale"/>
                        <span data-bind="text: locale"></span>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="default.code.label"/></label>
                    </td>
                    <td class="prop">
                        <input type="hidden" data-bind="value: code"/>
                        <span data-bind="text: code"></span>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="default.text.label"/></label>
                    </td>
                    <td class="prop">
                        <span data-bind="text: text"></span>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="default.translation.label"/></label>
                    </td>
                    <td class="prop">
                        <textarea cols="60" rows="6" data-bind="value: translation"></textarea>
                    </td>
                </tr>

                </tbody>
                <tfoot>
                    <tr>
                        <td></td>
                        <td>
                            <button id="save-localization-btn" class="button">Save</button>
                            <button id="delete-localization-btn" class="button danger" data-bind="visible: id">Delete</button>
                            <button id="close-localization-dialog-btn" class="button">Cancel</button>
                        </td>
                    </tr>
                </tfoot>
            </table>


        </g:form>
    </div>
</div>
