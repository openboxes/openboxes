
<style>
.menubar {
	width: 100%;
}

.menubar ul {
	margin: 0;
	padding: 0;
	float: left;
}

.menubar ul li {
	display: inline;
}

.menubar ul li a {
	float: left;
	text-decoration: none;
	padding: 10.5px 11px;
	color: #333;
}

.menubar ul li
a:visited {
	color: #333;
}

.menubar ul li a:hover,.menubar ul li .current {
	color: #0b75b2;
}
</style>

<div class="menubar">
	<ul>
		<li><g:link controller="test" action="jQuery">jQuery</g:link></li>
		<li><g:link controller="test" action="jQueryAutocomplete">jQuery UI autocomplete</g:link></li>
		<li><g:link controller="test" action="jQueryAutoSuggestTag">jQuery UI autosuggest</g:link></li>
		<li><g:link controller="test" action="jQueryDatepicker">jQuery datepicker</g:link></li>
		<li><g:link controller="test" action="jqueryTabs">jQuery tabs</g:link></li>
		<li><g:link controller="test" action="sendEmail">email</g:link></li>
	</ul>
</div>
<br clear="all"/>



