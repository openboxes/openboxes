                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Choose product template</title>  
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
						<li>
							<div class="template-image">
								<g:link controller="createProductFromTemplate" action="create" event="next" params="[templateName:'masks']">
									<img src="${createLinkTo(dir:'images/productTemplates',file:'masks.jpg')}" height="128" width="128"/>
								</g:link>
							</div>		
							<div class="template-name">Masks</div>						
						</li>
						<li>
							<div class="template-image">
								<g:link controller="createProductFromTemplate" action="create" event="next" params="[templateName:'medicine']">
									<img src="${createLinkTo(dir:'images/productTemplates',file:'pills.png')}" height="128" width="128"/>
								</g:link>
							</div>		
							<div class="template-name">Medicine</div>						
						</li>
							
							
					</ul>			
				
				
				</div>			
				<%-- 
				<div class="buttons">
                    <g:submitButton class="back" name="back" value="Back" />
                    <g:submitButton class="next" name="next" value="Next" />
                    <g:submitButton class="cancel" name="cancel" value="Cancel" />
					
					
				</div>
				--%>
            </g:form>
        </div>
		<script>			
			$(document).ready(function() {
				
			});
		</script> 				
    </body>
</html>
