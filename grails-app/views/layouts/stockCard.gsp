<html>
<head>
	<meta name="layout" content="custom" />
	<g:layoutHead />
    <script src="${resource(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${resource(dir:'js/', file:'knockout_binding.js')}" type="text/javascript" ></script>
    <script src="${resource(dir:'js/', file:'inventory.js')}" type="text/javascript" ></script>
    <script src="${resource(dir:'js/jquery.cycle', file:'jquery.cycle.lite.js')}" type="text/javascript" ></script>
    <script src="${resource(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>

	<title><g:pageProperty name="page.title"/></title>

	<style>
		.line {
			display: flex;
			min-width: 0;
		}

		.line-base {
			text-overflow: ellipsis;
			white-space: nowrap;
			overflow: hidden;
			max-width: 50px;
		}

		.line-extension {
			text-overflow: ellipsis;
			white-space: nowrap;
			overflow: hidden;
			max-width: 100px;
		}

		.recalled {
			background-color: #ffcccb !important;
		}

		.restricted {
			background-color: #fca714 !important;
		}
	</style>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message" role="status" aria-label="message">
				${flash.message}
			</div>
		</g:if>
		<g:if test="${flash.error}">
			<div class="errors" role="alert" aria-label="error-message">
				${flash.error}
			</div>
		</g:if>

		<g:hasErrors bean="${commandInstance}">
			<div class="errors" role="alert" aria-label="error-message">
				<g:renderErrors bean="${commandInstance}" as="list" />
			</div>
		</g:hasErrors>

		<g:hasErrors bean="${flash.errors}">
			<div class="errors" role="alert" aria-label="error-message">
				<g:renderErrors bean="${flash.errors}" as="list" />
			</div>
		</g:hasErrors>
		<g:render template="/product/summary" model="[productInstance:commandInstance?.product,
													  inventoryInstance:commandInstance?.inventory,
													  inventoryLevelInstance: commandInstance?.inventoryLevel,
													  totalQuantity: commandInstance?.totalQuantity,
													  totalQuantityAvailableToPromise: commandInstance?.totalQuantityAvailableToPromise]"/>
		<div class="dialog">
            <div class="yui-gf">
                <div class="yui-u first">
                    <g:render template="productDetails"
                        model="[productInstance:commandInstance?.product,
								inventoryInstance:commandInstance?.inventory,
								inventoryLevelInstance: commandInstance?.inventoryLevel,
								totalQuantity: commandInstance?.totalQuantity,
								totalQuantityAvailableToPromise: commandInstance?.totalQuantityAvailableToPromise]"/>
                </div>
                <div class="yui-u">

                    <g:pageProperty name="page.content"/>
					<div class="loading">Loading...</div>
                </div>
            </div>
        </div>
	</div>
	<script>
		$(document).ready(function() {
            $(".loading").hide();

			$('.nailthumb-container img').nailthumb({width : 60, height : 60});

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
		});

	</script>


</body>
</html>
