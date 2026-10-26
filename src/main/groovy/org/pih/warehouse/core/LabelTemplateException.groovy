package org.pih.warehouse.core

/**
 * A stored label (Zebra) template that cannot be used to render a label: it is not a Zebra
 * template, or BeanPropertyTemplateService refuses it (unsupported syntax, a value that is not
 * there). The message names the template and is safe to show in a page or a log line.
 */
class LabelTemplateException extends RuntimeException {

    LabelTemplateException(Document document, Throwable cause) {
        super(describe(document, cause?.message), cause)
    }

    LabelTemplateException(Document document, String reason) {
        super(describe(document, reason))
    }

    private static String describe(Document document, String reason) {
        String label = "Label template '${clean(document?.name)}'" + (document?.documentNumber ? " (${clean(document.documentNumber)})" : "")
        return "${label} can't be rendered: ${clean(reason)}. Update the template and upload it again."
    }

    // the text comes from uploaded documents: keep it on one line and out of HTML
    private static String clean(String text) {
        return text?.replaceAll(/[\r\n\t]+/, " ")?.replaceAll(/[<>"'&]/, "")
    }
}
