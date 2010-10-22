                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Confirm Purchase</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link action="list">Person List</g:link></span>
        </div>
        <div class="body">
           <h1>Confirm Purchase</h1>
           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>
           <g:hasErrors bean="${person}">
                <div class="errors">
                    <g:renderErrors bean="${person}" as="list" />
                </div>
           </g:hasErrors>
           <g:form action="shoppingCart" method="post" >
               <div class="dialog">     
					<h2>Please confirm your purchase details:</h2>
					<h4>Items</h4>
					<g:each in="${cartItems}">
						<p>${it.title} - ${it.author} - ${it.price}</p>
					</g:each>                                          
					<p>Total: ${cartItems.count()}</p>
					<h4>Your Name:</h4>
					<p>${person.name}</p>   
					<h4>Delivery Address:</h4>
					<p>House Number: ${address?.number}</p>
					<p>Post Code: ${address?.postalCode}</p>
					<h4>Payment Info</h4>
					<p>Card Number: ${paymentDetails.cardNumber}</p>
					<p>Expiry Date: ${paymentDetails.expiryDate}</p>
					
               </div>
               <div class="buttons">
                     <span class="formButton">
						<g:submitButton name="back" value="Back"></g:submitButton>								
						<g:submitButton name="confirm" value="Confirm"></g:submitButton>
                     </span>
               </div>
            </g:form>
        </div>
    </body>
</html>
