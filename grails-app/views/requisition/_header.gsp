    <div id="requisition-header" class="box">
        <h2>
            <div class="box-button">
                <g:if test="${requisition?.id }">
                    <g:link controller="requisition" action="editHeader" id="${requisition?.id }" class="button icon edit">
                        ${warehouse.message(code:'requisition.button.edit.label', default: 'Edit header')}
                    </g:link>
                </g:if>
            </div>
            <warehouse:message code="requisition.label" default="Requisition"/>
        </h2>


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
                    <td class="name">
                        <label><warehouse:message
                                code="requisition.requisitionItemsByStatus.label" default="Item status"/></label>
                    </td>
                    <td class="value">
                        <g:set var="itemsByStatus" value="${requisition.requisitionItems.groupBy { it.status }}"/>
                        <g:each var="status" in="${itemsByStatus.keySet()}">
                            <div class="">
                                <format:metadata obj="${status}"/> (${itemsByStatus[status].size()})
                            </div>

                        </g:each>
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
                        <label for="origin.id">
                            <warehouse:message code="requisition.origin.label" />
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
                        <label><warehouse:message
                                code="default.lastUpdated.label" /></label>
                    </td>
                    <td class="value">
                        <g:formatDate date="${requisition?.lastUpdated }"/>
                        <div class="fade">
                            ${requisition?.updatedBy?.name }
                        </div>
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
            <h2>
                <warehouse:message code="default.workflow.label" default="Requisition workflow"/>
            </h2>
            <table>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="requisition.timeToProcess.label"/></label>
                    </td>
                    <td class="value">
                        <g:if test="${requisition.dateIssued && requisition.dateCreated}">
                            <g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.dateIssued, requisition.dateCreated)}"/>
                        </g:if>
                        <g:elseif test="${requisition.dateChecked && requisition.dateCreated}">
                            <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.dateChecked, requisition.dateCreated)}"/></i>
                        </g:elseif>
                        <g:elseif test="${requisition?.picklist?.datePicked && requisition.dateCreated}">
                            <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition?.picklist?.datePicked, requisition.dateCreated)}"/></i>
                        </g:elseif>
                        <g:elseif test="${requisition.dateVerified && requisition.dateCreated}">
                            <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.dateVerified, requisition.dateCreated)}"/></i>
                        </g:elseif>
                        <g:elseif test="${requisition.lastUpdated && requisition.dateCreated}">
                            <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.lastUpdated, requisition.dateCreated)}"/></i>
                        </g:elseif>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.requested.label" /></label></td>
                    <td class="value">
                        <g:formatDate date="${requisition?.dateRequested }"/>
                        <div class="fade">
                            ${requisition?.requestedBy?.name }
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message
                                code="requisition.created.label" /></label>
                    </td>
                    <td class="value">
                        <g:formatDate date="${requisition?.dateCreated }"/>
                        <div class="fade">
                            ${requisition?.createdBy?.name}
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name"><label><warehouse:message
                            code="requisition.verified.label" /></label></td>
                    <td class="value">
                        <g:if test="${requisition?.verifiedBy?.name }">
                            <g:formatDate date="${requisition?.dateVerified }"/>
                            <div class="fade">
                                ${requisition?.verifiedBy?.name }
                            </div>
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
                            code="picklist.picked.label" /></label></td>
                    <td class="value">
                        <g:if test="${requisition?.picklist?.picker }">
                            <g:formatDate date="${requisition?.picklist?.datePicked }"/>
                            <div class="fade">
                                ${requisition?.picklist?.picker?.name }
                            </div>
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
                            code="requisition.checked.label" /></label></td>
                    <td class="value">

                        <g:if test="${requisition?.checkedBy}">
                            <g:formatDate date="${requisition?.dateChecked }"/>
                            <div class="fade">
                                ${requisition?.checkedBy?.name }
                            </div>
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
                            code="requisition.issued.label" /></label></td>
                    <td class="value">

                        <g:if test="${requisition?.issuedBy}">
                            <g:formatDate date="${requisition?.dateIssued }"/>
                            <div class="fade">
                                ${requisition?.issuedBy?.name }
                            </div>
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
                            code="requisition.delivered.label" default="Delivered" /></label></td>
                    <td class="value">

                        <g:if test="${requisition?.deliveredBy}">
                            <g:formatDate date="${requisition?.dateDelivered }"/>
                            <div class="fade">
                                ${requisition?.deliveredBy?.name }
                            </div>
                        </g:if>
                        <g:else>
                            <span class="fade">
                                ${warehouse.message(code:'default.none.label')}
                            </span>
                        </g:else>
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
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
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
