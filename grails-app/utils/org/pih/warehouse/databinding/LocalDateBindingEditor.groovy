package org.pih.warehouse.databinding

import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class LocalDateBindingEditor extends CustomDateBindingEditor<LocalDate> {

    @Override
    LocalDate getDate(Calendar c) {
        // +1 to month because Calendar is zero-indexed but LocalDate is not.
        LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    @Override
    Class<?> getTargetType() {
        LocalDate
    }
}
