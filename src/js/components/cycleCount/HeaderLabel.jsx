import React from 'react';

const HeaderLabel = ({ label, value, className }) => (
  <div className={`pt-3 d-flex align-items-center ${className}`}>
    <div className="d-flex header-container mb-2">
      <p className="count-step-label count-step-label-select mr-2 mt-2">
        {label}
        <span className="count-step-value ml-1">
          {value}
        </span>
      </p>
    </div>
  </div>
);

export default HeaderLabel;
