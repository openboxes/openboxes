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

	/* Covers the report while it's being generated, so that it can't be run again until it's done (see #6201) */
	.body {
		position: relative;
	}

	.report-overlay {
		display: none;
		position: absolute;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
		z-index: 1000;
		background-color: rgba(128, 128, 128, 0.5);
		cursor: wait;
	}

	.report-overlay.active {
		display: block;
	}

	.report-overlay-message {
		position: sticky;
		top: 40%;
		margin: 15% auto 0;
		width: 320px;
		padding: 20px;
		background-color: #fff;
		border-radius: 4px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
		text-align: center;
		color: #333;  /* Set explicitly so that the site's alert styles don't change the text color */
	}

	.report-overlay-title {
		font-size: 1.2em;
		font-weight: bold;
	}

	.report-overlay-title img {
		vertical-align: middle;
		margin-right: 5px;
	}

	.report-overlay-elapsed {
		margin: 10px 0;
		font-size: 1.1em;
	}

	.report-overlay-hint {
		color: #666;
	}

	/* Transaction report table. Scoped to the wrapper because DataTables moves the header and footer into their
	   own tables when the table scrolls. */
	#transactionReport_wrapper th {
		font-weight: 600;
	}

	/* Same horizontal padding in the header, body and footer so that the columns line up */
	#transactionReport_wrapper th,
	#transactionReport_wrapper td {
		padding: 6px 10px;
	}

	/* Keep the sort icon on the same line as the column name */
	#transactionReport_wrapper .DataTables_sort_wrapper {
		display: flex;
		align-items: center;
		gap: 2px;
	}

	#transactionReport_wrapper th.right .DataTables_sort_wrapper {
		justify-content: flex-end;
	}

	#transactionReport_wrapper .DataTables_sort_icon {
		flex: none;
	}

	#transactionReport_wrapper th.right,
	#transactionReport_wrapper td.right {
		text-align: right;
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	/* Show at most two lines of long product and category names (the full name is in the tooltip) */
	#transactionReport_wrapper .clamp {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	/* Zebra stripes, overriding the DataTables colors for the sorted column */
	#transactionReport_wrapper tbody tr.odd td,
	#transactionReport_wrapper tbody tr.odd td.sorting_1 {
		background-color: #fff;
	}

	#transactionReport_wrapper tbody tr.even td,
	#transactionReport_wrapper tbody tr.even td.sorting_1 {
		background-color: #f7f7f9;
	}

	/* Rows open the product's transactions when clicked */
	#transactionReport_wrapper tbody tr:hover td,
	#transactionReport_wrapper tbody tr:hover td.sorting_1 {
		background-color: #eaf2fb;
		cursor: pointer;
	}

	#transactionReport_wrapper .credit {
		color: #2e7d32;
	}

	#transactionReport_wrapper .debit {
		color: #c62828;
	}

	#transactionReport_wrapper .zero {
		color: #bbb;
	}

	#transactionReport_wrapper tfoot th {
		border-top: 2px solid #ccc;
		background-color: #f0f0f2;
	}
	</style>
</head>
<body>

	<div class="body">
		<div class="report-overlay" role="alert" aria-busy="true">
			<div class="report-overlay-message">
				<div class="report-overlay-title">
					<img alt="spinner" src="${resource(dir: 'images', file: 'spinner.gif')}" />
					<g:message code="report.generating.label" default="Generating report..."/>
				</div>
				<div class="report-overlay-elapsed">
					<g:message code="report.elapsed.label" default="Elapsed"/>: <span class="report-overlay-elapsed-time">0:00</span>
				</div>
				<div class="report-overlay-hint">
					<g:message code="report.generating.hint" default="Large date ranges can take a few minutes. Please don't refresh the page or click again."/>
				</div>
			</div>
		</div>
		<g:if test="${flash.message}">
			<div class="message" role="status" aria-label="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors" role="alert" aria-label="error-message">
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
									<warehouse:message code="report.startDate.label"/>
								</label>

								<g:jqueryDatePicker name="startDate"
													cssClass="filter"
													value="${command?.startDate}"
                                />

							</div>
							<div class="filter-list-item">

								<label>
									<warehouse:message code="report.endDate.label"/>
								</label>

								<g:jqueryDatePicker name="endDate"
													cssClass="filter"
													value="${command?.endDate}"
                                />
							</div>
							<div class="filter-list-item">
								<label>
									<warehouse:message code="report.location.label"/>
								</label>
								<g:selectLocation
									class="chzn-select-deselect filter"
									id="locationId"
									name="location.id"
									activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"
									noSelection="['':'']"
									maxChars="75"
									groupBy="locationType"
									value="${command?.location?.id}"
								/>
							</div>
							<div class="filter-list-item">
								<label><warehouse:message code="category.label"/></label>
								<p>
									<g:selectCategory
										id="category"
									    class="chzn-select-deselect filter"
									    data-placeholder="${g.message(code: 'category.selectCategory.label', default: 'Select a category')}"
									    name="category"
									    noSelection="['':'']"
									    value="${params?.category?.id?:command?.rootCategory?.id}"
									/>
								</p>
								<p>
									<label>
										<g:checkBox name="includeCategoryChildren" value="${params?.includeCategoryChildren}" checked="true"/>
										${warehouse.message(
											code:'report.search.includeCategoryChildren.label',
											default: 'Include all products in all subcategories',
										)}
									</label>
								</p>
							</div>
                            <div class="filter-list-item">
                                <label><g:message code="product.label"/></label>
                                <p>
                                    <g:selectProductAjax id="products-select"
                                                         value="${params?.products}"
                                                         multiple="true"
                                    />
                                </p>
                            </div>
							<div class="filter-list-item">
								<label><warehouse:message code="tag.label"/></label>
								<p>
									<g:selectTags
										name="tags"
									    id="tags"
									    value="${params?.tags}"
										data-placeholder="${g.message(code: 'default.selectOptions.label', default: 'Select Options')}"
										multiple="true"
									    class="chzn-select-deselect"
									/>
								</p>
							</div>
							<div class="filter-list-item">
								<label><warehouse:message code="catalogs.name.label"/></label>
								<p>
									<g:selectCatalogs
										id="catalogs"
										name="catalogs"
										noSelection="['null':'']"
									    value="${params?.catalogs}"
										data-placeholder="${g.message(code: 'default.selectOptions.label', default: 'Select Options')}"
										class="chzn-select-deselect"
									/>
								</p>
							</div>
						</div>
						<div class="buttons">
							<button class="submit-button button">
								<img src="${resource(dir:'images/icons/silk',file:'play_blue.png')}" />
								<g:set var="reportLabel" value="${g.message(code:'default.report.label', default: 'Report')}"/>
								${g.message(code: 'default.run.label', args: [reportLabel])}
							</button>
							<button class="download-button button">
								<img src="${resource(dir:'images/icons/silk',file:'page_excel.png')}" />
								<g:set var="dataLabel" value="${g.message(code:'default.data.label', default: 'Data')}"/>
								${g.message(code: 'default.download.label', args: [dataLabel])}
							</button>
						</div>
					</div>
					<div class="box">
						<h2><warehouse:message code="report.metadata.label" default="Metadata"/></h2>
						<g:render template="showTransactionReportMetadata"/>
					</div>
				</div>
			</div>
			<div class="yui-u">
				<div class="box">
					<h2>
						${warehouse.message(code:'report.transactionReport.title', default: "Transaction Report")}
					</h2>

                    <table id="transactionReport" class="dataTable">
                        <thead>
                        <tr>
                            <th><warehouse:message code="product.productCode.label" default="Code" /></th>
                            <th><warehouse:message code="product.label" default="Product" /></th>
                            <th><warehouse:message code="category.label" default="Category" /></th>
                            <th class="right"><warehouse:message code="report.transaction.openingBalance.label" default="Opening"/></th>
                            <th class="right"><warehouse:message code="default.credits.label" default="Credits"/></th>
                            <th class="right"><warehouse:message code="default.debits.label" default="Debits"/></th>
                            <th class="right"><warehouse:message code="report.transaction.netChange.label" default="Net Change"/></th>
                            <th class="right"><warehouse:message code="report.transaction.closingBalance.label" default="Closing"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                        <tfoot>
                        <%-- Totals of the rows that match the search. Filled in by fnFooterCallback. --%>
                        <tr>
							<th><warehouse:message code="default.total.label" default="Total" /></th>
							<th></th>
							<th></th>
                            <th class="right"></th>
                            <th class="right"></th>
                            <th class="right"></th>
                            <th class="right"></th>
                            <th class="right"></th>
                        </tr>
                        </tfoot>
                    </table>
				</div>
			</div>
		</div>
	</div>
	<div class="loading">
		Loading...
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

    // Only one report request can be in progress at a time. While it is, the report is covered by an overlay
    // and the buttons are disabled, so that it can't be run again until it's done (see #6201).
    var reportRequestInProgress = false;
    var reportElapsedTimer = null;
    var reportTimeout = ${grailsApplication.config.openboxes.report.transactionReport.timeout ?: 300000};

    function startReportRequest() {
        if (reportRequestInProgress) {
            return false;
        }
        reportRequestInProgress = true;
        $(".submit-button, .download-button").prop("disabled", true);

        var startTime = Date.now();
        $(".report-overlay-elapsed-time").text("0:00");
        reportElapsedTimer = setInterval(function () {
            var seconds = Math.floor((Date.now() - startTime) / 1000);
            var remainder = seconds % 60;
            $(".report-overlay-elapsed-time").text(Math.floor(seconds / 60) + ":" + (remainder < 10 ? "0" : "") + remainder);
        }, 1000);
        $(".report-overlay").addClass("active");
        return true;
    }

    function finishReportRequest() {
        clearInterval(reportElapsedTimer);
        $(".report-overlay").removeClass("active");
        $(".submit-button, .download-button").prop("disabled", false);
        reportRequestInProgress = false;
    }

    // Returns a message to show the user for a failed report request
    function getReportErrorMessage(status, responseText) {
        if (status === "timeout") {
            return "${g.message(code: 'report.timeout.message', default: 'The report took too long to generate. Try a shorter date range or fewer products.')}";
        }
        var message = "${g.message(code: 'report.error.message', default: 'An unexpected error has occurred on the server. Please contact your system administrator.')}";
        try {
            var errorMessage = JSON.parse(responseText).errorMessage;
            if (errorMessage) {
                return message + "\n\n" + errorMessage;
            }
        } catch (e) {
            // The response wasn't JSON (e.g. an error page), so fall back to the generic message
        }
        return message;
    }

    // Downloads the report in the background (instead of navigating to it) so that we know when it's done and can
    // show any error, and so that clicking again doesn't silently cancel the first download.
    function downloadReport(url) {
        if (!startReportRequest()) {
            return;
        }
        var xhr = new XMLHttpRequest();
        xhr.open("GET", url);
        xhr.responseType = "blob";
        xhr.timeout = reportTimeout;
        xhr.onload = function () {
            var contentType = xhr.getResponseHeader("Content-Type") || "";
            if (xhr.status === 200 && contentType.indexOf("text/csv") === 0) {
                var disposition = xhr.getResponseHeader("Content-Disposition") || "";
                var match = /filename="?([^";]+)"?/.exec(disposition);
                var link = document.createElement("a");
                link.href = URL.createObjectURL(xhr.response);
                link.download = match ? match[1] : "Transaction-Report.csv";
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                setTimeout(function () { URL.revokeObjectURL(link.href); }, 1000);
                finishReportRequest();
                return;
            }
            if (xhr.status === 200) {
                // Probably the login page, because the session expired
                finishReportRequest();
                alert("${g.message(code: 'report.sessionExpired.message', default: 'Unable to download the report. Your session may have expired, please refresh the page and try again.')}");
                return;
            }
            // Errors are JSON, so read the blob as text to get the message
            var reader = new FileReader();
            reader.onload = function () {
                finishReportRequest();
                alert(getReportErrorMessage(xhr.status, reader.result));
            };
            reader.readAsText(xhr.response);
        };
        xhr.ontimeout = function () {
            finishReportRequest();
            alert(getReportErrorMessage("timeout"));
        };
        xhr.onerror = function () {
            finishReportRequest();
            alert(getReportErrorMessage(xhr.status));
        };
        xhr.send();
    }

    // Shows a quantity in a table cell. Zeros are dimmed, and non-zero quantities get the given class (if any).
    function renderQuantity(cell, value, nonZeroClass) {
        var quantity = Number(value) || 0;
        cell.removeClass('credit debit zero').html(quantity.toLocaleString('en-US'));
        if (quantity === 0) {
            cell.addClass('zero');
        } else if (nonZeroClass) {
            cell.addClass(nonZeroClass);
        }
    }

    // Shows a change in quantity as a credit if it's positive, or as a debit (without the sign) if it's negative
    function renderNetChange(cell, value) {
        var quantity = Number(value) || 0;
        renderQuantity(cell, Math.abs(quantity), quantity > 0 ? 'credit' : 'debit');
    }

    function destroyDataTable() {
    	$('#transactionReport').dataTable().fnDestroy();
	}

	function initializeDataTable(validated) {
    	const baseOptions = {
    		"bJQueryUI": true,
			"bDestroy": true,
			"bProcessing": false,  // The report overlay shows that the report is loading
			"iDisplayLength": 100,
			"bSearch": false,
			"bScrollCollapse": true,
			"bAutoWidth": false,  // Use the column widths below
			"bScrollInfinite": true,
			"sScrollY": 500,
			"sPaginationType": "two_button",
			"oLanguage": {
				"sEmptyTable": "${g.message(code: 'default.dataTable.noData.label', default: 'No data available in table')}",
				"sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 to 0 of 0 entries')}",
				"sInfo": "${g.message(code: 'default.dataTable.showing.label', 'Showing')} " +
						"_START_" +
						" ${g.message(code: 'default.dataTable.to.label', default: 'to')} " +
						"_END_" +
						" ${g.message(code: 'default.dataTable.of.label', default: 'of')} " +
						"_TOTAL_" +
						" ${g.message(code: 'default.dataTable.entries.label', default: 'entries')}",
				"sSearch": "${g.message(code: 'default.dataTable.search.label', default: 'Search:')}",
				"sZeroRecords": "${g.message(code: 'default.dataTable.noRecordsFound.label', default: 'No records found')}",
				"sProcessing": "<img alt='spinner' src=\"${resource(dir: 'images', file: 'spinner.gif')}\" /> ${g.message(code: 'default.loading.label', default: 'Loading...')}",
				"sInfoFiltered": "(${g.message(code: 'default.dataTable.filteredFrom.label', default: 'filtered from')} " +
						"_MAX_" +
						" ${g.message(code: 'default.dataTable.totalEntries.label', default: 'total entries')})"
			},
			"aLengthMenu": [
				[5, 15, 25, 50, 100, 500, 1000, -1],
				[5, 15, 25, 50, 100, 500, 1000, "All"]
			],
			// Totals of the rows that match the search (on every page, not just the current one)
			"fnFooterCallback": function (nFoot, aaData, iStart, iEnd, aiDisplay) {
				var cells = $('th', nFoot);
				["Opening", "Credits", "Debits", "Net Change", "Closing"].forEach(function (column, index) {
					var cell = cells.eq(index + 3);
					if (!aiDisplay.length) {
						cell.html('').removeClass('credit debit zero');
						return;
					}
					var total = aiDisplay.reduce(function (sum, row) {
						return sum + (Number(aaData[row][column]) || 0);
					}, 0);
					if (column === "Net Change") {
						renderNetChange(cell, total);
					} else {
						renderQuantity(cell, total, column === "Credits" ? 'credit' : column === "Debits" ? 'debit' : null);
					}
				});
			}
		}
		const dataOptions = {
			"sAjaxSource": "${request.contextPath}/json/getTransactionReport",
			"fnServerParams": function ( data ) {
				data.push({ name: "location.id", value: $("#locationId").val() });
				data.push({ name: "startDate", value: $("#startDate").val() });
				data.push({ name: "endDate", value: $("#endDate").val() });
				data.push({ name: "category", value: $("#category").val() });

                const selectedProducts = $("#products-select").val();
                if (selectedProducts) {
                  selectedProducts.forEach(productId => {
                    data.push({ name: "products", value: productId });
                  });
                }

				data.push({ name: "tags", value: $("#tags").val() });
				data.push({ name: "catalogs", value: $("#catalogs").val() });
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
					"timeout": reportTimeout,
					"complete": finishReportRequest,
					"error": function (xhr, status, error) {
						// User probably refreshed page or clicked on a link, so this isn't really an error
						if (status !== "timeout" && (xhr.readyState == 0 || xhr.status == 0)) {
							return;
						}
						alert(getReportErrorMessage(status === "timeout" ? status : xhr.status, xhr.responseText));
						destroyDataTable();
					}
				})
			},
			"aoColumns": [
				// The product name gets whatever width is left over
				{"mData": "Code", "sWidth": "70px"},
				{"mData": "Name"},
				{"mData": "Category", "sWidth": "170px"},
				{"mData": "Opening", "sType": 'numeric', "sClass": "right", "sWidth": "90px" },
				{"mData": "Credits", "sType": 'numeric', "sClass": "right", "sWidth": "90px" },
				{"mData": "Debits", "sType": 'numeric', "sClass": "right", "sWidth": "90px" },
				{"mData": "Net Change", "sType": 'numeric', "sClass": "right", "sWidth": "90px" },
				{"mData": "Closing", "sType": 'numeric', "sClass": "right", "sWidth": "90px" }
			],
			"bUseRendered": false,
			"dom": '<"top"i>rt<"bottom"flp><"clear">',
            "fnRowCallback": function (nRow, aData) {
				// The tooltip shows the full name (long names are truncated), or the original name if we display
				// the DisplayName
				$('td:eq(1)', nRow).empty().append($('<div class="clamp">').text(aData?.DisplayName ?? aData?.Name))
					.attr('title', aData?.Name);
				$('td:eq(2)', nRow).empty().append($('<div class="clamp">').text(aData?.Category ?? ''))
					.attr('title', aData?.Category);
				renderQuantity($('td:eq(3)', nRow), aData["Opening"]);
				renderQuantity($('td:eq(4)', nRow), aData["Credits"], 'credit');
				renderQuantity($('td:eq(5)', nRow), aData["Debits"], 'debit');
				renderNetChange($('td:eq(6)', nRow), aData["Net Change"]);
				renderQuantity($('td:eq(7)', nRow), aData["Closing"]);
				return nRow;
	          }
		};

		const options = validated ? { ...baseOptions, ...dataOptions } : baseOptions
		$('#transactionReport').dataTable(options);
	}


	$(document).ready(function() {

		$(".loading").hide();
		initializeDataTable(false);

		// DataTables doesn't resize the columns when the page width changes. Because the header and footer are in
		// their own tables when the table scrolls, they'd no longer line up with the rows.
		var resizeTimer = null;
		$(window).on('resize', function () {
			clearTimeout(resizeTimer);
			resizeTimer = setTimeout(function () {
				$('#transactionReport').dataTable().fnAdjustColumnSizing(false);
			}, 100);
		});

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

			if (validated && startReportRequest()) {
				initializeDataTable(true);
			}
		});

		$(".download-button").click(function(event) {
			event.preventDefault();
			var params = {
				"location.id": $("#locationId").val(),
				startDate: $("#startDate").val(),
				endDate: $("#endDate").val(),
				category: $("#category").val(),
                products: $("#products-select").val(),
				tags: $("#tags").val(),
				catalogs: $("#catalogs").val(),
				format: "text/csv"
			};
			if($('#includeCategoryChildren').is(':checked')) {
				params.includeCategoryChildren = $("#includeCategoryChildren").val();
			}
			var queryString = $.param(params, true);
			downloadReport('${request.contextPath}/json/getTransactionReport?' + queryString);
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
		});

		$("#refreshTransactionFact").click(function(event) {
			event.preventDefault();
			var url = $(this).data("url");
			var confirmationPrompt = $(this).data("confirmation-prompt");
			var confirmed = confirm(confirmationPrompt);
			if (confirmed) {
				$.ajax({
					url: url,
					data: {},
					cache: false,
					timeout: ${grailsApplication.config.openboxes.ajaxRequest.timeout},
					success: function (html) {
						console.log(html);
						alert("Data has been refreshed")
					},
					error: function (error) {
						alert("An error occurred while refreshing the data");
					},
					beforeSend: function () {
						$('.loading').livequery(function () {
							$(this).show();
						});
					},
					complete: function () {
						$(".loading").hide();
						window.location.reload();
					}
				});
			}
		});
	});

</script>

    </body>
</html>
