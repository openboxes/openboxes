import React from 'react';

import PropTypes from 'prop-types';
import { RiPrinterLine, RiSave2Line } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import ButtonFileSelect from 'components/form-elements/v2/ButtonFileSelect';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import FileFormat from 'consts/fileFormat';
import useTranslate from 'hooks/useTranslate';
import AlertMessage from 'utils/AlertMessage';
import RedirectButton from 'utils/RedirectButton';
import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

const ResolveStepHeader = ({
  next,
  save,
  printRecountForm,
  refreshCountItems,
  isFormDisabled,
  sortByProductName,
  setSortByProductName,
  importItems,
  importErrors,
}) => {
  const translate = useTranslate();

  return (
    <div>
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
              disabled={isFormDisabled}
            />
            <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
              <a href="#" className="dropdown-item" onClick={() => printRecountForm(FileFormat.PDF)} role="button" aria-disabled={isFormDisabled}>
                <Translate
                  id="react.cycleCount.printRecountFormPdf.label"
                  defaultMessage="Print Recount Form PDF"
                />
              </a>
              <a href="#" className="dropdown-item" onClick={() => printRecountForm(FileFormat.XLS)} role="button" aria-disabled={isFormDisabled}>
                <Translate
                  id="react.cycleCount.exportRecountSheet.label"
                  defaultMessage="Export Recount Sheet"
                />
              </a>
            </div>
          </div>
          <ButtonFileSelect
            onFileUpload={importItems}
            defaultLabel="Import Recount"
            label="react.cycleCount.importRecount.label"
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
          <CustomTooltip
            content={translate(
              'react.cycleCount.table.refreshQuantitiesTooltip.label',
              'Fix missing or transaction error on this product and Refresh to see updated discrepancies.',
            )}
          >
            <Button
              onClick={() => refreshCountItems()}
              label="react.default.button.refresh.label"
              defaultLabel="Reload"
              variant="primary"
              disabled={isFormDisabled}
            />
          </CustomTooltip>
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
};

export default ResolveStepHeader;

ResolveStepHeader.propTypes = {
  next: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
  printRecountForm: PropTypes.func.isRequired,
  refreshCountItems: PropTypes.func.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
  sortByProductName: PropTypes.bool.isRequired,
  setSortByProductName: PropTypes.func.isRequired,
  importItems: PropTypes.func.isRequired,
  importErrors: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
