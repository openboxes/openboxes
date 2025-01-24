import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

const WizardTitle = (props) => {
  if (!props.title && !props.additionalTitle) {
    return null;
  }

  return (
    <div className="panel-heading movement-number" data-testid="wizardTitle">
      {props.title
        ? (
          <div>
            {
              _.map(props.title, (element) => (
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
      {props.additionalTitle}
    </div>
  );
};

export default WizardTitle;

WizardTitle.propTypes = {
  /** Array of steps names */
  title: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  additionalTitle: PropTypes.oneOf([PropTypes.string, PropTypes.func]),
};

WizardTitle.defaultProps = {
  additionalTitle: null,
};
