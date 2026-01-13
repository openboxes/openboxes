import React from 'react';

import PropTypes from 'prop-types';

import ModalWithTable from 'components/modals/ModalWithTable';
import useItemInStockModal from 'hooks/useItemInStockModal';
import useTranslate from 'hooks/useTranslate';

const ItemInStockModal = ({
  isOpen, onCancel, item,
}) => {
  const translate = useTranslate();
  const { columns, data } = useItemInStockModal({ productId: item?.id });

  return (
    <ModalWithTable
      isOpen={isOpen}
      title={translate(
        'react.outboundImport.itemInStock.label',
        `In Stock ${item?.productCode} ${item?.name}`,
        {
          productCode: item?.productCode,
          productName: item?.name,
        },
      )}
      columns={columns}
      data={data}
      cancelLabel={{
        key: 'react.default.close.label',
        default: 'Close',
      }}
      onCancel={onCancel}
    />
  );
};

ItemInStockModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onCancel: PropTypes.func.isRequired,
  item: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    productCode: PropTypes.string.isRequired,
  }),
};

ItemInStockModal.defaultProps = {
  item: {},
};

export default ItemInStockModal;
