
<div class="box">

    <h2>
        <g:isUserAdmin>

            <div class="action-menu" style="position:absolute;top:5px;right:5px">
                <button class="action-btn">
                    <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
                </button>
                <div class="actions">
                    <div class="action-menu-item">

                        <g:if test="${!params.editCatalogs}">
                            <g:link controller="dashboard" action="index" params="[editCatalogs:true]">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" style="vertical-align: middle" />
                                <warehouse:message code="catalogs.editCatalogs.label" default="Edit catalogs"></warehouse:message>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="dashboard" action="index">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'control_end.png')}" style="vertical-align: middle" />
                                <warehouse:message code="tag.doneEditing.label" default="Done editing"></warehouse:message>
                            </g:link>
                        </g:else>
                    </div>
                </div>
            </div>
        </g:isUserAdmin>


        <warehouse:message code="catalogs.label" default="Formularies"/>
    </h2>

	<div class="widget-content" style="max-height: 300px; overflow: auto;" >
        <div id="tag-summary" >
            <g:if test="${params.editCatalogs}">
                <g:isUserAdmin>
                    <table>
                        <thead>
                            <tr>
                                <th><warehouse:message code="catalogs.name.label" default="Formulary"/></th>
                                <th><warehouse:message code="tag.count.label" default="Count"/></th>
                                <th><warehouse:message code="default.actions.label"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${catalogs }" var="catalog" status="i">
                                <tr class="${i%2?'odd':'even'}">
                                    <td>
                                        ${catalog.name?:"Empty catalog"}
                                    </td>
                                    <td>
                                        ${catalog.count}
                                    </td>
                                    <td>
                                        <g:link controller="dashboard" action="hideCatalog" id="${catalog}" params="[editCatalogs:true]">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_cross.png')}"/></g:link>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </g:isUserAdmin>
            </g:if>
            <g:else>
                <g:if test="${catalogs}">
                    <div class="tagcloud">
                        <g:each in="${catalogs }" var="catalog">
                            <g:if test="${catalog?.count > 1}">
                                <g:link controller="inventory" action="browse" params="['catalogs':catalog.id]" rel="${catalog?.count }">
                                    ${catalog.name?:"Empty catalog" } (${catalog?.count })</g:link>
                            </g:if>
                        </g:each>
                    </div>
                </g:if>
                <g:else>
                    <div style="margin:10px;" class="center">
                        <span class="fade"><warehouse:message code="catalogs.noCatalogs.label"/></span>
                    </div>
                </g:else>
            </g:else>
        </div>
		<div class="clear"></div>
	</div>
</div>
<script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>

