<%@ page defaultCodec="html" %>
<%-- Parameters: origin, destination --%>
<g:if test="${origin?.address || destination?.address}">
    <div id="address" class="page-content">
        <table class="w100 fixed-layout b-0">
            <tr>
                <td class="b-0">
                    <h2><g:message code="deliveryNote.receivedFrom.label" default="Received From"/></h2>
                    <table class="no-border-table w100">
                        <tr>
                            <td>
                                <table class="w100 no-wrap left">
                                    <tr><td><strong>${origin?.name}</strong></td></tr>
                                    <tr><td>${origin?.address?.address}</td></tr>
                                    <g:if test="${origin?.address?.address2}">
                                        <tr><td>${origin?.address?.address2}</td></tr>
                                    </g:if>
                                    <tr>
                                        <td>
                                            ${origin?.address?.city}
                                            ${origin?.address?.stateOrProvince}
                                            ${origin?.address?.postalCode}
                                        </td>
                                    </tr>
                                    <tr><td>${origin?.address?.country}</td></tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>

                <td class="b-0 top">
                    <h2><g:message code="deliveryNote.deliveredTo.label" default="Delivered To"/></h2>
                    <table class="no-border-table w100">
                        <tr>
                            <td>
                                <table class="w100 no-wrap">
                                    <tr><td><strong>${destination?.name}</strong></td></tr>
                                    <tr><td>${destination?.address?.address}</td></tr>
                                    <g:if test="${destination?.address?.address2}">
                                        <tr><td>${destination?.address?.address2}</td></tr>
                                    </g:if>
                                    <tr>
                                        <td>
                                            ${destination?.address?.city}
                                            ${destination?.address?.stateOrProvince}
                                            ${destination?.address?.postalCode}
                                        </td>
                                    </tr>
                                    <tr><td>${destination?.address?.country}</td></tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</g:if>

