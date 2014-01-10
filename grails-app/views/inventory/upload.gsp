<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="inventory.upload.label" default="Upload inventory"/></title>
</head>
<body>
    <g:form action="upload" method="post" enctype="multipart/form-data">
        <label for="file">File:</label>
        <input type="file" name="file" id="file" />
        <input class="save" type="submit" value="Upload" />
    </g:form>


    <g:if test="${inventoryList}">
        <table>
            <g:each var="row" in="${inventoryList}">
                <tr>
                    <g:each var="column" in="${row}">
                        <td>${column.key} = ${column.value}</td>
                    </g:each>
                </tr>
            </g:each>
        </table>
    </g:if>

</body>
</html>