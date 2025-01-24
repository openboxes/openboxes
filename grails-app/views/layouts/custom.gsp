<%@ page import="com.newrelic.api.agent.NewRelic; java.util.Locale" %>
<?xml version="1.0" encoding="UTF-8"?>
<html lang="en">
<head>
    <%= NewRelic.getBrowserTimingHeader() %>

    <!-- Include default page title -->
    <title><g:layoutTitle default="OpenBoxes" /></title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.14.7/dist/umd/popper.min.js" integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>
    <!-- YUI -->
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/yui/2.9.0/reset-fonts-grids/reset-fonts-grids.css" type="text/css">

    <!-- Remix icons -->
    <link href="https://cdn.jsdelivr.net/npm/remixicon@2.5.0/fonts/remixicon.css" rel="stylesheet">

    <!-- Include Favicon -->
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

    <!-- Include Main CSS -->
    <link rel="stylesheet" href="${resource(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'js/chosen',file:'chosen.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/select2@4.0.13/dist/css/select2.min.css" type="text/css" />
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/css/jquery.dataTables.min.css" type="text/css">
    <link rel="stylesheet" href="${resource(dir:'css',file:'footable.css')}" type="text/css" media="all" />
    <link href='https://fonts.googleapis.com/css?family=Inter' rel='stylesheet'>

    <g:if test="${session.useDebugLocale}">
        <script type="text/javascript">
            var _jipt = [];
            _jipt.push(['project', 'openboxes']);
        </script>
        <script type="text/javascript" src="//cdn.crowdin.com/jipt/jipt.js"></script>
    </g:if>

    <!-- Include javascript files -->
    <g:javascript library="application"/>

    <!-- Include jQuery UI files -->
    <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.8.24/jquery-ui.min.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.8.24/themes/smoothness/jquery-ui.min.css" type="text/css" media="all" />

    <link rel="stylesheet" href="${resource(dir:'css',file:'openboxes.css')}?v=${g.meta(name: 'app.version')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'loading.css')}" type="text/css" media="all" />

    <!-- jquery validation messages -->
    <g:if test="${ session?.user?.locale && session?.user?.locale != 'en'}">
        <script src="${resource(dir:'js/jquery.validation/', file:'messages_'+ session?.user?.locale + '.js')}"  type="text/javascript" ></script>
    </g:if>

    <!-- Grails Layout : write head element for page-->
    <g:layoutHead />

    <g:render template="/common/customCss"/>
    <g:render template="/common/fullstory"/>
    <g:render template="/common/hotjar"/>

    <g:googleSiteTag />
    <r:layoutResources/>

    <!-- TODO: replace fontawesowe by remix icons -->
    <script src="https://use.fontawesome.com/releases/v5.14.0/js/all.js" data-auto-replace-svg="nest"></script>
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
        <div class="impersonate-box d-flex justify-content-between align-items-center" role="alert" aria-label="impersonate">
            <div class="info d-flex align-items-center">
                <i class="ri-shield-user-line"></i>
                <span>
                    <g:message code="user.impersonate.message" args="[session.user.username]" default="You are impersonating user"/>
                    <span class="font-weight-bold">${session?.user?.username}</span>
                </span>
            </div>
            <a href="/openboxes/auth/logout">
                <button class="primary-button">
                    ${g.message(code:'default.logout.label', default: "Logout")}
                </button>
            </a>
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
        <nav aria-label="main" id="main-wrapper" class="navbar navbar-expand-md navbar-light bg-light bg-white p-0 px-md-4">
            <div class="d-flex p-2 p-md-0 justify-content-between flex-grow-1">
                <div class="d-flex align-items-center">
                <g:displayLogo location="${session?.warehouse?.id}" includeLink="${true}" />
                <g:set var="locationColor" value="${session?.warehouse?.bgColor?.replace('#', '')?.toUpperCase()}"/>
                <g:if test="${locationColor && ['FFFFFF', 'FFFF'].any{ it == locationColor }}">
                    <g:set var="locationColorVariable" value="--location-color: unset"/>
                </g:if>
                <g:else>
                    <g:set var="locationColorVariable" value="--location-color: #${locationColor}"/>
                </g:else>
                <div class="tooltip2">
                    <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + (request.queryString?'?':'') + (request.queryString?:'') }"/>
                    <button
                        class="btn-show-dialog location-chooser__button"
                        style="${locationColorVariable}"
                        aria-label="location-chooser"
                        data-dialog-class="location-chooser"
                        data-resizable="false"
                        data-draggable="true"
                        data-width="900"
                        data-title="${g.message(code:'dashboard.chooseLocation.label')}" data-height="300"
                        data-url="${request.contextPath}/dashboard/changeLocation?targetUri=${targetUri}"
                    >
                        <span class="location-chooser__button-title">${session?.warehouse?.name}</span>
                        <g:if  test="${grailsApplication.config.openboxes.logo.label}">
                            <span class="location-chooser__button-tag">
                                ${grailsApplication.config.openboxes.logo.label}
                            </span>
                        </g:if>
                    </button>
                    <g:if test="${session?.warehouse?.name?.length() > 20}">
                        <span class="tooltiptext2">${session?.warehouse?.name}</span>
                    </g:if>
                </div>
            </div>
                <button
                    class="navbar-toggler"
                    type="button"
                    data-toggle="collapse"
                    data-target="#navbarToggler"
                    aria-controls="navbarToggler"
                    aria-expanded="false"
                    aria-label="Toggle navigation"
                >
                    <i class="ri-menu-line"></i>
                </button>
            </div>
            <div class="collapse navbar-collapse w-100" id="navbarToggler">
                <g:include controller="dashboard" action="megamenu" />
            </div>
        </nav>
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
            <g:include controller="dashboard" action="footer"/>
        </div>
    </g:if>
</div>
<div id="dlgShowDialog" class="dialog hidden">
    <div id="dlgShowDialogContent" class="empty center">
        Loading ...
    </div>
</div>
<!-- Include other plugins -->
<script src="${resource(dir:'js/jquery.ui/js/', file:'jquery.ui.autocomplete.selectFirst.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.cookies/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.cookie/', file:'jquery.cookie.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.tmpl/', file:'jquery.tmpl.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.tmplPlus/', file:'jquery.tmplPlus.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.livequery/', file:'jquery.livequery.min.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.livesearch/', file:'jquery.livesearch.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.hoverIntent/', file:'jquery.hoverIntent.minified.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${resource(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${resource(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
<g:if test="${System.getenv().get('headless') != 'false'}" env="test">
    <!--headless driver throw error when using watermark-->
</g:if>
<g:else>
    <script src="${resource(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
</g:else>
<script src="${resource(dir:'js/', file:'global.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/jquery.megaMenu/', file:'jquery.megamenu.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/', file:'underscore-min.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/chosen/', file:'chosen.jquery.min.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/feedback/', file:'feedback.js')}" type="text/javascript" ></script>
<script src="${resource(dir:'js/footable/', file:'footable.js')}" type="text/javascript" ></script>
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

            // Delete localition event handler
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

    function openModalDialog(target, title, width, height, url, reload, resizable, draggable, dialogClass) {

        var position = {
            my: "center",
            at: "center",
            of: window,
            offset: "0 -100%"
        };

        $(target).attr("title", title);
        $(target).dialog({
            title: title,
            autoOpen: true,
            modal: true,
            width: width,
            autoResize:true,
            resizable: resizable,
            minHeight: height,
            position: position,
            draggable: draggable,
            dialogClass: dialogClass,
            create: function(event, ui) {
                var widget = $(this).dialog("widget");
                $(".ui-dialog-titlebar-close span", widget)
                        .removeClass("ui-icon-closethick")
                        .removeClass("ui-icon")
                        .addClass("ri-close-line")
                        .empty();

                $(".ui-dialog-titlebar-close")
                        .attr('aria-label', 'close');
            },
            close: function(event, ui) {
              if (reload) {
                location.reload();
              }
            },
            open: function(event, ui) {
              const loadingElement =
              "<div class='spinner-container'>" +
              "<div class='spinner-border circle-spinner' role='status'>" +
              "<span class='sr-only'>Loading...</span>" +
              "</div>";
              "</div>";
                $(this).html(loadingElement);
                $(this).load(url, function(response, status, xhr) {
                    if (xhr.status !== 200) {
                        $(this).text("");
                        $("<p></p>").addClass("error").text("Error: " + xhr.status + " " + xhr.statusText).appendTo($(this));
                        try {
                            var error = JSON.parse(response);
                            var stack = $("<div></div>").addClass("stack empty").appendTo($(this));
                            $("<code></code>").text(error.errorMessage).appendTo(stack)
                        } catch (err) {
                            console.log(err);
                        }
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
            var reload = $(this).data("reload") || false;
            var resizable = $(this).data("resizable");
            var draggable = $(this).data("draggable");
            var dialogClass = $(this).data("dialog-class") || "";
            openModalDialog(target, title, width, height, url, reload, resizable, draggable, dialogClass);
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

    const applyActiveSection = (sectionName) => {
      const menuConfigValues = $(".menu-config-value").toArray();
      const section = menuConfigValues.find(it => sectionName === it.name);

      const matchingMenuSection = $("#" + section?.name).get(0);
      const matchingMenuSectionCollapsable = $("#" + section?.name + "-collapsed").get(0);

      if (matchingMenuSection) {
        matchingMenuSection.classList.add('active-section');
      }
      if (matchingMenuSectionCollapsable) {
        matchingMenuSectionCollapsable.classList.add('active-section');
      }
    }
</script>

<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.scannerDetection.enabled)}">
    <script src="${resource(dir:'js/jquery.scannerdetection', file:'jquery.scannerdetection.js')}" type="text/javascript" ></script>
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
<%= NewRelic.getBrowserTimingFooter() %>
</body>
</html>
