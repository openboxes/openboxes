import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

// Zod schema for the editable "Receiving now" rows in the edit modal. Every row has to carry
// a quantity - empty/null values block saving (0 is a valid quantity) - and it must be a
// non-negative integer.
const useEditLineItemValidation = () => {
  const translate = useTranslate();

  const requiredFieldMessage = translate(
    'react.default.error.requiredField.label',
    'This field is required',
  );

  const lineItemSchema = z.object({
    quantityReceiving: z.preprocess(
      (v) => (v === '' || v == null ? undefined : Number(v)),
      z.number({
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
        )),
    ),
  });

  const validationSchema = z.object({
    lineItems: z.array(lineItemSchema),
  });

  return { validationSchema };
};

export default useEditLineItemValidation;
