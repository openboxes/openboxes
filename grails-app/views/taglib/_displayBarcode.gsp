<div class="barcode">
    <div class="barcode-image">
        <img src="${createLink(controller:'product',action:'barcode',params:[data:attrs?.data,format:attrs?.format?:'CODE_39',width:attrs?.width?:200,height:attrs?.height?:20]) }" class="top"/>
    </div>
    <g:if test="${attrs.showData}">
        <div class="barcode-data">
            ${attrs.data}
        </div>
    </g:if>
</div>
