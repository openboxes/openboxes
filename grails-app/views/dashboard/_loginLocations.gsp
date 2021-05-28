<style>

</style>
<g:if test="${loginLocationsMap && !loginLocationsMap.isEmpty() }">
    <div class="tabs tabs-left">
        <ul>
             <g:if test="${savedLocations}">
                <li><a href="#saved-locations"><g:message code="user.savedLocations.label"/></a></li>
             </g:if>
            <g:each var="organizationName" in="${loginLocationsMap.keySet()}" status="i">
                <li><a href="#organization-${i}">${organizationName?:'No organization'}</a></li>
            </g:each>
        </ul>
        <div id="saved-locations">
            <g:if test="${savedLocations}">
                <div class="header-border">
                    <h6 class="heading"><span><g:message code="user.savedLocations.label"/></span></h6>
                    <g:each var="location" in="${savedLocations}">
                        <span>
                            <a href='${createLink(action:"chooseLocation", id: location?.id)}' class="element" style="background-color: ${location.bgColor}">
                                <span><i class="fa fa-map-marker-alt"></i> <format:metadata obj="${location}"/></span>
                            </a>
                        </span>
                    </g:each>
                </div>
            </g:if>
        </div>
        <g:each var="entry" in="${loginLocationsMap}" status="i">
            <g:set var="locationMap" value="${entry.value.sort()}"/>
            <g:set var="organizationName" value="${entry.key }"/>
            <div id="organization-${i}" class="organization-group">
                <g:set var="locations" value="${entry.value}"/>
                <g:set var="locationGroups" value="${locations.collect { it?.locationGroup }.unique()}"/>
                <g:each var="locationGroup" in="${locationGroups.sort() { a,b ->  !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}" status="status">
                    <div class="header-border">
                        <h6 class="heading"><span>${locationGroup?:'No Location Group'}</span></h6>
                        <g:each var="location" in="${locations?.findAll {it.locationGroup == locationGroup }}">
                            <span>
                                <a href='${createLink(action:"chooseLocation", id: location?.id, params:['targetUri':params.targetUri])}' class="element" style="background-color: ${location.backgroundColor}">
                                    <span><i class="fa fa-map-marker-alt"></i> ${location?.name}</span>
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
