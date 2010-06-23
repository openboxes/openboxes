
<%@ page import="org.pih.warehouse.Warehouse" %>
<g:form controller="auth" action="doLogin" method="post">		  
    <div class="dialog">
		<g:if test="${flash.message}">
		    <div class="message">${flash.message}</div>
		</g:if>		
		<div id="loginForm">

			<g:hasErrors bean="${userInstance}">
	           <div class="errors">
	               <g:renderErrors bean="${userInstance}" as="list" />
	           </div>
	        </g:hasErrors>		

			<div class="notice">
				Login as <b>manager</b> : <b>password</b> to use the system as a <b>Warehouse Manager</b>. 
			</div>			

			<fieldset> 			
			
				<div  style="padding: 15px;">
					<div class="prop">
						<!-- <label for="warehouse.id">Log into:</label><br/> -->
					    <g:select class="large" name="warehouse.id" from="${org.pih.warehouse.Warehouse.list()}" 
					    	optionKey="id" value=""
					    	noSelection="[null: 'Choose warehouse to manage']"/>
					</div>  					
					<hr style="margin-bottom: 1.4em; margin-top: 1.4em"/>
					    
					<div class="prop">
						<label for="username">Username or email:</label><br/>
						<input type="text" class="title" name="username" id="username" value="${userInstance?.username}">
					</div> 
					<div  class="prop">
						<label for="password">Password:</label><br/>
						<input type="password" class="title" name="password" id="password" value="${userInstance?.password}">
						
						<span class="buttons" >
							<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Login</button>					   
						</span>
											
					</div>	
				</div>
			</div>
		</fieldset> 
	</div>
	
	<%-- 
    <div class="buttons">
		<span class="button">
			<g:submitButton name="login" class="save" value="${message(code: 'default.button.login.label', default: 'Login')}" />
		</span>	
    </div>
    --%>	
	<br/>


</g:form>