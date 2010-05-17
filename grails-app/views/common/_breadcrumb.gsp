<!-- Add breadcrumb -->
<span class="menuButton">

<a class="home" href="${createLink(uri: '/home/index')}">Home</a>
&raquo;
<a class="none" href="${createLink(uri: '/warehouse/show/' + session.warehouse?.id)}">${session.warehouse?.name}</a>
&raquo;
</span>
