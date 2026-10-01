import { z } from 'zod';

import useQuantityReceivingValidation from 'hooks/receiving/v2/useQuantityReceivingValidation';

/**
 * Zod schema for the editable rows in the edit modal.
 *
 * @returns {{ validationSchema: z.ZodType }}
 */
const useEditLineItemValidation = () => {
  const { requiredQuantityReceivingSchema } = useQuantityReceivingValidation();

  // Map the errors by their associated field name
  const lineItemSchema = z.object({
    quantityReceiving: requiredQuantityReceivingSchema,
  });

  const validationSchema = z.object({
    lineItems: z.array(lineItemSchema),
  });

  return { validationSchema };
};

export default useEditLineItemValidation;
