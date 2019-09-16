                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProductFromTemplate.label"/></title>  
         <style>
			.gallery {background:'white'; overflow:hidden;}
			.gallery ul {list-style:none; margin:0; padding:0; margin-left:-1em; margin-top:-1em;}
			.gallery ul li {width:21%; margin-left:1em; margin-top:1em; float:left; height: 15em; background:'white'; padding:1em; border: 1px solid white; }
			.gallery ul li:hover { background-color: #fafafa; border: 1px solid blue; }
			         
         
         </style>
         
    </head>
    <body>
    	<div class="body">
			<g:render template="header" model="['currentState':'chooseTemplate']"/>
						 	
			<g:form action="create" method="post" >				
				<div class="box gallery" style="height: 60%;" >					
					<ul>
						<li>
							<div class="template-image">
								<g:link controller="createProductFromTemplate" action="create" event="next" params="[templateName:'gloves']">
									<img src="${createLinkTo(dir:'images/productTemplates',file:'exam-gloves.jpg')}" height="128" width="128"/>
								</g:link>
							</div>		
							<div class="template-name">Gloves</div>						
						</li>
					</ul>
				</div>
            </g:form>
        </div>
		<script>			
			$(document).ready(function() {
				
			});
		</script> 				
    </body>
</html>
