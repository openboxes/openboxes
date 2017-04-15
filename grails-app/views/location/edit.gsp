
<%@ page import="org.pih.warehouse.core.LocationTypeCode; org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'larehouse.label', default: 'Location')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery',file:'jquery.colorpicker.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.colorpicker.js')}" type="text/javascript" ></script>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:render template="summary"/>

            <g:form method="post" action="update">
                <g:hiddenField name="id" value="${locationInstance?.id}" />
                <g:hiddenField name="version" value="${locationInstance?.version}" />
                <div class="dialog">
                    <div id="location-tabs" class="tabs">
                        <ul>
                            <li><a href="#location-details-tab"><warehouse:message code="location.label"/></a></li>
                            <li><a href="#location-status-tab"><warehouse:message code="location.status.label" default="Status"/></a></li>
                            <g:if test="${locationInstance?.locationType?.locationTypeCode != LocationTypeCode.BIN_LOCATION}">
                                <li><a href="#location-address-tab"><warehouse:message code="location.address.label" default="Address"/></a></li>
                                <li><a href="#location-binLocations-tab"><warehouse:message code="location.binLocations.label" default="Bin Locations"/></a></li>
                            </g:if>
                            <g:else>
                                <li><a href="${request.contextPath}/location/showContents/${locationInstance?.id}"><warehouse:message code="binLocation.contents.label" default="Contents"/></a></li>
                            </g:else>


                        </ul>
                        <div id="location-details-tab">
                            <div class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_detail.png')}" class="middle"/>
                                    <warehouse:message code="location.details.label" default="Details"/>
                                </h2>
                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="default.name.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
                                                <g:textField name="name" value="${locationInstance?.name}" class="text" size="80"/>
                                            </td>
                                        </tr>
                                        <%--
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="locationNumber"><warehouse:message code="location.locationNumber.label" default="Short Code"/></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'locationNumber', 'errors')}">
                                                <g:textField name="locationNumber" value="${locationInstance?.locationNumber}" class="text" size="80"/>
                                            </td>
                                        </tr>
                                        --%>
                                        <g:if test="${locationInstance?.locationType?.locationTypeCode == LocationTypeCode.BIN_LOCATION}">
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="name"><warehouse:message code="location.parentLocation.label" /></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'parentLocation', 'errors')}">

                                                    <g:selectLocation name="parentLocation.id" value="${locationInstance?.parentLocation?.id}"
                                                                      noSelection="['null':'']"
                                                                      class="chzn-select-deselect"/>
                                                </td>
                                            </tr>
                                        </g:if>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="location.locationType.label" /></label>

                                            </td>
                                            <td valign="top" class="value">
                                                <g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}" class="chzn-select-deselect"
                                                    optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.locationType?.id}" noSelection="['null':'']" />
                                            </td>
                                        </tr>
                                        <g:if test="${locationInstance?.locationType?.locationTypeCode != LocationTypeCode.BIN_LOCATION}">
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="name"><warehouse:message code="location.locationGroup.label" /></label>
                                                </td>
                                                <td valign="top" class="value">
                                                    <g:select class="chzn-select-deselect"
                                                            name="locationGroup.id" from="${org.pih.warehouse.core.LocationGroup.list()}"
                                                              optionKey="id" value="${locationInstance?.locationGroup?.id}" noSelection="['null':'']" />
                                                </td>
                                            </tr>
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="manager.id"><warehouse:message code="warehouse.manager.label" /></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'manager', 'errors')}">
                                                    <g:select class="chzn-select-deselect"
                                                              name="manager.id" from="${org.pih.warehouse.core.User.list().sort{it.lastName}}" optionKey="id" value="${locationInstance?.manager?.id}"  noSelection="['null':'']" />
                                                </td>
                                            </tr>


                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                  <label for="bgColor"><warehouse:message code="location.bgColor.label"/></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'bgColor', 'errors')}">
                                                    <g:textField name="bgColor" value="${locationInstance?.bgColor}" class="text" size="10"/>
                                                    <%--
                                                    <g:select name="bgColor" class="colorpicker"
                                                        from="${org.pih.warehouse.core.Constants.COLORS}"
                                                        value="${locationInstance?.bgColor}" />

                                                    <span class="fade">#${locationInstance?.bgColor }</span>
                                                     --%>
                                                </td>
                                            </tr>
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                  <label for="fgColor"><warehouse:message code="location.fgColor.label" /></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'fgColor', 'errors')}">
                                                    <g:textField name="fgColor" value="${locationInstance?.fgColor}" class="text" size="10"/>
                                                    <%--
                                                    <g:select name="fgColor" class="colorpicker"
                                                        from="${org.pih.warehouse.core.Constants.COLORS}"
                                                        value="${locationInstance?.fgColor}"/>

                                                    <span class="fade">#${locationInstance?.fgColor }</span>
                                                    --%>
                                                </td>
                                            </tr>

                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="bgColor"><warehouse:message code="location.sortOrder.label" default="Sort order"/></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'sortOrder', 'errors')}">
                                                    <g:textField name="sortOrder" value="${locationInstance?.sortOrder}" class="text" size="10"/>
                                                    <%--
                                                    <g:select name="bgColor" class="colorpicker"
                                                        from="${org.pih.warehouse.core.Constants.COLORS}"
                                                        value="${locationInstance?.bgColor}" />

                                                       <span class="fade">#${locationInstance?.bgColor }</span>
                                                        --%>
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
                        </div>
                        <div id="location-status-tab">
                            <div class="box">
                                <h2>
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'flag_red.png')}" class="middle"/>
                                    <warehouse:message code="default.status.label" default="Status"/>
                                </h2>
                                <table>
                                    <tbody>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="active"><warehouse:message code="warehouse.active.label" /></label>
                                        </td>
                                        <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
                                                <g:checkBox name="active" value="${locationInstance?.active}" />

                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="local"><warehouse:message code="warehouse.local.label" /></label>
                                        </td>
                                        <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
                                            <g:checkBox name="local" value="${locationInstance?.local}" />
                                        </td>
                                    </tr>


                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
                                        </td>
                                        <td valign="top" class="value">
                                            <g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
                                            <g:select name="supportedActivities" multiple="true" from="${activityList }" size="${activityList.size()+1 }" style="width: 150px"
                                                      optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.supportedActivities?:locationInstance?.locationType?.supportedActivities}"
                                                      noSelection="['':warehouse.message(code:'location.useDefaultActivities.label')]" />

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
                        </div>

                        <g:if test="${locationInstance?.locationType?.locationTypeCode != LocationTypeCode.BIN_LOCATION}">

                            <div id="location-address-tab">
                                <g:hiddenField name="address.id" value="${locationInstance?.address?.id}"/>
                                <div class="box">
                                    <h2>
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}" class="middle"/>
                                        <warehouse:message code="address.label" default="Address"/>
                                    </h2>
                                    <table>
                                        <tbody>
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="name"><warehouse:message code="address.address.label" /></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'address', 'errors')}">
                                                    <g:textField name="address.address" value="${locationInstance?.address?.address}" class="text" size="60"/>
                                                </td>
                                            </tr>
                                            <tr class="prop">
                                                <td valign="top" class="name">
                                                    <label for="name"><warehouse:message code="address.address2.label" /></label>
                                                </td>
                                                <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'address2', 'errors')}">
                                                    <g:textField name="address.address2" value="${locationInstance?.address?.address2}" class="text" size="60"/>
                                                </td>
                                            </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.city.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'city', 'errors')}">
                                                <g:textField name="address.city" value="${locationInstance?.address?.city}" class="text" size="60"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.stateOrProvince.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'stateOrProvince', 'errors')}">
                                                <g:textField name="address.stateOrProvince" value="${locationInstance?.address?.stateOrProvince}" class="text" size="60"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.postalCode.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'postalCode', 'errors')}">
                                                <g:textField name="address.postalCode" value="${locationInstance?.address?.postalCode}" class="text" size="60"/>
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.country.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'country', 'errors')}">
                                                <g:textField name="address.country" value="${locationInstance?.address?.country}" class="text" size="60"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="name"><warehouse:message code="address.description.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: locationInstance?.address, field: 'description', 'errors')}">
                                                <g:textArea name="address.description" value="${locationInstance?.address?.description}" class="text" rows="6" cols="80"/>
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
                            </div>
                            <div id="location-binLocations-tab">


                                <div class="box">
                                    <h2><warehouse:message code="binLocations.label" default="Bin Locations" /></h2>
                                    <div class="dialog">
                                        <table>
                                            <tr>
                                                <th width="1%"><g:message code="default.actions.label"/></th>
                                                <th width="1%"><g:message code="warehouse.active.label" default="Active"/></th>
                                                <th><g:message code="location.binLocation.label" default="Bin Location"/></th>
                                                <th><g:message code="default.actions.label"></g:message></th>
                                            </tr>

                                            <g:each in="${locationInstance?.locations?.sort { it.name }}" var="binLocation" status="status">
                                                <tr class="prop ${status%2?'even':'odd'}">
                                                    <td>
                                                        <div class="action-menu">
                                                            <button class="action-btn">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
                                                                     style="vertical-align: middle" />
                                                            </button>
                                                            <div class="actions">
                                                                <div class="action-menu-item">
                                                                    <a href="javascript:void(-1)" class="btnShowContents" data-id="${binLocation?.id}" fragment="location-details-tab">
                                                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" class="middle"/>&nbsp;
                                                                        ${warehouse.message(code: 'default.show.label', args: [warehouse.message(code:'binLocation.label')])}
                                                                    </a>
                                                                </div>
                                                                <div class="action-menu-item">
                                                                    <g:link class="edit" action="edit" id="${binLocation?.id}" fragment="location-details-tab">
                                                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" class="middle"/>&nbsp;
                                                                        ${warehouse.message(code: 'default.edit.label', args: [warehouse.message(code:'binLocation.label')])}
                                                                    </g:link>
                                                                </div>
                                                                <div class="action-menu-item">
                                                                    <g:link class="delete" action="delete" id="${binLocation?.id}"
                                                                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" class="middle"/>&nbsp;
                                                                        ${warehouse.message(code: 'default.delete.label', args: [warehouse.message(code:'binLocation.label')])}
                                                                    </g:link>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        ${binLocation.active}
                                                    </td>
                                                    <td>
                                                        <g:link controller="location" action="edit" id="${binLocation.id}" fragment="location-details-tab">
                                                            ${binLocation.name}
                                                        </g:link>
                                                    </td>
                                                    <td>
                                                        <a href="javascript:void(-1)" class="btnShowContents button" data-id="${binLocation.id}"><g:message code="default.show.label" args="['Contents']"/></a>
                                                    </td>
                                                </tr>
                                            </g:each>
                                            <g:unless test="${locationInstance.locations}">
                                                <tr>
                                                    <td colspan="3">
                                                        <div class="emtpty center fade">
                                                            <g:message code="location.noBinLocations.label" default="No bin locations"/>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </g:unless>
                                            <tfoot>
                                                <tr>
                                                    <td colspan="4">
                                                        <button class="btnAddBinLocation button">
                                                            <g:message code="default.add.label" args="[g.message(code:'location.binLocation.label')]"/>
                                                        </button>
                                                    </td>
                                                </tr>
                                            </tfoot>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </div>
                </div>
            </g:form>
        </div>
    </div>
    <div id="dlgAddBinLocation" title="${g.message(code: 'default.add.label', args: [g.message(code:'location.binLocation.label')])}">
        <div class="dialog">
            <g:form controller="location" action="update">
                <g:hiddenField name="parentLocation.id" value="${locationInstance?.id}" />
                <g:hiddenField name="version" value="${locationInstance?.version}" />
                <table>
                    <tbody>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="name"><warehouse:message code="location.locationType.label" /></label>
                        </td>
                        <td valign="top" class="value">
                            <g:set var="binLocationTypes" value="${org.pih.warehouse.core.LocationType.findAllByLocationTypeCode(org.pih.warehouse.core.LocationTypeCode.BIN_LOCATION)}"/>
                            <g:set var="defaultBinLocationType" value="${org.pih.warehouse.core.LocationType.findByLocationTypeCode(org.pih.warehouse.core.LocationTypeCode.BIN_LOCATION)}"/>
                            <g:select name="locationType.id" from="${binLocationTypes}" class="chzn-select-deselect"
                                      value="${binLocation?.locationType?.id?:defaultBinLocationType?.id}"
                                      optionKey="id" optionValue="${{format.metadata(obj:it)}}" noSelection="['null':'']" />
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="name"><warehouse:message code="location.binLocation.label" /></label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
                            <g:textField name="name" value="${binLocation?.name}" class="text" size="80"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">

                        </td>
                        <td valign="top" class="value">
                            <button type="submit" class="button icon approve">
                                <warehouse:message code="default.button.save.label"/>
                            </button>

                        </td>
                    </tr>


                    </tbody>
                </table>

            </g:form>
        </div>
    </div>

    <div id="dlgShowContents" title="${g.message(code: 'default.show.label', args: [g.message(code:'location.binLocation.label')])}">
        <!-- Contents loaded dynamically -->
    </div>


    <script type="text/javascript">

        function selectCombo(comboBoxElem, value) {
            alert(comboBoxElem + " " + value)
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

        $(document).ready(function() {

            $(".tabs").tabs({cookie:{expires:1}});

            // Show Contents dialog
            $("#dlgShowContents").dialog({ autoOpen: false, modal: true, width: 800 });
            $(".btnShowContents").click(function(event) {
                var id = $(this).data("id");
                var url = "${request.contextPath}/location/showContents/" + id;
                //var url = "/openboxes/dashboard/index";
                console.log(url);
                $("#dlgShowContents").load(url).dialog('open');
                event.preventDefault();
            });


            // Add Bin Location dialog
            $(".btnAddBinLocation").click(function(event) {
                $("#dlgAddBinLocation").dialog('open');
                event.preventDefault();
            });
            $("#dlgAddBinLocation").dialog({ autoOpen: false, modal: true, width: 800 });

            $(".btnCloseDialog").click( function() {
                $("#dlgAddBinLocation").dialog('close');
            });
            /*
            $('#bgColor').colorpicker({
                size: 20,
                label: '',
                hide: true
            });

            $('#fgColor').colorpicker({
                size: 20,
                label: '',
                hide: true
            });
            */
        });

    </script>
    </body>
</html>
