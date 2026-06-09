import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import WizardPageHeader from 'components/wizard/v2/WizardPageHeader';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import PageWrapper from 'wrappers/PageWrapper';

import 'utils/utils.scss';
import 'components/stock-movement-wizard/StockMovement.scss';
import 'components/wizard/v2/WizardPageLayout.scss';

/**
 * Shared layout for v2 wizard pages (examples: stock movement, cycle count, receiving).
 * Owns the page skeleton (header bar, steps bar, content, navigation) while staying
 * workflow-agnostic — the header content is supplied through the `title` prop.
 */
const WizardPageLayout = ({
  title: {
    label,
    info,
    status,
  } = {},
  wizard: {
    steps,
    currentStepKey,
  },
  buttons: {
    onNext,
    onPrevious,
    isNextDisabled = false,
    isPreviousDisabled = false,
    nextLabel = 'react.default.button.next.label',
    nextDefaultLabel = 'Next',
    previousLabel = 'react.default.button.previous.label',
    previousDefaultLabel = 'Previous',
  } = {},
  className,
  children,
}) => (
  <PageWrapper className={className}>
    <Section showTitle={false} className="mt-4 wizard-page-section">
      <WizardPageHeader label={label} info={info} status={status} />
    </Section>
    <div className="mb-4 mt-2">
      <WizardStepsV2 steps={steps} currentStepKey={currentStepKey} />
    </div>
    <Section showTitle={false} className="wizard-page-section">
      {children}
    </Section>
    {(onNext || onPrevious) && (
      <div className="submit-buttons d-flex justify-content-between mt-3">
        {onPrevious && (
          <Button
            label={previousLabel}
            defaultLabel={previousDefaultLabel}
            variant="primary"
            onClick={onPrevious}
            disabled={isPreviousDisabled}
          />
        )}
        {onNext && (
          <Button
            label={nextLabel}
            defaultLabel={nextDefaultLabel}
            variant="primary"
            onClick={onNext}
            disabled={isNextDisabled}
            className="ml-auto"
          />
        )}
      </div>
    )}
  </PageWrapper>
);

export default WizardPageLayout;

WizardPageLayout.propTypes = {
  /** Header bar content */
  title: PropTypes.shape({
    /** Prefix label, e.g. "Receiving" */
    label: PropTypes.string,
    /** Colored title segments rendered after the label */
    info: PropTypes.arrayOf(
      PropTypes.shape({
        text: PropTypes.string,
        color: PropTypes.string,
        delimeter: PropTypes.string,
        isDate: PropTypes.bool,
      }),
    ),
    /** Optional status badge rendered on the right side of the header */
    status: PropTypes.string,
  }),
  /** Wizard configuration */
  wizard: PropTypes.shape({
    /** Steps configuration passed to the wizard */
    steps: PropTypes.arrayOf(
      PropTypes.shape({
        title: PropTypes.string.isRequired,
        key: PropTypes.oneOfType([
          PropTypes.number,
          PropTypes.string,
        ]).isRequired,
      }),
    ).isRequired,
    /** Key of the currently active step */
    currentStepKey: PropTypes.oneOfType([
      PropTypes.number,
      PropTypes.string,
    ]).isRequired,
  }).isRequired,
  /** Previous/Next navigation buttons configuration */
  buttons: PropTypes.shape({
    /** Next button handler, the button is hidden when omitted */
    onNext: PropTypes.func,
    /** Previous button handler, the button is hidden when omitted */
    onPrevious: PropTypes.func,
    isNextDisabled: PropTypes.bool,
    isPreviousDisabled: PropTypes.bool,
    /** Translation id for the next button label */
    nextLabel: PropTypes.string,
    nextDefaultLabel: PropTypes.string,
    /** Translation id for the previous button label */
    previousLabel: PropTypes.string,
    previousDefaultLabel: PropTypes.string,
  }),
  /** Additional class passed to the page wrapper */
  className: PropTypes.string,
  /** Main page content */
  children: PropTypes.node,
};

WizardPageLayout.defaultProps = {
  title: {},
  buttons: {},
  className: '',
  children: null,
};
