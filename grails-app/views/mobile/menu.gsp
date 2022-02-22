<header class="p-3 mb-3 border-bottom bg-light">
    <div class="container">
        <div class="d-flex flex-wrap align-items-center justify-content-center justify-content-lg-start">
            <g:link controller="mobile"
                    class="d-flex align-items-center my-2 my-lg-0 me-lg-auto text-decoration-none">
                <g:if test="${session.warehouse}">
                    <g:displayLogo location="${session?.warehouse?.id}" includeLink="${false}"/>
                </g:if>
                <g:else>
                    <a class="navbar-brand" href="#">
                        <img src="https://openboxes.com/img/logo_30.png"/>
                    </a>
                </g:else>
            </g:link>
            <ul class="nav col-12 col-lg-auto my-2 justify-content-center my-md-0 text-small">
                <li class="nav-item">
                    <g:link controller="mobile" href="#" class="nav-link text-black">
                        <i class="fa fa-tachometer-alt d-block mx-auto mb-1"></i>
                        Dashboard
                    </g:link>
                </li>
                <li class="nav-item">
                    <g:link controller="mobile" action="inboundList" href="#"
                            class="nav-link text-black">
                        <i class="fa fa-dolly d-block mx-auto mb-1"></i>
                        Inbound
                    </g:link>
                </li>
                <li>
                    <g:link controller="mobile" action="productList" href="#"
                            class="nav-link text-black">
                        <i class="fa fa-box d-block mx-auto mb-1"></i>
                        Inventory
                    </g:link>
                </li>
                <li class="nav-item">
                    <g:link controller="mobile" action="outboundList" href="#"
                            class="nav-link text-black">
                        <i class="fa fa-truck-loading d-block mx-auto mb-1"></i>
                        Outbound
                    </g:link>
                </li>
                <g:isSuperuser>
                    <li class="nav-item">
                        <g:link controller="mobile" action="messageList" href="#"
                                class="nav-link text-black">
                            <i class="fa fa-envelope d-block mx-auto mb-1"></i>
                            Messages
                        </g:link>
                    </li>
                </g:isSuperuser>
                <li class="nav-item">
                    <div class="dropdown text-end">
                        <a href="#" class="d-block link-dark text-decoration-none dropdown-toggle"
                           id="dropdownUser1" data-bs-toggle="dropdown" aria-expanded="false">
                            <g:if test="${session.user?.id && session?.user?.photo}">
                                <img src="${createLink(controller: 'user', action: 'viewThumb', id: session?.user?.id)}"
                                     width="48" height="48" class="rounded-circle"/>
                            </g:if>
                            <g:else>
                                <img src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg')}"
                                     width="48" height="48" class="rounded-circle"/>
                            </g:else>
                            ${session?.user?.name}
                        </a>
                        <ul class="dropdown-menu text-small" aria-labelledby="dropdownUser1">
                            <g:isSuperuser>
                                <li><g:link controller="dashboard" action="index"
                                            class="dropdown-item" href="#">Manage Warehouse</g:link></li>
                                <li><g:link controller="user" action="edit" id="${session?.user?.id}"
                                            class="dropdown-item" href="#">Edit Profile</g:link></li>
                                <li><hr class="dropdown-divider"></li>
                            </g:isSuperuser>
                            <li><g:link controller="auth" action="logout" class="dropdown-item"
                                        href="#">Sign out</g:link></li>
                        </ul>
                    </div>

                </li>
            </ul>
        </div>
    </div>
</header>
