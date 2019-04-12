import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';

const WizardSteps = props => (
  <div className="wizard-box container-fluid d-print-none">
    <div className="row">
      <div className="wizard-steps d-flex flex-wrap">
        { _.map(props.steps, (step, index) => (
          <div
            key={index}
            className={`
              ${index + 1 === props.currentStep ? 'active-step' : ''} 
              ${index + 1 < props.currentStep ? 'completed-step' : ''} 
              ${index + 1 > props.currentStep || !props.stepsClickable ? 'disabled-step ' : ''}`
            }
          >
            <a href="#" onClick={() => props.onClick(index + 1)}>
              {step}
            </a>
          </div>
          ))
        }
      </div>
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
};

WizardSteps.defaultProps = {
  onClick: stepIdx => stepIdx,
  stepsClickable: false,
};
