package org.pih.warehouse.core.db

import liquibase.changelog.ChangeSet
import liquibase.changelog.StandardChangeLogHistoryService

/**
 * Records a changeset that is already in DATABASECHANGELOG and gets marked as ran again (ex: a runAlways changeset
 * whose precondition fails with onFail="MARK_RAN") as re-ran, by updating its row instead of inserting a duplicate.
 *
 * Registered through META-INF/services/liquibase.changelog.ChangeLogHistoryService.
 */
class RerunAwareChangeLogHistoryService extends StandardChangeLogHistoryService {

    @Override
    int getPriority() {
        // Takes over from the standard service only, specialized ones (ex: offline mode) keep their precedence
        return PRIORITY_DEFAULT + 1
    }

    @Override
    void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) {
        // Backport of https://github.com/liquibase/liquibase/pull/6860 (Liquibase 4.32.0)
        boolean isRerun = execType == ChangeSet.ExecType.MARK_RAN && getRanChangeSet(changeSet) != null
        super.setExecType(changeSet, isRerun ? ChangeSet.ExecType.RERAN : execType)
    }
}
