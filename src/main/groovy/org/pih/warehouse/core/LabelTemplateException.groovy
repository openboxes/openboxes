package org.pih.warehouse.core

/**
 * A stored label (Zebra) template that cannot be rendered with the model it was given,
 * e.g. it uses syntax BeanPropertyTemplateService does not support or reads a value
 * that is not there. Carries a message that tells the user which template to fix.
 */
class LabelTemplateException extends RuntimeException {

    LabelTemplateException(Document document, Throwable cause) {
        super("Label template '${document?.name}'" +
                (document?.documentNumber ? " (${document.documentNumber})" : "") +
                " can't be rendered: ${cause?.message}. Update the template and upload it again.", cause)
    }
}
