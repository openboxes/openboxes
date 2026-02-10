import React from 'react';

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
  if (!isOpen) {
    document.body.style.overflowY = 'auto';
    return null;
  }

  return (
    <Modal isOpen={isOpen} className="modal-content min-width-1000">
      <div data-testid="modal-with-table">
        <div className="modal-content__header">
          {title && <p className="modal-content__header__title">{title}</p>}
          {subtitle && <p className="modal-content__header__subtitle">{subtitle}</p>}
        </div>
        <div className="modal-content__main">
          <DataTable totalCount={data?.length} data={data} columns={columns} disablePagination />
        </div>
        <div className="modal-content__buttons">
          {onCancel && (
          <Button
            defaultLabel={cancelLabel.defaultMessage}
            label={cancelLabel.id}
            variant="secondary"
            onClick={onCancel}
          />
          )}
          {onConfirm && (
          <Button
            defaultLabel={confirmLabel.defaultMessage}
            label={confirmLabel.id}
            variant="primary"
            onClick={onConfirm}
          />
          )}
        </div>
      </div>
    </Modal>
  );
};

ModalWithTable.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  title: PropTypes.string,
  subtitle: PropTypes.string,
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
  data: PropTypes.arrayOf(PropTypes.object).isRequired,
  confirmLabel: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  cancelLabel: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  onConfirm: PropTypes.func,
  onCancel: PropTypes.func,
};

ModalWithTable.defaultProps = {
  title: null,
  subtitle: null,
  confirmLabel: {
    id: 'react.default.yes.label',
    defaultMessage: 'Yes',
  },
  cancelLabel: {
    id: 'react.default.no.label',
    defaultMessage: 'No',
  },
  onConfirm: null,
  onCancel: null,
};

export default ModalWithTable;
