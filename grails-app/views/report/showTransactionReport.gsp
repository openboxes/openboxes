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
								<g:selectLocation class="chzn-select-deselect filter" name="locationId" id="locationId" activityCode="MANAGE_INVENTORY"
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
								${g.message(code: 'default.button.run.label')}
							</button>
							<button class="download-button button">
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
								<th class="center"><warehouse:message code="inventoryBalance.inbound.label" default="Transferred In"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.outbound.label" default="Transferred Out"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.expired.label" default="Expired"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.damaged.label" default="Damaged"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.adjusted.label" default="Adjustment"/></th>
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
								<th class="center"><warehouse:message code="inventoryBalance.inbound.label" default="Transferred In"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.outbound.label" default="Transferred Out"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.expired.label" default="Expired"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.damaged.label" default="Damaged"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.adjusted.label" default="Adjustment"/></th>
								<th class="center"><warehouse:message code="inventoryBalance.closingBalance.label" default="Closing Balance"/></th>
							</tr>
							</tfoot>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>

<script>

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
				{"mData": "Code"},
				{"mData": "Name", "sWidth": "250px"},
				{"mData": "Cycle Count"},
				{"mData": "Opening Balance", "sType": 'numeric'},
				{"mData": "Transferred In", "sType": 'numeric'},
				{"mData": "Transferred Out", "sType": 'numeric'},
				{"mData": "Expired", "sType": 'numeric'},
				{"mData": "Damaged", "sType": 'numeric'},
				{"mData": "Adjustment", "sType": 'numeric'},
				{"mData": "Closing Balance", "sType": 'numeric'}
			],
			"bUseRendered": false,
			"dom": '<"top"i>rt<"bottom"flp><"clear">',
            "fnRowCallback": function( nRow, aData) {
                $('td:eq(3)', nRow).html(Number(aData["Opening Balance"]).toLocaleString('en-US'));
                $('td:eq(4)', nRow).html(Number(aData["Transferred In"]).toLocaleString('en-US'));
                $('td:eq(5)', nRow).html(Number(aData["Transferred Out"]).toLocaleString('en-US'));
                $('td:eq(6)', nRow).html(Number(aData["Expired"]).toLocaleString('en-US'));
                $('td:eq(7)', nRow).html(Number(aData["Damaged"]).toLocaleString('en-US'));
                $('td:eq(8)', nRow).html(Number(aData["Adjustment"]).toLocaleString('en-US'));
                $('td:eq(9)', nRow).html(Number(aData["Closing Balance"]).toLocaleString('en-US'));

                if (aData["Transferred In"] > 0) {
                  $('td:eq(4)', nRow).css('color', 'green')
                }

			    if (aData["Transferred Out"] > 0) {
				  $('td:eq(5)', nRow).css('color', 'red')
			    }

                if (aData["Expired"] > 0) {
                  $('td:eq(6)', nRow).css('color', 'red')
                }

                if (aData["Damaged"] > 0) {
                  $('td:eq(7)', nRow).css('color', 'red')
                }

                if (aData["Adjustment"] > 0) {
                  $('td:eq(8)', nRow).css('color', 'green')
                } else if (aData["Adjustment"] < 0) {
                  $('td:eq(8)', nRow).html(Number(aData["Adjustment"]).toString().replace("-", "(")).append(")").css('color', 'red')
			    }

            return nRow;
          }
			//"aaSorting": [[ 2, "desc" ], [3, "desc"]],

		};

		$('#inventoryBalanceReport').dataTable(options);
	}


	$(document).ready(function() {


		$(".submit-button").click(function(event){
			event.preventDefault();
			initializeDataTable();
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
			var locationId = $("#locationId").val();
			var startDate = $("#startDate").val();
			var endDate = $("#endDate").val();

			var params = {
				productCode: productCode,
				locationId: $("#locationId").val(),
				startDate: $("#startDate").val(),
				endDate: $("#endDate").val()
			};

			var queryString = $.param(params);
			var url = "${request.contextPath}/report/showTransactionReportDialog?" + queryString;
			openModalDialog("#dlgShowDialog", productCode + " " + productName, 800, null, url);
		} );

	});

</script>
    </body>
</html>
