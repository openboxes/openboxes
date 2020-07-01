<%@ page import="java.util.Locale" %>
<?xml version="1.0" encoding="UTF-8"?>
<html lang="en">
<head>
    <!-- Include default page title -->
    <title><g:layoutTitle default="OpenBoxes" /></title>

    <!-- YUI -->
    <yui:stylesheet dir="reset-fonts-grids" file="reset-fonts-grids.css" />

    <!-- Include Favicon -->
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}?v2" type="image/x-icon" />

    <!-- Include Main CSS -->
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/chosen',file:'chosen.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/select2@4.0.13/dist/css/select2.min.css" type="text/css" />
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/css/jquery.dataTables.min.css" type="text/css">
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footable.css')}" type="text/css" media="all" />

    <!-- Include javascript files -->
    <g:javascript library="application"/>

    <!-- Include jQuery UI files -->
    <g:javascript library="jquery" plugin="jquery" />
    <jqui:resources />
    <link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />


    <!-- Include Jquery Validation and Jquery Validation UI plugins -->
    <jqval:resources />
    <jqvalui:resources />

    <link rel="stylesheet" href="${resource(dir:'css',file:'openboxes.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'loading.css')}" type="text/css" media="all" />

    <!-- jquery validation messages -->
    <g:if test="${ session?.user?.locale && session?.user?.locale != 'en'}">
        <script src="${createLinkTo(dir:'js/jquery.validation/', file:'messages_'+ session?.user?.locale + '.js')}"  type="text/javascript" ></script>
    </g:if>

    <!-- Grails Layout : write head element for page-->
    <g:layoutHead />

    <g:render template="/common/customCss"/>
    <g:render template="/common/fullstory"/>
    <g:render template="/common/hotjar"/>

    <ga:trackPageview />
    <r:layoutResources/>
</head>
<body class="yui-skin-sam">

<g:render template="/common/customVariables"/>
<div id="doc3">

    <g:if test="${grailsApplication.config.openboxes.system.notification.enabled}">
        <div class="notice">
            ${grailsApplication.config.openboxes.system.notification.message}
        </div>
    </g:if>
    <g:if test="${session.impersonateUserId}">
        <div class="notice">
            <g:message code="user.impersonate.message" args="[session.user.username]" default="You are impersonating user {0}."/>
            <g:link controller="auth" action="logout">
                ${g.message(code:'default.logout.label', default: "Logout")}
            </g:link>
        </div>
    </g:if>
    <g:if test="${session.useDebugLocale}">

        <div id="debug-header" class="notice" style="margin: 10px;">
            <warehouse:message code="localization.custom.message"/>
            <g:link controller="localization" action="list" class="button">
                <warehouse:message code="default.list.label" args="[message(code: 'localizations.label')]"/>
            </g:link>
            <g:link controller="localization" action="create" class="button">
                <warehouse:message code="default.add.label" args="[message(code: 'localization.label')]"/>
            </g:link>
            <div class="right">
                <g:link controller="user" action="disableLocalizationMode" class="button">
                    <warehouse:message code="localization.disable.label"/>
                </g:link>
            </div>
            <div id="localizations">
            <!--
                        At some point we may want to display all translations for the page in a single div.
                        For the time being, flash.localizations is empty.
                     -->
                <g:each var="localization" in="${flash.localizations }">
                    <div>
                        ${localization.code } = ${localization.text }
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>

    <!-- Header "hd" includes includes logo, global navigation -->
    <g:if test="${session?.user && session?.warehouse}">
        <div id="hd" role="banner">
            <g:render template="/common/header"/>
        </div>
        <div id="megamenu">
            <g:include controller="dashboard" action="megamenu" params="[locationId:session?.warehouse?.id,userId:session?.user?.id]"/>
            <div id="loader" style="display:none; position: absolute; right: 0; top: 0" class="right notice">
                ${g.message(code: 'default.loading.label')}
            </div>
        </div>
        <div id="breadcrumb">
            <g:render template="/common/breadcrumb"/>
        </div>
    </g:if>


<!-- Body includes the divs for the main body content and left navigation menu -->
    <div id="bd" role="main">
        <div id="yui-main">
            <div id="content" class="yui-b">
                <g:layoutBody />
            </div>
        </div>
    </div>

    <g:if test="${session.useDebugLocale}">
        <g:render template="/common/localization"/>
    </g:if>


<!-- YUI "footer" block that includes footer information -->
    <g:if test="${session?.user && session?.warehouse}">
        <div id="ft" role="contentinfo">
            <g:render template="/common/footer" />
        </div>
    </g:if>
</div>
<div id="dlgShowDialog" class="dialog hidden">
    <div id="dlgShowDialogContent" class="empty center">
        Loading ...
    </div>
</div>
<!-- Include other plugins -->
<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery.ui.autocomplete.selectFirst.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.cookies/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.cookie/', file:'jquery.cookie.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.tmpl/', file:'jquery.tmpl.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.tmplPlus/', file:'jquery.tmplPlus.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.livequery/', file:'jquery.livequery.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.livesearch/', file:'jquery.livesearch.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.hoverIntent/', file:'jquery.hoverIntent.minified.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
<g:if test="${System.getenv().get('headless') != 'false'}" env="test">
    <!--headless driver throw error when using watermark-->
</g:if>
<g:else>
    <script src="${createLinkTo(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
</g:else>
<script src="${createLinkTo(dir:'js/', file:'global.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/jquery.megaMenu/', file:'jquery.megamenu.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/', file:'underscore-min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/chosen/', file:'chosen.jquery.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/feedback/', file:'feedback.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/footable/', file:'footable.js')}" type="text/javascript" ></script>
<script src="//cdn.jsdelivr.net/npm/select2@4.0.13/dist/js/select2.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js" type="text/javascript" ></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/notify/0.4.2/notify.js" type="text/javascript"></script>

<!-- JIRA Issue Collector -->
<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.jira.issue.collector.enabled)}">
    <script type="text/javascript" src="${grailsApplication.config.openboxes.jira.issue.collector.url}"></script>
</g:if>

<!-- Localization -->
<g:if test="${session.useDebugLocale}">
    <script type="text/javascript">
        // Define the localization
        if(typeof openboxes === "undefined") openboxes = {};
        if(typeof openboxes.localization === "undefined") openboxes.localization = {};
        openboxes.localization.Localization = function(data) {
            console.log(data);
            var self = this;
            if(!data) data = {};
            self.id = ko.observable(data.id);
            self.code = ko.observable(data.code);
            self.locale = ko.observable(data.locale);
            self.text = ko.observable(data.text);
            self.translation = ko.observable(data.translation);
        };

        $(document).ready(function() {
            // Initialize the localization dialog
            $("#localization-dialog").dialog({ autoOpen: false, modal: true, width: '600px' });

            // Instantiate a new localization object to be used
            var data = { id:"", code: "", text: "", translation: "" };
            var viewModel = new openboxes.localization.Localization(data);
            ko.applyBindings(viewModel);

            // Delete localization event handler
            $("#delete-localization-btn").click(function() {
                event.preventDefault();
                console.log("delete localization");
                console.log($(this));
                console.log($(event));
                if (viewModel.id() == undefined) {
                    alert("This translation is not currently saved to the database so it cannot be deleted.");
                }
                else {
                    $.ajax({
                        url: "${request.contextPath}/json/deleteLocalization",
                        type: "get",
                        contentType: 'text/json',
                        dataType: "json",
                        data: {id: viewModel.id() },
                        success: function(data) {
                            alert("You have successfully deleted this localization.");
                            location.reload();
                        },
                        error: function(data) {
                            alert("An error occurred while deleting this translation.");
                        }
                    });
                }

            });

            // Close dialog event handler
            $("#close-localization-dialog-btn").click(function() {
                event.preventDefault();
                $("#localization-dialog").dialog("close");
            });

            // Help event handler
            $("#help-localization-btn").click(function() {
                event.preventDefault();
                $.ajax({
                    url: "${request.contextPath}/json/getTranslation",
                    type: "get",
                    contentType: 'text/json',
                    dataType: "json",
                    data: {text: viewModel.text, src: "en", dest: "fr"},
                    success: function(data) {
                        console.log(data);
                        viewModel.translation = data;
                    },
                    error: function(data) {
                        viewModel.translation = "Error. Try again.";
                    }
                });
            });

            // Save event handler
            $("#save-localization-btn").click(function() {
                event.preventDefault();
                var jsonData = ko.toJSON(viewModel);
                console.log("save localization");
                console.log(jsonData);

                $.ajax({
                    url: "${request.contextPath}/json/saveLocalization",
                    type: "post",
                    contentType: 'text/json',
                    dataType: "json",
                    data: jsonData,
                    success: function(data) {
                        $("#localization-dialog").dialog("close");
                        location.reload();
                    },
                    error: function(data) {
                        $("#localization-dialog").dialog("close");
                        location.reload();
                    }
                });
            });

            // Open dialog event handler
            $(".open-localization-dialog").click(function() {
                var id = $(this).attr("data-id");
                var code = $(this).attr("data-code");
                var resolvedMessage = $(this).attr("data-resolved-message");
                console.log("Get localization");
                console.log(id);
                console.log(code);
                var url = "${request.contextPath}/json/getLocalization";
                $.getJSON( url, { id: id, code: code, resolvedMessage: resolvedMessage },
                        function (data, status, jqxhr) {
                            console.log("getJSON response: ");
                            console.log(data);
                            viewModel.id(data.id);
                            viewModel.code(data.code);
                            viewModel.text(data.text);
                            viewModel.locale(data.locale);
                            viewModel.translation(data.translation);
                        }
                );

                $("#localization-dialog").dialog('open');
                event.preventDefault();
            });

        });
    </script>
</g:if>

<g:javascript>

    function openModalDialog(target, title, width, height, url) {

        var position = {
            my: "center center",
            at: "center center",
            of: window
        };

        $(target).attr("title", title);
        $(target).dialog({
            title: title,
            autoOpen: true,
            modal: true,
            width: width,
            autoResize:true,
            resizable: true,
            minHeight: height,
            position: position,
            open: function(event, ui) {
                $(this).html("Loading...");
                $(this).load(url, function(response, status, xhr) {
                    if (xhr.status !== 200) {
                        $(this).text("");
                        $("<p></p>").addClass("error").text("Error: " + xhr.status + " " + xhr.statusText).appendTo($(this));
                        var error = JSON.parse(response);
                        var stack = $("<div></div>").addClass("stack empty").appendTo($(this));
                        $("<code></code>").text(error.errorMessage).appendTo(stack)
                    }
                });
            }
        }).dialog('open');
    }

    $(document).ready(function() {

        $(".btn-show-dialog").live("click", function (event) {
            event.preventDefault();

            // Prevents dialog from opening if the link is supposed to be disabled
            var disabled = $(this).data("disabled");
            if (disabled) {
              alert("Access denied");
              return false;
            }

            var url = $(this).data("url");
            var title = $(this).data("title");
            var target = $(this).data("target") || "#dlgShowDialog";
            var width = $(this).data("width") || "800";
            var height = $(this).data("height") || "auto";
            openModalDialog(target, title, width, height, url)
        });

        $(".btn-close-dialog").live("click", function (event) {
            event.preventDefault();
            var target = $(this).data("target") || "#dlgShowDialog";
            $(target).dialog( "close" );
        });

	});


</g:javascript>
<script type="text/javascript">
    $(document).ready(function() {

      // Megamenu
      $(".megamenu")
      .megamenu({
        'show_method': 'simple',
        'hide_method': 'simple'
      });

      // Chosen select default default configuration
      $(".chzn-select")
      .chosen({
        width: '100%',
        search_contains: true,
      });

      $(".chzn-select-deselect").livequery(function() {
        $(this)
        .chosen({
          width: '100%',
          search_contains: true,
          allow_single_deselect: true,
        });
      });

      // Select 2 default configuration
      $(".select2")
      .select2({
        placeholder: $(this).data("placeholder") || 'Select an option',
        width: '100%',
        allowClear: true,
      });

      $(".select2withTag")
      .select2({
        placeholder: 'Select an option',
        width: '100%',
        allowClear: true,
        tags: true,
        tokenSeparators: [","],
        createTag: function (tag) {
          return {
            id: tag.term,
            text: tag.term + " (create new)",
            isNew : true
          };
        }
      });

      $(".ajaxSelect2")
      .select2({
        placeholder: $(this).data("placeholder") || 'Select an option',
        width: '100%',
        allowClear: true,
        ajax: {
          // Instead of changing all of the JSON endpoints to return a map with key
          // results (as required by select2), we're just going to change the data
          // on the client side. In other words, select2 is expecting the data to be
          // in data.results, but we're just going to return the data from the server.
          processResults: function (data) {
            return { results: data };
          }
        }
      });

      $(".warehouse-switch")
      .click(function () {
        $("#warehouseMenu")
        .dialog({
          autoOpen: true,
          modal: true,
          width: 800
        });
      });

      function showActions() {
      }

      function hideActions() {
        $(this)
        .children(".actions")
        .hide();
      }

      /* This is used to remove the action menu when the cursor is no longer over the menu */
      $(".action-menu")
      .hoverIntent({
        sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
        interval: 5,   // number = milliseconds for onMouseOver polling interval
        over: showActions,     // function = onMouseOver callback (required)
        timeout: 100,   // number = milliseconds delay before onMouseOut
        out: hideActions       // function = onMouseOut callback (required)
      });

      // Added to fix bug with the now dynamically load Current Stock tab on the stock card page
      $(".action-menu")
      .livequery(function () {
        $(this)
        .hoverIntent({
          sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
          interval: 5,   // number = milliseconds for onMouseOver polling interval
          over: showActions,     // function = onMouseOver callback (required)
          timeout: 100,   // number = milliseconds delay before onMouseOut
          out: hideActions       // function = onMouseOut callback (required)
        });
      });

      $(".action-btn")
      .live('click', function (event) {
        //show the menu directly over the placeholder
        var actions = $(this)
        .parent()
        .children(".actions");

        // Need to toggle before setting the position
        actions.toggle();

        // Set the position for the actions menu
        actions.position({
          my: "left top",
          at: "left bottom",
          of: $(this)
          .closest(".action-btn"),
          collision: "flip fit"
        });

        // To prevent the action button from POST'ing to the server
        event.preventDefault();
      });

      $(".action-menu-item")
      .click(function (event) {
        var actions = $(this)
        .parent()
        .children(".actions");

        // Need to toggle before setting the position
        actions.toggle();
      });

      $(".action-hover-btn")
      .click(function (event) {
        //show the menu directly over the placeholder
        var actions = $(this)
        .parent()
        .children(".actions");

        // Need to toggle before setting the position
        actions.toggle();

        // Set the position for the actions menu
        actions.position({
          my: "right top",
          at: "right bottom",
          of: $(this)
          .closest(".action-hover-btn"),
          collision: "flip"
        });

        // To prevent the action button from POST'ing to the server
        event.preventDefault();
      });

      // OBPIH-2683 Prevent double clicking links
      $("a")
      .one("click", function () {
        $(this)
        .click(function () {
          return false;
        });
      });

    });
</script>

<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.scannerDetection.enabled)}">
    <script src="${createLinkTo(dir:'js/jquery.scannerdetection', file:'jquery.scannerdetection.js')}" type="text/javascript" ></script>
    <script>
        $(document).ready(function() {
          var scanner = $(document).scannerDetection({ ignoreIfFocusOn: ':input' });
          scanner.bind('scannerDetectionComplete', function (event, data) {
            console.log("scanner detected", data, event);
            var barcode = data.string;
            $.ajax({
              dataType: "json",
              url: "${request.contextPath}/json/scanBarcode?barcode=" + barcode,
              success: function (data) {
                console.log(data);
                if (data.url) {
                  window.location.replace(data.url);
                } else {
                  // Perform global search after short delay
                  $("#globalSearch")
                  .notify("Unable to locate object with barcode " + barcode
                    + ". Attempting to search inventory ...", { className: "info" });
                  setTimeout(function () {
                    $("#globalSearch")
                    .val(barcode)
                    .closest("form")
                    .submit();
                  }, 1000);
                }
              },
              error: function (xhr, status, error) {
                console.log(status);
              }
            });
          });
          scanner.bind('scannerDetectionError', function (event, data) {
          });
        });
    </script>
</g:if>
<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.uservoice.widget.enabled)}">
    <script type="text/javascript">
        // Include the UserVoice JavaScript SDK (only needed once on a page)
        UserVoice=window.UserVoice||[];(function(){var uv=document.createElement('script');uv.type='text/javascript';uv.async=true;uv.src='//widget.uservoice.com/YkvS1YXcD9o2f8tiOphf5Q.js';var s=document.getElementsByTagName('script')[0];s.parentNode.insertBefore(uv,s)})();

        //
        // UserVoice Javascript SDK developer documentation:
        // https://www.uservoice.com/o/javascript-sdk
        //

        // Set colors
        UserVoice.push(['set', {
            accent_color: '#448dd6',
            trigger_color: 'white',
            trigger_background_color: 'rgba(46, 49, 51, 0.6)'
        }]);

        // Identify the user and pass traits
        // To enable, replace sample data with actual user traits and uncomment the line
        UserVoice.push(['identify', {
            id: '${session?.user?.id}',
            email: '${session?.user?.email}',
            name: '${session?.user?.name}',
            created_at: '${session?.user?.dateCreated?.time}'
        }]);

        // Add default trigger to the bottom-right corner of the window:
        UserVoice.push(['addTrigger', {
            mode: 'contact',
            trigger_style: 'tab',
            trigger_position: '${grailsApplication.config.openboxes.uservoice.widget.position?:"bottom-right"}',
            trigger_background_color: '#448dd6',
            locale: '${session?.user?.locale?:"en"}'
        }]);
    </script>
</g:if>
<!-- Live Chat -->
<g:if test="${grailsApplication.config.openboxes.zopim.widget.enabled}">
    <!--Start of Zopim Live Chat Script-->
    <script type="text/javascript">
        window.$zopim||(function(d,s){var z=$zopim=function(c){z._.push(c)},$=z.s=
                d.createElement(s),e=d.getElementsByTagName(s)[0];z.set=function(o){z.set.
                _.push(o)};z._=[];z.set._=[];$.async=!0;$.setAttribute("charset","utf-8");
            $.src="${grailsApplication.config.openboxes.zopim.widget.url}";z.t=+new Date;$.
                    type="text/javascript";e.parentNode.insertBefore($,e)})(document,"script");
    </script>
    <!--End of Zopim Live Chat Script-->

    <g:if test="${session.user}">
        <script>
            $zopim(function() {
                $zopim.livechat.setName('${session?.user?.name}');
                $zopim.livechat.setEmail('${session?.user?.email}');
            });
        </script>
    </g:if>
</g:if>
<r:layoutResources/>

</body>
</html>
