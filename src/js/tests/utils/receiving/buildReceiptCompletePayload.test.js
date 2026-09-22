import buildReceiptCompletePayload from 'utils/receiving/buildReceiptCompletePayload';

import '@testing-library/jest-dom';

const itemsToComplete = [
  { receiptItem: { id: 'receipt-item-1' }, cancelRemainingQuantity: true },
];

describe('buildReceiptCompletePayload()', () => {
  it('should pass the completion options through', () => {
    expect(buildReceiptCompletePayload({ dateDelivered: null, itemsToComplete }).itemsToComplete)
      .toEqual(itemsToComplete);
  });

  it('should default to no completion options', () => {
    expect(buildReceiptCompletePayload()).toEqual({ dateDelivered: null, itemsToComplete: [] });
  });
});
