import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseLine, RiSearchLine } from 'react-icons/ri';

import Input from 'utils/Input';

const SearchInput = (props) => {
  const handleClear = () => {
    if (props.onChange) {
      props.onChange('');
    }
  };

  return (
    <div data-testid="search-input-div" className={`d-flex flex-row align-items-center justify-content-center search-input ${props.value ? 'search-input-has-value' : ''}`}>
      <RiSearchLine />
      <Input {...props} />
      {props.value &&
        <button
          type="button"
          onClick={handleClear}
        >
          <RiCloseLine />
        </button>}
    </div>
  );
};

export default SearchInput;

SearchInput.propTypes = {
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func,
};

SearchInput.defaultProps = {
  onChange: null,
};
