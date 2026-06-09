<%@ page defaultCodec="html" %>
<%-- Parameters: title, documentNumber, documentName, origin, destination, shipDate, receivedDate --%>
<div id="header" class="header">
    <table class="w100 fixed-layout no-border-table">
        <tr>
            <td colspan="2" class="b-0">
                <table class="w100">
                    <tr>
                        <td class="left top" width="5%">
                            <g:displayReportLogo/>
                        </td>
                        <td class="left top">
                            <h1 class="m-5">${title}</h1>
                            <div class="m-5 large">${documentNumber} ${documentName}</div>
                            <div class="m-0">
                                <g:if test="${documentNumber}">
                                    <img src="${createLink(controller: 'product', action: 'barcode', params: [data: documentNumber, height: 30, format: 'CODE_128'])}"/>
                                </g:if>
                            </div>
                        </td>
                        <td class="right" width="25%">
                            <table class="w100 no-wrap">
                                <tr>
                                    <td><label><warehouse:message code="requisition.origin.label"/>:</label></td>
                                    <td>${origin?.name}</td>
                                </tr>
                                <tr>
                                    <td><label><warehouse:message code="requisition.destination.label"/>:</label></td>
                                    <td>${destination?.name}</td>
                                </tr>
                                <g:if test="${requestedBy}">
                                    <tr>
                                        <td><label><warehouse:message code="requisition.requestedBy.label"/>:</label></td>
                                        <td>${requestedBy}</td>
                                    </tr>
                                </g:if>
                                <g:if test="${dateRequested}">
                                    <tr>
                                        <td><label><warehouse:message code="requisition.date.label"/>:</label></td>
                                        <td><g:formatDate date="${dateRequested}" format="d MMMMM yyyy  hh:mma"/></td>
                                    </tr>
                                </g:if>
                                <tr>
                                    <td><label><warehouse:message code="deliveryNote.shipDate.label" default="Ship date"/>:</label></td>
                                    <td><g:formatDate date="${shipDate}" format="d MMMMM yyyy  hh:mma"/></td>
                                </tr>
                                <tr>
                                    <td><label><g:message code="deliveryNote.receivedDate.label" default="Received date"/>:</label></td>
                                    <td><g:formatDate date="${receivedDate}" format="d MMMMM yyyy  hh:mma"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>

