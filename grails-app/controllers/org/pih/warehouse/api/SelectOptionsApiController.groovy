/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.api

import grails.converters.JSON

class SelectOptionsApiController {

    GenericApiService genericApiService;

    def glAccountOptions = {
        def glAccounts = genericApiService.getGlAccountsOptions()
        render([data: glAccounts] as JSON)
    }

    def tagOptions = {
        def tags = genericApiService.getTagsOptions()
        render([data: tags] as JSON)
    }

    def catalogOptions = {
        def catalogs = genericApiService.getCatalogsOptions()
        render([data: catalogs] as JSON)
    }

    def categoryOptions = {
        def categories = genericApiService.getCategoryOptions()
        render([data: categories] as JSON)
    }
}
