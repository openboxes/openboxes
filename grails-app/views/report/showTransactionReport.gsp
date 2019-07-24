<%@ page defaultCodec="html" %>
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
								<g:selectLocation class="chzn-select-deselect filter" name="locationId" id="locationId"
												  noSelection="['':'']" maxChars="75" groupBy="locationType" value="${command?.location?.id}"/>
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

						</div>
						<div class="buttons">
							<button class="submit-button button">
								<img src="${createLinkTo(dir:'images/icons/silk',file:'play_blue.png')}" />
								${g.message(code: 'default.button.run.label')}
							</button>
							<button class="download-button button">
								<img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
								${g.message(code: 'default.button.download.label')}
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

					<div>
						<table id="inventoryBalanceReport" class="dataTable">

							<thead>
							<tr>
								<th><warehouse:message code="product.productCode.label"/></th>
								<th><warehouse:message code="product.label"/></th>
								<th><warehouse:message code="inventoryBalance.cycleCount.label" default="Cycle Count"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.openingBalance.label" default="Opening Balance"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.inbound.label" default="Inbound"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.outbound.label" default="Outbound"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.expired.label" default="Expired"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.damaged.label" default="Damaged"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.adjusted.label" default="Adjusted"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.closingBalance.label" default="Closing Balance"/></th>
							</tr>
							</thead>
							<tbody>
							<tr>
								<td colspan="11" class="empty fade center">
									${g.message(code: 'default.noResults.label')}

								</td>
							</tr>
							</tbody>
							<tfoot>
							<tr>
								<th><warehouse:message code="product.productCode.label"/></th>
								<th><warehouse:message code="product.label"/></th>
								<th><warehouse:message code="inventoryBalance.cycleCount.label" default="Cycle Count"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.openingBalance.label" default="Opening Balance"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.inbound.label" default="Inbound"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.outbound.label" default="Outbound"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.expired.label" default="Expired"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.damaged.label" default="Damaged"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.adjusted.label" default="Adjusted"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.closingBalance.label" default="Closing Balance"/></th>
							</tr>
							</tfoot>
						</table>

					</div>
				</div>
				<g:isSuperuser>
					<div class="box button-bar fade">
						<div class="right">
						<g:link controller="report" action="refreshTransactionFact" class="button">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'reload.png')}" />
							${message(code:"default.button.refresh.label")} ${message(code:"default.data.label")} ${message(code: "default.now.label")}
						</g:link>

						</div>

						<g:set var="transactionCount" value="${org.pih.warehouse.reporting.TransactionFact.count()}"/>
						<g:set var="maxTransactionDate" value="${org.pih.warehouse.reporting.TransactionFact.maxTransactionDate.list()}"/>
						<g:if test="${maxTransactionDate}">
							<div>Data includes <b><g:formatNumber number="${transactionCount}" maxFractionDigits="0"/></b> transactions
							with latest transaction from <b><g:prettyDateFormat date="${maxTransactionDate}"/></b>.</div>
							<div>Data will be refreshed
								<b><g:prettyDateFormat date="${nextFireTime}"/></b>.</div>
						</g:if>
					</div>

				</g:isSuperuser>

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
        destroyDataTable();
    }

    function destroyDataTable() {
    	$('#inventoryBalanceReport').dataTable().fnDestroy();
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
			"sAjaxSource": "${request.contextPath}/json/getInventoryBalanceReport",
			"fnServerParams": function ( data ) {
				data.push({ name: "location.id", value: $("#locationId").val() });
				data.push({ name: "startDate", value: $("#startDate").val() });
				data.push({ name: "endDate", value: $("#endDate").val() });
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
			//"fnInitComplete": fnInitComplete,
			//"iDisplayLength" : -1,
			"aLengthMenu": [
				[5, 15, 25, 50, 100, 500, 1000, -1],
				[5, 15, 25, 50, 100, 500, 1000, "All"]
			],
			"aoColumns": [
				{"mData": "productCode"},
				{"mData": "productName"},
				{"mData": "cycleCountOccurred"},
				{"mData": "balanceOpening", "sType": 'numeric'},
				{"mData": "quantityInbound", "sType": 'numeric'},
				{"mData": "quantityOutbound", "sType": 'numeric'},
				{"mData": "quantityExpired", "sType": 'numeric'},
				{"mData": "quantityDamaged", "sType": 'numeric'},
				{"mData": "quantityAdjusted", "sType": 'numeric'},
				{"mData": "balanaceClosing", "sType": 'numeric'}
			],
			"bUseRendered": false,
			"dom": '<"top"i>rt<"bottom"flp><"clear">'
			//"aaSorting": [[ 2, "desc" ], [3, "desc"]],

		};

		$('#inventoryBalanceReport').dataTable(options);
	}


	$(document).ready(function() {


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
				format: "text/csv"
			};
			var queryString = $.param(params);
			window.location.href = '${request.contextPath}/json/getInventoryBalanceReport?' + queryString;
		});

		$('#inventoryBalanceReport tbody').on('click', 'tr', function () {
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
