import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';

const WizardTitle = props => (
  <div className="panel-heading movement-number">
    {props.title ?
      (
        <div>
          {
            _.map(props.title, element => (
              <span style={{ color: element.color }}>
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

export default WizardTitle;

WizardTitle.propTypes = {
  /** Array of steps names */
  title: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  additionalTitle: PropTypes.oneOf([PropTypes.string, PropTypes.func]),
};

WizardTitle.defaultProps = {
  additionalTitle: null,
};
