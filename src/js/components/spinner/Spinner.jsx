import React from 'react';

const Spinner = () => (
  <div className="d-flex w-100 h-100 justify-content-center align-items-center">
    <div className="circle-spinner spinner-border" role="status">
      <span className="sr-only">Loading...</span>
    </div>
  </div>
);


export default Spinner;
