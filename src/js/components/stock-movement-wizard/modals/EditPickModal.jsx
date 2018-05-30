import React from 'react';
import PropTypes from 'prop-types';
import ModalWrapper from '../../form-elements/ModalWrapper';

const EditPickModal = (props) => {
  const {
    fieldConfig: { attributes, getDynamicAttr },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };

  return (
    <ModalWrapper {...attr}>
      <div>Edit Pick form placeholder</div>
    </ModalWrapper>
  );
};

export default EditPickModal;

EditPickModal.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};
