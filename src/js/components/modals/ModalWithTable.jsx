import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';

const ModalWithTable = ({
  isOpen,
  title,
  subtitle,
  columns,
  data,
  confirmLabel,
  cancelLabel,
  onConfirm,
  onCancel,
}) => {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflowY = 'hidden';
    }

    return () => {
      document.body.style.overflowY = 'auto';
    };
  }, [isOpen]);

  // When the modal is not displayed we want to show the scrollbar to users.
  // The return below is necessary to block executing hooks that are related to
  // the hidden modal.
  if (!isOpen) {
    return null;
  }

  return (
    <Modal isOpen={isOpen} className="modal-content min-width-1000">
      <div className="modal-content__header">
        <p className="modal-content__header__title">{title}</p>
        <p className="modal-content__header__subtitile">{subtitle}</p>
      </div>
      <div className="modal-content__main">
        <DataTable data={data} columns={columns} disablePagination />
      </div>
      <div className="modal-content__buttons">
        <Button
          defaultLabel={cancelLabel.default}
          label={cancelLabel.key}
          variant="secondary"
          onClick={onCancel}
        />
        <Button
          defaultLabel={confirmLabel.default}
          label={confirmLabel.key}
          variant="primary"
          onClick={onConfirm}
        />
      </div>
    </Modal>
  );
};

ModalWithTable.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  title: PropTypes.string.isRequired,
  subtitle: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
  data: PropTypes.arrayOf(PropTypes.object).isRequired,
  confirmLabel: PropTypes.shape({
    key: PropTypes.string.isRequired,
    default: PropTypes.string.isRequired,
  }).isRequired,
  cancelLabel: PropTypes.shape({
    key: PropTypes.string.isRequired,
    default: PropTypes.string.isRequired,
  }).isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

export default ModalWithTable;
