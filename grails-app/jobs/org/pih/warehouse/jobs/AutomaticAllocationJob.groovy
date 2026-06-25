package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationStrategy
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.quartz.JobExecutionContext

class AutomaticAllocationJob {

    def authService
    def allocationService
    def stockMovementService

    def sessionRequired = false

    // Requisitions younger than this are left to their event-triggered job (scheduled shortly after creation). The
    // fallback sweep only picks up older requisitions, so it never races an allocation that is still in flight.
    static final long FALLBACK_MINIMUM_AGE_MILLIS = 5 * 60 * 1000

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticAllocationJob),
        cronExpression: JobUtils.getCronExpression(AutomaticAllocationJob)
    }

    def execute(JobExecutionContext context) {
        if (!Holders.config.openboxes.jobs.automaticAllocationJob.enabled) {
            log.info"Automatic allocation job is disabled"
            return
        }

        // Jobs run without a logged-in user. Authenticate as the system user so that audited records created during
        // allocation and the status transition (e.g. EventLog, whose created_by_id/updated_by_id are NOT NULL) get a
        // valid author.
        authService.withSystemUser {
            String requisitionId = context.mergedJobDataMap.get('requisitionId')
            if (requisitionId) {
                try {
                    Requisition requisition = Requisition.get(requisitionId)
                    if (!requisition) {
                        // The creating transaction may still be racing this job (see OBLS-821). The AFTER_COMMIT
                        // listener makes this rare, but retry a few times before leaving it to the fallback sweep.
                        rescheduleOnNotFound(requisitionId, context)
                        return
                    }
                    allocateRequisition(requisition)
                } catch (Exception e) {
                    log.error("Error processing requisition ${requisitionId}", e)
                }
                return
            }

            // Fallback in case the event-triggered allocation job never ran for a requisition (e.g. the app was
            // restarted before the scheduled job fired, or the requisition was created before the scheduling fix).
            // Allocates any requisition still stuck in CREATED with auto-allocation enabled. See OBLS-821.
            if (Holders.config.openboxes.jobs.automaticAllocationJob.bulkRequisitionAutoAllocation) {
                Date createdBefore = new Date(System.currentTimeMillis() - FALLBACK_MINIMUM_AGE_MILLIS)
                List<Requisition> requisitions = Requisition.findAllByStatusAndAutoAllocationEnabledAndDateCreatedLessThan(
                        RequisitionStatus.CREATED, Boolean.TRUE, createdBefore)
                if (requisitions) {
                    log.info("Running automatic allocation fallback for ${requisitions.size()} requisition(s) stuck in CREATED...")
                }
                requisitions.each { Requisition requisition ->
                    try {
                        allocateRequisition(requisition)
                    } catch (Exception e) {
                        log.error("Error processing requisition ${requisition.id}", e)
                    }
                }
            }
        }
    }

    private void rescheduleOnNotFound(String requisitionId, JobExecutionContext context) {
        int maxRetries = (Holders.config.openboxes.jobs.automaticAllocationJob.notFoundMaxRetries ?: 0) as int
        int attempt = (context.mergedJobDataMap.get('attempt') ?: 0) as int
        if (attempt >= maxRetries) {
            log.warn("Requisition ${requisitionId} not found after ${maxRetries} retries, leaving it for the bulk fallback sweep")
            return
        }
        long retryDelay = (Holders.config.openboxes.jobs.automaticAllocationJob.notFoundRetryDelayInMilliseconds ?: 1000) as long
        Date runAt = new Date(System.currentTimeMillis() + retryDelay)
        log.warn("Requisition ${requisitionId} not found, retrying (${attempt + 1}/${maxRetries}) in ${retryDelay} ms")
        AutomaticAllocationJob.schedule(runAt, [requisitionId: requisitionId, attempt: attempt + 1])
    }

    private void allocateRequisition(Requisition requisition) {
        // Guard against double-allocation: only allocate requisitions that are still in CREATED and have no existing
        // picklist items, so an event-triggered job and the fallback sweep can never both allocate the same one.
        boolean alreadyAllocated = requisition.requisitionItems?.any { it.picklistItems }
        if (requisition.status != RequisitionStatus.CREATED || alreadyAllocated) {
            log.info("Requisition ${requisition.id} is already allocated or no longer in CREATED status (${requisition.status}), skipping")
            return
        }
        log.info("Automatic allocation for requisition ${requisition.id} ...")
        allocationService.allocate(requisition, AllocationMode.AUTO, [AllocationStrategy.WAREHOUSE_FIRST])
        stockMovementService.updateRequisitionStatus(requisition.id, RequisitionStatus.PICKING)
    }

}
