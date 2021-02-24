<style>
#tabs-left {
    position: relative;
    padding-left: 35%;
    overflow: auto;
    display: flex;
}
#tabs-left .ui-tabs-nav {
    position: absolute;
    left: 0.25em;
    top: 0.25em;
    width: 33%;
    padding: 0.2em 0 0.5em 0.5em;
    background-color: #888;
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
    margin: 2px 0px 2px 0px;
}
#tabs-left .ui-tabs-nav li.ui-tabs-selected,
#tabs-left .ui-tabs-nav li.ui-state-active {
    border-right: 1px solid transparent;
    background-color: #ffffff;
    border-color: white;
}
#tabs-left .ui-tabs-nav li a {
    float: left;
    width: 100%;
    text-align: left;
    color: black;
}
#tabs-left .ui-tabs-panel {
    height: 400px;
    width: 100%;
}
.organization-group {
    height: 400px;
}
.header-border {
    margin-bottom: 15px;
    margin-top: 10px;
    border: 1px solid #888;
    border-radius: 4px;
    display: inline-block;
}
.header-border .heading {
    margin-left: 20px;
    margin-top: -10px;
}
.header-border .heading > span {
    background-color: white;
}
.element {
    margin: 5px;
    display: inline-block;
    padding: 0.5em 1em;
    border: 1px solid #d4d4d4;
    text-align: center;
    color: black !important;
    background-clip: padding-box;
    border-radius: 0.2em;
}
</style>
<g:if test="${loginLocationsMap && !loginLocationsMap.isEmpty() }">
    <div id="tabs-left" class="tabs">
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
    $(document).ready(function(){
      $(".tabs").tabs({});
    });
</script>
