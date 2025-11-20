import React, { memo, useMemo } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getUsers, makeGetCycleCountCountedBy } from 'selectors';

import { updateCountedBy } from 'actions';
import HeaderLabel from 'components/cycleCount/HeaderLabel';
import HeaderSelect from 'components/cycleCount/HeaderSelect';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';
import CustomTooltip from 'wrappers/CustomTooltip';

const CountedByHeader = ({ cycleCountId, isStepEditable, isFormDisabled }) => {
  const dispatch = useDispatch();

  const translate = useTranslate();

  const getCycleCountCountedBy = useMemo(makeGetCycleCountCountedBy, []);

  const cycleCountCountedBy = useSelector(
    (state) => getCycleCountCountedBy(state, cycleCountId),
  );

  const users = useSelector(getUsers);

  const handleUpdateCountedBy = (countedBy) => {
    dispatch(updateCountedBy(cycleCountId, countedBy));
  };

  const defaultValue = cycleCountCountedBy
    ? { ...cycleCountCountedBy, label: cycleCountCountedBy.name }
    : undefined;

  return (
    isStepEditable ? (
      <HeaderSelect
        label={translate('react.cycleCount.countedBy.label', 'Counted by')}
        className="ml-4"
      >
        <CustomTooltip
          content={cycleCountCountedBy?.name || translate('react.cycleCount.countedBy.label', 'Counted By')}
        >
          <div className="position-relative">
            <SelectField
              placeholder="Select"
              options={users}
              onChange={handleUpdateCountedBy}
              className="min-width-250"
              defaultValue={defaultValue}
              disabled={isFormDisabled}
            />
          </div>
        </CustomTooltip>
      </HeaderSelect>
    ) : (
      <HeaderLabel
        label={translate('react.cycleCount.countedBy.label', 'Counted by')}
        value={cycleCountCountedBy?.name}
        className="ml-4"
      />
    )
  );
};

export default memo(CountedByHeader);
