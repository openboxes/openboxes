import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import { RiCloseCircleLine, RiLogoutBoxRLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getReceivingTranslationsFetched } from 'selectors';

import FilterForm from 'components/Filter/FilterForm';
import Button from 'components/form-elements/Button';
import filterFields from 'components/receivingV2/FilterFields';
import useTranslate from 'hooks/useTranslate';

/**
 * Action bar of the check step: the search and receipt status filters on the left
 * (rendered through FilterForm from the FilterFields config) and the action buttons
 * on the right.
 */
const ConfirmReceiptFilters = ({ onCancelAllRemaining, onSaveAndExit }) => {
  const translate = useTranslate();
  const translationsFetched = useSelector(getReceivingTranslationsFetched);
  const fields = useMemo(() => filterFields(translate), [translate]);

  return (
    <div className="confirm-receipt__action-bar">
      <div className="confirm-receipt__action-bar-filters">
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
      </div>
      <div className="d-flex gap-8">
        <Button
          label="react.receiving.cancelAllRemaining.label"
          defaultLabel="Cancel All Remaining"
          variant="secondary"
          onClick={onCancelAllRemaining}
          EndIcon={<RiCloseCircleLine size={16} />}
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

ConfirmReceiptFilters.propTypes = {
  onCancelAllRemaining: PropTypes.func,
  onSaveAndExit: PropTypes.func,
};

ConfirmReceiptFilters.defaultProps = {
  onCancelAllRemaining: () => {},
  onSaveAndExit: () => {},
};

export default ConfirmReceiptFilters;
