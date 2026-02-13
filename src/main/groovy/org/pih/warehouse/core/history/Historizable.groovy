package org.pih.warehouse.core.history

import org.springframework.core.GenericTypeResolver

import org.pih.warehouse.core.AppUtil
import org.pih.warehouse.core.HistoryItem
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.Referenceable

/**
 * The ability for some entity to produce one or more entries of a report on historical data.
 *
 * While not strictly required, this trait will often be used by entities in conjunction with the following fields:
 * - a List of {@link EventLog} to maintain an event/audit log of all relevant changes
 * - a List of {@link org.pih.warehouse.core.Event} to maintain a history of state
 */
trait Historizable<T extends HistoryBuilder> implements Referenceable {

    T getHistoryBuilder() {
        // Determines (statically but at runtime) the class type of the history builder and uses that to fetch the bean.
        return AppUtil.getBean((Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), Historizable.class))
    }

    /**
     * Returns a List of HistoryItem representing some historical data on the entity. Typically this will be some
     * kind of event/audit log built from a List of {@link EventLog} on the entity, but it is up to the implementing
     * HistoryBuilder to determine what qualifies as a history item.
     *
     * Returns a list to allow for a single historizable object to produce multiple history records if needed.
     */
    List<HistoryItem> getHistory() {
        return historyBuilder.getHistory(this)
    }

    @Override
    ReferenceDocument getReferenceDocument() {
        return historyBuilder.getReferenceDocument(this)
    }
}
