package org.pih.warehouse

import java.text.SimpleDateFormat;
import java.util.Date;

class TableTagLib {
   	
	def displayTable = { attrs, body ->
	
		out << """
			<table border="1">
			
				<thead>
					<tr>
						<td></td>
					</tr>
				</thead>
				
				<tbody>
					<tr>
						<td></td>
					</tr>
				</tbody>
			</table>
		
		"""
			
	}
		
}
