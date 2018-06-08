import React from 'react';
import PropTypes from 'prop-types';
import ModalWrapper from '../../form-elements/ModalWrapper';

const AdjustInventoryModal = (props) => {
  const {
    fieldConfig: { attributes, getDynamicAttr },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };

  return (
    <ModalWrapper {...attr}>
      <div>Adjust Inventory form placeholder</div>
    </ModalWrapper>
  );
};

export default AdjustInventoryModal;

AdjustInventoryModal.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};
