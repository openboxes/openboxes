import React from 'react';

const HeaderSelect = ({ label, children, className }) => (
  <div className={`d-flex header-container ${className}`}>
    <span className="count-step-label count-step-label-select mr-2 mt-2">
      {label}
    </span>
    {' '}
    {children}
  </div>
);

export default HeaderSelect;
