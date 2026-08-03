import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

// Zod schema for the editable "Receiving now" rows in the edit modal. Empty/null values
// are treated as "not entered yet" and skip validation; anything numeric must be a
// non-negative integer.
const useEditLineItemValidation = () => {
  const translate = useTranslate();

  const lineItemSchema = z.object({
    quantityReceiving: z.preprocess(
      (v) => (v === '' || v == null ? undefined : Number(v)),
      z.number()
        .int(translate(
          'react.receiving.error.quantityDecimal.label',
          'Decimals are not allowed',
        ))
        .min(0, translate(
          'react.receiving.error.quantityNegative.label',
          'Negative values are not allowed',
        ))
        .optional(),
    ),
  });

  const validationSchema = z.object({
    lineItems: z.array(lineItemSchema),
  });

  return { validationSchema };
};

export default useEditLineItemValidation;
