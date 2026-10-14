import getReceivingRowStatus from 'utils/receiving/getReceivingRowStatus';

import '@testing-library/jest-dom';

const translate = (id, defaultMessage) => defaultMessage;
const formatNumber = (value) => String(value);

const getStatus = (params) => getReceivingRowStatus({ translate, formatNumber, ...params });

describe('getReceivingRowStatus()', () => {
  it('should report the quantity a flagged line cancels', () => {
    expect(getStatus({ quantityRemaining: 6, isRemainingCanceled: true })).toEqual({
      className: 'status-cell status-cell--canceled',
      value: '6 cancelled',
    });
  });

  it.each([
    ['a completed line', { isCompleted: true, quantityRemaining: 6 }],
    ['a line the pending receipt covers', { quantityRemaining: 0 }],
    ['an over received line', { quantityRemaining: -2 }],
  ])('should keep the canceled status of %s', (_label, overrides) => {
    const { value } = getStatus({ ...overrides, isRemainingCanceled: true });

    expect(value).toBe(`${overrides.quantityRemaining} cancelled`);
  });

  it('should report a completed line', () => {
    expect(getStatus({ quantityRemaining: 0, isCompleted: true })).toEqual({
      className: 'status-cell status-cell--completed',
      value: 'Complete',
    });
  });

  it('should report how much an over received line went over', () => {
    expect(getStatus({ quantityRemaining: -2 })).toEqual({
      className: 'status-cell status-cell--over',
      value: '2 over',
    });
  });

  it('should report a line the receipt covers exactly', () => {
    expect(getStatus({ quantityRemaining: 0 })).toEqual({
      className: 'status-cell status-cell--completed',
      value: 'Complete',
    });
  });

  it('should report the quantity left on a partially received line', () => {
    expect(getStatus({ quantityRemaining: 6 })).toEqual({
      className: 'status-cell',
      value: '6 remaining',
    });
  });
});
