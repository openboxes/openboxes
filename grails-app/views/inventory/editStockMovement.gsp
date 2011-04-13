
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>    
        <style>
        	optgroup { font-weight: bold; } 
        	#transactionEntryTable { border: 1px solid #ccc; } 
			#transactionEntryTable td { padding: 5px; text-align: center; }
			#transactionEntryTable th { text-align: center; } 
        	#prodSelectRow { padding: 10px; }  
        	#transactionEntryTable td.prodNameCell { text-align: left; } 
        	
        </style>
    </head>    

    <body>
        <div class="body" style="width: 60%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    
            
            
			<div class="nav">
				<g:link action="listAllTransactions">All transactions</g:link> &nbsp;|&nbsp;
				<g:link action="listPendingTransactions">Pending transactions</g:link> &nbsp;|&nbsp; 
				<g:link action="listConfirmedTransactions">Confirmed transactions</g:link> &nbsp;|&nbsp;
				<g:link action="createTransaction">Create transaction</g:link> 				
			</div>
            

			<div class="dialog">
			
				<g:form>
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
				
					<fieldset>
						<legend>Transaction Details</legend>
						<table>
							<tr class="prop">
								<td class="name"><label>Transaction ID</label></td>
								<td class="value">
									<g:if test="${transactionInstance?.id }">
										${transactionInstance?.id }
									</g:if>
									<g:else><span class="fade">(new transaction)</span></g:else>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label>Transaction Date</label>
								</td>
								<td class="value">
									<g:jqueryDatePicker id="transactionDate" name="transactionDate"
											value="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Transaction Type</label></td>
								<td class="value">
									<g:select name="transactionType.id" from="${transactionTypeList}" 
			                       		optionKey="id" optionValue="name" value="${transactionInstance.transactionType?.id}" noSelection="['null': '']" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Source</label></td>
								<td class="value">
									<g:select name="source.id" from="${warehouseInstanceList}" 
			                       		optionKey="id" optionValue="name" value="${transactionInstance?.source?.id}" noSelection="['null': '']" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Destination</label></td>
								<td class="value">
									${warehouseInstance?.name }
									<g:hiddenField name="destination.id" value="${warehouseInstance?.id }"/>
								</td>
							</tr>
						</table>
					</fieldset>
									
					<g:if test="${transactionInstance?.id }">
						
						<fieldset>
							<legend>Transaction Entries</legend>
							<table id="transactionEntryTable">
								<tr>
									<td colspan="2">
										<table id="prodEntryTable" border="1" style="border: 1px solid #ccc;">
											<tr>
												<th>ID</th>
												<th>Product</th>
												<th>Qty</th>
												<th>Lot Number</th>
												<th>Expiration Date</th>
												<th>&nbsp;</th>
											</tr>
											<g:each in="${transactionInstance?.transactionEntries.sort { it.inventoryItem?.product.name } }" var="transactionEntry" status="status">
												<tr class="${(status%2==0)?'even':'odd'}">
													<td>
														${transactionEntry?.id }
													</td>
													<td style="text-align: left;">
														${transactionEntry?.inventoryItem?.product?.name }
													</td>										
													<td>
														${transactionEntry?.quantity}
													</td>		
													<td>
														${transactionEntry?.inventoryItem?.lotNumber }
													</td>		
													<td>
														${transactionEntry?.inventoryItem?.expirationDate }
													</td>
												</tr>
											</g:each>
											<%-- 
											<tr>
												<td>
													<select name="transactionEntries[0].product.id">
														<g:each var="key" in="${productInstanceMap.keySet() }">
															<g:set var="productInstanceList" value="${productInstanceMap.get(key) }"/>
															<optgroup label="${key?.name?:'None'}"></optgroup>
															<g:each var="productInstance" in="${productInstanceList}">
																<option value="${productInstance?.id }">${productInstance?.name }</option>
															</g:each>
														</g:each>
													</select>
												</td>
												<td>
													<input type="text" name="transactionEntries[0].quantity" value="10" size="2" />
												</td>
												<td>
													<input type="text" name="transactionEntries[0].lotNumber" value="" size="10" />
												</td>
												<td>
													<g:datePicker name="transactionEntries[0].expirationDate" precision="month" 
														 years="${2000..2030}" default="none"
														noSelection="['':'-Choose-']"/>
												</td> 
											</tr>
											--%>
											 
											<tr id="prodSelectRow" >
												<td colspan="5" style="text-align: center; padding: 10px;">
													<select id="productSelect">
														<option value="">Choose a product to add</option>
														<g:each var="key" in="${productInstanceMap.keySet() }">
															<g:set var="productInstanceList" value="${productInstanceMap.get(key) }"/>
															<optgroup label="${key?.name?:'None'}"></optgroup>
															<g:each var="productInstance" in="${productInstanceList}">
																<option value="${productInstance?.id }">${productInstance?.name }</option>
															</g:each>
														</g:each>
													</select>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</g:if>
					</fieldset>
					<div class="buttons">
						<g:actionSubmit value="Save" action="saveTransaction" />
						<g:link action="deleteTransaction" id="${transactionInstance?.id }">delete</g:link>
						
					</div>
				</g:form>
				
			</div>
		</div>

					<script type="text/javascript">var monthsYears = '<option value=""></option><option value="2000-01-31">2000 Jan</option>,<option value="2000-02-29">2000 Feb</option>,<option value="2000-03-31">2000 Mar</option>,<option value="2000-04-30">2000 Apr</option>,<option value="2000-05-31">2000 May</option>,<option value="2000-06-30">2000 Jun</option>,<option value="2000-07-31">2000 Jul</option>,<option value="2000-08-31">2000 Aug</option>,<option value="2000-09-30">2000 Sep</option>,<option value="2000-10-31">2000 Oct</option>,<option value="2000-11-30">2000 Nov</option>,<option value="2000-12-31">2000 Dec</option>,<option value="2001-01-31">2001 Jan</option>,<option value="2001-02-28">2001 Feb</option>,<option value="2001-03-31">2001 Mar</option>,<option value="2001-04-30">2001 Apr</option>,<option value="2001-05-31">2001 May</option>,<option value="2001-06-30">2001 Jun</option>,<option value="2001-07-31">2001 Jul</option>,<option value="2001-08-31">2001 Aug</option>,<option value="2001-09-30">2001 Sep</option>,<option value="2001-10-31">2001 Oct</option>,<option value="2001-11-30">2001 Nov</option>,<option value="2001-12-31">2001 Dec</option>,<option value="2002-01-31">2002 Jan</option>,<option value="2002-02-28">2002 Feb</option>,<option value="2002-03-31">2002 Mar</option>,<option value="2002-04-30">2002 Apr</option>,<option value="2002-05-31">2002 May</option>,<option value="2002-06-30">2002 Jun</option>,<option value="2002-07-31">2002 Jul</option>,<option value="2002-08-31">2002 Aug</option>,<option value="2002-09-30">2002 Sep</option>,<option value="2002-10-31">2002 Oct</option>,<option value="2002-11-30">2002 Nov</option>,<option value="2002-12-31">2002 Dec</option>,<option value="2003-01-31">2003 Jan</option>,<option value="2003-02-28">2003 Feb</option>,<option value="2003-03-31">2003 Mar</option>,<option value="2003-04-30">2003 Apr</option>,<option value="2003-05-31">2003 May</option>,<option value="2003-06-30">2003 Jun</option>,<option value="2003-07-31">2003 Jul</option>,<option value="2003-08-31">2003 Aug</option>,<option value="2003-09-30">2003 Sep</option>,<option value="2003-10-31">2003 Oct</option>,<option value="2003-11-30">2003 Nov</option>,<option value="2003-12-31">2003 Dec</option>,<option value="2004-01-31">2004 Jan</option>,<option value="2004-02-29">2004 Feb</option>,<option value="2004-03-31">2004 Mar</option>,<option value="2004-04-30">2004 Apr</option>,<option value="2004-05-31">2004 May</option>,<option value="2004-06-30">2004 Jun</option>,<option value="2004-07-31">2004 Jul</option>,<option value="2004-08-31">2004 Aug</option>,<option value="2004-09-30">2004 Sep</option>,<option value="2004-10-31">2004 Oct</option>,<option value="2004-11-30">2004 Nov</option>,<option value="2004-12-31">2004 Dec</option>,<option value="2005-01-31">2005 Jan</option>,<option value="2005-02-28">2005 Feb</option>,<option value="2005-03-31">2005 Mar</option>,<option value="2005-04-30">2005 Apr</option>,<option value="2005-05-31">2005 May</option>,<option value="2005-06-30">2005 Jun</option>,<option value="2005-07-31">2005 Jul</option>,<option value="2005-08-31">2005 Aug</option>,<option value="2005-09-30">2005 Sep</option>,<option value="2005-10-31">2005 Oct</option>,<option value="2005-11-30">2005 Nov</option>,<option value="2005-12-31">2005 Dec</option>,<option value="2006-01-31">2006 Jan</option>,<option value="2006-02-28">2006 Feb</option>,<option value="2006-03-31">2006 Mar</option>,<option value="2006-04-30">2006 Apr</option>,<option value="2006-05-31">2006 May</option>,<option value="2006-06-30">2006 Jun</option>,<option value="2006-07-31">2006 Jul</option>,<option value="2006-08-31">2006 Aug</option>,<option value="2006-09-30">2006 Sep</option>,<option value="2006-10-31">2006 Oct</option>,<option value="2006-11-30">2006 Nov</option>,<option value="2006-12-31">2006 Dec</option>,<option value="2007-01-31">2007 Jan</option>,<option value="2007-02-28">2007 Feb</option>,<option value="2007-03-31">2007 Mar</option>,<option value="2007-04-30">2007 Apr</option>,<option value="2007-05-31">2007 May</option>,<option value="2007-06-30">2007 Jun</option>,<option value="2007-07-31">2007 Jul</option>,<option value="2007-08-31">2007 Aug</option>,<option value="2007-09-30">2007 Sep</option>,<option value="2007-10-31">2007 Oct</option>,<option value="2007-11-30">2007 Nov</option>,<option value="2007-12-31">2007 Dec</option>,<option value="2008-01-31">2008 Jan</option>,<option value="2008-02-29">2008 Feb</option>,<option value="2008-03-31">2008 Mar</option>,<option value="2008-04-30">2008 Apr</option>,<option value="2008-05-31">2008 May</option>,<option value="2008-06-30">2008 Jun</option>,<option value="2008-07-31">2008 Jul</option>,<option value="2008-08-31">2008 Aug</option>,<option value="2008-09-30">2008 Sep</option>,<option value="2008-10-31">2008 Oct</option>,<option value="2008-11-30">2008 Nov</option>,<option value="2008-12-31">2008 Dec</option>,<option value="2009-01-31">2009 Jan</option>,<option value="2009-02-28">2009 Feb</option>,<option value="2009-03-31">2009 Mar</option>,<option value="2009-04-30">2009 Apr</option>,<option value="2009-05-31">2009 May</option>,<option value="2009-06-30">2009 Jun</option>,<option value="2009-07-31">2009 Jul</option>,<option value="2009-08-31">2009 Aug</option>,<option value="2009-09-30">2009 Sep</option>,<option value="2009-10-31">2009 Oct</option>,<option value="2009-11-30">2009 Nov</option>,<option value="2009-12-31">2009 Dec</option>,<option value="2010-01-31">2010 Jan</option>,<option value="2010-02-28">2010 Feb</option>,<option value="2010-03-31">2010 Mar</option>,<option value="2010-04-30">2010 Apr</option>,<option value="2010-05-31">2010 May</option>,<option value="2010-06-30">2010 Jun</option>,<option value="2010-07-31">2010 Jul</option>,<option value="2010-08-31">2010 Aug</option>,<option value="2010-09-30">2010 Sep</option>,<option value="2010-10-31">2010 Oct</option>,<option value="2010-11-30">2010 Nov</option>,<option value="2010-12-31">2010 Dec</option>,<option value="2011-01-31">2011 Jan</option>,<option value="2011-02-28">2011 Feb</option>,<option value="2011-03-31">2011 Mar</option>,<option value="2011-04-30">2011 Apr</option>,<option value="2011-05-31">2011 May</option>,<option value="2011-06-30">2011 Jun</option>,<option value="2011-07-31">2011 Jul</option>,<option value="2011-08-31">2011 Aug</option>,<option value="2011-09-30">2011 Sep</option>,<option value="2011-10-31">2011 Oct</option>,<option value="2011-11-30">2011 Nov</option>,<option value="2011-12-31">2011 Dec</option>,<option value="2012-01-31">2012 Jan</option>,<option value="2012-02-29">2012 Feb</option>,<option value="2012-03-31">2012 Mar</option>,<option value="2012-04-30">2012 Apr</option>,<option value="2012-05-31">2012 May</option>,<option value="2012-06-30">2012 Jun</option>,<option value="2012-07-31">2012 Jul</option>,<option value="2012-08-31">2012 Aug</option>,<option value="2012-09-30">2012 Sep</option>,<option value="2012-10-31">2012 Oct</option>,<option value="2012-11-30">2012 Nov</option>,<option value="2012-12-31">2012 Dec</option>,<option value="2013-01-31">2013 Jan</option>,<option value="2013-02-28">2013 Feb</option>,<option value="2013-03-31">2013 Mar</option>,<option value="2013-04-30">2013 Apr</option>,<option value="2013-05-31">2013 May</option>,<option value="2013-06-30">2013 Jun</option>,<option value="2013-07-31">2013 Jul</option>,<option value="2013-08-31">2013 Aug</option>,<option value="2013-09-30">2013 Sep</option>,<option value="2013-10-31">2013 Oct</option>,<option value="2013-11-30">2013 Nov</option>,<option value="2013-12-31">2013 Dec</option>,<option value="2014-01-31">2014 Jan</option>,<option value="2014-02-28">2014 Feb</option>,<option value="2014-03-31">2014 Mar</option>,<option value="2014-04-30">2014 Apr</option>,<option value="2014-05-31">2014 May</option>,<option value="2014-06-30">2014 Jun</option>,<option value="2014-07-31">2014 Jul</option>,<option value="2014-08-31">2014 Aug</option>,<option value="2014-09-30">2014 Sep</option>,<option value="2014-10-31">2014 Oct</option>,<option value="2014-11-30">2014 Nov</option>,<option value="2014-12-31">2014 Dec</option>,<option value="2015-01-31">2015 Jan</option>,<option value="2015-02-28">2015 Feb</option>,<option value="2015-03-31">2015 Mar</option>,<option value="2015-04-30">2015 Apr</option>,<option value="2015-05-31">2015 May</option>,<option value="2015-06-30">2015 Jun</option>,<option value="2015-07-31">2015 Jul</option>,<option value="2015-08-31">2015 Aug</option>,<option value="2015-09-30">2015 Sep</option>,<option value="2015-10-31">2015 Oct</option>,<option value="2015-11-30">2015 Nov</option>,<option value="2015-12-31">2015 Dec</option>,<option value="2016-01-31">2016 Jan</option>,<option value="2016-02-29">2016 Feb</option>,<option value="2016-03-31">2016 Mar</option>,<option value="2016-04-30">2016 Apr</option>,<option value="2016-05-31">2016 May</option>,<option value="2016-06-30">2016 Jun</option>,<option value="2016-07-31">2016 Jul</option>,<option value="2016-08-31">2016 Aug</option>,<option value="2016-09-30">2016 Sep</option>,<option value="2016-10-31">2016 Oct</option>,<option value="2016-11-30">2016 Nov</option>,<option value="2016-12-31">2016 Dec</option>,<option value="2017-01-31">2017 Jan</option>,<option value="2017-02-28">2017 Feb</option>,<option value="2017-03-31">2017 Mar</option>,<option value="2017-04-30">2017 Apr</option>,<option value="2017-05-31">2017 May</option>,<option value="2017-06-30">2017 Jun</option>,<option value="2017-07-31">2017 Jul</option>,<option value="2017-08-31">2017 Aug</option>,<option value="2017-09-30">2017 Sep</option>,<option value="2017-10-31">2017 Oct</option>,<option value="2017-11-30">2017 Nov</option>,<option value="2017-12-31">2017 Dec</option>,<option value="2018-01-31">2018 Jan</option>,<option value="2018-02-28">2018 Feb</option>,<option value="2018-03-31">2018 Mar</option>,<option value="2018-04-30">2018 Apr</option>,<option value="2018-05-31">2018 May</option>,<option value="2018-06-30">2018 Jun</option>,<option value="2018-07-31">2018 Jul</option>,<option value="2018-08-31">2018 Aug</option>,<option value="2018-09-30">2018 Sep</option>,<option value="2018-10-31">2018 Oct</option>,<option value="2018-11-30">2018 Nov</option>,<option value="2018-12-31">2018 Dec</option>,<option value="2019-01-31">2019 Jan</option>,<option value="2019-02-28">2019 Feb</option>,<option value="2019-03-31">2019 Mar</option>,<option value="2019-04-30">2019 Apr</option>,<option value="2019-05-31">2019 May</option>,<option value="2019-06-30">2019 Jun</option>,<option value="2019-07-31">2019 Jul</option>,<option value="2019-08-31">2019 Aug</option>,<option value="2019-09-30">2019 Sep</option>,<option value="2019-10-31">2019 Oct</option>,<option value="2019-11-30">2019 Nov</option>,<option value="2019-12-31">2019 Dec</option>,<option value="2020-01-31">2020 Jan</option>,<option value="2020-02-29">2020 Feb</option>,<option value="2020-03-31">2020 Mar</option>,<option value="2020-04-30">2020 Apr</option>,<option value="2020-05-31">2020 May</option>,<option value="2020-06-30">2020 Jun</option>,<option value="2020-07-31">2020 Jul</option>,<option value="2020-08-31">2020 Aug</option>,<option value="2020-09-30">2020 Sep</option>,<option value="2020-10-31">2020 Oct</option>,<option value="2020-11-30">2020 Nov</option>,<option value="2020-12-31">2020 Dec</option>,<option value="2021-01-31">2021 Jan</option>,<option value="2021-02-28">2021 Feb</option>,<option value="2021-03-31">2021 Mar</option>,<option value="2021-04-30">2021 Apr</option>,<option value="2021-05-31">2021 May</option>,<option value="2021-06-30">2021 Jun</option>,<option value="2021-07-31">2021 Jul</option>,<option value="2021-08-31">2021 Aug</option>,<option value="2021-09-30">2021 Sep</option>,<option value="2021-10-31">2021 Oct</option>,<option value="2021-11-30">2021 Nov</option>,<option value="2021-12-31">2021 Dec</option>,<option value="2022-01-31">2022 Jan</option>,<option value="2022-02-28">2022 Feb</option>,<option value="2022-03-31">2022 Mar</option>,<option value="2022-04-30">2022 Apr</option>,<option value="2022-05-31">2022 May</option>,<option value="2022-06-30">2022 Jun</option>,<option value="2022-07-31">2022 Jul</option>,<option value="2022-08-31">2022 Aug</option>,<option value="2022-09-30">2022 Sep</option>,<option value="2022-10-31">2022 Oct</option>,<option value="2022-11-30">2022 Nov</option>,<option value="2022-12-31">2022 Dec</option>,<option value="2023-01-31">2023 Jan</option>,<option value="2023-02-28">2023 Feb</option>,<option value="2023-03-31">2023 Mar</option>,<option value="2023-04-30">2023 Apr</option>,<option value="2023-05-31">2023 May</option>,<option value="2023-06-30">2023 Jun</option>,<option value="2023-07-31">2023 Jul</option>,<option value="2023-08-31">2023 Aug</option>,<option value="2023-09-30">2023 Sep</option>,<option value="2023-10-31">2023 Oct</option>,<option value="2023-11-30">2023 Nov</option>,<option value="2023-12-31">2023 Dec</option>,<option value="2024-01-31">2024 Jan</option>,<option value="2024-02-29">2024 Feb</option>,<option value="2024-03-31">2024 Mar</option>,<option value="2024-04-30">2024 Apr</option>,<option value="2024-05-31">2024 May</option>,<option value="2024-06-30">2024 Jun</option>,<option value="2024-07-31">2024 Jul</option>,<option value="2024-08-31">2024 Aug</option>,<option value="2024-09-30">2024 Sep</option>,<option value="2024-10-31">2024 Oct</option>,<option value="2024-11-30">2024 Nov</option>,<option value="2024-12-31">2024 Dec</option>,<option value="2025-01-31">2025 Jan</option>,<option value="2025-02-28">2025 Feb</option>,<option value="2025-03-31">2025 Mar</option>,<option value="2025-04-30">2025 Apr</option>,<option value="2025-05-31">2025 May</option>,<option value="2025-06-30">2025 Jun</option>,<option value="2025-07-31">2025 Jul</option>,<option value="2025-08-31">2025 Aug</option>,<option value="2025-09-30">2025 Sep</option>,<option value="2025-10-31">2025 Oct</option>,<option value="2025-11-30">2025 Nov</option>,<option value="2025-12-31">2025 Dec</option>,<option value="2026-01-31">2026 Jan</option>,<option value="2026-02-28">2026 Feb</option>,<option value="2026-03-31">2026 Mar</option>,<option value="2026-04-30">2026 Apr</option>,<option value="2026-05-31">2026 May</option>,<option value="2026-06-30">2026 Jun</option>,<option value="2026-07-31">2026 Jul</option>,<option value="2026-08-31">2026 Aug</option>,<option value="2026-09-30">2026 Sep</option>,<option value="2026-10-31">2026 Oct</option>,<option value="2026-11-30">2026 Nov</option>,<option value="2026-12-31">2026 Dec</option>,<option value="2027-01-31">2027 Jan</option>,<option value="2027-02-28">2027 Feb</option>,<option value="2027-03-31">2027 Mar</option>,<option value="2027-04-30">2027 Apr</option>,<option value="2027-05-31">2027 May</option>,<option value="2027-06-30">2027 Jun</option>,<option value="2027-07-31">2027 Jul</option>,<option value="2027-08-31">2027 Aug</option>,<option value="2027-09-30">2027 Sep</option>,<option value="2027-10-31">2027 Oct</option>,<option value="2027-11-30">2027 Nov</option>,<option value="2027-12-31">2027 Dec</option>,<option value="2028-01-31">2028 Jan</option>,<option value="2028-02-29">2028 Feb</option>,<option value="2028-03-31">2028 Mar</option>,<option value="2028-04-30">2028 Apr</option>,<option value="2028-05-31">2028 May</option>,<option value="2028-06-30">2028 Jun</option>,<option value="2028-07-31">2028 Jul</option>,<option value="2028-08-31">2028 Aug</option>,<option value="2028-09-30">2028 Sep</option>,<option value="2028-10-31">2028 Oct</option>,<option value="2028-11-30">2028 Nov</option>,<option value="2028-12-31">2028 Dec</option>,<option value="2029-01-31">2029 Jan</option>,<option value="2029-02-28">2029 Feb</option>,<option value="2029-03-31">2029 Mar</option>,<option value="2029-04-30">2029 Apr</option>,<option value="2029-05-31">2029 May</option>,<option value="2029-06-30">2029 Jun</option>,<option value="2029-07-31">2029 Jul</option>,<option value="2029-08-31">2029 Aug</option>,<option value="2029-09-30">2029 Sep</option>,<option value="2029-10-31">2029 Oct</option>,<option value="2029-11-30">2029 Nov</option>,<option value="2029-12-31">2029 Dec</option>,<option value="2030-01-31">2030 Jan</option>,<option value="2030-02-28">2030 Feb</option>,<option value="2030-03-31">2030 Mar</option>,<option value="2030-04-30">2030 Apr</option>,<option value="2030-05-31">2030 May</option>,<option value="2030-06-30">2030 Jun</option>,<option value="2030-07-31">2030 Jul</option>,<option value="2030-08-31">2030 Aug</option>,<option value="2030-09-30">2030 Sep</option>,<option value="2030-10-31">2030 Oct</option>';</script>
					<script type="text/javascript">
						var numberOfRows = '${(transactionInstance?.transactionEntries)?transactionInstance?.transactionEntries.size():0}';
						var lotsForProducts = new Array();
						var existingProdLots = null;
						
						$(document).ready(function() {
							$("#productSelect").change(function() {
								addRowForProduct($(this).val());
								$(this).val('');
							});
							
						});

						function updateRowSpan(prodId, num) {
							var productLinkCell = $('#addProductCell' + prodId);
							var productNameCell = $('#prodNameCell' + prodId);
							
							var currSpan = productNameCell.attr('rowspan');
							productLinkCell.attr('rowspan', currSpan + num);
							productNameCell.attr('rowspan', currSpan + num);
							
						}
					
						function removeRow(prodId, lotIndex) {
							numberOfRows--;
							$('#prodRow' + prodId + lotIndex).remove();
							updateRowSpan(prodId, -1);
						}
					
						function selectProduct(prodId) {
							$("#productSelect").val(prodId).change();
						}
					
						
						
						function addRowForProduct(prodId) {
							var prodName = $("#productSelect option:selected").text();
							var lotIndex = lotsForProducts[prodId];
							if (lotIndex == null) {
								lotIndex = 1;
							}
							else {
								lotIndex = lotIndex + 1;
							}
							lotsForProducts[prodId] = lotIndex;
					
					
							var productField = $('<input>')
								.attr('type', 'hidden')
								.attr('value', prodId)								
								.attr('name', 'transactionEntries[' + numberOfRows + '].product.id');
								
							var $quantityField = $('<input>')
								.attr('type', 'text')
								.attr('size','3')
								.attr('name','transactionEntries[' + numberOfRows + '].quantity');
								
							var $lotNumberField = $('<input>')
								.attr('type', 'text')
								.attr('size','10')
								.attr('id', 'transactionEntries[' + numberOfRows + '].lotNumber')
								.attr('name','transactionEntries[' + numberOfRows + '].lotNumber');
								
							var $expiryField = $('<select name="transactionEntries[' + numberOfRows + '].expirationDate">' + monthsYears + '</select>');
							
							$newRow = $('<tr id="prodRow' + prodId + lotIndex + '" class="prodRow' + prodId + '">');
							
							
							
							if (lotIndex == 1) {
								var addIcon = $('<img>').attr('src', '/warehouse/images/icons/silk/add.png');
								
								$productNameCell = $('<td id="prodNameCell' + prodId + '" class="prodNameCell">')
									.attr('valign','top').attr('rowspan','1')																
									.html(prodName);								
							
								var addProductLink = $('<a href="javascript:selectProduct(' + prodId + ');">').html(addIcon);								
								$addProductCell = $('<td id="addProductCell' + prodId + '" class="addProductCell">')
									.attr('valign','top')
									.attr('rowspan','1')									
									.html(addProductLink);
								
								$newRow.append($addProductCell);
								$newRow.append($productNameCell);
							}
							updateRowSpan(prodId, 1);
							
							
							var removeIcon = $('<img>').attr('src', '/warehouse/images/icons/silk/cross.png');
							
							$newRow.append( $('<td>').append($quantityField).append(productField) );
							$newRow.append( $('<td>').append($lotNumberField) );
							$newRow.append( $('<td>').append($expiryField) );
							
							//$newRow.append($('<td>').text(''));
							
					
							if (lotIndex > 1) {
								$newRow.append(
								$('<td>').append(
									$('<a href="javascript:removeRow(' + prodId + ', ' + lotIndex + ');">').html(removeIcon)));									
							}
							else {
								$newRow.append($('<td>').text(''));
							}
							
							if (lotIndex == 1) {
								$('#prodSelectRow').before($newRow);
							}
							else {
								$('.prodRow' + prodId).last().after($newRow);
							}
							
							if (existingProdLots != null && existingProdLots[prodId].length > 0) {
								$textField.autocomplete(existingProdLots[prodId],{
									minChars: 0,
									width: 600,
									scroll: true,
									matchContains: true,
									autoFill: false,
									max: 1000,
									formatItem: function(row, i, max) { return row.label; },
									formatMatch: function(row, i, max) { return row.label; },
									formatResult: function(row) { return row.label; }
								});
								$lotNumberField.blur(function() {
									$lotNumberField.removeAttr('autocomplete');
								});
								$lotNumberField.focus(function() {
									$lotNumberField.attr('autocomplete', 'off');
								});
								$lotNumberField.result(function(event, data, formatted) {
									$expiryField.val(lotExpOptions[data.code]);
								});
							}		
							numberOfRows++;
							
						}
						
						
						
						
					</script>


    </body>
</html>
