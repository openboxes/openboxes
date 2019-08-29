<%@ page import="org.pih.warehouse.core.ActivityCode" defaultCodec="html" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="${params.print?'print':'custom' }" />
	<title><warehouse:message code="report.showTransactionReport.label" /></title>

	<style>
	.chosen-container-multi .chosen-choices li.search-field input[type="text"] {
		height: 26px;
		line-height: 26px;
	}

	.location-container {
		height: 120px;
	}

	.location-container .chosen-container-multi .chosen-choices {
		height: 120px !important;
		overflow-y: auto;
	}
	</style>
</head>
<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>


		<div class="yui-gf">

			<div class="yui-u first">

				<div class="sidebar">

					<div class="box">
						<h2><warehouse:message code="report.parameters.label" default="Parameters"/></h2>

						<div class="parameters">

							<div class="filter-list-item">
								<label>
									<warehouse:message code="report.location.label"/>
								</label>
								<g:selectLocation class="chzn-select-deselect filter"
												  name="locationId"
												  id="locationId"
												  activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"
												  noSelection="['':'']"
												  maxChars="75"
												  groupBy="locationType"
												  value="${command?.location?.id}"/>
							</div>
							<div class="filter-list-item">

								<label>
									<warehouse:message code="report.startDate.label"/>
								</label>

								<g:jqueryDatePicker id="startDate"
													name="startDate"
													cssClass="filter"
													value="${command?.startDate }"
													format="MM/dd/yyyy"
													autocomplete="off"/>

							</div>
							<div class="filter-list-item">

								<label>
									<warehouse:message code="report.endDate.label"/>
								</label>

								<g:jqueryDatePicker id="endDate"
													name="endDate"
													cssClass="filter"
													value="${command?.endDate }"
													format="MM/dd/yyyy"
													autocomplete="off"/>

							</div>
							<div class="filter-list-item">
								<label><warehouse:message code="category.label"/></label>
								<p>
									<g:selectCategory id="category"
													  class="chzn-select-deselect filter"
													  data-placeholder="Select a category"
													  name="category"
													  noSelection="['':'']"
													  value="${params?.category?.id}"
									/>
								</p>
								<p>
									<g:checkBox name="includeCategoryChildren" value="${params?.includeCategoryChildren}" checked="true"/>
									<label>${warehouse.message(code:'search.includeCategoryChildren.label', default: 'Include all products in all subcategories.')}</label>
								</p>
							</div>
						</div>
						<div class="buttons">
							<button class="submit-button button">
								<img src="${createLinkTo(dir:'images/icons/silk',file:'play_blue.png')}" />
								<g:set var="reportLabel" value="${g.message(code:'default.report.label', default: 'Report')}"/>
								${g.message(code: 'default.run.label', args: [reportLabel])}
							</button>
							<button class="download-button button">
								<img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
								<g:set var="dataLabel" value="${g.message(code:'default.data.label', default: 'Data')}"/>
								${g.message(code: 'default.download.label', args: [dataLabel])}
							</button>
						</div>
					</div>


				</div>
			</div>
			<div class="yui-u">
				<div class="box">
					<h2>
						${warehouse.message(code:'report.transactionReport.label', default: "Transaction Report")}
					</h2>

                    <table id="transactionReport" class="dataTable">
                        <thead>
                        <tr>
                            <th><warehouse:message code="product.productCode.label"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="transactionReport.cycleCount.label" default="Cycle Count"/></th>
                            <th class="center"><warehouse:message code="transactionReport.openingBalance.label" default="Opening Balance"/></th>
                            <th class="center"><warehouse:message code="transactionReport.inbound.label" default="Inbound"/></th>
                            <th class="center"><warehouse:message code="transactionReport.outbound.label" default="Outbound"/></th>
                            <th class="center"><warehouse:message code="transactionReport.adjustments.label" default="Adjustments"/></th>
                            <th class="center"><warehouse:message code="transactionReport.closingBalance.label" default="Closing Balance"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                        <tfoot>
                        <tr>
                            <th><warehouse:message code="product.productCode.label"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="transactionReport.cycleCount.label" default="Cycle Count"/></th>
                            <th class="center"><warehouse:message code="transactionReport.openingBalance.label" default="Opening Balance"/></th>
                            <th class="center"><warehouse:message code="transactionReport.inbound.label" default="Inbound"/></th>
                            <th class="center"><warehouse:message code="transactionReport.outbound.label" default="Outbound"/></th>
                            <th class="center"><warehouse:message code="transactionReport.adjustments.label" default="Adjustments"/></th>
                            <th class="center"><warehouse:message code="transactionReport.closingBalance.label" default="Closing Balance"/></th>
                        </tr>
                        </tfoot>
                    </table>
					<div class="buttons right">
						<a href="javascript:void(0);" class="btn-show-dialog button"
						   data-title="${g.message(code:'default.show.label', args: [g.message(code: 'default.metadata.label')])}"
						   data-url="${request.contextPath}/json/showTransactionReportMetadata">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'application_key.png')}" />&nbsp;
							<g:message code="default.show.label" args="[g.message(code: 'default.metadata.label')]"/>
						</a>
					</div>
				</div>
			</div>
		</div>
	</div>

<script>

    function handleAjaxError(xhr, status, error) {
        if (status === 'timeout') {
            errorMessage('The server took too long to send the data.');
        } else {
            if (xhr.responseText) {
                var errorMessage = JSON.parse(xhr.responseText).errorMessage;
                alert(errorMessage);
            }
        }
    }

    function destroyDataTable() {
    	$('#transactionReport').dataTable().fnDestroy();
	}

	function initializeDataTable() {
		var options = {
			"bDestroy": true,
			"bProcessing": true,
			"iDisplayLength": 25,
			"bSearch": false,
			"bScrollCollapse": true,
			"bJQueryUI": true,
			"bAutoWidth": true,
			"bScrollInfinite": true,
			"sScrollY": 500,
			"sPaginationType": "two_button",
			"sAjaxSource": "${request.contextPath}/json/getTransactionReport",
			"fnServerParams": function ( data ) {
				data.push({ name: "location.id", value: $("#locationId").val() });
				data.push({ name: "startDate", value: $("#startDate").val() });
				data.push({ name: "endDate", value: $("#endDate").val() });
				data.push({ name: "category", value: $("#category").val() });
				if($('#includeCategoryChildren').is(':checked')) {
					data.push({ name: "includeCategoryChildren", value: $("#includeCategoryChildren").val() });
				}
			},
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax({
                    "dataType": 'json',
                    "type": "POST",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 30000,
                    "error": handleAjaxError
                })
            },
			"oLanguage": {
				"sZeroRecords": "No records found",
				"sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> Loading... "
			},
			"aLengthMenu": [
				[5, 15, 25, 50, 100, 500, 1000, -1],
				[5, 15, 25, 50, 100, 500, 1000, "All"]
			],
			"aoColumns": [
				{"mData": "Code"},
				{"mData": "Name", "sWidth": "250px"},
				{"mData": "Cycle Count"},
				{"mData": "Opening Balance", "sType": 'numeric'},
				{"mData": "Inbound", "sType": 'numeric'},
				{"mData": "Outbound", "sType": 'numeric'},
				{"mData": "Adjustments", "sType": 'numeric'},
				{"mData": "Closing Balance", "sType": 'numeric'}
			],
			"bUseRendered": false,
			"dom": '<"top"i>rt<"bottom"flp><"clear">',
            "fnRowCallback": function (nRow, aData) {
                $('td:eq(3)', nRow).html(Number(aData["Opening Balance"]).toLocaleString('en-US'));
                $('td:eq(4)', nRow).html(Number(aData["Inbound"]).toLocaleString('en-US'));
                $('td:eq(5)', nRow).html(Number(aData["Outbound"]).toLocaleString('en-US'));
                $('td:eq(6)', nRow).html(Number(aData["Adjustments"]).toLocaleString('en-US'));
                $('td:eq(7)', nRow).html(Number(aData["Closing Balance"]).toLocaleString('en-US'));

                if (aData["Inbound"] > 0) {
                  $('td:eq(4)', nRow).addClass('credit')
                }

                if (aData["Outbound"] > 0) {
                  $('td:eq(5)', nRow).addClass('debit')
                }

                if (aData["Adjustments"] > 0) {
                  $('td:eq(6)', nRow).addClass('credit')
                } else if (aData["Adjustments"] < 0) {
                  var normalized = 0 - Number(aData["Adjustments"]);
                  $('td:eq(6)', nRow).html(normalized.toLocaleString('en-US')).addClass('debit')
			    }

            return nRow;
          }

		};

		$('#transactionReport').dataTable(options);
	}


	$(document).ready(function() {

		$('#transactionReport').dataTable({"bJQueryUI": true});

		$(".submit-button").click(function(event){
			event.preventDefault();
			var today = new Date();
			var locationId = $("#locationId").val();
			var startDate = $("#startDate").val();
			var endDate = $("#endDate").val();
			var validated = true;

			// FIXME Needed basic form validation to prevent some unwanted issues on the backend
			if (!endDate || !startDate || !locationId) {
				alert("All fields are required");
				validated = false
			}

			startDate = Date.parse(startDate);
			endDate = Date.parse(endDate);

			if (startDate > endDate) {
				alert("Start date must occur before end date");
				validated = false
			}

			if (endDate > today) {
				alert("End date must occur on or before today");
				validated = false
			}

			if (validated) {
				initializeDataTable();
			}
		});

		$(".download-button").click(function(event) {
			event.preventDefault();
			var params = {
				locationId: $("#locationId").val(),
				startDate: $("#startDate").val(),
				endDate: $("#endDate").val(),
				category: $("#category").val(),
				includeCategoryChildren: $("#includeCategoryChildren").val(),
				format: "text/csv"
			};
			var queryString = $.param(params);
			window.location.href = '${request.contextPath}/json/getTransactionReport?' + queryString;
		});

		$('#transactionReport tbody').on('click', 'tr', function () {
			var nTds = $('td', this);
			var productCode = $(nTds[0]).text();
			var productName = $(nTds[1]).text();
			var params = {
				productCode: productCode,
				locationId: $("#locationId").val(),
				startDate: $("#startDate").val(),
				endDate: $("#endDate").val()
			};

			if (productCode && productName) {
				var queryString = $.param(params);
				var url = "${request.contextPath}/report/showTransactionReportDialog?" + queryString;
				openModalDialog("#dlgShowDialog", productCode + " " + productName, 800, null, url);
			}
		} );

	});

</script>
    </body>
</html>
