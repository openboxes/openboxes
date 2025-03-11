import React from 'react';

import PropTypes from 'prop-types';
import { RiPrinterLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import FileFormat from 'consts/fileFormat';
import RedirectButton from 'utils/RedirectButton';
import Translate from 'utils/Translate';

const ResolveStepHeader = ({ next, printRecountForm }) => (
  <div className="d-flex justify-content-sm-between align-items-end">
    <RedirectButton
      label="react.cycleCount.redirectToResolveTab.label"
      defaultMessage="Back to Resolve tab"
      redirectTo={CYCLE_COUNT.list(TO_RESOLVE_TAB)}
      className="pt-5"
    />
    <div className="d-flex gap-8">
      <div className="btn-group">
        <Button
          isDropdown
          defaultLabel="Print recount form"
          label="react.cycleCount.printRecountForm.label"
          variant="primary-outline"
          StartIcon={<RiPrinterLine size={18} />}
        />
        <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
          <a href="#" className="dropdown-item" onClick={() => printRecountForm(FileFormat.PDF)} role="button">
            <Translate
              id="react.cycleCount.printRecountFormPdf.label"
              defaultMessage="Print Recount Form PDF"
            />
          </a>
          <a href="#" className="dropdown-item" onClick={() => printRecountForm(FileFormat.XLS)} role="button">
            <Translate
              id="react.cycleCount.exportRecountSheet.label"
              defaultMessage="Export Recount Sheet"
            />
          </a>
        </div>
      </div>
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
