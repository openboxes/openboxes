<g:form action="update" method="post">
    <g:hiddenField name="action" value="save"/>
    <g:hiddenField name="id" value="${productInstance?.id}" />
    <g:hiddenField name="version" value="${productInstance?.version}" />
    <div class="box dialog" >
        <h2>
            <warehouse:message code="product.attributes.label" default="Product attributes"/>
        </h2>

        <g:set var="attributes" value="${org.pih.warehouse.product.Attribute.list()}"/>
        <g:unless test="${attributes}">
            <div class="empty center">
                There are no attributes
            </div>

        </g:unless>

        <table>
            <tbody>
            <g:each var="attribute" in="${attributes}" status="status">
                <tr class="prop">
                    <td class="name">
                        <label for="productAttributes.${attribute?.id}.value"><format:metadata obj="${attribute}"/></label>
                    </td>
                    <td class="value">
                        <g:set var="attributeFound" value="f"/>
                        <g:if test="${attribute.options}">
                            <select name="productAttributes.${attribute?.id}.value" class="attributeValueSelector chzn-select-deselect">
                                <option value=""></option>
                                <g:each var="option" in="${attribute.options}" status="optionStatus">
                                    <g:set var="selectedText" value=""/>
                                    <g:if test="${productInstance?.attributes[status]?.value == option}">
                                        <g:set var="selectedText" value=" selected"/>
                                        <g:set var="attributeFound" value="t"/>
                                    </g:if>
                                    <option value="${option}"${selectedText}>${option}</option>
                                </g:each>
                                <g:set var="otherAttVal" value="${productInstance?.attributes[status]?.value != null && attributeFound == 'f'}"/>
                                <g:if test="${attribute.allowOther || otherAttVal}">
                                    <option value="_other"<g:if test="${otherAttVal}"> selected</g:if>>
                                        <g:message code="product.attribute.value.other" default="Other..." />
                                    </option>
                                </g:if>
                            </select>
                        </g:if>
                        <g:set var="onlyOtherVal" value="${attribute.options.isEmpty() && attribute.allowOther}"/>
                        <g:textField class="otherAttributeValue text medium"
                                     size="100"
                                     style="${otherAttVal || onlyOtherVal ? '' : 'display:none;'}"
                                     name="productAttributes.${attribute?.id}.otherValue"
                                     value="${otherAttVal || onlyOtherVal ? productInstance?.attributes[status]?.value : ''}"/>
                    </td>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="2">

                    <div class="right">
                        <g:link controller="attribute" action="list" class="button"><g:message code="attributes.label"/></g:link>
                    </div>
                    <div class="center">
                        <button type="submit" class="button icon approve">
                            ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                        </button>
                    </div>
                </td>
            </tr>
            </tfoot>
        </table>
    </div>
</g:form>