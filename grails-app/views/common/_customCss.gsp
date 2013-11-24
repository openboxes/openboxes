	<style>
		<g:if test="${session?.warehouse?.fgColor && session?.warehouse?.bgColor }">
			#hd {
				padding: 0px;
				margin: 0px; 
				background-color: #${session.warehouse.bgColor}; 
				color: #${session.warehouse.fgColor}; 
			}
            /*
            h2 {
                background-color: #${session.warehouse.bgColor};
                color: #${session.warehouse.fgColor};
            }
            */
			#hd a {
				color: #${session.warehouse.fgColor}; 
			}  		
		</g:if>		
		.warehouse { border: 0px solid #F5F5F5; padding: 5px; display: block; } 
		
		/* Autocomplete */
		.autocomplete {
			background-image: url('${request.contextPath}/images/icons/silk/magnifier.png');
			background-repeat: no-repeat;
			background-position: center right;
			/*padding-left: 20px;*/
		}
	</style>
