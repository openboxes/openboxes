<%@ page contentType="text/html"%>
<g:applyLayout name="email">
    ${warehouse.message(code: 'email.putawayDiscrepancy.message', args: [discrepancyReason])}
</g:applyLayout>
