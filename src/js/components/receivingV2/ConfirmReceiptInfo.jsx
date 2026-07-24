import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import { DateFormatDateFns } from 'consts/timeFormat';
import useConfirmReceiptDetails from 'hooks/receiving/v2/useConfirmReceiptDetails';
import ItemDetails from 'utils/ItemDetails';

const ConfirmReceiptInfo = ({ control }) => {
  const { badge, fields } = useConfirmReceiptDetails();

  return (
    <ItemDetails
      badge={badge}
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
};

export default ConfirmReceiptInfo;
