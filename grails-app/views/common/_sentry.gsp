<%@ page import="org.pih.warehouse.monitoring.SentryConfigResolver" %>

<%--
Initializes Sentry on GSPs. React pages initialize Sentry via the main bundle instead.
See src/js/sentry.js and webpack.config.js.
--%>
<g:if test="${grailsApplication.config.getProperty('sentry.web.dsn')}">
    <%--
    Allows the frontend to continue the backend's trace for this request so that both show up as a single trace.
    This is important for GSPs since they're closely tied together (React pages already tie their traces to API calls).
    --%>
    <g:set var="sentryTraceparent" value="${SentryConfigResolver.traceparent}"/>
    <g:if test="${sentryTraceparent}">
        <meta name="sentry-trace" content="${sentryTraceparent}"/>
    </g:if>
    <g:set var="sentryBaggage" value="${SentryConfigResolver.baggage}"/>
    <g:if test="${sentryBaggage}">
        <meta name="baggage" content="${sentryBaggage}"/>
    </g:if>
    <script type="text/javascript">
        window.SENTRY_CONFIG = ${raw(SentryConfigResolver.getWebConfigJson(session.user, session.warehouse))};
    </script>
    <g:render template="/common/sentryBundle"/>
</g:if>
