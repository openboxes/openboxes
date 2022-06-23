import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import 'components/wizard/WizardSteps.scss';


const WizardSteps = props => (
  <div className="steps-main-box">
    <div className="steps-inside-wrapper">
      {_.map(props.steps, (step, index) => (
        <div
          key={index}
          className={`step-container ${props.currentStep === index + 1 ? 'active' : ''}`}
        >
          <div
            className={props.showStepNumber ? 'circle filled' : 'circle'}
            onClick={() => props.onClick(index + 1)}
            onKeyPress={() => props.onClick(index + 1)}
            role="button"
            tabIndex="0"
            disabled={!props.stepsClickable}
          >
            {props.showStepNumber && <span className="number">{index + 1}</span>}
          </div>
          <div className="step-name">
            {step}
          </div>
        </div>
      ))
        }
    </div>
  </div>
);

export default WizardSteps;

WizardSteps.propTypes = {
  /** Array of steps names */
  steps: PropTypes.arrayOf(PropTypes.string).isRequired,
  /** Current step number */
  currentStep: PropTypes.number.isRequired,
  /** Function called after clicking on step, (non required) */
  /* eslint-disable-next-line react/no-unused-prop-types */
  onClick: PropTypes.func,
  /** Indicator if steps are clickable (default = false) */
  stepsClickable: PropTypes.bool,
  showStepNumber: PropTypes.bool,
};

WizardSteps.defaultProps = {
  onClick: stepIdx => stepIdx,
  stepsClickable: false,
  showStepNumber: false,
};
