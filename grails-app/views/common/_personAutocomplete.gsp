<div>
	<input id="carrier-suggest" type="text" value="${shipmentInstance?.carrier?.firstName} ${shipmentInstance?.carrier?.lastName}">
	<img id="carrier-icon" src="${request.contextPath}/images/icons/search.png" style="vertical-align: middle;"/>
	<input id="carrier-id" name="carrier.id" type="hidden" value="${shipmentInstance?.carrier?.id}"/>
	<span id="carrier-name"></span>
</div>
<script>
	$(document).ready(function() {
		$('#carrier-suggest').focus();
		$("#carrier-name").click(function() {
			$('#carrier-suggest').val("");
			$('#carrier-name').hide();
			$('#carrier-suggest').show();
			$('#carrier-suggest').focus();
			$('#carrier-suggest').select();
		});
		  $("#carrier-suggest").autocomplete({
			width: 400,
			minLength: 2,
			dataType: 'json',
			highlight: true,
			selectFirst: true,
			scroll: true,
			autoFill: true,
			//define callback to format results
			source: function(req, add){
				//pass request to server
				$.getJSON("${request.contextPath}/test/searchByName", req, function(data) {
					var people = [];
					$.each(data, function(i, item){
						people.push(item);
					});
					add(people);
				});
			  },
			focus: function(event, ui) {
				  $('#carrier-suggest').val(ui.item.label);
				  return false;
			},
			select: function(event, ui) {
				$('#carrier-suggest').val(ui.item.label);
				$('#carrier-name').html(ui.item.label);
				$('#carrier-id').val(ui.item.value);
				$('#carrier-icon').attr('src', '${request.contextPath}/images/icons/silk/user.png');
				$('#carrier-suggest').hide();
				$('#carrier-name').show();
				return false;
			}
		});
	});
</script>
