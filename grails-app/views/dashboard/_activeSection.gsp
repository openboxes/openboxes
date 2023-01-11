<script type="text/javascript">
    $(document).ready(function() {
          const matchingMenuSection = $("#${section}").get(0);
          const matchingMenuSectionCollapsable = $("#" + "${section}" + "-collapsed").get(0);
          if (matchingMenuSection) {
            matchingMenuSection.classList.add('active-section');
          }
          if (matchingMenuSectionCollapsable) {
            matchingMenuSectionCollapsable.classList.add('active-section');
          }
    })
</script>
