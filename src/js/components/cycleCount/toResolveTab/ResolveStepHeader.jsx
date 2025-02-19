import React from 'react';

import PropTypes from 'prop-types';
import { RiPrinterLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import RedirectButton from 'utils/RedirectButton';

const ResolveStepHeader = ({ next, printRecountForm }) => (
  <div className="d-flex justify-content-sm-between align-items-end">
    <RedirectButton
      label="react.cycleCount.redirectToList.label"
      defaultMessage="Back to Cycle Count List"
      redirectTo={CYCLE_COUNT.list(TO_COUNT_TAB)}
      className="pt-5"
    />
    <div className="d-flex gap-8">
      <Button
        onClick={printRecountForm}
        label="react.cycleCount.printRecountForm.label"
        defaultLabel="Print recount form"
        variant="primary-outline"
        StartIcon={<RiPrinterLine size={18} />}
      />
      <Button
        onClick={next}
        label="react.default.button.next.label"
        defaultLabel="Next"
        variant="primary"
      />
    </div>
  </div>
);

export default ResolveStepHeader;

ResolveStepHeader.propTypes = {
  next: PropTypes.func.isRequired,
  printRecountForm: PropTypes.func.isRequired,
};
