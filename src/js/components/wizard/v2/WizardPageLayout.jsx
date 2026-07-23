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
    previous,
    next,
  } = {},
  topSection,
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
    {topSection}
    <Section showTitle={false} className="wizard-page-section">
      {children}
    </Section>
    {(previous || next) && (
      <div className="submit-buttons d-flex justify-content-between mt-3">
        {previous && (
          <Button
            label={previous.label ?? 'react.default.button.previous.label'}
            defaultLabel={previous.defaultLabel ?? 'Previous'}
            variant={previous.variant ?? 'primary'}
            onClick={previous.onClick}
            disabled={previous.disabled}
          />
        )}
        {next && (
          <Button
            label={next.label ?? 'react.default.button.next.label'}
            defaultLabel={next.defaultLabel ?? 'Next'}
            variant={next.variant ?? 'primary'}
            onClick={next.onClick}
            disabled={next.disabled}
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
  /** Previous/Next navigation buttons configuration, each button is hidden when omitted */
  buttons: PropTypes.shape({
    previous: PropTypes.shape({
      onClick: PropTypes.func,
      disabled: PropTypes.bool,
      /** Translation id for the button label */
      label: PropTypes.string,
      defaultLabel: PropTypes.string,
      variant: PropTypes.string,
    }),
    next: PropTypes.shape({
      onClick: PropTypes.func,
      disabled: PropTypes.bool,
      /** Translation id for the button label */
      label: PropTypes.string,
      defaultLabel: PropTypes.string,
      variant: PropTypes.string,
    }),
  }),
  /** Content rendered between the steps bar and the main content section */
  topSection: PropTypes.node,
  /** Additional class passed to the page wrapper */
  className: PropTypes.string,
  /** Main page content */
  children: PropTypes.node,
};

WizardPageLayout.defaultProps = {
  title: {},
  buttons: {},
  topSection: null,
  className: '',
  children: null,
};
