import React, { useCallback, useMemo } from 'react';

import PropTypes from 'prop-types';
import {
  RiArrowDownSLine,
  RiLogoutBoxRLine,
  RiMagicLine,
  RiRefreshLine,
} from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getHasBinLocationSupport, getReceivingTranslationsFetched } from 'selectors';

import FilterForm from 'components/Filter/FilterForm';
import Button from 'components/form-elements/Button';
import SlidingButtonGroup from 'components/form-elements/v2/SlidingButtonGroup';
import Switch from 'components/form-elements/v2/Switch';
import filterFields from 'components/receivingV2/FilterFields';
import { AutosaveStatus } from 'consts/autosaveStatuses';
import receivingViewOptions, { ReceivingView } from 'consts/receivingViewOptions';
import useTranslate from 'hooks/useTranslate';
import AutosaveIndicator from 'utils/AutosaveIndicator';

/**
 * Filters bar rendered above the receiving table. The filter (search and
 * receipt status) are rendered through FilterForm from the FilterFields config.
 */
const ReceivingFilters = ({
  view,
  onViewChange,
  putawayEnabled,
  onPutawayChange,
  onAutofillQuantities,
  onSaveAndExit,
  autosaveStatus,
  onResetSort,
  updateFilterParams,
  clearFilterParams,
  packingListViewEnabled,
}) => {
  const translate = useTranslate();
  // Add loading for filters section. Loading will display before the translations are fetched.
  // It fixes the issue of untranslated labels in the filters.
  const translationsFetched = useSelector(getReceivingTranslationsFetched);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
  // Recomputed whenever translations change, so that the field configs hold
  // already translated labels.
  const fields = useMemo(() => filterFields(translate), [translate]);

  const viewOptions = useMemo(() => receivingViewOptions.map((option) => {
    // If the shipment has no pack levels, disable the packing list view.
    const disabled = option.value === ReceivingView.PACKING_LIST && !packingListViewEnabled;

    return {
      ...option,
      disabled,
      disabledTooltip: disabled
        ? translate('react.receiving.packingListView.disabled.label', 'No packing information entered for this shipment.')
        : null,
    };
  }), [packingListViewEnabled, translate]);

  // Clearing the filters is not a submit, so the snapshot of matching rows has to be
  // dropped here as well - otherwise the table would keep showing the previously
  // filtered rows until the next search.
  const onClear = useCallback((form) => {
    form.reset({});
    clearFilterParams();
  }, [clearFilterParams]);

  return (
    <div className="receiving-filters">
      <div className="receiving-filters__row d-flex justify-content-between align-items-center">
        <SlidingButtonGroup
          options={viewOptions}
          defaultOption={view}
          onChange={onViewChange}
        />
        <div className="receiving-filters__autosave-slot">
          <AutosaveIndicator status={autosaveStatus} />
        </div>
      </div>
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
      <div className="receiving-filters__row receiving-filters__actions d-flex flex-wrap justify-content-end align-items-center">
        {hasBinLocationSupport && (
          <Switch
            className="receiving-filters__switch"
            value={putawayEnabled}
            onChange={onPutawayChange}
            titles={{
              checked: {
                id: 'react.receiving.enablePutaway.label',
                defaultMessage: 'Enable Putaway',
              },
              unchecked: {
                id: 'react.receiving.enablePutaway.label',
                defaultMessage: 'Enable Putaway',
              },
            }}
          />
        )}
        {view !== ReceivingView.PACKING_LIST && (
          <Button
            label="react.receiving.resetSorting.label"
            defaultLabel="Reset sorting"
            variant="secondary"
            onClick={onResetSort}
            EndIcon={<RiRefreshLine size={16} />}
          />
        )}
        <Button
          label="react.receiving.autofillQuantities.label"
          defaultLabel="Autofill quantities"
          variant="secondary"
          onClick={onAutofillQuantities}
          EndIcon={<RiMagicLine size={16} />}
        />

        <Button
          label="react.receiving.import.label"
          defaultLabel="Import"
          variant="secondary"
          isDropdown
          EndIcon={<RiArrowDownSLine size={16} />}
        />
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

ReceivingFilters.propTypes = {
  view: PropTypes.string.isRequired,
  onViewChange: PropTypes.func.isRequired,
  putawayEnabled: PropTypes.bool.isRequired,
  onPutawayChange: PropTypes.func.isRequired,
  onAutofillQuantities: PropTypes.func.isRequired,
  onSaveAndExit: PropTypes.func.isRequired,
  autosaveStatus: PropTypes.oneOf(Object.values(AutosaveStatus)).isRequired,
  onResetSort: PropTypes.func.isRequired,
  updateFilterParams: PropTypes.func.isRequired,
  clearFilterParams: PropTypes.func.isRequired,
  packingListViewEnabled: PropTypes.bool.isRequired,
};

export default ReceivingFilters;
