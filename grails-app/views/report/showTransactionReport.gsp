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
                            <th class="center"><warehouse:message code="report.transaction.openingBalance.label" default="Opening"/></th>
                            <th class="center"><warehouse:message code="default.credits.label" default="Credits"/></th>
                            <th class="center"><warehouse:message code="default.debits.label" default="Debits"/></th>
                            <th class="center"><warehouse:message code="report.transaction.adjustments.label" default="Adjustments"/></th>
                            <th class="center"><warehouse:message code="report.transaction.closingBalance.label" default="Closing"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                        <tfoot>
                        <tr>
							<th><warehouse:message code="product.productCode.label" default="Code" /></th>
							<th><warehouse:message code="product.label" default="Product" /></th>
							<th><warehouse:message code="category.label" default="Category" /></th>
                            <th class="center"><warehouse:message code="report.transaction.openingBalance.label" default="Opening"/></th>
                            <th class="center"><warehouse:message code="default.credits.label" default="Credits"/></th>
                            <th class="center"><warehouse:message code="default.debits.label" default="Debits"/></th>
                            <th class="center"><warehouse:message code="report.transaction.adjustments.label" default="Adjustments"/></th>
                            <th class="center"><warehouse:message code="report.transaction.closingBalance.label" default="Closing"/></th>
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

    function destroyDataTable() {
    	$('#transactionReport').dataTable().fnDestroy();
	}

	function initializeDataTable(validated) {
    	const baseOptions = {
    		"bJQueryUI": true,
			"bDestroy": true,
			"bProcessing": true,
			"iDisplayLength": 100,
			"bSearch": false,
			"bScrollCollapse": true,
			"bAutoWidth": true,
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
		}
		const dataOptions = {
			"sAjaxSource": "${request.contextPath}/json/getTransactionReport",
			"fnServerParams": function ( data ) {
				data.push({ name: "location.id", value: $("#locationId").val() });
				data.push({ name: "startDate", value: $("#startDate").val() });
				data.push({ name: "endDate", value: $("#endDate").val() });
				data.push({ name: "category", value: $("#category").val() });
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
					"timeout": ${grailsApplication.config.openboxes.ajaxRequest.timeout},
					"error": function (xhr, status, error) {
						var message = "An unexpected error has occurred on the server. Please contact your system administrator.";
						if (xhr.responseText) {
							// User probably refreshed page or clicked on a link, so this isn't really an error
							if (xhr.readyState == 0 || xhr.status == 0) {
								return;
							}
							var errorMessage = JSON.parse(xhr.responseText).errorMessage;
							if (errorMessage) {
								message += "\n\n" + errorMessage
							}
						}
						alert(message);
						destroyDataTable();
					}
				})
			},
			"aoColumns": [
				{"mData": "Code"},
				{"mData": "Name", "sWidth": "250px"},
				{"mData": "Category"},
				{"mData": "Opening", "sType": 'numeric', "sClass": "right" },
				{"mData": "Credits", "sType": 'numeric', "sClass": "right" },
				{"mData": "Debits", "sType": 'numeric', "sClass": "right" },
				{"mData": "Adjustments", "sType": 'numeric', "sClass": "right" },
				{"mData": "Closing", "sType": 'numeric', "sClass": "right" }
			],
			"bUseRendered": false,
			"dom": '<"top"i>rt<"bottom"flp><"clear">',
            "fnRowCallback": function (nRow, aData) {
				$('td:eq(1)', nRow).text(aData?.DisplayName ?? aData?.Name);
				// If we display DisplayName, we want to have tooltip with original name of the product
				if (aData?.DisplayName) {
					$('td:eq(1)', nRow).attr('title', aData?.Name);
				}
                $('td:eq(3)', nRow).html(Number(aData["Opening"]).toLocaleString('en-US'));
                $('td:eq(4)', nRow).html(Number(aData["Credits"]).toLocaleString('en-US'));
                $('td:eq(5)', nRow).html(Number(aData["Debits"]).toLocaleString('en-US'));
                $('td:eq(6)', nRow).html(Number(aData["Adjustments"]).toLocaleString('en-US'));
                $('td:eq(7)', nRow).html(Number(aData["Closing"]).toLocaleString('en-US'));

                if (aData["Credits"] > 0) {
                  $('td:eq(4)', nRow).addClass('credit')
                }

                if (aData["Debits"] > 0) {
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

		const options = validated ? { ...baseOptions, ...dataOptions } : baseOptions
		$('#transactionReport').dataTable(options);
	}


	$(document).ready(function() {

		$(".loading").hide();
		initializeDataTable(false);

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
				tags: $("#tags").val(),
				catalogs: $("#catalogs").val(),
				format: "text/csv"
			};
			if($('#includeCategoryChildren').is(':checked')) {
				params.includeCategoryChildren = $("#includeCategoryChildren").val();
			}
			var queryString = $.param(params, true);
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
