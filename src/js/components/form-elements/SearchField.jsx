import React from 'react';

import BaseField from 'components/form-elements/BaseField';
import SearchInput from 'utils/SearchInput';

const SearchField = (props) => {
  const renderInput = attributes => (
    <SearchInput
      {...attributes}
    />
  );

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default SearchField;
