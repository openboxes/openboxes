import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

/**
 * Zod schemas for the "Receiving now" quantity field.
 */
const useQuantityReceivingValidation = () => {
  const translate = useTranslate();

  const requiredFieldMessage = translate(
    'react.default.error.requiredField.label',
    'This field is required',
  );

  const quantitySchema = z.number({
    required_error: requiredFieldMessage,
    invalid_type_error: requiredFieldMessage,
  })
    .int(translate(
      'react.receiving.error.quantityDecimal.label',
      'Value cannot be a decimal number',
    ))
    .min(0, translate(
      'react.receiving.error.quantityNegative.label',
      'Value cannot be a negative number',
    ));

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
