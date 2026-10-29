import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

export const QuantityReceivingErrorMessage = {
  REQUIRED: {
    id: 'react.default.error.requiredField.label',
    defaultMessage: 'This field is required',
  },
  DECIMAL: {
    id: 'react.receiving.error.quantityDecimal.label',
    defaultMessage: 'Value cannot be a decimal number',
  },
  NEGATIVE: {
    id: 'react.receiving.error.quantityNegative.label',
    defaultMessage: 'Value cannot be a negative number',
  },
};

/**
 * Zod schemas for the "Receiving now" quantity field.
 */
const useQuantityReceivingValidation = () => {
  const translate = useTranslate();
  const translateMessage = ({ id, defaultMessage }) => translate(id, defaultMessage);

  const requiredFieldMessage = translateMessage(QuantityReceivingErrorMessage.REQUIRED);

  const quantitySchema = z.number({
    required_error: requiredFieldMessage,
    invalid_type_error: requiredFieldMessage,
  })
    .int(translateMessage(QuantityReceivingErrorMessage.DECIMAL))
    .min(0, translateMessage(QuantityReceivingErrorMessage.NEGATIVE));

  // The same quantity validation except the field is also required.
  const requiredQuantityReceivingSchema = z.preprocess(
    (v) => (v === '' || v == null ? null : Number(v)),
    quantitySchema
      .nullable()
      .refine((quantity) => quantity !== null, requiredFieldMessage),
  );

  const quantityReceivingSchema = quantitySchema.nullish();

  return { requiredQuantityReceivingSchema, quantityReceivingSchema };
};

export default useQuantityReceivingValidation;
