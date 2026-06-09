package org.pih.warehouse.core.history

import org.pih.warehouse.core.Referenceable

/**
 * The ability for some entity to be transformed into one or more entries of a report on historical data.
 *
 * While not strictly required, Historizable entities will often have a hasMany relationship to {@link EventLog}
 * because that is the easiest way to maintain an event/audit log of all relevant changes.
 */
interface Historizable extends Referenceable {
    // Currently this interface is simply a label. We expect the actual logic for generating the event
    // history to be defined in a *HistoryProvider component.
}
