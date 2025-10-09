<!-- If we're in localization mode, enable Crowdin's In-Context Localization feature -->
<g:if test="${session.useDebugLocale}">
    <script type="text/javascript">
      var _jipt = [];
      // Enables the Crowdin integration
      _jipt.push(['project', 'openboxes']);
      // Enables the 'X' button for quitting out of In-Context Localization mode before you've even
      // signed in. Needed so that users can bail out of the flow if they don't have a Crowdin account.
      _jipt.push(['escape', function() {
        window.location.href = '${request.contextPath}/user/disableLocalizationMode';
      }]);
    </script>
    <script type="text/javascript" src="//cdn.crowdin.com/jipt/jipt.js"></script>
</g:if>
