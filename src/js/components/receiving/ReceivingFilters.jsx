import React, { useMemo } from 'react';

import {
  RiArrowDownSLine,
  RiLogoutBoxRLine,
  RiMagicLine,
  RiRefreshLine,
} from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getPartialReceivingTranslationsFetched } from 'selectors';

import FilterForm from 'components/Filter/FilterForm';
import Button from 'components/form-elements/Button';
import SlidingButtonGroup from 'components/form-elements/v2/SlidingButtonGroup';
import Switch from 'components/form-elements/v2/Switch';
import filterFields from 'components/receiving/FilterFields';
import receivingViewOptions from 'consts/receivingViewOptions';
import useTranslate from 'hooks/useTranslate';

import 'components/receiving/ReceivingFilters.scss';

/**
 * Filters bar rendered above the receiving table. The filter (search and
 * receipt status) are rendered through FilterForm from the FilterFields config.
 */
const ReceivingFilters = () => {
  const translate = useTranslate();
  // Add loading for filters section. Loading will display before the translations are fetched.
  // It fixes the issue of untranslated labels in the filters.
  const translationsFetched = useSelector(getPartialReceivingTranslationsFetched);
  // Recomputed whenever translations change, so that the field configs hold
  // already translated labels.
  const fields = useMemo(() => filterFields(translate), [translate]);

  return (
    <div className="receiving-filters">
      <div className="receiving-filters__row d-flex justify-content-between align-items-center">
        <SlidingButtonGroup options={receivingViewOptions} defaultOption="table" />
        {/* Autosave indicator should be added here in OBPIH-7850 */}
        <div className="receiving-filters__autosave-slot" />
      </div>
      <FilterForm
        searchFieldId="q"
        searchFieldPlaceholder="react.partialReceiving.search.placeholder.label"
        searchFieldDefaultPlaceholder="Search..."
        filterFields={fields}
        updateFilterParams={() => {}}
        hidden={false}
        showFilterVisibilityToggler={false}
        alignButtonsToFilters
        isLoading={!translationsFetched}
      />
      <div className="receiving-filters__row receiving-filters__actions d-flex flex-wrap justify-content-end align-items-center">
        <Switch
          className="receiving-filters__switch"
          titles={{
            checked: {
              id: 'react.partialReceiving.enablePutaway.label',
              defaultMessage: 'Enable Putaway',
            },
            unchecked: {
              id: 'react.partialReceiving.enablePutaway.label',
              defaultMessage: 'Enable Putaway',
            },
          }}
        />
        <Button
          label="react.partialReceiving.resetSorting.label"
          defaultLabel="Reset sorting"
          variant="secondary"
          EndIcon={<RiRefreshLine size={16} />}
        />
        <Button
          label="react.partialReceiving.autofillQuantities.label"
          defaultLabel="Autofill quantities"
          variant="secondary"
          EndIcon={<RiMagicLine size={16} />}
        />

        <Button
          label="react.partialReceiving.import.label"
          defaultLabel="Import"
          variant="secondary"
          isDropdown
          EndIcon={<RiArrowDownSLine size={16} />}
        />
        <Button
          label="react.partialReceiving.saveAndExit.label"
          defaultLabel="Save & Exit"
          variant="secondary"
          EndIcon={<RiLogoutBoxRLine size={16} />}
        />
      </div>
    </div>
  );
};

export default ReceivingFilters;
