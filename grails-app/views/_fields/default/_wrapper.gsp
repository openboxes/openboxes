<tr class="prop">
    <td valign="top" class="name">
        <label for="${(prefix ?: '') + property}">${label}<g:if test="${required}"> <span class="required-indicator">*</span></g:if></label>
    </td>
    <td valign="top" class="value ${invalid ? 'errors' : ''}">
        ${raw(widget)}
    </td>
</tr>
