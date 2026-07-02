import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import EditLineItemModalFooter from 'components/receivingV2/editModal/EditLineItemModalFooter';
import EditLineItemModalHeader from 'components/receivingV2/editModal/EditLineItemModalHeader';
import ReceivedLineItemsTable from 'components/receivingV2/editModal/ReceivedLineItemsTable';
import ReceivingLineItemsTable from 'components/receivingV2/editModal/ReceivingLineItemsTable';
import useReceivedLineItems from 'hooks/receiving/v2/useReceivedLineItems';
import useReceivingLineItems from 'hooks/receiving/v2/useReceivingLineItems';
import useShipmentItemDetails from 'hooks/receiving/v2/useShipmentItemDetails';
import ItemDetails from 'utils/ItemDetails';

const EditLineItemModal = ({ onClose, lineItem }) => {
  const {
    fields,
    columns,
    addRow,
    copyToReceiving,
    revertToOriginal,
    receivingNow,
    summaryData,
  } = useReceivingLineItems(lineItem);

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
        <EditLineItemModalHeader onClose={onClose} />
        <ItemDetails badge={badge} fields={detailsFields} className="mt-3" />
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
        />
      </div>
    </Modal>
  );
};

EditLineItemModal.propTypes = {
  onClose: PropTypes.func.isRequired,
  lineItem: PropTypes.shape({
    product: PropTypes.shape({ name: PropTypes.string }),
    lotNumber: PropTypes.string,
    expirationDate: PropTypes.string,
    recipient: PropTypes.shape({ name: PropTypes.string }),
    binLocation: PropTypes.shape({ name: PropTypes.string }),
    quantityShipped: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    quantityReceived: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  }),
};

EditLineItemModal.defaultProps = {
  lineItem: undefined,
};

export default EditLineItemModal;
