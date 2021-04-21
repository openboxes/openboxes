<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom">
</head>
<body>

    <div class="dialog">
        <div class="button-bar">
            <div id="migration-status" class="right tag tag-info">None</div>
            <g:link class="button" action="index"><g:message code="default.list.label" args="[g.message(code:'migrations.label', default: 'Migrations')]"/></g:link>
        </div>
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <div id="status">
            <div id="message"></div>
        </div>
        <div class="tabs">
            <ul>
                <li><a href="${request.contextPath}/migration/dataQuality"><warehouse:message code="data.quality.label" default="Quality"/></a></li>
                <li><a href="${request.contextPath}/migration/dataMigration"><warehouse:message code="data.migration.label" default="Migration"/></a></li>
                <li><a href="${request.contextPath}/migration/dimensionTables"><warehouse:message code="data.dimensions.label" default="Dimensions"/></a></li>
                <li><a href="${request.contextPath}/migration/factTables"><warehouse:message code="data.facts.label" default="Facts"/></a></li>
                <li><a href="${request.contextPath}/migration/materializedViews"><warehouse:message code="data.materializedViews.label" default="Materialized Views"/></a></li>
                <li><a href="${request.contextPath}/migration/productAvailability"><warehouse:message code="data.productAvailability.label" default="Product Availability" /></a></li>
            </ul>
        </div>
    </div>
<div class="loading">Loading...</div>
<g:javascript>
    $(document).ready(function() {
        $(".loading").hide();
        $(".tabs").tabs({
            cookie : {
                expires : 1
            },
            ajaxOptions: {
                error: function(xhr, status, index, anchor) {
                    var errorMessage = "Error loading tab: " + xhr.status + " " + xhr.statusText;
                    // Attempt to get more detailed error message
                    if (xhr.responseText) {
                        var json = JSON.parse(xhr.responseText);
                        if (json.errorMessage) {
                            errorMessage = json.errorMessage
                        }
                    }
                    // Display error message
                    $(anchor.hash).text(errorMessage);

                    // Reload the page if session has timed out
                    if (xhr.statusCode == 401) {
                        window.location.reload();
                    }
                },
                beforeSend: function() {
                    $('.loading').show();
                },
                complete: function() {
                    $(".loading").hide();
                }
            }
        });
        $(".indicator").each(function(index, object){
          $("#" + object.id).load('${request.contextPath}/migration/' + object.id + '?format=count');
        });

        $(".fetch-indicator").live("click", function(){
          $(this).text("Loading...");
          var url = $(this).data("url");
          $(this).load(url, {}, function(xhr, textStatus, request) {
            if (textStatus !== "success") {
              $(this).text("");
              var error = JSON.parse(xhr);
              $(this).text("Error: " + error.errorMessage);
              return;
            }
            $(this).text(xhr);
          });
        });

        $("#btn-fetch-all-indicators").live("click", function(){
          $(".fetch-indicator").each(function(index, object) {
            $(object).trigger("click");
          });
        });

        $(".btn-post-data").live("click", function(){
          var url = $(this).data("url");
          onLoading();
          $.post(url, {}, function(data, textStatus, xhr) {
            onComplete();
          });
        });

    });

    function onLoading() {
        $(".loading").show();
        $("#status").hide();
        $("#migration-status").text("Starting migration...")
    }

    function onComplete() {
        $(".loading").hide();
        $("#status").show();
        $("#migration-status").text("Completed migration! ")
    }
</g:javascript>

</body>

</html>
