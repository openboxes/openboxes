<div class="box">
    <h2><warehouse:message code="default.render.label" args="[documentInstance?.filename]" /></h2>

    <g:if test="${documentInstance?.documentType?.documentCode == org.pih.warehouse.core.DocumentCode.SHIPPING_TEMPLATE}">
        <g:form method="post">
            <g:hiddenField name="id" value="${documentInstance?.id}" />
            <g:hiddenField name="version" value="${documentInstance?.version}" />
            <table>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="shipmentId"><warehouse:message code="shipment.label" /></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
                        <g:selectShipment name="shipmentId" noSelection="['':'']" class="chzn-select-deselect"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top"></td>
                    <td valign="top">
                        <div class="buttons left">
                            <g:actionSubmit class="button" action="render" value="${warehouse.message(code: 'default.button.render.label', default: 'Render')}" />
                        </div>
                    </td>
                </tr>
            </table>
        </g:form>
    </g:if>
    <g:else>
        <iframe src="${request.contextPath}/document/download/${documentInstance?.id}?inline=true" width="100%" height="100%"/>
    </g:else>
</div>