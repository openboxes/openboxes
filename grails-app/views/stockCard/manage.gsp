
<%@ page import="org.pih.warehouse.StockCard" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'stockCard.label', default: 'Manage Stock Card Entries')}" />
        <title><g:message code="default.manage.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
	    <!--
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>

	    -->
        </div>
        <div class="body">
	    <h1>
	      <g:message code="stockCard.product.label" default="Product" />
	      <g:link controller="product" action="show" id="${stockCardInstance?.product?.id}">${stockCardInstance?.product?.encodeAsHTML()}</g:link>
	      (${stockCardInstance?.product?.upc})
	    </h1>
	    <h2>
	      <g:message code="product.description.label" default="Description" />
	      ${stockCardInstance?.product?.description}.
	    </h2>
            <g:if test="${flash.message}">
              <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
		  <thead>
		    <tr>
		      <th>ID</th>
		      <th>Date</th>
		      <th>Starting balance</th>
		      <th>IN</th>
		      <th>OUT</th>
		      <th>Ending balance</th>
		      <th>Save</th>
		    </tr>
		  </thead>
                    <tbody>
		      <g:each in="${stockCardInstance.entries}" var="stockCardEntry" status="i">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			  <td>${fieldValue(bean: stockCardEntry, field: "id")}</td>
                          <td>${fieldValue(bean: stockCardEntry, field: "entryDate")}</td>                            
                          <td>${fieldValue(bean: stockCardEntry, field: "startingBalance")}</td>
                          <td>${fieldValue(bean: stockCardEntry, field: "quantityIncoming")}</td>
                          <td>${fieldValue(bean: stockCardEntry, field: "quantityOutgoing")}</td>
                          <td>${fieldValue(bean: stockCardEntry, field: "remainingBalance")}</td>
                          <td>			    
			  </td>
                        </tr>
		      </g:each>

		      <g:hasErrors bean="${stockCardEntryInstance}">
			<tr>
			  <td colspan="7">
			    <div class="errors">
				<g:renderErrors bean="${stockCardEntryInstance}" as="list" />
			    </div>
			  </td>
			</tr>
		      </g:hasErrors>


		      <tr>
			<g:form controller="stockCard" action="saveEntry" method="post" >
			  <g:hiddenField name="stockCard.id" value="${stockCardInstance?.id}" />

			  <td>
			    ${stockCardEntryInstance?.id}
			  </td>
			    <td class="value ${hasErrors(bean: stockCardEntryInstance, field: 'entryDate', 'errors')}">
			      <g:datePicker name="entryDate" value="${stockCardEntryInstance?.entryDate}" precision="day" noSelection="['':'-Choose-']"/>
			    </td>
			    <td class="value ${hasErrors(bean: stockCardEntryInstance, field: 'startingBalance', 'errors')}">
			      <g:textField name="startingBalance" value="${stockCardEntryInstance?.startingBalance}" size="5" />
			    </td>
			    <td class="value ${hasErrors(bean: stockCardEntryInstance, field: 'quantityIncoming', 'errors')}">
			      <g:textField name="quantityIncoming" value="${stockCardEntryInstance?.quantityIncoming}" size="5" />
			    </td>
			    <td class="value ${hasErrors(bean: stockCardEntryInstance, field: 'quantityOutgoing', 'errors')}">
			      <g:textField name="quantityOutgoing" value="${stockCardEntryInstance?.quantityOutgoing}" size="5" />
			    </td>
			    <td class="value ${hasErrors(bean: stockCardEntryInstance, field: 'remainingBalance', 'errors')}">
			      <g:textField name="remainingBalance" value="${stockCardEntryInstance?.remainingBalance}" size="5" />
			    </td>
                            <td>
			      <div class="buttons">
				<span class="button">
				  <g:submitButton name="save" class="save" value="${message(code: 'default.button.save.label', default: 'Save')}" />
				</span>
			      </div>
			    </td>

			   </g:form>



			</tr>
			<g:each var="i" in="${ (0..<10) }">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			      <td></td>
			      <td>__________________________</td>
			      <td>_______</td>
			      <td>_______</td>
			      <td>_______</td>
			      <td>_______</td>
			      <td></td>
			    </tr>
			</g:each>



                      </tbody>
                </table>
          </div>
	</div>

    </body>
</html>
