<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	<h2>${warehouse.message(code: 'email.errorReportSubject.message')}</h2>	
	<table>	
		<g:each var="entry" in="${params }">
			<tr>			
				<td>
					<g:if test="${entry.key != 'dom' }">				
						<b>${entry.key }</b>
						<div>
							${entry.value ?: warehouse.message(code: 'default.none.label') }
						</div>
					</g:if>
				</td>
			</tr>
		</g:each>
	</table>		
</g:applyLayout>
