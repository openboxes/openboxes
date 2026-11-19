import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

const WizardTitle = ({ title, additionalTitle = null }) => {
  if (!title && !additionalTitle) {
    return null;
  }

  return (
    <div className="panel-heading movement-number" data-testid="wizardTitle">
      {title
        ? (
          <div>
            {
              _.map(title, (element) => (
                <span key={element.text} style={{ color: element.color }}>
                  {element.text}
                  <span style={{ color: 'black' }}>
                    {element.delimeter}
                  </span>
                </span>
              ))
            }
          </div>
        )
        : null}
      {additionalTitle}
    </div>
  );
};

export default WizardTitle;

WizardTitle.propTypes = {
  /** Array of steps names */
  title: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  additionalTitle: PropTypes.oneOf([PropTypes.string, PropTypes.func]),
};
