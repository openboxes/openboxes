    <div id="requisition-template-header" class="box dialog">
        <div style="line-height: 20px;">
            <h2>

                <%--
                <a class="toggle" href="javascript:void(0);">
                    <img id="toggle-icon" src="${resource(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
                </a>
                <h3 style="display: inline" class="toggle"><label>${requisition?.requestNumber }</label> ${requisition?.name }</h3>
                &nbsp;
                --%>
                <warehouse:message code="requisitionTemplate.label" default="Requisition template"/>
                <div class="right">
                    <g:if test="${requisition?.id }">
                        <g:link controller="requisitionTemplate" action="editHeader" id="${requisition?.id }" class=" icon edit">
                            ${warehouse.message(code:'requisition.button.edit.label')}
                        </g:link>
                    </g:if>
                </div>
            </h2>
        </div>


        <table id="requisition-header-details-table" class="header-summary-table">

            <tbody>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.requisitionType.label" /></label></td>
                    <td class="value">
                        <g:if test="${requisition?.type}">
                            <format:metadata obj="${requisition?.type }"/>
                        </g:if>
                        <g:else>
                            <span class="fade">
                                ${warehouse.message(code:'default.none.label')}
                            </span>
                        </g:else>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.commodityClass.label" /></label></td>
                    <td class="value">
                        <g:if test="${requisition?.commodityClass}">
                            <format:metadata obj="${requisition?.commodityClass }"/>
                        </g:if>
                        <g:else>
                            <span class="fade">
                                ${warehouse.message(code:'default.none.label')}
                            </span>

                        </g:else>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="origin.id"> <g:if
                                test="${requisition.isWardRequisition()}">
                            <warehouse:message code="requisition.requestingWard.label" />
                        </g:if> <g:else>
                            <warehouse:message code="requisition.requestingDepot.label" />
                        </g:else>
                        </label>
                    </td>
                    <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                        <span id="origin.id">${requisition?.origin?.name }</span>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="destination.id">
                            <warehouse:message code="requisition.destination.label" />
                        </label>
                    </td>
                    <td class="value">

                        <span id="destination.id"> ${requisition?.destination?.name }</span>
                    </td>
                </tr>
                <g:if test="${requisition.isDepotRequisition()}">
                    <tr>
                        <td class="name"><label><warehouse:message
                                code="requisition.program.label" /></label></td>
                        <td class="value">
                            ${requisition?.recipientProgram }
                        </td>
                    </tr>
                </g:if>
                <tr class="prop">
                    <td class="name">
                        <label for="description">
                            <warehouse:message code="default.comments.label" />
                        </label>
                    </td>

                    <td class="value">
                        <span id="description">
                            ${requisition?.description?:warehouse.message(code:'default.none.label') }
                        </span>
                    </td>
                </tr>

            </table>
        </div>

        <div id="requisition-workflow" class="box dialog">
            <div style="line-height: 20px;">
                <h2>
                    <warehouse:message code="default.workflow.label" default="Requisition workflow"/>
                </h2>
            </div>
            <table>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message
                                code="default.version.label" default="Version"/></label>
                    </td>
                    <td class="value">
                        v${requisition?.version}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message
                                code="requisition.createdBy.label" /></label>
                    </td>
                    <td class="value">
                        ${requisition?.createdBy?.name}
                        <div class="fade">
                            <g:formatDate date="${requisition?.dateCreated }"/>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message
                                code="default.updatedBy.label" /></label>
                    </td>
                    <td class="value">
                        ${requisition?.updatedBy?.name }
                        <div class="fade">
                            <g:formatDate date="${requisition?.lastUpdated }"/>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>