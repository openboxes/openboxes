import React from 'react';

import PropTypes from 'prop-types';

import ModalWithTable from 'components/modals/ModalWithTable';
import useConfirmDuplicatedItemsModal from 'hooks/inboundV2/addItems/useShowDuplicatedItems';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';

const ConfirmDuplicatedItemsModal = ({
  isOpen, data, onConfirm, onCancel,
}) => {
  useTranslation('stockMovement');
  const translate = useTranslate('stockMovement');
  const { columns } = useConfirmDuplicatedItemsModal();

  return (
    <ModalWithTable
      isOpen={isOpen}
      title={translate('react.stockMovement.message.confirmSave.label', 'Confirm save')}
      subtitle={translate('react.stockMovement.confirmTransition.label', 'You have entered the same code twice. Do you want to continue?')}
      columns={columns}
      data={data}
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

ConfirmDuplicatedItemsModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  data: PropTypes.arrayOf(
    PropTypes.shape({
      code: PropTypes.string,
      product: PropTypes.shape({}),
      quantityRequested: PropTypes.number,
    }),
  ),
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

ConfirmDuplicatedItemsModal.defaultProps = {
  data: [],
};

export default ConfirmDuplicatedItemsModal;
