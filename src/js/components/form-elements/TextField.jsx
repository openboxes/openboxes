import React from 'react';

import BaseHtmlField from './BaseHtmlField';

const TextField = (props) => {
  const renderInput = attributes => (
    <input
      type="text"
      className="form-control"
      {...attributes}
    />
  );

  return (
    <BaseHtmlField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default TextField;
