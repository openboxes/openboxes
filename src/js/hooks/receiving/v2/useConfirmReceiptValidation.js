import { isAfter, isBefore, startOfMinute } from 'date-fns';
import { useSelector } from 'react-redux';
import { getReceivingShipmentDetails } from 'selectors';
import { z } from 'zod';

import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { parseStringToDate } from 'utils/dateUtils';

/**
 * Zod validation schema for the confirmation step form.
 *
 * @returns {{ validationSchema: z.ZodType, dateShipped: string }}
 */
const useConfirmReceiptValidation = () => {
  const translate = useTranslate();

  // This date (and format) comes from the stock movement API response
  const { dateShipped } = useSelector(getReceivingShipmentDetails);
  const shippedDate = parseStringToDate(dateShipped, DateFormatDateFns.MM_DD_YYYY_HH_MM_Z);

  const requiredFieldMessage = translate(
    'react.default.error.requiredField.label',
    'This field is required',
  );

  const dateDeliveredSchema = z.string({
    required_error: requiredFieldMessage,
    invalid_type_error: requiredFieldMessage,
  })
    .min(1, requiredFieldMessage)
    .refine((value) => {
      const deliveredDate = parseStringToDate(value, DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS);
      return !deliveredDate || !isAfter(deliveredDate, new Date());
    }, translate(
      'react.receiving.dateDelivered.error.future.label',
      'Delivery date cannot be in the future',
    ))
    .refine((value) => {
      const deliveredDate = parseStringToDate(value, DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS);
      if (!deliveredDate || !shippedDate) {
        return true;
      }
      // The shipped date only has minute precision, so compare by the minute. Otherwise,
      // a delivery in the same minute as the shipment would fail depending on its seconds.
      return !isBefore(startOfMinute(deliveredDate), startOfMinute(shippedDate));
    }, translate(
      'react.receiving.dateDelivered.error.beforeShipped.label',
      'Delivery date cannot be before the shipped date',
    ));

  const validationSchema = z.object({
    dateDelivered: dateDeliveredSchema,
  });

  return { validationSchema, dateShipped };
};

export default useConfirmReceiptValidation;
