import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import {
  RiArrowDownSLine,
  RiLogoutBoxRLine,
  RiMagicLine,
  RiRefreshLine,
} from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getReceivingTranslationsFetched } from 'selectors';

import FilterForm from 'components/Filter/FilterForm';
import Button from 'components/form-elements/Button';
import SlidingButtonGroup from 'components/form-elements/v2/SlidingButtonGroup';
import Switch from 'components/form-elements/v2/Switch';
import filterFields from 'components/receivingV2/FilterFields';
import { AutosaveStatus } from 'consts/autosaveStatuses';
import receivingViewOptions from 'consts/receivingViewOptions';
import useTranslate from 'hooks/useTranslate';
import AutosaveIndicator from 'utils/AutosaveIndicator';

/**
 * Filters bar rendered above the receiving table. The filter (search and
 * receipt status) are rendered through FilterForm from the FilterFields config.
 */
const ReceivingFilters = ({ view, onViewChange }) => {
  const translate = useTranslate();
  // Add loading for filters section. Loading will display before the translations are fetched.
  // It fixes the issue of untranslated labels in the filters.
  const translationsFetched = useSelector(getReceivingTranslationsFetched);
  // Recomputed whenever translations change, so that the field configs hold
  // already translated labels.
  const fields = useMemo(() => filterFields(translate), [translate]);

  return (
    <div className="receiving-filters">
      <div className="receiving-filters__row d-flex justify-content-between align-items-center">
        <SlidingButtonGroup
          options={receivingViewOptions}
          defaultOption={view}
          onChange={onViewChange}
        />
        <div className="receiving-filters__autosave-slot">
          <AutosaveIndicator status={AutosaveStatus.SAVED} />
        </div>
      </div>
      <FilterForm
        searchFieldId="q"
        searchFieldPlaceholder="react.receiving.search.placeholder.label"
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
              id: 'react.receiving.enablePutaway.label',
              defaultMessage: 'Enable Putaway',
            },
            unchecked: {
              id: 'react.receiving.enablePutaway.label',
              defaultMessage: 'Enable Putaway',
            },
          }}
        />
        <Button
          label="react.receiving.resetSorting.label"
          defaultLabel="Reset sorting"
          variant="secondary"
          EndIcon={<RiRefreshLine size={16} />}
        />
        <Button
          label="react.receiving.autofillQuantities.label"
          defaultLabel="Autofill quantities"
          variant="secondary"
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
          EndIcon={<RiLogoutBoxRLine size={16} />}
        />
      </div>
    </div>
  );
};

ReceivingFilters.propTypes = {
  view: PropTypes.string.isRequired,
  onViewChange: PropTypes.func.isRequired,
};

export default ReceivingFilters;
