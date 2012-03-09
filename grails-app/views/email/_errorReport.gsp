<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	<h2>${warehouse.message(code: 'email.errorReportSubject.message')}</h2>	
	<table>	
		<g:each var="entry" in="${params }">
			<tr>			
				<td style="vertical-align: top;">
					${entry.key }				
				</td>
				<td style="vertical-align: top;">
					<g:if test="${entry.key != 'dom' }">				
						${entry.value }
					</g:if>
				</td>
			</tr>
		</g:each>
	</table>		
</g:applyLayout>
