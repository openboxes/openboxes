<g:if test="${productInstance?.hasDuplicateFacilityInventoryLevels()}">
    <div class="warning" role="alert" aria-label="duplicate-inventory-levels-warning">
        <g:message code="inventoryLevel.duplicateFacilityLevel.warning"
                   default="Duplicate facility inventory level. Verify whether this is intentional and fix it if necessary."/>
    </div>
</g:if>
