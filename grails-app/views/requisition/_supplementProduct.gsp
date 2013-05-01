<div class="box">
    <h2>${warehouse.message(code:'requisitionItem.addAddition.label') }</h2>
    <div class="dialog">
        <g:form controller="requisition" action="addAddition" fragment="${selectedRequisitionItem?.id}">
            <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
            <table>
                <tr>
                    <td>
                        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                       width="200" valueId="" valueName="" styleClass="text"/>
                    </td>
                    <td>
                        <g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                    </td>
                    <td>
                        <button class="button">
                            ${warehouse.message(code:'default.button.save.label') }
                        </button>
                    </td>
                    <td>
                        <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}">
                            ${warehouse.message(code:'default.button.cancel.label') }
                        </g:link>
                    </td>
                </tr>

            </table>

        </g:form>
    </div>
</div>