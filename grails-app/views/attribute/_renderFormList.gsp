<g:set var="showUnlinkedAttributes" value="${showUnlinkedAttributes?:false}"/>
<g:set var="availableAttributes" value="${org.pih.warehouse.product.Attribute.findAllByActive(true)}"/>
<g:set var="availableAttributes" value="${availableAttributes.findAll {((it.entityTypeCodes.any { it in entityTypeCodes} ) ||
        (it.entityTypeCodes.empty && showUnlinkedAttributes))}}"/>

<g:if test="${availableAttributes}">
    <g:each var="attribute" in="${availableAttributes}" status="status">
        <tr class="prop">
            <td class="name">
                <label for="${fieldPrefix}${attribute?.code}.value" class="${attribute.required?'required':''}">
                    <format:metadata obj="${attribute}"/>
                </label>
            </td>
            <td class="value">
                <g:set var="productAttribute" value="${populatedAttributes?.find { it.attribute.id == attribute.id } }"/>
                <g:set var="otherSelected" value="${productAttribute?.value && !attribute.options.contains(productAttribute?.value)}"/>
                <g:if test="${attribute.options}">
                    <select name="${fieldPrefix}${attribute?.id}.value" class="attributeValueSelector chzn-select-deselect">
                        <option value=""></option>
                        <g:each var="option" in="${attribute.options}" status="optionStatus">
                            <g:set var="selectedText" value=""/>
                            <g:if test="${productAttribute?.value == option}">
                                <g:set var="selectedText" value=" selected"/>
                            </g:if>
                            <option value="${option}"${selectedText}>${option}</option>
                        </g:each>
                        <g:if test="${attribute.allowOther || otherSelected}">
                            <option value="_other"<g:if test="${otherSelected}"> selected</g:if>>
                                <g:message code="default.other.label" default="Other" />
                            </option>
                        </g:if>
                    </select>
                </g:if>
                <g:set var="onlyOtherVal" value="${attribute.allowOther && otherSelected || !attribute.options}"/>
                <g:textField size="50" class="otherAttributeValue text medium"
                             style="${otherAttVal || onlyOtherVal ? '' : 'display:none;'}"
                             name="${fieldPrefix}${attribute?.id}.otherValue"
                             value="${otherAttVal || onlyOtherVal ? productAttribute?.value : ''}"/>
            </td>
        </tr>
    </g:each>
</g:if>
