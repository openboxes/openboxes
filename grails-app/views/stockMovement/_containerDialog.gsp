<div id="packageType" class="box">
    <g:form name="stockMovement" action="saveContainer">
        <g:if test="${container}">
            <g:hiddenField name="container.id" value="${container?.id}"/>
        </g:if>
        <g:hiddenField name="shipment.id" value="${stockMovement?.shipment?.id}"/>
        <g:hiddenField name="containerType.id" value="${container?.containerType?.id}"/>
        <g:hiddenField name="stockMovement.id" value="${stockMovement?.id}"/>
        <table>
            <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="container.containerType.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        ${container?.containerType?.name}
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.name.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField id="name" name="name" size="50" class="text large" value="${container?.name}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="container.containerNumber.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField id="containerNumber" name="containerNumber" size="50" class="text large" value="${container?.containerNumber}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.weight.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField id="weight" name="weight" size="6" class="text large" value="${container?.weight}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.weightUnits.label" default="Weight Uom"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:select name="weightUnits" from="${org.pih.warehouse.core.Constants.WEIGHT_UNITS}" value="${container?.weightUnits}"
                                  class="chzn-select-deselect"/>
                    </td>
                </tr>



                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.length.label" default="Length"/></label>
                    </td>
                    <td class="value bottom">
                        <g:textField id="length" name="length" size="5" value="${container?.length}" class="text large" placeholder="Length"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.width.label" default="Width"/></label>
                    </td>
                    <td class="value bottom">
                        <g:textField id="width" name="width" size="5" value="${container?.width}" class="text large" placeholder="Width"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.height.label" default="Height"/></label>
                    </td>
                    <td class="value bottom">
                        <g:textField id="height" name="height" size="5" value="${container?.height}" class="text large" placeholder="Height"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="container.volumeUnits.label" default="Volume Uom"/></label>
                    </td>
                    <td class="value bottom">
                        <g:select name="volumeUnits" from="${org.pih.warehouse.core.Constants.VOLUME_UNITS}" value="${container?.volumeUnits}"
                            class="chzn-select-deselect"/>
                    </td>
                </tr>
            </tbody>
            <tfoot>
                <tr>
                    <td></td>
                    <td>
                        <div class="buttons left">
                            <g:actionSubmit action="saveContainer" class="button icon approve" value="Save" id="save">
                                ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                            </g:actionSubmit>
                            <button name="cancelDialog" type="reset" onclick="$('#dlgShowDialog').dialog('close');" class="button"><warehouse:message code="default.button.cancel.label"/></button>
                        </div>
                    </td>
                </tr>
            </tfoot>
        </table>
    </g:form>
</div>
