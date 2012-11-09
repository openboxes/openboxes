<td valign='top' class='value' style="border-left-width: 1px; border-left-style: solid; border-left-color: black">
    <label>${i+1}. ${requisitionItem?.product.name}</label>
</td>
<td valign='top' class='value'>
    <div style="width: 120px;">Requested: ${requisitionItem?.quantity}</div>
</td>
<td valign='top' class='value'>
    <div style="width: 100px;">Picked: %{--${picklistItem?.quantity}--}%</div>
</td>
<td valign='top' class='value'>
    <div style="width: 150px;">Remaining: %{--${requisitionItem?.quantity - picklistItem?.quantity}--}%</div>
</td>
<td style="border-right-width: 1px; border-right-style: solid; border-right-color: black">
    <div class="center">
        %{--<g:if test="${picklistItem?.quantity == 0}">--}%
            %{--<img src="${createLinkTo(dir: 'images/icons/silk', file: 'asterisk_red.png')}" class="top"/>--}%
        %{--</g:if>--}%
        %{--<g:elseif test="${requisitionItem?.quantity > picklistItem?.quantity}">--}%
            %{--<img src="${createLinkTo(dir: 'images/icons/silk', file: 'asterisk_yellow.png')}" class="top"/>--}%
        %{--</g:elseif>--}%
        %{--<g:elseif test="${requisitionItem?.quantity == picklistItem?.quantity}">--}%
            %{--<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_green.png')}" class="top"/>--}%
        %{--</g:elseif>--}%
    </div>
</td>
