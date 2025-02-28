import React from 'react';

import PropTypes from 'prop-types';
import { RiPrinterLine, RiSave2Line } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import RedirectButton from 'utils/RedirectButton';

const CountStepHeader = ({ printCountForm, next, save }) => (
  <div className="d-flex justify-content-sm-between align-items-end">
    <RedirectButton
      label="react.cycleCount.redirectToList.label"
      defaultMessage="Back to Cycle Count List"
      redirectTo={CYCLE_COUNT.list(TO_COUNT_TAB)}
      className="pt-5"
    />
    <div className="d-flex gap-8">
      <Button
        onClick={printCountForm}
        label="react.cycleCount.printCountForm.label"
        defaultLabel="Print count form"
        variant="primary-outline"
        StartIcon={<RiPrinterLine size={18} />}
      />
      <Button
        onClick={save}
        label="react.cycleCount.save.label"
        defaultLabel="Save progress"
        variant="primary"
        StartIcon={<RiSave2Line size={18} />}
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

export default CountStepHeader;

CountStepHeader.propTypes = {
  printCountForm: PropTypes.func.isRequired,
  next: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
};
