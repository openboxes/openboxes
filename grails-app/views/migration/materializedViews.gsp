<div id="tabs-5">
    <div class="box">
        <h2><g:message code="data.materializedViews.label" default="Materialized Views"/></h2>
        <table>
            <thead>
            <tr>
                <th>Table</th>
                <th>Count</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr class="prop">
                <td class="name">Product Demand</td>
                <td class="value">
                    ${productDemandCount}
                </td>
                <td>
                    <g:remoteLink controller="report" action="refreshProductDemand" class="button"
                                  onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">Product Availability</td>
                <td class="value">
                    ${productAvailabilityCount}
                </td>
                <td>
                    <div class="button-group">
                        <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="false"
                           data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                           data-url="${request.contextPath}/migration/productAvailability">
                            <g:message code="default.button.list.label"/>
                        </a>
                        <g:remoteLink controller="report" action="refreshProductAvailability" class="button"
                                onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
