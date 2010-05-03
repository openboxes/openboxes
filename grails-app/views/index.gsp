<html>
    <head>
        <title>Welcome to Grails</title>
		<meta name="layout" content="main" />
		<style type="text/css" media="screen">

			#nav {
				margin-top:20px;
				margin-left:30px;
				width:228px;
				float:left;

			}
			.homePagePanel * {
				margin:0px;
			}
			.homePagePanel .panelBody ul {
				list-style-type:none;
				margin-bottom:10px;
			}
			.homePagePanel .panelBody h1 {
				text-transform:uppercase;
				font-size:1.1em;
				margin-bottom:10px;
			}
			.homePagePanel .panelBody {
			    background: url(images/leftnav_midstretch.png) repeat-y top;
				margin:0px;
				padding:15px;
			}
			.homePagePanel .panelBtm {
			    background: url(images/leftnav_btm.png) no-repeat top;
				height:20px;
				margin:0px;
			}

			.homePagePanel .panelTop {
			    background: url(images/leftnav_top.png) no-repeat top;
				height:11px;
				margin:0px;
			}
			h2 {
				margin-top:15px;
				margin-bottom:15px;
				font-size:1.2em;
			}
			#pageBody {
				margin-left:280px;
				margin-right:20px;
			}
		</style>
    </head>
    <body>

        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
        </div>

		<div id="nav">
			<div class="homePagePanel">
				<div class="panelTop"></div>
				<div class="panelBody">
					<h1>Actions</h1>
					<ul>
					  <li><a href="warehouse/list">Show All Warehouses</a></li>
					  <li><a href="product/list">Show All Products</a></li>
					  <li><a href="user/list">Show All Users</a></li>
					</ul>
					<h1>Controllers</h1>
					<ul>
					  <g:each var="c" in="${grailsApplication.controllerClasses}">
						<li class="controller"><g:link controller="${c.logicalPropertyName}">${c.name}</g:link></li>
					  </g:each>
					</ul>

					<!--
					<h1>Application Status</h1>
					<ul>
						<li>App version: <g:meta name="app.version"></g:meta></li>
						<li>Grails version: <g:meta name="app.grails.version"></g:meta></li>
						<li>JVM version: ${System.getProperty('java.version')}</li>
						<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
						<li>Domains: ${grailsApplication.domainClasses.size()}</li>
						<li>Services: ${grailsApplication.serviceClasses.size()}</li>
						<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
					</ul>
					<h1>Installed Plugins</h1>
					<ul>
						<g:set var="pluginManager"
						       value="${applicationContext.getBean('pluginManager')}"></g:set>

						<g:each var="plugin" in="${pluginManager.allPlugins}">
							<li>${plugin.name} - ${plugin.version}</li>
						</g:each>

					</ul>
					-->
				</div>
				<div class="panelBtm">
					
				</div>
			</div>


		</div>
		<div id="pageBody">
		  <h1>Welcome to the <strong>PIH&copy;</strong> Warehouse Inventory Tracking System</h1>
		  <p>Some blurb about the system ...</p>
		  
		  
		  
		</div>
    </body>
</html>