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
      'Decimals are not allowed',
    ))
    .min(0, translate(
      'react.receiving.error.quantityNegative.label',
      'Negative values are not allowed',
    ));

  // The same quantity validation except the field is also required.
  const requiredQuantityReceivingSchema = z.preprocess(
    (v) => (v === '' || v == null ? undefined : Number(v)),
    quantitySchema,
  );

  const quantityReceivingSchema = quantitySchema.nullish();

  return { requiredQuantityReceivingSchema, quantityReceivingSchema };
};

export default useQuantityReceivingValidation;
