import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import './WizardSteps.scss';

const WizardSteps = props => (
  <div className="steps-box d-print-none">
    <div className="steps d-flex flex-wrap">
      { _.map(props.steps, (step, index) => (
        <div
          key={index}
          className="step-container"
        >
          <button className="step" onClick={() => props.onClick(index + 1)} disabled={!props.stepsClickable}>
            <i
              className={`fa 
                ${index + 1 === props.currentStep ? 'fa-circle-o' : ''} 
                ${index + 1 < props.currentStep ? 'fa-check-circle-o' : ''} 
                ${index + 1 > props.currentStep || (index + 1 !== props.currentStep && !props.stepsClickable) ? 'fa-circle-o disabled' : ''}`}
              aria-hidden="true"
            />
            <span className="step-text">
              {step}
            </span>
          </button>
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
};

WizardSteps.defaultProps = {
  onClick: stepIdx => stepIdx,
  stepsClickable: false,
};
