import React, { useCallback, useMemo } from 'react';

import PropTypes from 'prop-types';
import { RiCloseCircleLine, RiLogoutBoxRLine, RiRefreshLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getHasPartialReceivingSupport, getReceivingTranslationsFetched } from 'selectors';

import FilterForm from 'components/Filter/FilterForm';
import Button from 'components/form-elements/Button';
import filterFields from 'components/receivingV2/FilterFields';
import { ReceivingView } from 'consts/receivingViewOptions';
import useTranslate from 'hooks/useTranslate';

/**
 * Action bar of the check step: the search and receipt status filters on the left
 * (rendered through FilterForm from the FilterFields config) and the action buttons
 * on the right.
 */
const ConfirmReceiptFilters = ({
  view,
  onCancelAllRemaining,
  onSaveAndExit,
  onResetSort,
  updateFilterParams,
  clearFilterParams,
}) => {
  const translate = useTranslate();
  const translationsFetched = useSelector(getReceivingTranslationsFetched);
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);
  const fields = useMemo(() => filterFields(translate), [translate]);

  // Clearing the filters is not a submit, so the snapshot of matching rows has to be
  // dropped here as well - otherwise the table would keep showing the previously
  // filtered rows until the next search.
  const onClear = useCallback((form) => {
    form.reset({});
    clearFilterParams();
  }, [clearFilterParams]);

  return (
    <div className="confirm-receipt__action-bar">
      <div className="confirm-receipt__action-bar-filters">
        <FilterForm
          searchFieldId="q"
          searchFieldPlaceholder="react.receiving.search.placeholder.label"
          searchFieldDefaultPlaceholder="Search..."
          filterFields={fields}
          updateFilterParams={updateFilterParams}
          onClear={onClear}
          disableAutoUpdateFilterParams
          allowEmptySubmit
          hidden={false}
          showFilterVisibilityToggler={false}
          alignButtonsToFilters
          isLoading={!translationsFetched}
        />
      </div>
      <div className="d-flex gap-8">
        {view !== ReceivingView.PACKING_LIST && (
          <Button
            label="react.receiving.resetSorting.label"
            defaultLabel="Reset sorting"
            variant="secondary"
            onClick={onResetSort}
            EndIcon={<RiRefreshLine size={16} />}
          />
        )}
        {hasPartialReceivingSupport && (
          <Button
            label="react.receiving.cancelAllRemaining.label"
            defaultLabel="Cancel All Remaining"
            variant="secondary"
            onClick={onCancelAllRemaining}
            EndIcon={<RiCloseCircleLine size={16} />}
          />
        )}
        <Button
          label="react.receiving.saveAndExit.label"
          defaultLabel="Save & Exit"
          variant="secondary"
          onClick={onSaveAndExit}
          EndIcon={<RiLogoutBoxRLine size={16} />}
        />
      </div>
    </div>
  );
};

ConfirmReceiptFilters.propTypes = {
  view: PropTypes.string.isRequired,
  onCancelAllRemaining: PropTypes.func,
  onSaveAndExit: PropTypes.func,
  onResetSort: PropTypes.func,
  updateFilterParams: PropTypes.func.isRequired,
  clearFilterParams: PropTypes.func.isRequired,
};

ConfirmReceiptFilters.defaultProps = {
  onCancelAllRemaining: () => {},
  onSaveAndExit: () => {},
  onResetSort: () => {},
};

export default ConfirmReceiptFilters;
