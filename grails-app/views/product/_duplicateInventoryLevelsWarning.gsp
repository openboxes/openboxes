<g:if test="${productInstance?.hasDuplicateFacilityInventoryLevels()}">
    <div class="warning" role="alert">
        <g:message code="inventoryLevel.duplicateFacilityLevel.warning"
                   default="More than one facility inventory level has been detected for this product. Verify if this is intentional and fix it if necessary."/>
    </div>
</g:if>
