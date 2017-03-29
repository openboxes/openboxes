<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showShippingReport.label" /></title>    
        <style media="print">
        	body, td, th, div { font-family: 'Times New Roman'; }
        </style>
        <style>
			table { -fs-table-paginate: paginate; }			
			.filter { padding-right: 30px; border-right: 1px solid lightgrey; }
        	/*th { text-transform: uppercase; }*/

        </style>
    </head>    
    <body>

        <g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>

        <div class="summary">

            <div class="right">
                <g:link class="button" controller="report" action="showShippingReport" params="['downloadFormat':'html','shipment.id':command?.shipment?.id]">
                    <warehouse:message code="report.preview.html.label" default="HTML"/>
                </g:link>
                <g:link class="button" controller="report" action="showShippingReport" params="[downloadFormat:'pdf','shipment.id':command?.shipment?.id]">
                    <warehouse:message code="report.preview.pdf.label" default="PDF"/>
                </g:link>
                <%--
                <g:link class="button" controller="report" action="showShippingReport" params="[downloadFormat:'docx','shipment.id':command?.shipment?.id]">
                    <warehouse:message code="report.exportAs.docx.label"/>
                </g:link>
                --%>
            </div>
            <div class="clearfix"></div>
            <h1><warehouse:message code="report.showShippingReport.label" /></h1>

        </div>

        <div class="yui-gf">
            <div class="yui-u first">
                <div class="box">
                    <g:form controller="report" action="showShippingReport" method="GET">
                        <h2>
                            <g:message code="report.parameters.label" default="Report Parameters"/>
                        </h2>
                        <table>
                            <tr class="prop">
                                <td>
                                    <label>
                                        <warehouse:message code="shipment.label"/>
                                    </label>
                                    <g:selectShipment id="shipment-filter" class="chzn-select-deselect filter"
                                                      name="shipment.id" noSelection="['null':'']" value="${command?.shipment?.id}"/>

                                </td>
                                <%--
                                <span class="filter">
                                    <label>Start date</label>
                                    <g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy"/>
                                </span>
                                <span class="filter">
                                    <label>End date</label>
                                    <g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy"/>
                                </span>
                                <span>
                                    <button type="submit" class="btn">Run Report</button>
                                </span>
                                --%>
                            </tr>

                        </table>

                    </g:form>
                </div>

            </div>
            <div class="yui-u">
                <div class="box">
                    <h2><g:message code="report.preview.label" default="Preview Report"/></h2>
                    <g:if test="${command?.shipment}">
                        <embed src="${g.createLink(controller: "report", action: "downloadShippingReport",
                                params: [downloadFormat:params.downloadFormat, 'shipment.id':command?.shipment?.id])}" width="100%" height="60%">
                        </embed>
                    </g:if>
                    <g:else>
                        <div class="empty fade center">
                            <warehouse:message code="report.selectShipment.label"/>
                        </div>
                    </g:else>
                </div>
                <g:if test="${command.shipment}">
                    <div class="buttons center">
                        <g:link class="button" target="_blank" controller="report" action="downloadShippingReport" params="['downloadFormat':params.downloadFormat,'shipment.id':command?.shipment?.id]">
                            <warehouse:message code="report.download.label" default="Download"/>
                        </g:link>

                    </div>
                </g:if>
            </div>
        </div>



        <script>
            $(document).ready(function() {
                $(".filter").change(function() {
                    $(this).closest("form").submit();
                });
            });
        </script>
    </body>
</html>