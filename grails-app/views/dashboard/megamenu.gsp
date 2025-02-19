<div id="navbarSupportedContent" class="menu-wrapper flex-grow-1">
    <ul class="d-flex align-items-center navbar-nav mr-auto flex-wrap align-items-stretch">
        <g:set var="breakPoint" value="md" />
        <g:hiddenField id="menuSectionUrlParts" name="menuSectionUrlParts" value="${menuSectionsUrlParts}"/>
        <g:each var="menuItem" in="${menu}">
            <g:if test="${menuItem?.href}">
                <li id="${menuItem?.id}" class="nav-item dropdown align-items-center d-flex">
                    <a href="${menuItem?.href}" class="nav-link flex-grow-1" role="menuitem" >
                        ${menuItem?.label}
                    </a>
                </li>
            </g:if>
            <g:else>
                %{-- DROPDOWN NAV-ITEM ( show > break point ) browser view --}%
                <li id="${menuItem?.id}" class="nav-item dropdown align-items-center d-none d-${breakPoint}-flex">
                    <a class="nav-link dropdown-toggle" role="menuitem">
                        ${menuItem?.label}
                    </a>
                    <div class="dropdown-menu d-block dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <g:each in="${menuItem?.subsections}" var="subsection">
                                <div class="padding-8">
                                    <g:if test="${subsection?.label}">
                                        <span class="subsection-title">${subsection?.label}</span>
                                    </g:if>
                                    <g:each in="${subsection?.menuItems}" var="item">
                                        <a href="${item?.href}" class="dropdown-item" role="menuitem">
                                            ${item?.label}
                                        </a>
                                    </g:each>
                                </div>
                            </g:each>
                            <div class="padding-8">
                                <g:each in="${menuItem?.menuItems}" var="item">
                                    <a href="${item?.href}" class="dropdown-item" role="menuitem" >
                                        ${item?.label}
                                    </a>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </li>
                %{-- COLLAPSABLE NAV-ITEM ( show < break point ) mobile view --}%
                <li id="${menuItem?.id}-collapsed" class="nav-item collapsable align-items-center flex-column d-flex d-${breakPoint}-none">
                    <button
                            data-target="#collapse-${menuItem?.id}"
                            aria-controls="collapse-${menuItem?.id}"
                            class="nav-link d-flex justify-content-between align-items-center w-100"
                            data-toggle="collapse"
                            aria-expanded="false"
                    >
                        ${menuItem?.label}
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-${menuItem?.id}">
                        <div class="d-flex flex-wrap">
                            <g:each in="${menuItem?.subsections}" var="subsection">
                                <div class="padding-8">
                                    <g:if test="${subsection?.label}">
                                        <span class="subsection-title">${subsection?.label}</span>
                                    </g:if>
                                    <g:each in="${subsection?.menuItems}" var="item">
                                        <a href="${item?.href}" class="dropdown-item" role="menuitem" >
                                            ${item?.label}
                                        </a>
                                    </g:each>
                                </div>
                            </g:each>
                            <div class="padding-8">
                                <g:each in="${menuItem?.menuItems}" var="item">
                                    <a href="${item?.href}" class="dropdown-item" role="menuitem">
                                        ${item?.label}
                                    </a>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </li>
            </g:else>
          </g:each>
        <g:each var="configItem" in="${megamenuConfig}">
            <g:hiddenField
                id="${configItem.key}-config"
                class="menu-config-value"
                name="${configItem.key}"
                value="${configItem.value}"
            />
        </g:each>
    </ul>
</div>
<div class="d-flex align-items-center justify-content-end navbar-icons">
    <g:render
        template="/common/menuicons"
        model="[
            confMenu    : configurationSection,
            breakPoint  : breakPoint,
        ]"
    />
</div>

<script type="text/javascript">
  $(document).ready(function () {
    const path = window.location.pathname
    const menuConfigValues = $(".menu-config-value").toArray();
    // Menu can be empty for locations without manage inventory and with submit request
    // in this case we don't want to search for active menu section
    if (menuConfigValues?.length) {
      const urlPartsWithSection = $("#menuSectionUrlParts").val();
      const parsedUrlPartsWithSection = JSON.parse(urlPartsWithSection);

      const matchSection = () => {
        // match the whole url
        const exactMatch = menuConfigValues.find(it => it.value.includes(path))

        if (exactMatch) {
          return exactMatch;
        }

        // check if current section exists in object with wrongly underlined urls
        const sectionFromJson = Object.keys(parsedUrlPartsWithSection).find(sectionName => {
          return !!parsedUrlPartsWithSection[sectionName].some(section => path.includes(section));
        })

        if (sectionFromJson) {
          return menuConfigValues.find(it => it.name === sectionFromJson);
        }

        return null;
      };

      const matchingSection = matchSection();

      applyActiveSection(matchingSection?.name);
    }

    function repositionNavDropdowns() {
      const dropdownRightClass = "dropdown-menu-right";
      const itemDropdowns = $(".navbar-nav .dropdown-menu").toArray();
      itemDropdowns.forEach(dropdown => {
        const rect = dropdown.getBoundingClientRect();
        if ([...dropdown.classList].includes(dropdownRightClass) && window.innerWidth > rect.right + rect.width) {
          dropdown.classList.remove(dropdownRightClass)
        }
        if (window.innerWidth < rect.right) {
          dropdown.classList.add(dropdownRightClass);
        }
      })
    }
    repositionNavDropdowns();
    addEventListener("resize", repositionNavDropdowns);
  });
</script>
