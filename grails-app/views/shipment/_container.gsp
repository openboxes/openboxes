<label><g:if test="${container?.parentContainer}">${container?.parentContainer?.name }&nbsp;&rsaquo;&nbsp;</g:if><g:if test="${container?.name }">${container?.name }</g:if><g:else><warehouse:message code="shipping.unpacked.label"/></g:else></label>
<g:if test="${showDetails}">
    <div class="fade">
        <g:if test="${container?.weight || container?.width || container?.length || container?.height}">
            <g:if test="${container?.weight}">
                ${container?.weight} ${container?.weightUnits}
            </g:if>
            <g:if test="${container?.height && container?.width && container?.length }">
                ${container.height} ${container?.volumeUnits} x ${container.width} ${container?.volumeUnits} x ${container.length} ${container?.volumeUnits}
            </g:if>
        </g:if>
    </div>
</g:if>
