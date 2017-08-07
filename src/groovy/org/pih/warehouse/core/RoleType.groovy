/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

enum RoleType {

	ROLE_SUPERUSER('Superuser', 0),
    ROLE_ADMIN('Admin', 1),
	ROLE_MANAGER('Manager', 2),
    ROLE_ASSISTANT('Assistant', 3),
	ROLE_BROWSER('Browser', 4)
 
	String name
    Integer sortOrder


    RoleType(String name, Integer sortOrder) {
		this.name = name
        this.sortOrder = sortOrder
	}

    static expand(Collection roleTypes) {
        Set<RoleType> expandedRoleTypes = new HashSet<RoleType>()
        roleTypes.each { RoleType roleType ->
            expandedRoleTypes.addAll(expand(roleType))
        }
        return expandedRoleTypes
    }

	static expand(RoleType roleType) {
		return list().findAll { it.sortOrder <= roleType.sortOrder }
	}

	static list() {
		[ROLE_BROWSER, ROLE_ASSISTANT, ROLE_MANAGER,  ROLE_ADMIN, ROLE_SUPERUSER]
	}
}
