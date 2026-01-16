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
          {subtitle && <p className="modal-content__header__subtitile">{subtitle}</p>}
        </div>
        <div className="modal-content__main">
          <DataTable totalCount={data?.length} data={data} columns={columns} disablePagination />
        </div>
        <div className="modal-content__buttons">
          {onCancel && (
          <Button
            defaultLabel={cancelLabel.default}
            label={cancelLabel.key}
            variant="secondary"
            onClick={onCancel}
          />
          )}
          {onConfirm && (
          <Button
            defaultLabel={confirmLabel.default}
            label={confirmLabel.key}
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
    key: PropTypes.string.isRequired,
    default: PropTypes.string.isRequired,
  }),
  cancelLabel: PropTypes.shape({
    key: PropTypes.string.isRequired,
    default: PropTypes.string.isRequired,
  }),
  onConfirm: PropTypes.func,
  onCancel: PropTypes.func,
};

ModalWithTable.defaultProps = {
  title: null,
  subtitle: null,
  confirmLabel: {
    key: 'react.default.yes.label',
    default: 'Yes',
  },
  cancelLabel: {
    key: 'react.default.no.label',
    default: 'No',
  },
  onConfirm: null,
  onCancel: null,
};

export default ModalWithTable;
