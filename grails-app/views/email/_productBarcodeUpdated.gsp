<%@ page contentType="text/html" %>
<g:applyLayout name="email">
    <p>
        <strong>Product:</strong> ${product?.productCode ?: 'N/A'} – ${product?.name ?: ''}<br/>
        <strong>Old UPC:</strong> ${oldUpc ?: 'N/A'}<br/>
        <strong>New UPC:</strong> ${newUpc ?: 'N/A'}<br/>
        <strong>Edited by:</strong> ${user?.name ?: user?.username ?: 'Unknown User'}<br/>
        <strong>Date:</strong>
        <g:formatDate date="${new Date()}" format="yyyy-MM-dd HH:mm:ss" />
    </p>

    <p>
        The barcode (UPC) for this product was recently updated in OpenBoxes by
        ${user?.name ?: user?.username ?: 'a user'}.
        This change has been automatically recorded in OpenBoxes and distributed
        to users with the <strong>Product Notifications</strong> role for visibility.
        Please review this update as part of your normal product data verification
        process.
    </p>
</g:applyLayout>
