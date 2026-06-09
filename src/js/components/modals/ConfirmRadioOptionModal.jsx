import React, { useState } from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import Checkbox from 'components/form-elements/v2/Checkbox';
import translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'utils/utils.scss';

const ConfirmRadioOptionModal = ({
  labels: { title, content },
  initialValue,
  options,
  buttons,
  className,
}) => {
  const [selectedOption, setSelectedOption] = useState(initialValue);

  return (
    <div className={`d-flex flex-column custom-modal-content justify-content-between bg-white ${className}`}>
      <div className="d-flex justify-content-between">
        {title?.label && (
          <p className="custom-modal-title">
            {translate({
              id: title.label,
              defaultMessage: title.defaultMessage,
              data: title.data,
            })}
          </p>
        )}
      </div>

      <div>
        {content?.label && (
          <p className="custom-modal-text">
            {translate({
              id: content.label,
              defaultMessage: content.defaultMessage,
              data: content.data,
            })}
          </p>
        )}

        <div>
          {options.map((option) => (
            <div key={option.value} className="mb-2">
              <label className="d-flex align-items-center cursor-pointer m-0" htmlFor={option.value}>
                <Checkbox
                  noWrapper
                  id={option.value}
                  value={selectedOption === option.value}
                  onChange={() => setSelectedOption(option.value)}
                  className="cursor-pointer"
                />
                <span className="ml-2 custom-modal-text">
                  {translate({
                    id: option.label.id,
                    defaultMessage: option.label.defaultMessage,
                    data: option.label.data,
                  })}
                </span>
              </label>
            </div>
          ))}
        </div>
      </div>

      <div className="d-flex justify-content-end">
        {buttons(selectedOption)?.map((button) => (
          <Button
            key={button?.label}
            variant={button?.variant}
            defaultLabel={button?.defaultLabel}
            label={button?.label}
            onClick={button?.onClick}
            className="ml-2"
          />
        ))}
      </div>
    </div>
  );
};

ConfirmRadioOptionModal.propTypes = {
  labels: PropTypes.shape({
    title: PropTypes.shape({
      label: PropTypes.string,
      defaultMessage: PropTypes.string,
      data: PropTypes.shape({}),
    }),
    content: PropTypes.shape({
      label: PropTypes.string,
      defaultMessage: PropTypes.string,
      data: PropTypes.shape({}),
    }),
  }).isRequired,
  initialValue: PropTypes.string.isRequired,
  options: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.shape({
      id: PropTypes.string.isRequired,
      defaultMessage: PropTypes.string.isRequired,
      data: PropTypes.shape({}),
    }).isRequired,
    value: PropTypes.string.isRequired,
  })).isRequired,
  buttons: PropTypes.func.isRequired,
  className: PropTypes.string,
};

ConfirmRadioOptionModal.defaultProps = {
  className: '',
};

export default ConfirmRadioOptionModal;
