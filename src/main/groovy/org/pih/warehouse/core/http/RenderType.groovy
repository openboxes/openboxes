package org.pih.warehouse.core.http

/**
 * Enumerates the different server-side HTML rendering options that we support.
 */
enum RenderType {
    /**
     * A reusable UI sub-component that is embedded into a part of a View, a PDF, or used for AJAX calls.
     *
     * In Thymeleaf, this is equivalent to a fragment
     */
    TEMPLATE,

    /**
     * A full UI page.
     */
    VIEW,
}
