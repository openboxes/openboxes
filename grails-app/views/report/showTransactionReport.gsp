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

	/* The report period field and its range picker */
	.report-period input[type=text] {
		width: 100%;
		box-sizing: border-box;
	}

	.report-period .hint {
		cursor: help;
	}

	/* The picker sizes itself assuming content-box, so its months wrap onto two lines with the site's border-box */
	.date-picker-wrapper, .date-picker-wrapper * {
		box-sizing: content-box;
	}

	/* The picker's shortcuts, one line per group (days, months, quarters, years) */
	.date-picker-wrapper .footer .shortcuts {
		line-height: 1.8;
	}

	.date-picker-wrapper .shortcut-group {
		display: block;
	}

	.date-picker-wrapper .shortcut-group-label {
		display: inline-block;
		width: 70px;
		color: #666;
	}

	.date-picker-wrapper .shortcut-group .custom-shortcut {
		margin-right: 10px;
	}

	/* The message in the empty table, before a report has been run or when it has no rows */
	#transactionReport_wrapper td.dataTables_empty {
		padding: 40px 20px;
		text-align: center;
		color: #666;
		font-size: 1.1em;
	}

	/* Run and download buttons side by side, filling the width of the sidebar */
	.sidebar .buttons {
		display: flex;
		gap: 10px;
		padding: 10px;
	}

	.sidebar .buttons .button {
		flex: 1;
		justify-content: center;
		margin: 0;
	}

	.required-marker {
		color: #c00;
	}

	/* The optional filters, set apart from the required parameters above them */
	.product-filters {
		margin-top: 15px;
		border-top: 1px solid #ddd;
		padding-top: 5px;
	}

	.product-filters h3 {
		margin: 10px 10px 0;
		font-size: 1em;
		font-weight: bold;
	}

	.product-filters-hint {
		margin: 3px 10px 0;
		color: #666;
		font-size: 0.9em;
	}

	/* Covers the whole page (including the header and footer) while the report is being generated, so that it can't
	   be run again until it's done (see #6201) */
	.report-overlay {
		display: none;
		position: fixed;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
		z-index: 10000;
		align-items: center;
		justify-content: center;
		background-color: rgba(128, 128, 128, 0.5);
		cursor: wait;
	}

	.report-overlay.active {
		display: flex;
	}

	.report-overlay-message {
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
	#transactionReport_wrapper th,
	#dialogDataTable_wrapper th {
		font-weight: 600;
	}

	/* Same horizontal padding in the header, body and footer so that the columns line up */
	#transactionReport_wrapper th,
	#dialogDataTable_wrapper th,
	#transactionReport_wrapper td,
	#dialogDataTable_wrapper td {
		padding: 6px 10px;
	}

	/* Keep the sort icon on the same line as the column name */
	#transactionReport_wrapper .DataTables_sort_wrapper,
	#dialogDataTable_wrapper .DataTables_sort_wrapper {
		display: flex;
		align-items: center;
		gap: 2px;
	}

	#transactionReport_wrapper th.right .DataTables_sort_wrapper,
	#dialogDataTable_wrapper th.right .DataTables_sort_wrapper {
		justify-content: flex-end;
	}

	#transactionReport_wrapper .DataTables_sort_icon,
	#dialogDataTable_wrapper .DataTables_sort_icon {
		flex: none;
	}

	#transactionReport_wrapper th.right,
	#dialogDataTable_wrapper th.right,
	#transactionReport_wrapper td.right,
	#dialogDataTable_wrapper td.right {
		text-align: right;
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	/* Transaction dates in the details dialog: fixed-width digits so that they line up, a lighter time, a dimmed date
	   when it's the same as the row above, and a line above the first row of each day */
	#dialogDataTable_wrapper td.date-cell {
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	#dialogDataTable_wrapper td.date-cell .time {
		color: #999;
	}

	#dialogDataTable_wrapper td.date-cell .date.same-date {
		color: #ccc;
	}

	#dialogDataTable_wrapper tbody tr.new-day td {
		border-top: 1px solid #ddd;
	}

	/* "Ghost" rows in the details dialog: changes that no transaction recorded */
	#dialogDataTable_wrapper tbody tr.ghost-row td {
		font-style: italic;
		color: #888;
		cursor: help;
	}

	#dialogDataTable_wrapper tbody tr.ghost-row td:nth-child(2) {
		border-left: 3px dashed #ccc;
	}

	/* Internal transfers in the details dialog: they move stock between bins without changing the balance */
	#dialogDataTable_wrapper tbody tr.internal-row td,
	#dialogDataTable_wrapper tbody tr.internal-row td.credit,
	#dialogDataTable_wrapper tbody tr.internal-row td.debit,
	#dialogDataTable_wrapper tbody tr.internal-row td a {
		color: #aaa;
	}

	#dialogDataTable_wrapper tbody tr.internal-row {
		cursor: help;
	}

	.internal-transfers-toggle {
		display: inline-block;
		margin-bottom: 8px;
		cursor: help;
	}

	/* Fixed layout, so that the columns keep their widths and the product name gets the rest. Otherwise the browser
	   spreads the extra width over every column, which pushes the quantities away from the product. Applies to the
	   header and body tables, which DataTables splits the table into when it scrolls. */
	#transactionReport_wrapper table.dataTable {
		table-layout: fixed;
	}

	/* The column widths. DataTables sets its own width on every header cell (and doesn't keep the widths it's given),
	   so they're set here instead. The product name has no width, so it gets the rest. */
	#transactionReport_wrapper table.dataTable thead th:nth-child(1) {
		width: 80px !important;
	}

	#transactionReport_wrapper table.dataTable thead th:nth-child(2) {
		width: auto !important;
	}

	#transactionReport_wrapper table.dataTable thead th:nth-child(3) {
		width: 260px !important;
	}

	/* The site limits data table cells to 100px wide */
	#transactionReport_wrapper table.dataTable th:nth-child(2),
	#transactionReport_wrapper table.dataTable td:nth-child(2),
	#transactionReport_wrapper table.dataTable th:nth-child(3),
	#transactionReport_wrapper table.dataTable td:nth-child(3) {
		max-width: none;
	}

	#transactionReport_wrapper table.dataTable thead th:nth-child(n+4) {
		width: 75px !important;
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
	#dialogDataTable_wrapper tbody tr.odd td,
	#transactionReport_wrapper tbody tr.odd td.sorting_1,
	#dialogDataTable_wrapper tbody tr.odd td.sorting_1 {
		background-color: #fff;
	}

	#transactionReport_wrapper tbody tr.even td,
	#dialogDataTable_wrapper tbody tr.even td,
	#transactionReport_wrapper tbody tr.even td.sorting_1,
	#dialogDataTable_wrapper tbody tr.even td.sorting_1 {
		background-color: #f7f7f9;
	}

	/* Rows open the product's transactions when clicked */
	#transactionReport_wrapper tbody tr:hover td,
	#transactionReport_wrapper tbody tr:hover td.sorting_1 {
		background-color: #eaf2fb;
		cursor: pointer;
	}

	/* Except the message in an empty table, which isn't a product */
	#transactionReport_wrapper tbody tr:hover td.dataTables_empty {
		background-color: #fff;
		cursor: default;
	}

	#transactionReport_wrapper .credit,
	#dialogDataTable_wrapper .credit {
		color: #2e7d32;
	}

	#transactionReport_wrapper .debit,
	#dialogDataTable_wrapper .debit {
		color: #c62828;
	}

	#transactionReport_wrapper .zero,
	#dialogDataTable_wrapper .zero {
		color: #bbb;
	}

	/* A column header with a tooltip */
	#transactionReport_wrapper th .hint {
		cursor: help;
		border-bottom: 1px dotted;
	}

	#transactionReport_wrapper a,
	#dialogDataTable_wrapper a {
		color: #1a5ea8;
	}

	/* Details dialog: the opening and closing balances stand out from the transactions */
	#dialogDataTable_wrapper tbody tr.balance-row td {
		font-weight: 600;
		background-color: #f0f0f2;
	}

	.report-metadata-empty {
		padding: 10px;
		color: #666;
	}

	/* Counted quantities don't change the balance */
	#dialogDataTable_wrapper .counted {
		color: #999;
		font-style: italic;
		cursor: help;
	}
	</style>
	<%-- The same date range picker as the request list --%>
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.4/moment.min.js"></script>
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/jquery.daterangepicker.min.js"></script>
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
									<warehouse:message code="report.facility.label" default="Facility"/>
									<span class="required-marker">*</span>
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
							<%-- The report period, picked as a range. The hidden fields hold the dates in the format the server reads
							     (MM/dd/yyyy), and are filled in from the text field by syncReportPeriod. --%>
							<div class="filter-list-item report-period">
								<label for="reportPeriod" class="hint" title="${g.message(code: 'report.period.hint', default: 'Both dates are included: from the start of the first day to the end of the last.')}">
									<g:message code="report.period.label" default="Period"/>
									<span class="required-marker">*</span>
								</label>
								<input type="text" id="reportPeriod" class="text filter" autocomplete="off"
									   placeholder="${g.message(code: 'report.period.placeholder', default: 'e.g. 01/Jan/2026 - 31/Jan/2026')}"/>
								<input type="hidden" id="startDate" name="startDate" value="${formatDate(date: command?.startDate, format: 'MM/dd/yyyy')}"/>
								<input type="hidden" id="endDate" name="endDate" value="${formatDate(date: command?.endDate, format: 'MM/dd/yyyy')}"/>
							</div>

							<%-- Optional filters, ordered from broadest to narrowest. The report combines them with AND. --%>
							<div class="product-filters">
								<h3><g:message code="report.productFilters.label" default="Filter products (optional)"/></h3>
								<p class="product-filters-hint">
									<g:message code="report.productFilters.hint" default="Leave blank for all products. Products must match every filter you set."/>
								</p>
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
								<div class="filter-list-item">
									<label><warehouse:message code="tags.label" default="Tags"/></label>
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
									<label><g:message code="products.label" default="Products"/></label>
									<p>
										<g:selectProductAjax id="products-select"
															 value="${params?.products}"
															 multiple="true"
										/>
									</p>
								</div>
							</div>
							<div class="filter-list-item">
								<label class="hint" title="${g.message(code: 'report.transaction.includeProductsWithNoActivity.hint', default: 'Also lists products that had stock but no activity, i.e. their credits, debits and differences are all 0.')}">
									<g:checkBox name="includeProductsWithNoActivity" value="${params?.includeProductsWithNoActivity}"/>
									<g:message code="report.transaction.includeProductsWithNoActivity.label" default="Include products with no activity"/>
								</label>
							</div>
							<div class="filter-list-item">
								<label class="hint" title="${g.message(code: 'report.transaction.includeInternalTransfers.hint', default: 'Counts transfers between bin locations of the facility (e.g. putaways and stock transfers) in the credits and debits. They move stock between bins without changing the facility\'s quantity on hand, so they\'re left out by default.')}">
									<g:checkBox name="includeInternalTransfers" value="${params?.includeInternalTransfers}"/>
									<g:message code="report.transaction.includeInternalTransfers.label" default="Include internal transfers"/>
								</label>
							</div>
						</div>
						<div class="buttons">
							<button class="submit-button button primary">
								<img src="${resource(dir:'images/icons/silk',file:'play_blue.png')}" />
								<g:set var="reportLabel" value="${g.message(code:'default.report.label', default: 'Report')}"/>
								${g.message(code: 'default.run.label', args: [reportLabel])}
							</button>
							<button class="download-button button"
									title="${g.message(code: 'report.transaction.download.hint', default: 'Downloads the report as a CSV file, with a column for each transaction type')}">
								<img src="${resource(dir:'images/icons/silk',file:'page_excel.png')}" />
								<g:set var="csvLabel" value="${g.message(code:'default.csv.label', default: 'CSV')}"/>
								${g.message(code: 'default.download.label', args: [csvLabel])}
							</button>
						</div>
					</div>
					<%-- Metadata of the last report that was run. Filled in by showReportMetadata. --%>
					<div class="box report-metadata">
						<h2><g:message code="report.metadata.label" default="Report Metadata"/></h2>
						<p class="report-metadata-empty">
							<g:message code="report.metadata.empty" default="Run the report to see its details here."/>
						</p>
						<table class="report-metadata-values" style="display: none">
							<tbody>
								<tr class="prop">
									<td class="name"><label><g:message code="report.facility.label" default="Facility"/></label></td>
									<td class="value report-metadata-facility"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.period.label" default="Period"/></label></td>
									<td class="value report-metadata-period"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="products.label" default="Products"/></label></td>
									<td class="value report-metadata-products"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.generated.label" default="Generated"/></label></td>
									<td class="value report-metadata-generated"></td>
								</tr>
								<tr class="prop">
									<td class="name"><label><g:message code="report.metadata.duration.label" default="Duration"/></label></td>
									<td class="value report-metadata-duration"></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="yui-u">
				<div class="box">
					<h2>
						${warehouse.message(code:'report.details.label', default: "Report Details")}
					</h2>

                    <table id="transactionReport" class="dataTable">
                        <thead>
                        <tr>
                            <th><span class="hint" title="${g.message(code: 'report.transaction.code.hint', default: "The product code")}"><warehouse:message code="product.productCode.label" default="Code" /></span></th>
                            <th><span class="hint" title="${g.message(code: 'report.transaction.product.hint', default: "The product name. Click a row to see the product's transactions.")}"><warehouse:message code="product.label" default="Product" /></span></th>
                            <th><span class="hint" title="${g.message(code: 'report.transaction.category.hint', default: "The category of the product")}"><warehouse:message code="category.label" default="Category" /></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.openingBalance.hint', default: "Quantity on hand at the beginning of the start date")}"><warehouse:message code="report.transaction.openingBalance.label" default="Opening"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.credits.hint', default: "Quantity added by credit transactions during the period, e.g. receipts, transfers in and adjustments up")}"><warehouse:message code="default.credits.label" default="Credits"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.debits.hint', default: "Quantity removed by debit transactions during the period, e.g. issues, transfers out and adjustments down")}"><warehouse:message code="default.debits.label" default="Debits"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.differences.hint', default: "The change in quantity from counts that didn't match the balance, i.e. the change that credits and debits don't account for")}"><warehouse:message code="report.transaction.differences.label" default="Differences"/></span></th>
                            <th class="right"><span class="hint" title="${g.message(code: 'report.transaction.closingBalance.hint', default: "Quantity on hand at the end of the end date (or now, if the end date is today or later)")}"><warehouse:message code="report.transaction.closingBalance.label" default="Closing"/></span></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
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
    var reportStartTime = null;
    var reportTimeout = ${grailsApplication.config.openboxes.report.transactionReport.timeout ?: 300000};

    function startReportRequest() {
        if (reportRequestInProgress) {
            return false;
        }
        reportRequestInProgress = true;
        $(".submit-button, .download-button").prop("disabled", true);

        var startTime = reportStartTime = Date.now();
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
    function renderQuantityChange(cell, value) {
        var quantity = Number(value) || 0;
        renderQuantity(cell, Math.abs(quantity), quantity > 0 ? 'credit' : 'debit');
    }

    // Shows the details of the report that was just loaded
    function showReportMetadata(json) {
        var seconds = Math.round((Date.now() - reportStartTime) / 1000);
        $(".report-metadata-facility").text($("#locationId option:selected").text());
        $(".report-metadata-period").text($("#reportPeriod").val());
        $(".report-metadata-products").text(((json && json.aaData) || []).length.toLocaleString('en-US'));
        $(".report-metadata-generated").text(new Date().toLocaleString());
        $(".report-metadata-duration").text(Math.floor(seconds / 60) + ":" + (seconds % 60 < 10 ? "0" : "") + seconds % 60);
        $(".report-metadata-empty").hide();
        $(".report-metadata-values").show();
    }

	// The DataTables messages. The empty table message depends on whether a report has been run.
	function getDataTableLanguage(emptyTableMessage) {
		return {
			"sEmptyTable": emptyTableMessage,
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
		};
	}

	function initializeDataTable(validated) {
    	const baseOptions = {
    		"bJQueryUI": true,
			"bDestroy": true,
			"bProcessing": false,  // The report overlay shows that the report is loading
			"iDisplayLength": 100,
			"bSearch": false,
			"bScrollCollapse": true,
			"bAutoWidth": false,  // The column widths are set in the styles
			"bScrollInfinite": true,
			"sScrollY": 500,
			"sPaginationType": "two_button",
			"aoColumns": [
				// The column widths are set in the styles
				{"mData": "Code"},
				{"mData": "Name"},
				{"mData": "Category"},
				{"mData": "Opening", "sType": 'numeric', "sClass": "right" },
				{"mData": "Credits", "sType": 'numeric', "sClass": "right" },
				{"mData": "Debits", "sType": 'numeric', "sClass": "right" },
				{"mData": "Differences", "sType": 'numeric', "sClass": "right" },
				{"mData": "Closing", "sType": 'numeric', "sClass": "right" }
			],
			"aLengthMenu": [
				[5, 15, 25, 50, 100, 500, 1000, -1],
				[5, 15, 25, 50, 100, 500, 1000, "All"]
			]
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
				if ($('#includeProductsWithNoActivity').is(':checked')) {
					data.push({ name: "includeProductsWithNoActivity", value: true });
				}
				if ($('#includeInternalTransfers').is(':checked')) {
					data.push({ name: "includeInternalTransfers", value: true });
				}
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
					"success": function (json) {
						showReportMetadata(json);
						fnCallback(json);
					},
					"timeout": reportTimeout,
					"complete": finishReportRequest,
					"error": function (xhr, status, error) {
						// User probably refreshed page or clicked on a link, so this isn't really an error
						if (status !== "timeout" && (xhr.readyState == 0 || xhr.status == 0)) {
							return;
						}
						alert(getReportErrorMessage(status === "timeout" ? status : xhr.status, xhr.responseText));
						initializeDataTable(false);
					}
				})
			},
			"oLanguage": getDataTableLanguage("${g.message(code: 'report.transaction.noRows.message', default: 'No products had activity in this period. Check Include products with no activity to see products that had stock.')}"),
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
				renderQuantityChange($('td:eq(6)', nRow), aData["Differences"]);
				renderQuantity($('td:eq(7)', nRow), aData["Closing"]);
				return nRow;
	          }
		};

		// Before a report has been run, show only the column headers and a prompt (no info, search or paging)
		const emptyOptions = {
			"sDom": 'rt',
			"oLanguage": getDataTableLanguage("${g.message(code: 'report.transaction.notRun.message', default: 'Choose a facility and period, then click Run Report.')}")
		};
		const options = validated ? { ...baseOptions, ...dataOptions } : { ...baseOptions, ...emptyOptions }
		$('#transactionReport').dataTable(options);
	}


	// The format of the dates in the report period field, and the separator between them
	var REPORT_PERIOD_FORMAT = "DD/MMM/YYYY";
	var REPORT_PERIOD_SEPARATOR = " - ";

	// Sets the report period. The end date is capped at today.
	function setReportPeriod(startDate, endDate) {
		var today = new Date();
		$("#reportPeriod").data("dateRangePicker").setDateRange(startDate, endDate > today ? today : endDate);
		syncReportPeriod();
	}

	// Fills in the hidden start and end date fields from the report period field, or clears them when it isn't a
	// valid period, so that the run button's checks catch it.
	function syncReportPeriod() {
		var dates = $("#reportPeriod").val().split(REPORT_PERIOD_SEPARATOR);
		var startDate = moment(dates[0], REPORT_PERIOD_FORMAT, true);
		var endDate = moment(dates[1], REPORT_PERIOD_FORMAT, true);
		var valid = dates.length === 2 && startDate.isValid() && endDate.isValid();
		$("#startDate").val(valid ? startDate.format("MM/DD/YYYY") : "");
		$("#endDate").val(valid ? endDate.format("MM/DD/YYYY") : "");
	}

	// Returns the [start, end] dates of a period shortcut (e.g. last month). "Last n days" ends today, and "last n
	// months" is the n whole months before this one.
	function getShortcutPeriod(period) {
		var now = new Date();
		var year = now.getFullYear();
		var month = now.getMonth();
		var today = new Date(year, month, now.getDate());
		var daysAgo = function (days) { return new Date(year, month, now.getDate() - days); };
		var quarterStartMonth = month - month % 3;
		switch (period) {
			case "today": return [today, today];
			case "yesterday": return [daysAgo(1), daysAgo(1)];
			case "last7Days": return [daysAgo(6), today];
			case "last30Days": return [daysAgo(29), today];
			case "last90Days": return [daysAgo(89), today];
			case "thisMonth": return [new Date(year, month, 1), today];
			case "lastMonth": return [new Date(year, month - 1, 1), new Date(year, month, 0)];
			case "last3Months": return [new Date(year, month - 3, 1), new Date(year, month, 0)];
			case "last6Months": return [new Date(year, month - 6, 1), new Date(year, month, 0)];
			case "last12Months": return [new Date(year, month - 12, 1), new Date(year, month, 0)];
			case "thisQuarter": return [new Date(year, quarterStartMonth, 1), today];
			case "lastQuarter": return [new Date(year, quarterStartMonth - 3, 1), new Date(year, quarterStartMonth, 0)];
			case "thisYear": return [new Date(year, 0, 1), today];
			case "lastYear": return [new Date(year - 1, 0, 1), new Date(year - 1, 11, 31)];
		}
	}

	// The picker's shortcuts, in groups. The picker shows them all on one line, so initializeReportPeriod puts each
	// group on its own line with a label.
	var REPORT_PERIOD_SHORTCUTS = [
		{ label: "${g.message(code: 'report.period.days.label', default: 'Days')}", periods: {
			today: "${g.message(code: 'report.period.today.label', default: 'Today')}",
			yesterday: "${g.message(code: 'report.period.yesterday.label', default: 'Yesterday')}",
			last7Days: "${g.message(code: 'report.period.last7Days.label', default: 'Last 7 days')}",
			last30Days: "${g.message(code: 'report.period.last30Days.label', default: 'Last 30 days')}",
			last90Days: "${g.message(code: 'report.period.last90Days.label', default: 'Last 90 days')}"
		}},
		{ label: "${g.message(code: 'report.period.months.label', default: 'Months')}", periods: {
			thisMonth: "${g.message(code: 'report.period.thisMonth.label', default: 'This month')}",
			lastMonth: "${g.message(code: 'report.period.lastMonth.label', default: 'Last month')}",
			last3Months: "${g.message(code: 'report.period.last3Months.label', default: 'Last 3 months')}",
			last6Months: "${g.message(code: 'report.period.last6Months.label', default: 'Last 6 months')}",
			last12Months: "${g.message(code: 'report.period.last12Months.label', default: 'Last 12 months')}"
		}},
		{ label: "${g.message(code: 'report.period.quarters.label', default: 'Quarters')}", periods: {
			thisQuarter: "${g.message(code: 'report.period.thisQuarter.label', default: 'This quarter')}",
			lastQuarter: "${g.message(code: 'report.period.lastQuarter.label', default: 'Last quarter')}"
		}},
		{ label: "${g.message(code: 'report.period.years.label', default: 'Years')}", periods: {
			thisYear: "${g.message(code: 'report.period.thisYear.label', default: 'This year')}",
			lastYear: "${g.message(code: 'report.period.lastYear.label', default: 'Last year')}"
		}}
	];

	function initializeReportPeriod() {
		var customShortcuts = [];
		$.each(REPORT_PERIOD_SHORTCUTS, function (i, group) {
			$.each(group.periods, function (period, name) {
				// The picker can't pick a day after endDate, so cap the shortcut's end date at today
				customShortcuts.push({ name: name, dates: function () {
					var dates = getShortcutPeriod(period);
					var today = new Date();
					return [dates[0], dates[1] > today ? today : dates[1]];
				}});
			});
		});

		$("#reportPeriod").dateRangePicker({
			format: REPORT_PERIOD_FORMAT,
			separator: REPORT_PERIOD_SEPARATOR,
			autoClose: true,
			endDate: new Date(),
			startOfWeek: "monday",
			// Reports are about the past, so when the period is in one month, show it with the month before rather than
			// the month after (which is often in the future and can't be picked)
			lookBehind: true,
			showShortcuts: true,
			shortcuts: {},
			customShortcuts: customShortcuts
		}).on("datepicker-change", syncReportPeriod)
			// Typed periods are read by the picker, but it doesn't fire datepicker-change for them
			.on("change keyup", syncReportPeriod);

		// Put each group of shortcuts on its own line, with a label. The &nbsp; spacers between them would be left
		// between the groups, so remove them.
		var shortcuts = $("#reportPeriod").data("dateRangePicker").getDatePicker().find(".custom-shortcut");
		shortcuts.parent().contents().filter(function () { return this.nodeType === Node.TEXT_NODE; }).remove();
		var index = 0;
		$.each(REPORT_PERIOD_SHORTCUTS, function (i, group) {
			var count = Object.keys(group.periods).length;
			shortcuts.slice(index, index + count)
				.wrapAll('<div class="shortcut-group"></div>')
				.first().before($('<span class="shortcut-group-label"></span>').text(group.label));
			index += count;
		});

		// Close the picker after a shortcut is picked, like after picking the end date (autoClose)
		shortcuts.find("a").click(function () {
			$("#reportPeriod").data("dateRangePicker").close();
		});

		// Show the period that was submitted, or default to last month, so that the report can be run after only
		// choosing a facility
		var startDate = $("#startDate").val();
		var endDate = $("#endDate").val();
		if (startDate && endDate) {
			setReportPeriod(moment(startDate, "MM/DD/YYYY").toDate(), moment(endDate, "MM/DD/YYYY").toDate());
		} else {
			var lastMonth = getShortcutPeriod("lastMonth");
			setReportPeriod(lastMonth[0], lastMonth[1]);
		}
	}

	$(document).ready(function() {

		$(".loading").hide();
		initializeDataTable(false);

		initializeReportPeriod();

		// Pressing Enter in a field runs the report, like submitting a form. Enter is left alone in the dropdowns,
		// where it picks an option, and while the date picker is open.
		$(".parameters").on("keydown", "input[type=text]", function (event) {
			if (event.key !== "Enter" || $(this).closest(".chosen-container, .select2-container").length
					|| $(".date-picker-wrapper").is(":visible")) {
				return;
			}
			event.preventDefault();
			$(".submit-button").click();
		});

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
			if ($('#includeProductsWithNoActivity').is(':checked')) {
				params.includeProductsWithNoActivity = true;
			}
			if ($('#includeInternalTransfers').is(':checked')) {
				params.includeInternalTransfers = true;
			}
			if($('#includeCategoryChildren').is(':checked')) {
				params.includeCategoryChildren = $("#includeCategoryChildren").val();
			}
			var queryString = $.param(params, true);
			downloadReport('${request.contextPath}/json/getTransactionReport?' + queryString);
		});

		$('#transactionReport tbody').on('click', 'tr', function () {
			// The message in an empty table isn't a product
			if ($(this).find('td.dataTables_empty').length) {
				return;
			}
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
				openModalDialog("#dlgShowDialog", productCode + " " + productName, 1000, null, url);
			}
		});
	});

</script>

    </body>
</html>
