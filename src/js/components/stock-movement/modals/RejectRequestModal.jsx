import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import RejectRequestModalContent from 'components/stock-movement/modals/RejectRequestModalContent';
import RejectRequestModalHeader from 'components/stock-movement/modals/RejectRequestModalHeader';

import 'components/stock-movement/modals/RejectRequestModal.scss';

const RejectRequestModal = ({
  request,
  isOpenRejectionModal,
  closeRejectionModal,
  rejectRequest,
}) => (
  <Modal
    isOpen={isOpenRejectionModal}
    className="modal-content"
    shouldCloseOnOverlayClick={false}
  >
    <div>
      <RejectRequestModalHeader identifier={request?.identifier} />
      <RejectRequestModalContent
        requestor={request?.requestedBy}
        closeRejectionModal={closeRejectionModal}
        rejectRequest={rejectRequest(request?.id, request?.identifier)}
      />
    </div>
  </Modal>
);

export default RejectRequestModal;

RejectRequestModal.propTypes = {
  request: PropTypes.shape({
    id: PropTypes.string.isRequired,
    requestedBy: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    }),
  }).isRequired,
  isOpenRejectionModal: PropTypes.bool.isRequired,
  closeRejectionModal: PropTypes.func.isRequired,
  rejectRequest: PropTypes.func.isRequired,
};
