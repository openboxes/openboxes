<g:if test="${grailsApplication.config.openboxes.hotjar.enabled}">
<!-- Hotjar Tracking Code for openboxes.com -->
<script>
    var hjid = ${grailsApplication.config.openboxes.hotjar.hjid};
    var hjsv = ${grailsApplication.config.openboxes.hotjar.hjsv};
    (function(h,o,t,j,a,r){
        h.hj=h.hj||function(){(h.hj.q=h.hj.q||[]).push(arguments)};
        h._hjSettings={hjid:hjid,hjsv:hjsv};
        a=o.getElementsByTagName('head')[0];
        r=o.createElement('script');r.async=1;
        r.src=t+h._hjSettings.hjid+j+h._hjSettings.hjsv;
        a.appendChild(r);
    })(window,document,'https://static.hotjar.com/c/hotjar-','.js?sv=');

    // Hotjar user attributes
    // https://help.hotjar.com/hc/en-us/articles/4402892526487-What-are-User-Attributes-
    // https://help.hotjar.com/hc/en-us/articles/360038394053-How-to-Set-Up-User-Attributes
    var userId = '${session?.user?.username}' || null;
    window.hj('identify', userId, {});
</script>
</g:if>
