                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProduct.label"/></title>  
    </head>
    <body>
    	<div class="body">
    	
			<g:hasErrors bean="${search}">
				<div class="errors">
					<g:renderErrors bean="${search}" as="list" />
				</div>				
			</g:hasErrors> 			
			
			<g:render template="header" model="['currentState':'results']"/>
						 	
			<g:form action="create" method="post" >			
				<div class="box">
					<table>
		                <tbody>
		                    <tr >
		                        <td valign="top">	
			                        <label>Enter UPC/EAN</label>
			                    
		                            <input type="text" id="searchTerms" name="searchTerms" 
		                            	value="${search?.searchTerms}" class="text medium" size="80"/>
				                    <g:submitButton class="search" name="search" value="Search" />
				                    &nbsp;
		                            <g:link event="back">Reset</g:link>	
		                        </td>
		                    </tr>
		                </tbody>
	                </table>			
                </div>
				
						
				<h3>
					Google Results -
					<g:set var="endIndex" value="${(search.startIndex > search.totalResults) ? search?.startIndex + search?.itemsPerPage : search.totalResults}"/>
					
					<span class="fade">Showing ${search?.startIndex } - ${endIndex } of 
						${search.totalResults } results for</span> ${search.searchTerms }				
				</h3>
				
				<style>
					.navigation { position: relative; height: 30px; }
					.previous { position: absolute; left: 0; vertical-align: middle; padding: 10px; }
					.next { position: absolute; right: 0; vertical-align: middle;  padding: 10px; }
					.image { text-align: center; }
					.name { text-align: center; }
					.product { position: relative; height: 100%; }
					.author { text-align: center; }
				
					.gallery {background:'white'; overflow:hidden;}
					.gallery ul {list-style:none; margin:0; padding:0; margin-left:-1em; margin-top:-1em;}
					.gallery ul li {width:21%; margin-left:1em; margin-top:1em; float:left; height: 15em; background:'white'; padding:1em; border: 1px solid white; }
					.gallery ul li:hover { background-color: #fafafa; border: 1px solid blue; }
					
				</style>
				
				
				<g:hiddenField name="startIndex" value="${search?.startIndex}"/>
				<div class="navigation box center">
					<g:if test="${search.startIndex > 1 }">
						<span class="previous">
	    	                <g:link controller="createProduct" action="create" 
	    	                	event="previousResults" params="[searchTerms:search.searchTerms,startIndex:search.startIndex]">&laquo; Previous</g:link>
	                    </span>
                    </g:if>
                    <g:if test="${search.startIndex + search.itemsPerPage < search.totalResults }">
	                    <span class="next">
		                    <g:link controller="createProduct" action="create" 
		                    	event="nextResults" params="[searchTerms:search.searchTerms,startIndex:search.startIndex]">Next &raquo;</g:link>
						</span>
					</g:if>
				</div>
				
				<div class="gallery">
					<g:set var="count" value="${0 }"/>
					<ul>
						<g:each var="product" in="${search.results }" status="i">
							<li>
								<div id="${product?.googleId }" class="open-dialog product">
									<div class="image">
										<g:if test="${product.images }">
											<img src="${product.images[0] }" height="100" width="100"/>
										</g:if>
										<g:else>
											<img src="${createLinkTo(dir:'images',file:'default_product.png')}" class="middle" width="100" height="100"/>
										</g:else>
									</div>
									<br/>
									<div class="name">
										
										<a href="javascript:void(-1);" id="${product?.googleId }" class="open-dialog">${product.title } </a>		
									</div>
									<div class="author fade">${product.author }</div>
								</div>
							</li>
						</g:each>
					</ul>				
				</div>			
				<div class="buttons center box">					
                    <g:submitButton name="back" value="Back" />
                    <g:submitButton name="next" value="Next" />
                    <g:submitButton name="cancel" value="Cancel" />
                    <g:link controller="createProduct" action="index">Back to search</g:link>
					
				</div>
            </g:form>
        </div>
        
        <g:each var="product" in="${search.results }" status="i">
			<div class="dialog" id="dialog-${product?.googleId }" title="${product.title }" style="display: none;"> 
				<g:form action="create" method="post" >		
					<table>
						<tr>
							<td>
								<g:if test="${product.images }">
									<img src="${product.images[0] }" height="100" width="100" class="left top"/>
								</g:if>
								<g:else>
									<img src="${createLinkTo(dir:'images',file:'default_product.png')}" class="left top" width="100" height="100"/>
								</g:else>									
							</td>
							<td>
								<table>
									<tr class="">
										<td class="label right">
											<label class="clear">Name:</label>
										</td>
										<td class="value">
											${product.title }
										</td>
									</tr>										
									<tr class="prop">
										<td class="label right">
											<label class="clear">Description:</label>
										</td>
										<td class="value">
											${product.description }
										</td>
									</tr>										
									<tr class="prop">
										<td class="label right">
											<label class="clear">Source:</label>
										</td>
										<td class="value">
											<a href="${product.link }" target="_blank">${product.author }</a>
										</td>
									</tr>										
									<tr class="prop">
										<td class="label right">
											<label class="clear">Brand:</label>												
										</td>
										<td class="value">
											${product.brand }
										</td>
									</tr>										
									<tr class="prop">
										<td class="label right">
											<label class="clear">GTIN(s):</label>												
										</td>
										<td class="value">
											<ul>
												<g:each in="${product.gtins }" var="gtin">
													<li>${gtin }</li>
												</g:each>
											</ul>
										</td>
									</tr>				
									<tr class="prop">
										<td class="label right">
											<label class="clear">Google ID</label>
										</td>
										<td class="value">
											${product.googleId }
										</td>
									</tr>										
								</table>
							</td>
						</tr>							
					</table>
					<hr/>
					<div class="buttons center">
						<g:hiddenField name="id" value="${product?.googleId }"/>
						<g:submitButton class="select" name="select" value="Create new product based on this product" />
						&nbsp;								
						<a href="javascript:void(-1);" class="close-dialog" id="${product?.googleId }">Cancel</a>
					</div>
				</g:form>
			</div>        
        </g:each>
        
        
		<script>			
			$(document).ready(function() {
				$(".dialog").dialog({ autoOpen: false, modal: true, width: '800px', top: 10});	

				$(".open-dialog").click(function() { 
					var id = $(this).attr("id");
					$("#dialog-" + id).dialog('open');
				});
				$(".close-dialog").click(function() { 
					var id = $(this).attr("id");
					$("#dialog-" + id).dialog('close');
				});

				$("#searchTerms").focus();
				$("#searchTerms").select();
			});
		</script> 				
    </body>
</html>
