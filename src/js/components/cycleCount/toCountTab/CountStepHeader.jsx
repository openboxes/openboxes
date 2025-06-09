import React from 'react';

import PropTypes from 'prop-types';
import { RiPrinterLine, RiSave2Line } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import ButtonFileSelect from 'components/form-elements/v2/ButtonFileSelect';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import FileFormat from 'consts/fileFormat';
import AlertMessage from 'utils/AlertMessage';
import RedirectButton from 'utils/RedirectButton';
import Translate from 'utils/Translate';

const CountStepHeader = ({
  printCountForm,
  next,
  save,
  importItems,
  isFormDisabled,
  sortByProductName,
  setSortByProductName,
  importErrors,
}) => (
  <div>
    <div className="d-flex justify-content-sm-between align-items-end">
      <RedirectButton
        label="react.cycleCount.redirectToList.label"
        defaultMessage="Back to Cycle Count List"
        redirectTo={CYCLE_COUNT.list(TO_COUNT_TAB)}
        className="pt-5"
      />
      <div className="d-flex gap-8">
        <div className="btn-group">
          <Button
            isDropdown
            defaultLabel="Print count form"
            label="react.cycleCount.printCountForm.label"
            variant="primary-outline"
            StartIcon={<RiPrinterLine size={18} />}
            disabled={isFormDisabled}
          />
          <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
            <a href="#" className="dropdown-item" onClick={() => printCountForm(FileFormat.PDF)} role="button">
              <Translate
                id="react.cycleCount.printCountFormPdf.label"
                defaultMessage="Print Count form PDF"
              />
            </a>
            <a href="#" className="dropdown-item" onClick={() => printCountForm(FileFormat.XLS)} role="button">
              <Translate
                id="react.cycleCount.exportCountSheet.label"
                defaultMessage="Export Count sheet"
              />
            </a>
          </div>
        </div>
        <ButtonFileSelect
          onFileUpload={importItems}
          defaultLabel="Import Count"
          label="react.cycleCount.importCount.label"
          allowedExtensions={[FileFormat.XLS]}
          disabled={isFormDisabled}
          variant="primary-outline"
          className="no-transition"
        />
        <Button
          onClick={() => setSortByProductName((prev) => !prev)}
          label="react.cycleCount.sortAlphabetically.label"
          defaultLabel="Sort alphabetically"
          variant={sortByProductName ? 'secondary' : 'primary-outline'}
          disabled={isFormDisabled}
        />
        <Button
          onClick={save}
          label="react.cycleCount.save.label"
          defaultLabel="Save progress"
          variant="primary"
          StartIcon={<RiSave2Line size={18} />}
          disabled={isFormDisabled}
        />
        <Button
          onClick={next}
          label="react.default.button.next.label"
          defaultLabel="Next"
          variant="primary"
          disabled={isFormDisabled}
        />
      </div>
    </div>
    <AlertMessage show={importErrors.length > 0} message={importErrors} danger />
  </div>
);

export default CountStepHeader;

CountStepHeader.propTypes = {
  printCountForm: PropTypes.func.isRequired,
  next: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
  isFormDisabled: PropTypes.func.isRequired,
  importItems: PropTypes.func.isRequired,
  sortByProductName: PropTypes.bool.isRequired,
  setSortByProductName: PropTypes.func.isRequired,
  importErrors: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
