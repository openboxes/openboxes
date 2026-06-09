<%@ page defaultCodec="html" %>
<div class="print-header">
    <table class="w100 fixed-layout no-border-table">
        <tr>
            <td>
                <h1 class="m-0">${g.message(code: 'deliveryNote.button.print.label')}</h1>
            </td>
            <td class="right">
                <div class="button-container">
                    <g:select id="select-orientation" name="orientation" from="${['', 'portrait', 'landscape']}" value="${params.orientation}"/>
                    <button id="print-page" type="button" class="button">
                        <warehouse:message code="default.button.print.label"/>
                    </button>
                    <a href="javascript:window.close();" class="button">
                        <warehouse:message code="default.button.close.label"/>
                    </a>
                </div>
            </td>
        </tr>
    </table>
    <hr/>
</div>

