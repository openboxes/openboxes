import { renderHook } from '@testing-library/react-hooks';

import useEditLineItemValidation, {
  EditLineItemErrorMessage,
} from 'hooks/receiving/v2/useEditLineItemValidation';
import { QuantityReceivingErrorMessage } from 'hooks/receiving/v2/useQuantityReceivingValidation';

// Translations resolve to the default message, so the errors are asserted against it.
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

const product = { id: 'product-1', lotAndExpiryControl: false };
const lotControlledProduct = { id: 'product-2', lotAndExpiryControl: true };
const recipient = { id: 'person-1' };
const bin = { id: 'bin-1' };
const otherBin = { id: 'bin-2' };

const buildRow = (overrides = {}) => ({
  isSplitItem: true,
  product,
  lotNumber: 'LOT-1',
  expirationDate: '01/Jan/2030',
  recipient,
  binLocation: bin,
  quantityReceiving: 1,
  ...overrides,
});

/**
 * @return {Object} the error messages keyed by the path, e.g. { '0.lotNumber': [...] }
 */
const validate = (lineItems) => {
  const { result } = renderHook(() => useEditLineItemValidation());
  const { success, error } = result.current.validationSchema.safeParse({ lineItems });
  if (success) {
    return {};
  }
  return error.issues.reduce((acc, { path: [, ...path], message }) => {
    const key = path.join('.');
    return { ...acc, [key]: [...(acc[key] ?? []), message] };
  }, {});
};

describe('useEditLineItemValidation', () => {
  it('should accept a valid row', () => {
    expect(validate([buildRow()])).toEqual({});
  });

  describe('quantity receiving', () => {
    it.each([
      ['empty string', ''],
      ['null', null],
    ])('should require the quantity (%s)', (_, quantityReceiving) => {
      expect(validate([buildRow({ quantityReceiving })])).toEqual({
        '0.quantityReceiving': [QuantityReceivingErrorMessage.REQUIRED.defaultMessage],
      });
    });

    it('should accept zero', () => {
      expect(validate([buildRow({ quantityReceiving: 0 })])).toEqual({});
    });

    it.each([
      ['a negative number', -1, QuantityReceivingErrorMessage.NEGATIVE.defaultMessage],
      ['a decimal number', 1.5, QuantityReceivingErrorMessage.DECIMAL.defaultMessage],
    ])('should reject %s', (_, quantityReceiving, message) => {
      expect(validate([buildRow({ quantityReceiving })])).toEqual({
        '0.quantityReceiving': [message],
      });
    });

    it('should still run the other rules when the quantity is blank', () => {
      const errors = validate([buildRow({
        product: lotControlledProduct,
        lotNumber: '',
        expirationDate: '',
        quantityReceiving: '',
      })]);

      expect(errors).toEqual({
        '0.quantityReceiving': [QuantityReceivingErrorMessage.REQUIRED.defaultMessage],
        '0.lotNumber': [EditLineItemErrorMessage.LOT_NUMBER_REQUIRED.defaultMessage],
        '0.expirationDate': [EditLineItemErrorMessage.EXPIRATION_DATE_REQUIRED.defaultMessage],
      });
    });
  });

  describe('lot and expiry control', () => {
    it('should require the lot and the expiration date on a split line', () => {
      const errors = validate([buildRow({
        product: lotControlledProduct,
        lotNumber: '  ',
        expirationDate: null,
      })]);

      expect(errors).toEqual({
        '0.lotNumber': [EditLineItemErrorMessage.LOT_NUMBER_REQUIRED.defaultMessage],
        '0.expirationDate': [EditLineItemErrorMessage.EXPIRATION_DATE_REQUIRED.defaultMessage],
      });
    });

    it('should accept a split line with both the lot and the expiration date', () => {
      expect(validate([buildRow({ product: lotControlledProduct })])).toEqual({});
    });

    it('should not check the original line, since its lot comes from the shipper', () => {
      const errors = validate([buildRow({
        isSplitItem: false,
        product: lotControlledProduct,
        lotNumber: '',
        expirationDate: '',
      })]);

      expect(errors).toEqual({});
    });

    it('should not require a lot for a product without lot and expiry control', () => {
      expect(validate([buildRow({ lotNumber: '', expirationDate: '' })])).toEqual({});
    });
  });

  describe('expiration date', () => {
    it.each([
      ['empty', ''],
      ['blank lot', '   '],
    ])('should reject an expiration date without a lot (%s)', (_, lotNumber) => {
      expect(validate([buildRow({ lotNumber })])).toEqual({
        '0.expirationDate': [EditLineItemErrorMessage.EXPIRATION_DATE_WITHOUT_LOT.defaultMessage],
      });
    });

    it('should reject an expiration date without a lot on the original line too', () => {
      expect(validate([buildRow({ isSplitItem: false, lotNumber: '' })])).toEqual({
        '0.expirationDate': [EditLineItemErrorMessage.EXPIRATION_DATE_WITHOUT_LOT.defaultMessage],
      });
    });

    it('should reject a date before 2000', () => {
      expect(validate([buildRow({ expirationDate: '31/Dec/1999' })])).toEqual({
        '0.expirationDate': [EditLineItemErrorMessage.INVALID_EXPIRATION_DATE.defaultMessage],
      });
    });

    it('should accept the first day of 2000', () => {
      expect(validate([buildRow({ expirationDate: '01/Jan/2000' })])).toEqual({});
    });
  });

  describe('duplicated lines', () => {
    const errorsOfRows = (...indexes) => indexes.reduce((acc, index) => ({
      ...acc,
      [`${index}.product`]: [EditLineItemErrorMessage.DUPLICATED_LINE.defaultMessage],
      [`${index}.lotNumber`]: [EditLineItemErrorMessage.DUPLICATED_LINE.defaultMessage],
      [`${index}.recipient`]: [EditLineItemErrorMessage.DUPLICATED_LINE.defaultMessage],
      [`${index}.binLocation`]: [EditLineItemErrorMessage.DUPLICATED_LINE.defaultMessage],
    }), {});

    it('should mark every field of every duplicated row', () => {
      const errors = validate([
        buildRow({ isSplitItem: false }),
        buildRow({ lotNumber: 'LOT-2' }),
        buildRow(),
      ]);

      expect(errors).toEqual(errorsOfRows(0, 2));
    });

    it('should treat the blank values as equal', () => {
      const errors = validate([
        buildRow({ lotNumber: '', expirationDate: '', recipient: null }),
        buildRow({ lotNumber: '  ', expirationDate: '', recipient: undefined }),
      ]);

      expect(errors).toEqual(errorsOfRows(0, 1));
    });

    it.each([
      ['product', { product: { id: 'product-3' } }],
      ['lot', { lotNumber: 'LOT-2' }],
      ['recipient', { recipient: { id: 'person-2' } }],
      ['recipient, when one of them is blank', { recipient: null }],
      ['bin', { binLocation: otherBin }],
    ])('should accept rows that differ in the %s', (_, overrides) => {
      expect(validate([buildRow(), buildRow(overrides)])).toEqual({});
    });

    it('should not compare the rows without a product', () => {
      expect(validate([buildRow({ product: null }), buildRow({ product: null })])).toEqual({});
    });
  });

  describe('different expiration dates of the same lot', () => {
    it('should mark the expiration date of every row of the lot', () => {
      const errors = validate([
        buildRow({ binLocation: bin }),
        buildRow({ binLocation: otherBin, expirationDate: '02/Jan/2030' }),
        buildRow({ binLocation: { id: 'bin-3' } }),
      ]);

      expect(errors).toEqual({
        '0.expirationDate': [EditLineItemErrorMessage.DIFFERENT_EXPIRATION_DATES.defaultMessage],
        '1.expirationDate': [EditLineItemErrorMessage.DIFFERENT_EXPIRATION_DATES.defaultMessage],
        '2.expirationDate': [EditLineItemErrorMessage.DIFFERENT_EXPIRATION_DATES.defaultMessage],
      });
    });

    it('should treat a missing expiration date as a different one', () => {
      const errors = validate([
        buildRow({ binLocation: bin }),
        buildRow({ binLocation: otherBin, expirationDate: '' }),
      ]);

      expect(errors).toEqual({
        '0.expirationDate': [EditLineItemErrorMessage.DIFFERENT_EXPIRATION_DATES.defaultMessage],
        '1.expirationDate': [EditLineItemErrorMessage.DIFFERENT_EXPIRATION_DATES.defaultMessage],
      });
    });

    it('should accept the same lot number of different products', () => {
      const errors = validate([
        buildRow(),
        buildRow({ product: { id: 'product-3' }, expirationDate: '02/Jan/2030' }),
      ]);

      expect(errors).toEqual({});
    });

    it('should accept the same expiration date in different bins', () => {
      expect(validate([buildRow(), buildRow({ binLocation: otherBin })])).toEqual({});
    });
  });
});
