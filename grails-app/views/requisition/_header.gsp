<fieldset>
    <legend>
        <g:if test="${requisition?.id }">
            <div class="box">
                <a class="toggle" href="javascript:void(0);">
                    <img id="toggle-icon" src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
                </a>
                <h3 style="display: inline" class="toggle"><label>${requisition?.requestNumber }</label> ${requisition?.name }</h3>
                &nbsp;
                <g:link controller="requisition" action="editHeader" id="${requisition?.id }" class="edit">
                    ${warehouse.message(code:'requisition.button.edit.label')}
                </g:link>
            </div>
        </g:if>
    </legend>

    <div id="requisition-header" class="dialog ui-validation">
        <div class="yui-g">
            <div class="yui-u first">

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
                                code="requisition.requisitionType.label" /></label></td>
                        <td class="value">
                            <format:metadata obj="${requisition?.type }"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name"><label><warehouse:message
                                code="requisition.commodityClass.label" /></label></td>
                        <td class="value">
                            <format:metadata obj="${requisition?.commodityClass }"/>
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
                            ${requisition?.origin?.name }
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name"><label><warehouse:message
                                code="requisition.requestedBy.label" /></label></td>
                        <td class="value">
                            ${requisition?.requestedBy?.name }
                        </td>
                    </tr>

                    </tbody>
                </table>
            </div>

            <div class="yui-u">
                <table class="">
                    <tbody>
                    <tr class="prop">
                        <td class="name">
                            <label for="destination.id">
                                <warehouse:message code="requisition.destination.label" />
                            </label>
                        </td>
                        <td class="value">
                            ${session?.warehouse?.name }
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <label><warehouse:message
                                    code="requisition.processedBy.label" /></label>
                        </td>
                        <td class="value">
                            ${requisition?.createdBy?.name?:session?.user?.name }
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
                                <warehouse:message code="default.description.label" />
                            </label>
                        </td>

                        <td class="value">
                            ${requisition?.description?:warehouse.message(code:'default.none.label') }
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</fieldset>
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