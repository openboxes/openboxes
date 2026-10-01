import { z } from 'zod';

import useQuantityReceivingValidation from 'hooks/receiving/v2/useQuantityReceivingValidation';

/**
 * Zod schema for an editable row in the receiving table.
 *
 * @returns {{ lineItemSchema: z.ZodType }}
 */
const useReceivingLineItemValidation = () => {
  const { quantityReceivingSchema } = useQuantityReceivingValidation();

  // Map the errors by their associated field name
  const lineItemSchema = z.object({
    quantityReceiving: quantityReceivingSchema,
  });

  return { lineItemSchema };
};

export default useReceivingLineItemValidation;
