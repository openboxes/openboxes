import React from 'react';

import BaseHtmlField from './BaseHtmlField';

const TextField = (props) => {
  const renderInput = attributes => (
    <div className="col-md-4">
      <input
        type="text"
        className="form-control"
        {...attributes}
      />
    </div>
  );

  return (
    <BaseHtmlField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default TextField;
