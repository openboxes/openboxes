<g:form controller="productSupplier" method="post">

    <g:hiddenField name="id" value="${productSupplier?.id}"></g:hiddenField>
    <g:hiddenField name="product.id" value="${productSupplier?.product?.id}"></g:hiddenField>
    <g:hiddenField name="dialog" value="true"></g:hiddenField>

    <div style="max-height: 400px; overflow: auto">
        <table>
            <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="product"><warehouse:message code="product.label"/></label>
                    </td>
                    <td class="value ">
                        <div id="product">
                            ${productSupplier?.product?.productCode}
                            <format:product product="${productSupplier?.product}"/>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="code"><warehouse:message code="productSupplier.code.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="code" size="80" class="medium text"
                                     value="${productSupplier?.code}"
                                     placeholder="${g.message(code:'productSupplier.code.placeholder')}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="name"><warehouse:message code="default.name.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="name" size="80" class="medium text"
                                     value="${productSupplier?.name}" />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="name"><warehouse:message code="default.description.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textArea name="description" class="medium text"
                                    value="${productSupplier?.description}" />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="supplier"><warehouse:message code="productSupplier.supplier.label"/></label>
                    </td>
                    <td class="value ">
                        <g:selectOrganization name="supplier"
                                              noSelection="['':'']"
                                              roleTypes="[org.pih.warehouse.core.RoleType.ROLE_SUPPLIER]"
                                              value="${productSupplier?.supplier?.id}"
                                              class="chzn-select-deselect"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="supplierCode"><warehouse:message code="productSupplier.supplierCode.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="supplierCode" size="80" class="medium text"
                                     value="${productSupplier?.supplierCode}" />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="manufacturer"><warehouse:message code="productSupplier.manufacturer.label"/></label>
                    </td>
                    <td class="value ">
                        <g:selectOrganization name="manufacturer"
                                              noSelection="['':'']"
                                              roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                              value="${productSupplier?.manufacturer?.id}"
                                              class="chzn-select-deselect"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="manufacturerCode"><warehouse:message code="productSupplier.manufacturerCode.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="manufacturerCode" size="80" class="medium text"
                                     value="${productSupplier?.manufacturerCode}" />
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="brandName"><warehouse:message code="product.brandName.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="brandName" size="80" class="medium text"
                                     value="${productSupplier?.brandName}" />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="preferenceTypeCode"><warehouse:message code="productSupplier.preferenceTypeCode.label"/></label>
                    </td>
                    <td class="value ">
                        <g:selectPreferenceType name="preferenceTypeCode"
                                                noSelection="['':'']"
                                                value="${productSupplier?.preferenceTypeCode}"
                                                class="chzn-select-deselect"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="ratingTypeCode"><warehouse:message code="productSupplier.ratingTypeCode.label"/></label>
                    </td>
                    <td class="value ">
                        <g:selectRatingType name="ratingTypeCode"
                                            noSelection="['':'']"
                                            value="${productSupplier?.ratingTypeCode}"
                                            class="chzn-select-deselect"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="productCode"><warehouse:message code="productSupplier.productCode.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="productCode" size="80" class="medium text"
                                     value="${productSupplier?.productCode}" />
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="minOrderQuantity"><warehouse:message code="productSupplier.minOrderQuantity.label"/></label>
                    </td>
                    <td class="value ">
                        <g:textField name="minOrderQuantity" size="80" class="medium text"
                                     value="${productSupplier?.minOrderQuantity}" />
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:if test="${productSupplier.id}">
            <g:actionSubmit action="update" class="button icon accept" value="Save" id="update">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</g:actionSubmit>
            <g:actionSubmit action="delete" class="button icon trash" value="Delete" id="delete">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</g:actionSubmit>
        </g:if>
        <g:else>
            <g:actionSubmit action="save" class="button icon approve" value="Save" id="save">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</g:actionSubmit>
        </g:else>
        &nbsp;
        <button name="cancelDialog" class="btn-close-dialog button"><warehouse:message code="default.button.cancel.label"/></button>
    </div>
</g:form>
