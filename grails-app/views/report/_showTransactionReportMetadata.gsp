<div class="box fade">
    <ul>
        <li>Data includes <b><g:formatNumber number="${transactionCount}" maxFractionDigits="0"/> transactions</b>
        <li>Oldest transaction is from <b><g:prettyDateFormat date="${minTransactionDate}"/></b></li>
        <li>Most recent transaction is from <b><g:prettyDateFormat date="${maxTransactionDate}"/></b></li>
        <li>Data will be refreshed <b title="${nextFireTime}"><g:prettyDateFormat date="${nextFireTime}"/></b></li>
    </ul>
</div>
<div class="buttons center">
    <g:isSuperuser>
        <g:link controller="report" action="refreshTransactionFact" class="button"
                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'reload.png')}" />
            ${message(code:"default.button.refresh.label")} ${message(code:"default.data.label")}
        </g:link>
    </g:isSuperuser>
    <a href="javascript:void(-1);" class="btn-close-dialog button">
        <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" />
        ${message(code:"default.button.close.label")}
    </a>
</div>
