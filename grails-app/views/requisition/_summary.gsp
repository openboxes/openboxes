<%@ page import="org.pih.warehouse.requisition.RequisitionItemType" %>
<g:if test="${requisition?.id}">
    <div id="requisition-summary" class="summary">
        <table>
            <tbody>
                <tr>
                    <td class="top" width="1%">
                        <g:render template="../requisition/actions" model="[requisition:requisition]" />
                    </td>
                    <td class="center" width="1%">
                        <g:if test="${requisition?.requestNumber }">
                            <div class="box-barcode">
                                <img src="${createLink(controller:'product',action:'barcode',params:[data:requisition?.requestNumber,width:100,height:30,format:'CODE_128']) }"/>
                                <div class="barcode">${requisition.requestNumber}</div>
                            </div>
                        </g:if>
                    </td>
                    <td class="middle">
                        <g:if test="${requisition?.id}">
                            <div class="title" id="name">
                                ${requisition?.name }
                            </div>
                            <g:if test="${requisition.lastUpdated}">
                                <div class="fade">
                                    <warehouse:message code="default.lastUpdated.label" default="Last updated"/>
                                    <g:prettyDateFormat date="${requisition.lastUpdated}"/>
                                </div>
                            </g:if>
                        </g:if>
                        <g:else>
                            <div class="title" id="name"><g:message code="requisition.new.label"/></div>
                        </g:else>


                        </div>
                    </td>
                    <td>
                        <div class="top title right">
                            <div class="tag tag-alert">
                                <format:metadata obj="${requisition?.status }"/>
                            </div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <div id="flow-header" class="buttonBar">
        <div class="wizard-box">
            <div class="wizard-steps">
                <g:set var="currentState" value="${actionName}"/>

                <g:if test="${requisition.id}">
                    <g:set var="wizardSteps" value="${['show':'show', 'edit':'edit', 'review':'review', 'pick':'pick', 'confirm':'confirm', 'transfer':'transfer']}"/>
                </g:if>
                <g:else>
                    <g:set var="wizardSteps" value="${['create':'create', 'edit':'edit', 'review':'review', 'pick':'pick', 'confirm':'confirm', 'transfer':'transfer']}"/>
                </g:else>

                <g:each var="wizardStep" in="${wizardSteps}" status="status">

                    <g:set var="index" value="${wizardSteps?.keySet()?.findIndexOf{ it == currentState}}"/>

                    <g:if test="${index == status}">
                        <g:set var="styleClass" value="active-step"/>
                    </g:if>
                    <g:elseif test="${index > status}">
                        <g:set var="styleClass" value="completed-step"/>
                    </g:elseif>
                    <g:else>
                        <g:set var="styleClass" value=""/>
                    </g:else>
                    <div class="${styleClass}">
                        <g:if test="${requisition?.id}">
                            <g:link controller="requisition" action="${wizardStep?.value}" event="${wizardStep?.value}" id="${requisition?.id}">
                                <span>${status+1}</span>
                                <warehouse:message code="requisition.wizard.${wizardStep?.value}.label" default="${wizardStep?.value}"/>
                            </g:link>
                        </g:if>
                        <g:else>
                            <a href="#">
                                <span>${status+1}</span>
                                <warehouse:message code="requisition.wizard.${wizardStep?.value}.label" default="${wizardStep?.value}"/>
                            </a>
                        </g:else>
                    </div>
                </g:each>
            </div>
            <g:if test="${requisition.id}">
                <div class="right button-group">
                    <g:link controller="picklist" action="renderPdf" id="${requisition?.id}" target="_blank" class="button">
                        <img src="${resource(dir: 'images/icons', file: 'pdf.png')}" />&nbsp;
                        ${warehouse.message(code: 'picklist.button.print.label', default: 'Download pick list')}
                    </g:link>
                    <g:link controller="picklist" action="print" id="${requisition?.id}" target="_blank" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                        ${warehouse.message(code: 'picklist.button.print.label', default: 'Print pick list')}
                    </g:link>
                    <g:link controller="deliveryNote" action="print" id="${requisition?.id}" target="_blank" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                        ${warehouse.message(code: 'deliveryNote.button.print.label', default: 'Print delivery note')}
                    </g:link>
                </div>
            </g:if>
        </div>
        <g:if test="${!(requisition?.origin == session?.warehouse || requisition?.destination == session?.warehouse)}">
            <div class="error">
                <warehouse:message code="requisition.wrongLocation.message" default="CAUTION: You appear to be logged into the wrong location! Making any changes to this requisition within this location may cause it to become invalid."/>
            </div>
        </g:if>
    </div>
</g:if>