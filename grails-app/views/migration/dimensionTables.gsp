<div id="tabs-3">
    <div class="box">
        <h2><g:message code="data.dimensions.label" default="Dimension"/></h2>
        <table>
            <thead>
            <tr>
                <th>Table</th>
                <th>Count</th>
            </tr>
            </thead>
            <tbody>
            <tr class="prop">
                <td class="name">Date Dimension</td>
                <td class="value">${dateDimensionCount}</td>
            </tr>
            <tr class="prop">
                <td class="name">Location Dimension</td>
                <td class="value">${locationDimensionCount}</td>
            </tr>
            <tr class="prop">
                <td class="name">Lot Dimension</td>
                <td class="value">${lotDimensionCount}</td>
            </tr>
            <tr class="prop">
                <td class="name">Product Dimension</td>
                <td class="value">${productDimensionCount}</td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td></td>
                <td>
                    <div class="button-container">
                        <g:link controller="report" action="truncateDimensions" class="button">Truncate</g:link>
                        <g:link controller="report" action="buildDimensions" class="button">Build</g:link>
                    </div>
                </td>
            </tr>
            </tfoot>
        </table>
    </div>
</div>
