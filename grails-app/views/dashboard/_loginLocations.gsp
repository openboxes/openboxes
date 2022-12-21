<style>

</style>
<g:if test="${loginLocationsMap && !loginLocationsMap.isEmpty() }">
    <div class="tabs tabs-left">
        <ul class="scrollbar">
             <g:if test="${savedLocations}">
                <li class="organization-tab">
                    <a href="#saved-locations"><g:message code="user.savedLocations.label"/></a>
                </li>
             </g:if>
            <g:each var="organizationName" in="${loginLocationsMap.keySet()}" status="i">
                <li class="organization-tab">
                    <a href="#organization-${i}">${organizationName?:'No organization'}</a>
                </li>
            </g:each>
        </ul>
        <div id="saved-locations">
            <g:if test="${savedLocations}">
                <div class="location-group">
                    <h6 class="heading"><span><g:message code="user.savedLocations.label"/></span></h6>
                    <g:each var="location" in="${savedLocations}">
                        <g:set var="locationColor" value="${location.bgColor?.replace('#', '')?.toUpperCase()}"/>
                        <g:if test="${locationColor && ['FFFFFF', 'FFFF'].any{ it == locationColor }}">
                            <g:set var="locationColorVariable" value="--location-color: unset"/>
                        </g:if>
                        <g:else>
                            <g:set var="locationColorVariable" value="--location-color: #${locationColor}"/>
                        </g:else>
                        <span>
                            <a
                                href='${createLink(action:"chooseLocation", id: location?.id, params:['targetUri':params.targetUri])}'
                                class="element"
                                style="${locationColorVariable}"
                            >
                                <i class="ri-map-pin-line"></i>
                                <span><format:metadata obj="${location}"/></span>
                            </a>
                        </span>
                    </g:each>
                </div>
            </g:if>
        </div>
        <g:each var="entry" in="${loginLocationsMap}" status="i">
            <g:set var="locationMap" value="${entry.value.sort()}"/>
            <g:set var="organizationName" value="${entry.key }"/>
            <div id="organization-${i}" class="organization-group scrollbar">
                <g:set var="locations" value="${entry.value}"/>
                <g:set var="locationGroups" value="${locations.collect { it?.locationGroup }.unique()}"/>
                <g:each var="locationGroup" in="${locationGroups.sort() { a,b ->  !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}" status="status">
                    <div class="location-group">
                        <h6 class="heading"><span>${locationGroup?:'No Location Group'}</span></h6>
                        <g:each var="location" in="${locations?.findAll {it.locationGroup == locationGroup }}">
                            <g:set var="locationColor" value="${location.backgroundColor?.replace('#', '')?.toUpperCase()}"/>
                            <g:if test="${locationColor && ['FFFFFF', 'FFFF'].any{ it == locationColor }}">
                                <g:set var="locationColorVariable" value="--location-color: unset"/>
                            </g:if>
                            <g:else>
                                <g:set var="locationColorVariable" value="--location-color: #${locationColor}"/>
                            </g:else>
                            <span>
                                <a
                                    href='${createLink(action:"chooseLocation", id: location?.id, params:['targetUri':params.targetUri])}'
                                    class="element"
                                    style="${locationColorVariable}"
                                >
                                    <i class="ri-map-pin-line"></i>
                                    <span>${location?.name}</span>
                                </a>
                            </span>
                        </g:each>
                    </div>
                </g:each>
            </div>
        </g:each>
    </div>
</g:if>
<g:unless test="${loginLocationsMap }">
    <div class="error center">
        <warehouse:message code="dashboard.noWarehouse.message"/>
    </div>
</g:unless>
<script>
    $(document).ready(function() {
      $(".tabs").tabs({});
    });
</script>
