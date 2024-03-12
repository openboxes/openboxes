import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';

import Button from 'components/form-elements/Button';
import TextInput from 'components/form-elements/v2/TextInput';
import useTranslate from 'hooks/useTranslate';

import '../styles.scss';

const AddAttributeOptionModal = ({
  isOpen,
  close,
  selectedAttribute,
  setValue,
}) => {
  const [inputValue, setInputValue] = useState(null);
  const translate = useTranslate();

  const onChange = (e) => setInputValue(e.target.value);

  const onClose = () => {
    setInputValue(null);
    close();
  };

  const setAttributeValue = () => {
    setValue(`attributes.${selectedAttribute?.id}`, { id: selectedAttribute?.id, value: inputValue, label: inputValue });
    setInputValue(null);
    close();
  };

  return (
    <Modal
      isOpen={isOpen}
      className="modal-content w-50"
      shouldCloseOnOverlayClick={false}
    >
      <div>
        <div className="d-flex justify-content-between mb-3">
          <h4>{selectedAttribute?.name}</h4>
          <RiCloseFill
            size="24px"
            className="cursor-pointer"
            role="button"
            onClick={onClose}
          />
        </div>
        <div>
          <TextInput
            required
            title={{
              id: 'react.productSupplier.form.selectOtherValue.label',
              defaultMessage: 'Other',
            }}
            errorMessage={inputValue === '' && translate(
              'react.productSupplier.validation.otherIsRequired',
              'Other is required',
            )}
            value={inputValue}
            onChange={onChange}
          />
        </div>
        <div className="d-flex justify-content-end gap-8 mt-3">
          <Button
            variant="transparent"
            defaultLabel="Cancel"
            label="react.productSupplier.attributeModal.cancel.label"
            onClick={onClose}
          />
          <Button
            disabled={!inputValue}
            defaultLabel="Confirm"
            label="react.productSupplier.attributeModal.confirm.label"
            onClick={setAttributeValue}
          />
        </div>
      </div>
    </Modal>
  );
};

export default AddAttributeOptionModal;

AddAttributeOptionModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  close: PropTypes.func.isRequired,
  selectedAttribute: PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
  }),
  setValue: PropTypes.func.isRequired,
};

AddAttributeOptionModal.defaultProps = {
  selectedAttribute: {},
};
