import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import BaseField from 'components/form-elements/BaseField';
import Input from 'utils/Input';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/form-elements/FilterTextField.scss';

const FilterTextField = ({ translate, ...props }) => {
  const renderInput = ({
    placeholder, defaultPlaceholder, className, value, onChange, ...attributes
  }) => (
    <div className={`filter-text-input ${value ? 'filter-text-input-has-value' : ''}`}>
      <Input
        {...attributes}
        value={value}
        onChange={onChange}
        placeholder={translate(placeholder, defaultPlaceholder ?? placeholder)}
      />
      {value && (
        <button
          aria-label="Clear"
          type="button"
          onClick={() => onChange('')}
        >
          <RiCloseLine />
        </button>
      )}
    </div>
  );

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps)(FilterTextField);

FilterTextField.propTypes = {
  translate: PropTypes.func.isRequired,
};
