import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useConfirmExpirationDateModal from 'hooks/useConfirmExpirationDateModal';
import useTranslate from 'hooks/useTranslate';

const ConfirmExpirationDateModal = ({
  isOpen,
  itemsWithMismatchedExpiry,
  onConfirm,
  onCancel,
}) => {
  const translate = useTranslate();
  const { columns } = useConfirmExpirationDateModal();

  // When the modal is not displayed we want to show the scrollbar to users.
  // The return below is necessary to block executing hooks that are related to
  // the hidden modal.
  if (!isOpen) {
    document.body.style.overflowY = 'auto';
    return null;
  }

  return (
    <Modal isOpen={isOpen} className="modal-content min-width-1000">
      <div className="modal-content__header">
        <p className="modal-content__header__title">
          {translate('react.confirmExpirationDate.modal.title.label', 'Confirm save')}
        </p>
        <p className="modal-content__header__subtitile">
          {translate(
            'react.confirmExpirationDate.modal.subtitle.label',
            'This will update the expiry date across all depots in the system for the lots listed below. Are you sure you want to proceed?',
          )}
        </p>
      </div>

      <div className="modal-content__main">
        <DataTable
          data={itemsWithMismatchedExpiry}
          columns={columns}
          disablePagination
        />
      </div>

      <div className="modal-content__buttons">
        <Button
          defaultLabel="No"
          label="react.default.no.label"
          variant="secondary"
          onClick={onCancel}
        />
        <Button
          defaultLabel="Yes"
          label="react.default.yes.label"
          variant="primary"
          onClick={onConfirm}
        />
      </div>
    </Modal>
  );
};

ConfirmExpirationDateModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  itemsWithMismatchedExpiry: PropTypes.arrayOf(
    PropTypes.shape({
      code: PropTypes.string,
      product: PropTypes.shape({}),
      lotNumber: PropTypes.string,
      previousExpiry: PropTypes.string,
      newExpiry: PropTypes.string,
    }),
  ),
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

ConfirmExpirationDateModal.defaultProps = {
  itemsWithMismatchedExpiry: [],
};

export default ConfirmExpirationDateModal;
