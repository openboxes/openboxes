<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="mobile"/>
    <title><g:message code="dashboard.chooseLocation.label" default="Choose Location"/></title>
</head>

<body>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${userInstance}">
        <div class="errors">
            <g:renderErrors bean="${userInstance}" as="list" />
        </div>
    </g:hasErrors>
    <div class="row">
        <div class="col-sm-12">
            <div id="accordion" class="accordion">
                <div class="accordion-item">
                    <h2 id="heading0" class="accordion-header">
                        <button class="accordion-button" data-bs-toggle="collapse" data-bs-target="#collapse0" aria-expanded="true" aria-controls="collapse0">
                            <g:message code="user.savedLocations.label"/>
                        </button>
                    </h2>
                    <div id="collapse0"  class="accordion-collapse collapse show" aria-labelledby="heading0" data-bs-parent="#accordion">
                        <ul class="list-group">
                            <g:each var="location" in="${savedLocations}">
                                <li class="list-group-item list-group-item-action">
                                    <g:link controller="dashboard" action="chooseLocation" id="${location?.id}">
                                        <span class="text-truncate">${location?.name}</span>
                                    </g:link>
                                </li>
                            </g:each>
                        </ul>
                    </div>
                </div>

                <g:each var="entry" in="${loginLocationsMap}" status="status">
                    <div class="accordion-item">
                        <h2 id="heading${status+1}" class="accordion-header">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse${status+1}" aria-expanded="false" aria-controls="collapse${status+1}">
                                <g:if test="${entry.key}">
                                    ${entry.key}
                                </g:if>
                                <g:else>
                                    <g:message code="default.no.label"/>
                                    <g:message code="organization.label"/>
                                </g:else>
                            </button>
                        </h2>
                        <div id="collapse${status+1}" class="accordion-collapse collapse" aria-labelledby="heading${status+1}" data-bs-parent="#accordion">
                            <ul class="list-group">
                                <g:each var="location" in="${entry.value.sort { it.name }}">
                                    <li class="list-group-item list-group-item-action">
                                        <g:link controller="dashboard" action="chooseLocation" id="${location?.id}">
                                            <span class="text-truncate">${location?.name}</span>
                                        </g:link>
                                    </li>
                                </g:each>
                            </ul>
                        </div>
                    </div>
                </g:each>
            </ul>
        </div>
    </div>
</body>
</html>
