import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import 'components/wizard/WizardSteps.scss';

const WizardSteps = ({
  steps,
  currentStep,
  onClick = (stepIdx) => stepIdx,
  stepsClickable = false,
  showStepNumber = false,
}) => (
  <div className="steps-main-box">
    <div className="steps-inside-wrapper">
      {_.map(steps, (step, index) => (
        <div
          key={index}
          className={`step-container ${currentStep === index + 1 ? 'active' : ''}`}
          data-testid="wizard-step"
          data-stepstate={currentStep === index + 1 ? 'active' : 'inactive'}
        >
          <div
            className={showStepNumber ? 'circle filled' : 'circle'}
            onClick={() => onClick(index + 1)}
            onKeyPress={() => onClick(index + 1)}
            role="button"
            tabIndex="0"
            disabled={!stepsClickable}
          >
            {showStepNumber && <span className="number">{index + 1}</span>}
          </div>
          <div className="step-name">
            {step}
          </div>
        </div>
      ))}
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
