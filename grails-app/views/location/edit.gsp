<%@ page import="org.pih.warehouse.core.ActivityCode; org.pih.warehouse.core.LocationTypeCode; org.pih.warehouse.core.Location" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="custom"/>
    <g:set var="entityName"
           value="${warehouse.message(code: 'larehouse.label', default: 'Location')}"/>
    <title><warehouse:message code="default.edit.label" args="[entityName]"/></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
    <content tag="pageTitle"><warehouse:message code="default.edit.label"
                                                args="[entityName]"/></content>
    <link rel="stylesheet" href="${resource(dir: 'js/jquery', file: 'jquery.colorpicker.css')}"
          type="text/css" media="screen, projection"/>
    <script src="${resource(dir: 'js/jquery/', file: 'jquery.colorpicker.js')}"
            type="text/javascript"></script>
</head>

<body>
<div class="body">

    <g:if test="${flash.message}">
        <div class="message" role="status" aria-label="message">${flash.message}</div>
    </g:if>
    <g:if test="${flash.warning}">
        <div class="warning" role="status" aria-label="warning-message">${raw(flash.warning)}</div>
    </g:if>
    <g:hasErrors bean="${locationInstance}">
        <div class="errors" role="alert" aria-label="error-message">
            <g:renderErrors bean="${locationInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:render template="summary"/>

    <g:form method="post" action="update">
        <g:hiddenField name="id" value="${locationInstance?.id}"/>
        <g:hiddenField name="version" value="${locationInstance?.version}"/>
        <div class="dialog">
            <div id="location-tabs" class="tabs">
                <ul>
                    <li role="tab" aria-label="Location"><a href="#location-details-tab"><g:message code="location.label"/></a></li>
                    <li role="tab"  aria-label="Configuration"><a href="#location-configuration-tab"><g:message code="location.configuration.label"
                                                                  default="Configuration"/></a></li>
                    <g:if test="${!locationInstance?.isInternalLocation()}">
                        <g:if test="${!locationInstance?.isZoneLocation()}">
                            <li role="tab" aria-label="Address"><a href="#location-address-tab"><g:message code="location.address.label"
                                                                       default="Address"/></a></li>
                            <li role="tab" aria-label="Zone Locations"><a href="${request.contextPath}/location/showZoneLocations/${locationInstance?.id}"
                                   id="location-zoneLocations-tab">
                                <g:message code="location.zoneLocations.label" default="Zone Locations"/></a>
                            </li>
                        </g:if>
                    <%--<li><a href="#location-binLocations-tab"><g:message code="location.binLocations.label" default="Bin Locations"/></a></li>--%>
                        <li role="tab" aria-label="Bin Locations"><a href="${request.contextPath}/location/showBinLocations/${locationInstance?.id}"
                               id="location-binLocations-tab">
                            <g:message code="location.binLocations.label"
                                       default="Bin Locations"/></a>
                        </li>

                    </g:if>
                    <g:else>
                        <li role="tab" aria-label="Contents"><a href="${request.contextPath}/location/showContents/${locationInstance?.id}"><warehouse:message
                                code="binLocation.contents.label" default="Contents"/></a></li>
                    </g:else>
                    <li role="tab" aria-label="Forecasting"><a href="${request.contextPath}/location/showForecastingConfiguration/${locationInstance?.id}"
                           id="location-forecastingConfiguration-tab">
                        <g:message code="forecasting.label"
                                   default="Forecasting"/></a>
                    </li>
                </ul>

                <section id="location-details-tab" aria-label="Location">
                    <div class="box">
                        <h2>
                            <img src="${resource(dir: 'images/icons/silk', file: 'application_view_detail.png')}"/>
                            <warehouse:message code="location.details.label" default="Details"/>
                        </h2>
                        <table>
                            <tbody>
                            <g:if test="${locationInstance?.id}">
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.id.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance, field: 'id', 'errors')}">
                                            ${locationInstance?.id}
                                    </td>
                                </tr>
                            </g:if>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="location.name.label"
                                                                         default="Location name"/></label>
                                </td>
                                <td valign="top"
                                    class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${locationInstance?.name}"
                                                 class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="locationNumber"><warehouse:message
                                            code="location.locationNumber.label"
                                            default="Location code"/></label>
                                </td>
                                <td valign="top"
                                    class="value ${hasErrors(bean: locationInstance, field: 'locationNumber', 'errors')}">
                                    <g:textField name="locationNumber"
                                                 value="${locationInstance?.locationNumber}"
                                                 class="text" size="80"/>
                                </td>
                            </tr>
                            <g:if test="${locationInstance?.isInternalLocation() || locationInstance.isZoneLocation()}">
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="parentLocation.id"><warehouse:message
                                                code="location.parentLocation.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance, field: 'parentLocation', 'errors')}">
                                        <div data-testid="parent-location-select">
                                            <g:selectLocation name="parentLocation.id"
                                                              value="${locationInstance?.parentLocation?.id}"
                                                              noSelection="['null': '']"
                                                              class="chzn-select-deselect"/>
                                        </div>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${locationInstance?.isInternalLocation()}">
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="zone.id"><warehouse:message
                                                code="location.zoneLocation.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance, field: 'zone', 'errors')}">
                                        <div data-testid="zone-select">
                                            <g:selectZoneLocationByLocation
                                                    name="zone.id"
                                                    id="${locationInstance?.parentLocation?.id}"
                                                    value="${locationInstance?.zone?.id}"
                                                    noSelection="['null': '']"
                                                    class="chzn-select-deselect"/>
                                        </div>
                                    </td>
                                </tr>
                            </g:if>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="organization.id"><g:message
                                            code="organization.label"/></label>
                                </td>
                                <td valign="top" class="value">
                                    <div data-testid="organization-select">
                                        <g:selectOrganization name="organization.id"
                                                  class="chzn-select-deselect"
                                                  optionKey="id"
                                                  value="${locationInstance?.organization?.id}"
                                                  optionValue="${{ format.metadata(obj: it) }}"
                                                  noSelection="['null': '']"
                                                  active="${true}"
                                                  currentOrganizationId="${locationInstance?.organization?.id ?: ''}"
                                        />
                                    </div>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="locationType.id"><warehouse:message
                                            code="location.locationType.label"/></label>

                                </td>
                                <td valign="top" class="value">
                                    <div data-testid="location-type-select">
                                        <g:select name="locationType.id"
                                                  from="${org.pih.warehouse.core.LocationType.list()}"
                                                  class="chzn-select-deselect"
                                                  optionKey="id"
                                                  optionValue="${{ format.metadata(obj: it) }}"
                                                  value="${locationInstance?.locationType?.id}"
                                                  noSelection="['null': '']"/>
                                    </div>
                                </td>
                            </tr>
                            <g:if test="${!locationInstance?.isInternalLocation() && !locationInstance.isZoneLocation()}">
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="locationGroup.id"><warehouse:message
                                                code="location.locationGroup.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                      <div data-testid="location-group-select">
                                          <g:select class="chzn-select-deselect"
                                                    name="locationGroup.id"
                                                    from="${org.pih.warehouse.core.LocationGroup.list()}"
                                                    optionKey="id"
                                                    value="${locationInstance?.locationGroup?.id}"
                                                    noSelection="['null': '']"/>
                                      </div>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="manager.id"><warehouse:message
                                                code="warehouse.manager.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance, field: 'manager', 'errors')}">
                                        <div data-testid="manager-select">
                                            <g:select class="chzn-select-deselect"
                                                      name="manager.id"
                                                      from="${org.pih.warehouse.core.User.list().sort {
                                                          it.lastName
                                                      }}" optionKey="id"
                                                      value="${locationInstance?.manager?.id}"
                                                      noSelection="['null': '']"/>
                                        </div>
                                    </td>
                                </tr>
                            </g:if>

                            </tbody>
                            <tfoot>
                            <tr>
                                <td>

                                </td>
                                <td>
                                    <div class="buttons left">
                                        <button type="submit" class="button icon approve">
                                            <warehouse:message code="default.button.save.label"/>
                                        </button>
                                        &nbsp;
                                        <g:link action="list">
                                            ${warehouse.message(code: 'default.button.cancel.label')}
                                        </g:link>
                                    </div>

                                </td>
                            </tr>
                            </tfoot>

                        </table>
                    </div>
                </section>

                <section id="location-configuration-tab" aria-label="Configuration">
                    <div class="box">
                        <h2>
                            <img src="${resource(dir: 'images/icons/silk', file: 'flag_red.png')}"
                            <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
                            <warehouse:message code="default.configuration.label" default="Configuration"/>
                        </h2>
                        <table>
                            <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active"><warehouse:message
                                            code="warehouse.active.label"/></label>
                                </td>
                                <td valign="top"
                                    class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
                                    <g:checkBox name="active" value="${locationInstance?.active}"/>

                                </td>
                            </tr>

                            <g:if test="${!locationInstance?.isInternalLocation() && !locationInstance?.isZoneLocation()}">
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="bgColor"><warehouse:message
                                                code="location.bgColor.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance, field: 'bgColor', 'errors')}">
                                        <g:textField name="bgColor"
                                                     value="${locationInstance?.bgColor}"
                                                     class="text" size="10"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="fgColor"><warehouse:message
                                                code="location.fgColor.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance, field: 'fgColor', 'errors')}">
                                        <g:textField name="fgColor"
                                                     value="${locationInstance?.fgColor}"
                                                     class="text" size="10"/>
                                    </td>
                                </tr>
                            </g:if>
                            <tr class="prop">
                                <td valign="top" class="name" rowspan="2">
                                    <label for="name"><warehouse:message
                                            code="location.supportedActivities.label"/></label>
                                </td>
                                <td valign="top" class="value">
                                    <g:set var="sameAsDefaults"
                                           value="${locationInstance?.locationType?.supportedActivities?.equals(locationInstance?.supportedActivities)}"/>
                                    <g:set var="useDefault"
                                           value="${locationInstance?.supportedActivities?.isEmpty() || sameAsDefaults}"/>

                                    <g:if test="${useDefault}">
                                        <g:set var="supportedActivities"
                                               value="${locationInstance?.locationType?.supportedActivities}"/>
                                    </g:if>
                                    <g:else>
                                        <g:set var="supportedActivities"
                                               value="${locationInstance?.supportedActivities}"/>
                                    </g:else>

                                    <g:set var="activityList"
                                           value="${ActivityCode.list()}"/>
                                    <div class="buttons button-group right">
                                        <g:link controller="locationType" action="edit"
                                                id="${locationInstance?.locationType?.id}"
                                                class="button">
                                            <g:message code="default.edit.label"
                                                       args="['defaults settings']"/>
                                        </g:link>
                                        <g:link controller="location"
                                                action="resetSupportedActivities"
                                                id="${locationInstance?.id}" class="button">
                                            <g:message code="default.resetTo.label"
                                                       default="Reset to {0}"
                                                       args="['default settings']"/>
                                        </g:link>

                                    </div>

                                    <div>
                                        <label>
                                            <g:checkBox name="useDefault" value="${true}"
                                                        checked="${useDefault}"
                                                        class="use-default"/>
                                            Use Default Settings
                                        </label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <div data-testid="supported-activities-select">
                                        <select id="supported-activities" name="supportedActivities"
                                                class="chzn-select-deselect"
                                                multiple="true" ${useDefault ? 'disabled' : ''}>
                                            <g:each var="activity" in="${activityList}">
                                                <g:set var="isDefault"
                                                       value="${locationInstance?.locationType?.supportedActivities?.contains(activity.toString())}"/>
                                                <g:set var="isSelected"
                                                       value="${supportedActivities?.contains(activity.toString())}"/>
                                                <option value="${activity}" ${isSelected ? 'selected' : ''}>
                                                    ${format.metadata(obj: activity)}
                                                </option>
                                            </g:each>
                                        </select>
                                    </div>

                                </td>
                            </tr>
                            </tbody>
                            <tfoot>
                            <tr>
                                <td>

                                </td>
                                <td>
                                    <div class="buttons left">
                                        <button type="submit" class="button icon approve">
                                            <warehouse:message code="default.button.save.label"/>
                                        </button>
                                        &nbsp;
                                        <g:link action="list">
                                            ${warehouse.message(code: 'default.button.cancel.label')}
                                        </g:link>
                                    </div>

                                </td>
                            </tr>
                            </tfoot>

                        </table>
                    </div>
                </section>

                <g:if test="${!locationInstance?.isInternalLocation() && !locationInstance?.isZoneLocation()}">

                    <section id="location-address-tab" aria-label="Address">
                        <g:if test="${locationInstance?.address}">
                            <g:hiddenField name="address.id" value="${locationInstance?.address?.id}"/>
                        </g:if>
                        <div class="box">
                            <h2>
                                <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}"
                                     class="middle"/>
                                <warehouse:message code="address.label" default="Address"/>
                            </h2>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.address"><warehouse:message
                                                code="address.address.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'address', 'errors')}">
                                        <g:textField name="address.address"
                                                     value="${locationInstance?.address?.address}"
                                                     class="text" size="60"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.address2"><warehouse:message
                                                code="address.address2.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'address2', 'errors')}">
                                        <g:textField name="address.address2"
                                                     value="${locationInstance?.address?.address2}"
                                                     class="text" size="60"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.city"><warehouse:message
                                                code="address.city.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'city', 'errors')}">
                                        <g:textField name="address.city"
                                                     value="${locationInstance?.address?.city}"
                                                     class="text" size="60"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.stateOrProvince"><warehouse:message
                                                code="address.stateOrProvince.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'stateOrProvince', 'errors')}">
                                        <g:textField name="address.stateOrProvince"
                                                     value="${locationInstance?.address?.stateOrProvince}"
                                                     class="text" size="60"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.postalCode"><warehouse:message
                                                code="address.postalCode.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'postalCode', 'errors')}">
                                        <g:textField name="address.postalCode"
                                                     value="${locationInstance?.address?.postalCode}"
                                                     class="text" size="60"/>
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.country"><warehouse:message
                                                code="address.country.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'country', 'errors')}">
                                        <g:textField name="address.country"
                                                     value="${locationInstance?.address?.country}"
                                                     class="text" size="60"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="address.description"><warehouse:message
                                                code="address.description.label"/></label>
                                    </td>
                                    <td valign="top"
                                        class="value ${hasErrors(bean: locationInstance?.address, field: 'description', 'errors')}">
                                        <g:textArea name="address.description"
                                                    value="${locationInstance?.address?.description}"
                                                    class="text" rows="6" cols="80"/>
                                    </td>
                                </tr>
                                </tbody>
                                <tfoot>
                                <tr>
                                    <td>

                                    </td>
                                    <td>
                                        <div class="buttons left">
                                            <button type="submit" class="button icon approve">
                                                <warehouse:message
                                                        code="default.button.save.label"/>
                                            </button>
                                            &nbsp;
                                            <g:link action="list">
                                                ${warehouse.message(code: 'default.button.cancel.label')}
                                            </g:link>
                                        </div>

                                    </td>
                                </tr>
                                </tfoot>
                            </table>
                        </div>
                    </section>
                </g:if>
            </div>

            <div class="loading">Loading...</div>
        </div>
    </g:form>
</div>
</div>
<div id="dlgAddBinLocation"
     title="${g.message(code: 'default.add.label', args: [g.message(code: 'location.internal.label')])}">
    <div class="dialog">
        <g:form controller="location" action="update">
            <g:if test="${locationInstance?.isZoneLocation()}">
                <g:hiddenField name="zone.id" value="${locationInstance?.id}"/>
                <g:hiddenField name="parentLocation.id" value="${locationInstance?.parentLocation?.id}"/>
            </g:if>
            <g:else>
                <g:hiddenField name="parentLocation.id" value="${locationInstance?.id}"/>
            </g:else>
            <g:hiddenField name="version" value="${locationInstance?.version}"/>
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><warehouse:message
                                code="location.locationType.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:set var="binLocationTypes"
                               value="${org.pih.warehouse.core.LocationType.internalLocationTypes.sort()}"/>
                        <g:set var="defaultBinLocationType"
                               value="${org.pih.warehouse.core.LocationType.defaultInternalLocationType}"/>
                        <g:select name="locationType.id" from="${binLocationTypes}"
                                  class="chzn-select-deselect"
                                  value="${binLocation?.locationType?.id ?: defaultBinLocationType?.id}"
                                  optionKey="id" optionValue="${{ format.metadata(obj: it) }}"
                                  noSelection="['null': '']"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><warehouse:message code="location.name.label"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${binLocation?.name}" class="text"
                                     size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">

                    </td>
                    <td valign="top" class="value">
                        <button type="submit" class="button">
                            <warehouse:message code="default.button.save.label"/>
                        </button>

                    </td>
                </tr>

                </tbody>
            </table>

        </g:form>
    </div>
</div>

<div id="dlgShowContents"
     title="${g.message(code: 'default.show.label', args: [g.message(code: 'location.binLocation.label')])}">
    <!-- Contents loaded dynamically -->
</div>

<div id="dlgAddZoneLocation"
     title="${g.message(code: 'default.add.label', args: [g.message(code: 'location.zoneLocation.label')])}">
    <div class="dialog">
        <g:form controller="location" action="update">
            <g:hiddenField name="parentLocation.id" value="${locationInstance?.id}"/>
            <g:hiddenField name="version" value="${locationInstance?.version}"/>
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><warehouse:message
                                code="location.locationType.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:set var="zoneLocationTypes"
                               value="${org.pih.warehouse.core.LocationType.zoneLocationTypes.sort()}"/>
                        <g:set var="defaultZoneLocationType"
                               value="${org.pih.warehouse.core.LocationType.defaultZoneLocationType}"/>
                        <g:select name="locationType.id" from="${zoneLocationTypes}"
                                  class="chzn-select-deselect"
                                  value="${zoneLocation?.locationType?.id ?: defaultZoneLocationType?.id}"
                                  optionKey="id" optionValue="${{ format.metadata(obj: it) }}"
                                  noSelection="['null': '']"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><warehouse:message code="location.name.label"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${zoneLocation?.name}" class="text"
                                     size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">

                    </td>
                    <td valign="top" class="value">
                        <button type="submit" class="button">
                            <warehouse:message code="default.button.save.label"/>
                        </button>

                    </td>
                </tr>

                </tbody>
            </table>

        </g:form>
    </div>
</div>

<div id="dlgShowZoneContents"
     title="${g.message(code: 'default.show.label', args: [g.message(code: 'location.zoneLocation.label')])}">
    <!-- Contents loaded dynamically -->
</div>

<div id="dlgImportBinLocations"
     title="${g.message(code: 'default.import.label', args: [g.message(code: 'location.binLocation.label')])}">
    <div class="dialog">
        <g:uploadForm action="importBinLocations">
            <g:hiddenField name="id" value="${locationInstance?.id}"/>
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name"><label><warehouse:message
                            code="document.selectFile.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <input name="fileContents" type="file"/>
                    </td>
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <td></td>
                    <td>
                        <g:submitButton name="importBinLocations" value="Import Bin Locations"
                                        class="button icon add"></g:submitButton>
                    </td>
                </tr>
                </tfoot>
            </table>
        </g:uploadForm>
    </div>
</div>


<script type="text/javascript">

  function selectCombo(comboBoxElem, value) {
    alert(comboBoxElem + " " + value);
    if (comboBoxElem != null) {
      if (comboBoxElem.options) {
        for (var i = 0; i < comboBoxElem.options.length; i++) {
          if (comboBoxElem.options[i].value == value &&
              comboBoxElem.options[i].value != "") { //empty string is for "noSelection handling as "" == 0 in js
            comboBoxElem.options[i].selected = true;
            break
          }
        }
      }
    }
  }

  $(document).ready(function () {
    $(".loading").hide();
    $(".tabs").tabs({
          cookie: {
            expires: 1
          },
          ajaxOptions: {
            error: function (xhr, status, index, anchor) {
              $(anchor.hash).html();
            },
            beforeSend: function () {
              $('.loading').show();
            },
            complete: function () {
              $(".loading").hide();
            }
          }
        }
    );

    // Define all dialog windows
    $("#dlgShowContents").dialog({autoOpen: false, modal: true, width: 800});
    $("#dlgAddBinLocation").dialog({autoOpen: false, modal: true, width: 800});
    $("#dlgShowZoneContents").dialog({autoOpen: false, modal: true, width: 800});
    $("#dlgAddZoneLocation").dialog({autoOpen: false, modal: true, width: 800});
    $("#dlgImportBinLocations").dialog({autoOpen: false, modal: true, width: 800});

    $(".btnShowContents").livequery("click", function (event) {
      var id = $(this).data("id");
      var url = "${request.contextPath}/location/showContents/" + id;
      //var url = "/openboxes/dashboard/index";
      console.log(url);
      $("#dlgShowContents").load(url).dialog('open');
      event.preventDefault();
    });

    // Add event handlers for buttons
    $("#btnAddBinLocation").livequery("click", function (event) {
      event.preventDefault();
      $("#dlgAddBinLocation").dialog('open');
    });

    $(".btnCloseDialog").livequery("click", function () {
      event.preventDefault();
      $("#dlgAddBinLocation").dialog('close');
    });

    $(".btnShowZoneLocationContents").livequery("click", function (event) {
      var id = $(this).data("id");
      var url = "${request.contextPath}/location/showContents/" + id;
      console.log(url);
      $("#dlgShowZoneContents").load(url).dialog('open');
      event.preventDefault();
    });

    $("#btnAddZoneLocation").livequery("click", function (event) {
      event.preventDefault();
      $("#dlgAddZoneLocation").dialog('open');
    });

    // Import Bin Locations
    $("#btnImportBinLocations").livequery("click", function (event) {
      event.preventDefault();
      $("#dlgImportBinLocations").dialog('open');
    });
    $("#btnExportBinLocations").livequery("click", function (event) {
      var href = $(this).data("href");
      window.location.href = href;
      event.preventDefault();
    });

    $(".use-default").change(function () {
      if ($(this).is(':not(:checked)')) {
        $("#supported-activities").prop("disabled", false).trigger("chosen:updated");
      } else {
        $("#supported-activities").prop("disabled", true).trigger("chosen:updated");
      }
    });

  });

</script>
</body>
</html>
