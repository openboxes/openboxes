import React from 'react';

import PropTypes from 'prop-types';

import 'components/wizard/WizardSteps.scss';

const WizardStepsV2 = ({ steps, currentStepKey }) => {
  const isCurrentStep = (iteratedStep) => iteratedStep.key === currentStepKey;
  return (
    <div className="steps-main-box ml-0 mr-0">
      <div className="steps-inside-wrapper">
        {steps.map((step) => (
          <div
            key={step.key}
            className={`step-container ${isCurrentStep(step) ? 'active' : ''}`}
            data-testid="wizard-step"
            data-stepstate={isCurrentStep(step) ? 'active' : 'inactive'}
          >
            <div
              className="circle"
            />
            <div className="step-name">
              {step.title}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default WizardStepsV2;

WizardStepsV2.propTypes = {
  steps: PropTypes.arrayOf(
    PropTypes.shape({
      title: PropTypes.string.isRequired,
      key: PropTypes.oneOfType([
        PropTypes.number,
        PropTypes.string,
      ]).isRequired,
    }),
  ).isRequired,
  currentStepKey: PropTypes.oneOfType([
    PropTypes.number,
    PropTypes.string,
  ]).isRequired,
};
