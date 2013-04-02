
<div class="widget-large">
	<div class="widget-header">

        <g:isUserAdmin>
            <div style="float: right; font-weight: normal; font-size: 10px;" class="fade box">
                <g:if test="${!params.editTags}">
                    <g:link controller="dashboard" action="index" params="[editTags:true]">
                        <warehouse:message code="tag.editTags.label" default="Edit tags"></warehouse:message>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="dashboard" action="index">
                        <warehouse:message code="tag.doneEditing.label" default="Done editing"></warehouse:message>
                    </g:link>
                </g:else>
            </div>
        </g:isUserAdmin>
        <h2>
            <warehouse:message code="tags.label" default="Tags"/>
        </h2>
	</div>
	<div class="widget-content">
        <div id="tag-summary">
            <g:if test="${tags}">
                <div id="tagcloud">
                    <g:each in="${tags }" var="tag">
                        <g:link controller="inventory" action="browse" params="['tag':tag.tag]" rel="${tag?.products?.size() }">
                            ${tag.tag } (${tag?.products?.size() })</g:link>
                        <g:if test="${params.editTags}">
                            <g:isUserAdmin>
                                <g:link controller="dashboard" action="hideTag" id="${tag.id}" params="[editTags:true]">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_cross.png')}"/></g:link>
                            </g:isUserAdmin>
                            <br/>
                        </g:if>

                    </g:each>
                </div>
            </g:if>
            <g:else>
                <div style="margin:10px;" class="center">
                    <span class="fade"><warehouse:message code="tag.noTags.label"/></span>
                </div>
            </g:else>
        </div>

        <%--
		<div id="tagSummary">
            <g:isUserAdmin>
                <div style="float: right">
                    <g:if test="${!params.editTags}">
                        <g:link controller="dashboard" action="index" params="[editTags:true]">
                            <warehouse:message code="tag.editTags.label" default="Edit tags"></warehouse:message>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="dashboard" action="index">
                            <warehouse:message code="tag.doneEditing.label" default="Done editing"></warehouse:message>
                        </g:link>
                    </g:else>
                </div>
            </g:isUserAdmin>
			<g:each in="${tags }" var="tag">
                <span class="tag">
                    <g:link controller="inventory" action="browse" params="['tag':tag.tag]">
                        ${tag.tag } (${tag?.products?.size() })
                    </g:link>

                    <g:if test="${params.editTags}">
                        <g:isUserAdmin>
                            <g:link controller="dashboard" action="hideTag" id="${tag.id}">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_cross.png')}"/>
                            </g:link>
                        </g:isUserAdmin>
                    </g:if>
                </span>
			</g:each>
		</div>
		--%>
		<div class="clear"></div>
	</div>
</div>