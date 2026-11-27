package org.pih.warehouse.receiving

import org.hibernate.Hibernate
import org.pih.warehouse.core.Person

class ReceiptRecipientDto {
    String id
    String name
    String firstName
    String lastName
    String email
    String username

    static ReceiptRecipientDto toDto(Person recipient) {
        if (!recipient) {
            return null
        }
        // Reuse Person/User.toJson so anonymization stays consistent with the rest of the app,
        // but only copy the fields the receiving response exposes - notably we omit roles.
        // A lazy Person association is a Hibernate proxy of Person even when the row is a User
        Map json = Hibernate.unproxy(recipient).toJson()
        return new ReceiptRecipientDto(
                id: json.id,
                name: json.name,
                firstName: json.firstName,
                lastName: json.lastName,
                email: json.email,
                username: json.username,
        )
    }
}
