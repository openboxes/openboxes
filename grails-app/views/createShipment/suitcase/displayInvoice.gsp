                                                           
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Your Invoice</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a href="${createLinkTo(dir:'')}">Home</a></span>
        </div>
        <div class="body">
           <h1>Your Invoice</h1>
           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>
           <g:hasErrors bean="${person}">
                <div class="errors">
                    <g:renderErrors bean="${person}" as="list" />
                </div>
           </g:hasErrors>
               <div class="dialog">     
					<h2>Below is your invoice. Print it out for your records:</h2>
					<h4>Invoice Number:</h4>
					<p>${order.invoiceNumber}</p>
					<h4>Items</h4>
					<g:each in="${order.items}">
						<p>${it.title} - ${it.author} - ${it.price}</p>
					</g:each>                                          
					<h4>Your Name:</h4>
					<p>${order.person.name}</p>   
					<h4>Delivery Address:</h4>
					<p>House Number: ${order.shippingAddress.number}</p>
					<p>Post Code: ${order.shippingAddress.postCode}</p>
               </div>
        </div>
    </body>
</html>
