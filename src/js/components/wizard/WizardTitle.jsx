import React from 'react';
import PropTypes from 'prop-types';

const WizardTitle = props => (
  <div className="panel-heading movement-number">
    {props.title.length ? (<span>{props.title}</span>) : null}
    {props.additionalTitle}
  </div>
);

export default WizardTitle;

WizardTitle.propTypes = {
  /** Array of steps names */
  title: PropTypes.string.isRequired,
  additionalTitle: PropTypes.oneOf([PropTypes.string, PropTypes.func]),
};

WizardTitle.defaultProps = {
  additionalTitle: null,
};
