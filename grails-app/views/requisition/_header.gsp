    <div id="requisition-header" class="box dialog">
        <div style="line-height: 20px;">
            <h2>

                <%--
                <a class="toggle" href="javascript:void(0);">
                    <img id="toggle-icon" src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
                </a>
                <h3 style="display: inline" class="toggle"><label>${requisition?.requestNumber }</label> ${requisition?.name }</h3>
                &nbsp;
                --%>
                <warehouse:message code="requisition.label" default="Requisition"/>
                <div class="right">
                    <g:if test="${requisition?.id }">
                        <g:link controller="requisition" action="editHeader" id="${requisition?.id }" class="button icon edit">
                            ${warehouse.message(code:'requisition.button.edit.label', default: 'Edit header')}
                        </g:link>
                    </g:if>
                </div>
                <div class="clear-all"></div>
            </h2>
        </div>


        <table id="requisition-header-details-table" class="header-summary-table">

            <tbody>

                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.requisitionNumber.label" /></label></td>
                    <td class="value">
                        ${requisition.requestNumber}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.status.label" /></label></td>
                    <td class="value">
                        <format:metadata obj="${requisition?.status }"/>

                    </td>
                </tr>

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
                    <td class="name"><label><warehouse:message
                            code="requisition.dateRequested.label" /></label></td>
                    <td class="value">
                        <g:formatDate date="${requisition?.dateRequested }"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label" /></label></td>
                    <td class="value">
                        <g:formatDate date="${requisition?.requestedDeliveryDate }"/>

                    </td>
                </tr>
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

                    <%--
                    <a class="toggle" href="javascript:void(0);">
                        <img id="toggle-icon" src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
                    </a>
                    <h3 style="display: inline" class="toggle"><label>${requisition?.requestNumber }</label> ${requisition?.name }</h3>
                    &nbsp;
                    --%>
                    <warehouse:message code="default.workflow.label" default="Requisition workflow"/>
                </h2>
            </div>
            <table>
                <%--
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="default.version.label" default="Version" /></label>
                    </td>
                    <td class="value">
                        v${requisition?.version }
                    </td>
                </tr>
                --%>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.requestedBy.label" /></label></td>
                    <td class="value">
                        ${requisition?.requestedBy?.name }
                        <div class="fade">
                            <g:formatDate date="${requisition?.dateRequested }"/>
                        </div>
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
                    <td class="name"><label><warehouse:message
                            code="requisition.verifiedBy.label" /></label></td>
                    <td class="value">
                        ${requisition?.verifiedBy?.name }
                        <div class="fade">
                            <g:formatDate date="${requisition?.dateVerified }"/>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="picklist.picker.label" /></label></td>
                    <td class="value">
                        ${requisition?.picklist?.picker?.name }
                        <div class="fade">
                            <g:formatDate date="${requisition?.picklist?.datePicked }"/>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.checkedBy.label" /></label></td>
                    <td class="value">
                        ${requisition?.checkedBy?.name }
                        <div class="fade">
                            <g:formatDate date="${requisition?.dateChecked }"/>
                        </div>
                    </td>
                </tr>
                <%--
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.receivedBy.label" /></label></td>
                    <td class="value">
                        ${requisition?.receivedBy?.name }
                        <div class="fade">
                            <g:formatDate date="${requisition?.dateReceived }"/>
                        </div>
                    </td>
                </tr>
                --%>
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
                <tr class="prop">
                    <td class="name"><label><warehouse:message code="transactions.label" /></label></td>
                    <td class="value">
                        <g:each var="transaction" in="${requisition?.transactions }">
                            <div>
                                <g:link controller="inventory" action="showTransaction" id="${transaction?.id}">
                                    ${transaction.transactionNumber}
                                </g:link>
                            </div>
                        </g:each>
                        <g:unless test="${requisition?.transactions}">
                            <warehouse:message code="default.none.label"/>
                        </g:unless>


                    </td>
                </tr>

            </tbody>
        </table>
    </div>

    <script type="text/javascript">

    $(function () {
        $(".toggle").toggle(function() {
            // hides children divs if shown, shows if hidden
            $details = $("#requisition-header").slideUp();
            var icon = $("#toggle-icon");
            icon.attr('src', "${createLinkTo(dir: 'images/icons/silk', file: 'section_expanded.png')}");
            console.log(icon);
        }, function() {
            $details = $("#requisition-header").slideDown();
            var icon = $("#toggle-icon");
            icon.attr('src', "${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}");
            console.log(icon);
        });
    });

</script>