<!DOCTYPE html>
<html lang="en">
<head>
  <title>POPOVER TEST PAGE</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="../../src/css/main.scss">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
  <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.4/dist/jquery.slim.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>



<!-- This block initializes popovers on the page -->
<script>
$(function () {
  $('[data-toggle="popover"]').popover()
})
$('.popover-dismiss').popover({
  trigger: 'focus'
})
</script>



<!-- This block fetches the glossary -->
<script>
fetch("../glossary.json")
    .then(response => response.json())
    .then(data =>
        console.log(data.glossary.con.conTerm)
        document.querySelector("#consumption").innerText = data.glossary.con.conTerm
        );
    const glossarytest = this.response; // SCOPE problem?? Local variable?? "this" problem??
const glossary = TESTFETCH.response
</script>



<!-- Insert your glossary terms here! -->
<script>
html.onload = function TEST1 {
  JSON.parse(glossary.bin_l.bin_lTerm);
  JSON.stringify(this.response);
}
const bin_lTerm = TEST1.response

html.onload = function TEST2 {
  JSON.parse(glossary.bin_l.bin_lDef);
  JSON.stringify(this.response);
}
const bin_lDef = TEST2.response
</script>
</head>

<body>

<!-- Why doesn't hover work? Why only click? -->
<a href="#" id="test" data-toggle="popover" data-animation="true" data-placement="top" trigger="hover focus" title=bin_lTerm data-content=bin_lDef>Toggle popover</a>

<script src="../glossary.json"></script>
</body>
</html>
