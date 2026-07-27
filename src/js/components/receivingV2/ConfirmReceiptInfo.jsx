import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import { DateFormatDateFns } from 'consts/timeFormat';
import useConfirmReceiptDetails from 'hooks/receiving/v2/useConfirmReceiptDetails';
import useConfirmReceiptStatusTransition from 'hooks/receiving/v2/useConfirmReceiptStatusTransition';
import ItemDetails from 'utils/ItemDetails';

const ConfirmReceiptInfo = ({ control, lineItemsState }) => {
  const { badge, fields } = useConfirmReceiptDetails();
  const { nextBadge } = useConfirmReceiptStatusTransition({ lineItemsState });

  return (
    <ItemDetails
      badge={badge && { ...badge, next: nextBadge }}
      fields={fields}
      className="confirm-receipt__details"
    >
      <div className="confirm-receipt__delivered-on">
        <Controller
          name="dateDelivered"
          control={control}
          render={({ field }) => (
            <DateFieldDateFns
              {...field}
              title={{ id: 'react.receiving.deliveredOn.label', defaultMessage: 'Delivered on' }}
              required
              showTimeSelect
              customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
            />
          )}
        />
      </div>
    </ItemDetails>
  );
};

ConfirmReceiptInfo.propTypes = {
  control: PropTypes.shape({}).isRequired,
  lineItemsState: PropTypes.shape({
    entities: PropTypes.shape({}),
    ids: PropTypes.arrayOf(PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
      // Type for separator rows ({ isSeparator, name })
      PropTypes.shape({}),
    ])),
  }).isRequired,
};

export default ConfirmReceiptInfo;
