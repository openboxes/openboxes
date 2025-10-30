import React from 'react';

import PropTypes from 'prop-types';

import useConfirmExpirationDateModal from 'hooks/useConfirmExpirationDateModal';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';

import ModalWithTable from './ModalWithTable';

const ConfirmExpirationDateModal = ({
  isOpen, itemsWithMismatchedExpiry, onConfirm, onCancel,
}) => {
  useTranslation('confirmExpirationDate');
  const translate = useTranslate();
  const { columns } = useConfirmExpirationDateModal();

  return (
    <ModalWithTable
      isOpen={isOpen}
      title={translate('react.confirmExpirationDate.modal.title.label', 'Confirm save')}
      subtitle={translate('react.confirmExpirationDate.modal.subtitle.label', 'This will update the expiry date across all depots...')}
      columns={columns}
      data={itemsWithMismatchedExpiry}
      confirmLabel={{
        key: 'react.default.yes.label',
        default: 'Yes',
      }}
      cancelLabel={{
        key: 'react.default.no.label',
        default: 'No',
      }}
      onConfirm={onConfirm}
      onCancel={onCancel}
    />
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
