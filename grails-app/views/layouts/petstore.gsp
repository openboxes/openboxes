<%@ page import="gps.Item" %>
<html>
    <head>
        <title><g:layoutTitle default="Grails Pet Store" /></title>
        <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'petstore.css')}" />
        <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />				
    </head>
    <body>
	<div id="header">
        <% def cart = applicationContext.shoppingCart %>
        <% if (!cart.isEmpty()) { %>
        <div id="cart">
            <table>
                <caption>
                    <img src="${createLinkTo(dir:"images",file:"cart.png")}" alt=""/>
                    Shopping cart
                </caption>
                <tbody>
                <% cart.itemIds.each { itemId -> %>
                    <tr>
                        <td width="1%">${cart.getItemCount(itemId)}</td>
                        <td><g:link controller="item" action="show" id="${itemId}">${Item.get(itemId)?.name}</g:link></td>
                        <td width="1%">
                            <g:link controller="shoppingCart" action="add" id="${itemId}">
                                <img src="${createLinkTo(dir:"images",file:"add.png")}" alt="+"/>
                            </g:link>
                            <g:link controller="shoppingCart" action="remove" id="${itemId}">
                                <img src="${createLinkTo(dir:"images",file:"delete.png")}" alt="-"/>
                            </g:link>
                        </td>
                    </tr>
                <% } %>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="4">
                            <g:link controller="customerOrder" action="checkout">
                                <img src="${createLinkTo(dir:"images",file:"cart_go.png")}" alt=""/>
                                Checkout
                            </g:link>
                        </td>
                    </tr>
                </tfoot>
            </table>
        </div>
        <% } %>
        <div class="logo"><img src="${createLinkTo(dir:'images',file:'nylogo.png')}" alt="Grails" /></div>
        <div class="nav">
            <div id="searchableForm">
                <g:form url='[controller: "item", action: "search"]' name="searchableForm" method="get">
                    <g:textField name="q" value="${params.q}" size="20"/> <input type="submit" value="Search" />
                </g:form>
            </div>
            <div id="menuButtons">
                <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
                <span class="menuButton"><a class="list" href="${createLink(controller:"item",action:"list")}">Pet Catalog</a></span>
                <span class="menuButton"><a class="create" href="${createLink(controller:"item", action:"create")}">New Pet</a></span>
            </div>
            <div style="clear: both"> </div>
        </div>
	</div><!-- end header -->
        <g:layoutBody />
    </body>
</html>