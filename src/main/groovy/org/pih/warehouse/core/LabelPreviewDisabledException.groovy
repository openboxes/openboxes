package org.pih.warehouse.core

/**
 * Label previews through Labelary are turned off on this server
 * (openboxes.barcode.labelaryApi.enabled is false), so no label is sent to it.
 */
class LabelPreviewDisabledException extends RuntimeException {

    LabelPreviewDisabledException() {
        super("Label previews are turned off on this server (openboxes.barcode.labelaryApi.enabled is false).")
    }
}
