import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import EditLineItemModalFooter from 'components/receivingV2/editModal/EditLineItemModalFooter';
import EditLineItemModalHeader from 'components/receivingV2/editModal/EditLineItemModalHeader';
import ReceivedLineItemsTable from 'components/receivingV2/editModal/ReceivedLineItemsTable';
import ReceivingLineItemsTable from 'components/receivingV2/editModal/ReceivingLineItemsTable';
import useEditLineItemSave from 'hooks/receiving/v2/useEditLineItemSave';
import useReceivedLineItems from 'hooks/receiving/v2/useReceivedLineItems';
import useReceivingLineItems from 'hooks/receiving/v2/useReceivingLineItems';
import useShipmentItemDetails from 'hooks/receiving/v2/useShipmentItemDetails';
import ItemDetails from 'utils/ItemDetails';

const EditLineItemModal = ({
  onClose, lineItem, initialLineItems, receiptId, loadReceipt,
}) => {
  const {
    fields,
    columns,
    addRow,
    copyToReceiving,
    revertToOriginal,
    receivingNow,
    summaryData,
    getLineItems,
  } = useReceivingLineItems({ lineItem, initialLineItems });

  const { onSave } = useEditLineItemSave({
    receiptId,
    lineItem,
    initialLineItems,
    getLineItems,
    loadReceipt,
    onClose,
  });

  const {
    receivedItems,
    columns: receivedColumns,
    totalReceived,
  } = useReceivedLineItems(
    lineItem,
    {
      onCopyToReceive: copyToReceiving,
    },
  );

  const {
    badge,
    fields: detailsFields,
  } = useShipmentItemDetails(lineItem);

  return (
    <Modal isOpen className="modal-content">
      <div className="receiving-edit-modal" data-testid="receiving-edit-line-item-modal">
        <EditLineItemModalHeader
          onClose={onClose}
        />
        <ItemDetails
          badge={badge}
          fields={detailsFields}
          className="mt-3"
        />
        <ReceivedLineItemsTable
          receivedItems={receivedItems}
          columns={receivedColumns}
          totalReceived={totalReceived}
        />
        <ReceivingLineItemsTable
          fields={fields}
          columns={columns}
          receivingNow={receivingNow}
          revertToOriginal={revertToOriginal}
          addRow={addRow}
        />
        <EditLineItemModalFooter
          summaryData={summaryData}
          onClose={onClose}
          onSave={onSave}
        />
      </div>
    </Modal>
  );
};

EditLineItemModal.propTypes = {
  onClose: PropTypes.func.isRequired,
  receiptId: PropTypes.string,
  lineItem: PropTypes.shape({
    shipmentItemId: PropTypes.string,
    receiptItemId: PropTypes.string,
    product: PropTypes.shape({ name: PropTypes.string }),
    lotNumber: PropTypes.string,
    expirationDate: PropTypes.string,
    recipient: PropTypes.shape({ name: PropTypes.string }),
    binLocation: PropTypes.shape({ name: PropTypes.string }),
    quantityShipped: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    quantityReceived: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    previousReceiptItems: PropTypes.arrayOf(PropTypes.shape({
      productLot: PropTypes.shape({}),
      recipient: PropTypes.shape({}),
      binLocation: PropTypes.shape({}),
      quantityReceived: PropTypes.number,
    })),
  }),
  initialLineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  loadReceipt: PropTypes.func.isRequired,
};

EditLineItemModal.defaultProps = {
  lineItem: undefined,
  receiptId: null,
};

export default EditLineItemModal;
