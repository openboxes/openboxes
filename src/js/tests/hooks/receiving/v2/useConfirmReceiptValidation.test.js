import { renderHook } from '@testing-library/react-hooks';
import { addDays, format, subDays } from 'date-fns';
import { useSelector } from 'react-redux';

import { DateFormatDateFns } from 'consts/timeFormat';
import useConfirmReceiptValidation from 'hooks/receiving/v2/useConfirmReceiptValidation';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

// The formats that useConfirmReceiptValidation expects
const formatDelivered = (date) => format(date, DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS);
const formatShipped = (date) => format(date, DateFormatDateFns.MM_DD_YYYY_HH_MM_Z);

/**
 * @param dateDelivered
 * @param param1
 * @param param1.dateShipped
 * @return {null|string}
 */
const validate = (dateDelivered, { dateShipped = null } = {}) => {
  useSelector.mockReturnValue({ dateShipped });
  const { result } = renderHook(() => useConfirmReceiptValidation());
  const { success, error } = result.current.validationSchema.safeParse({ dateDelivered });
  return success ? null : error.issues[0].message;
};

describe('useConfirmReceiptValidation', () => {
  it('should accept a date in the past', () => {
    expect(validate(formatDelivered(subDays(new Date(), 1)))).toBeNull();
  });

  it.each([
    ['null', null],
    ['empty string', ''],
  ])('should reject a date with value (%s)', (_, deliveredDate) => {
    expect(validate(deliveredDate)).toBe('This field is required');
  });

  it('should reject a date in the future', () => {
    expect(validate(formatDelivered(addDays(new Date(), 1))))
      .toBe('Delivery date cannot be in the future');
  });

  it.each([
    ['at the start of the shipped minute', new Date(2025, 8, 17, 10, 30, 0)],
    ['at the end of the shipped minute', new Date(2025, 8, 17, 10, 30, 59)],
    ['after the shipped minute', new Date(2025, 8, 17, 10, 31, 0)],
    ['a day after the shipped date', new Date(2025, 8, 18, 10, 30, 0)],
  ])('should accept a date %s', (_, deliveredDate) => {
    const dateShipped = formatShipped(new Date(2025, 8, 17, 10, 30));
    expect(validate(formatDelivered(deliveredDate), { dateShipped })).toBeNull();
  });

  it('should reject a date a second before the shipped date', () => {
    const dateShipped = formatShipped(new Date(2025, 8, 17, 10, 30));
    expect(validate(formatDelivered(new Date(2025, 8, 17, 10, 29, 59)), { dateShipped }))
      .toBe('Delivery date cannot be before the shipped date');
  });
});
