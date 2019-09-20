<div class="barcode">
    <div class="barcode-image">
        <img src="${createLink(controller:'product',action:'barcode',params:[data:attrs?.data,format:attrs?.format?:'CODE_128',width:attrs?.width,height:attrs?.height?:36]) }" class="top"/>
    </div>
    <g:if test="${attrs.showData}">
        <div class="barcode-data center">
            ${attrs.data}
        </div>
    </g:if>
</div>
