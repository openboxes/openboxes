<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="custom" />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'processRequisition.js')}" type="text/javascript" ></script>
    <title>Process requisition</title>
</head>
<body>
<g:form name="requisitionForm" method="post" action="save">

    <h2>ID: <span data-bind="text: requisition.id"> </span> . </h2>
    <h2>ID: <span data-bind="text: requisition.color"> </span> . </h2>
</g:form>


<script type="text/javascript">

    var requisition = new process_requisition.Requisition("${requisition?.id}", ${requisitionItems});
    $(function(){

        console.log("Processing requisition " + requisition.id());
        console.log("Found requisition items: " + JSON.stringify( requisition.requisitionItems()));
        var viewModel = new process_requisition.ViewModel(requisition,${requisitionItems});
        ko.applyBindings(viewModel);
    });
</script>

</body>
</html>