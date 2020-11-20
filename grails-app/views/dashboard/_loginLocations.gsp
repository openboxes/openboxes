<style>
#tabs-left {
    position: relative;
    padding-left: 35%;
}
#tabs-left .ui-tabs-nav {
    position: absolute;
    left: 0.25em;
    top: 0.25em;
    bottom: 0.25em;
    width: 33%;
    padding: 0.2em 0 0.2em 0.2em;
}
#tabs-left .ui-tabs-nav li {
    right: 1px;
    width: 100%;
    border-right: none;
    border-bottom-width: 1px !important;
    -moz-border-radius: 4px 0px 0px 4px;
    -webkit-border-radius: 4px 0px 0px 4px;
    border-radius: 4px 0px 0px 4px;
    overflow: hidden;
}
#tabs-left .ui-tabs-nav li.ui-tabs-selected,
#tabs-left .ui-tabs-nav li.ui-state-active {
    border-right: 1px solid transparent;
}
#tabs-left .ui-tabs-nav li a {
    float: right;
    width: 100%;
    text-align: right;
}
#tabs-left .ui-tabs-panel {
    height: 400px;
    width: 100%;
}
.organization-group {
    width: 100%;
    height: 400px;
    overflow: auto;
}
</style>
<g:if test="${loginLocationsMap && !loginLocationsMap.isEmpty() }">
    <div id="tabs-left" class="tabs">
        <ul>
            <li><a href="#saved-locations"><g:message code="user.savedLocations.label"/></a></li>
            <g:each var="organizationName" in="${loginLocationsMap.keySet()}" status="i">
                <li><a href="#organization-${i}">${organizationName?:'No organization'}</a></li>
            </g:each>
        </ul>
        <div id="saved-locations">
            <g:if test="${savedLocations}">
                <h4><g:message code="user.savedLocations.label"/></h4>
                <div class="box">
                    <g:each var="location" in="${savedLocations}">
                        <div class="prop">
                            <a href='${createLink(action:"chooseLocation", id: location?.id)}' class="button">
                                <format:metadata obj="${location}"/>
                            </a>
                        </div>
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
                <g:each var="locationGroup" in="${locationGroups}" status="status">
                    <h4>${locationGroup?:'No Location Group'}</h4>
                    <div class="box">
                        <g:each var="location" in="${locations?.findAll {it.locationGroup == locationGroup }}">
                            <div class="prop">
                                <a href='${createLink(action:"chooseLocation", id: location?.id, params:['targetUri':params.targetUri])}' class="button">
                                    ${location?.name}
                                </a>
                            </div>
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
    $(document).ready(function(){
      $(".tabs").tabs({});
    });
</script>
