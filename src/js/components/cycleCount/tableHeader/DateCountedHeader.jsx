import React, { memo, useMemo } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getFormatLocalizedDate, makeGetCycleCountDateCounted } from 'selectors';

import { updateDateCounted } from 'actions';
import HeaderLabel from 'components/cycleCount/HeaderLabel';
import HeaderSelect from 'components/cycleCount/HeaderSelect';
import DateField from 'components/form-elements/v2/DateField';
import { DateFormat } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';

const DateCountedHeader = ({ cycleCountId, isStepEditable, isFormDisabled }) => {
  const dispatch = useDispatch();

  const translate = useTranslate();

  const getCycleCountDateCounted = useMemo(makeGetCycleCountDateCounted, []);

  const formatLocalizedDate = useSelector(getFormatLocalizedDate);

  const cycleCountDateCounted = useSelector(
    (state) => getCycleCountDateCounted(state, cycleCountId),
  );

  const handleUpdateDateCounted = (date) => {
    dispatch(updateDateCounted(cycleCountId, date));
  };

  return (
    isStepEditable ? (
      <HeaderSelect
        label={translate('react.cycleCount.dateCounted.label', 'Date counted')}
      >
        <DateField
          className="date-counted-date-picker date-field-input"
          onChangeRaw={handleUpdateDateCounted}
          value={cycleCountDateCounted}
          clearable={false}
          customDateFormat={DateFormat.DD_MMM_YYYY}
          disabled={isFormDisabled}
        />
      </HeaderSelect>
    ) : (
      <HeaderLabel
        label={translate('react.cycleCount.dateCounted.label', 'Date counted')}
        value={formatLocalizedDate(cycleCountDateCounted, DateFormat.DD_MMM_YYYY)}
      />
    )
  );
};

export default memo(DateCountedHeader);
