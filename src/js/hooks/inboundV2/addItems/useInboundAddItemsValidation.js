import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

const useInboundAddItemsV2Validation = () => {
  const translate = useTranslate();

  const lineItemSchema = z.object({
    palletName: z.string().optional(),
    boxName: z.string().optional(),
    product: z.object({
      id: z.string(),
      value: z.string(),
      label: z.string(),
    }).optional().nullable(),
    lotNumber: z.string().optional(),
    expirationDate: z.string().optional().nullable(),
    quantityRequested: z.number()
      .min(0, translate('react.stockMovement.error.enterQuantity.label', 'Enter proper quantity'))
      .optional()
      .nullable(),
    recipient: z.object({
      id: z.string(),
      value: z.string(),
      label: z.string(),
    }).optional().nullable(),
  })
    .refine((data) => !(data.boxName && !data.palletName), {
      message: translate('react.stockMovement.error.boxWithoutPallet.label', 'Please enter Pack level 1 before Pack level 2'),
      path: ['boxName'],
    })
    .refine((data) => !(data.expirationDate && !data.lotNumber), {
      message: translate('react.stockMovement.error.expiryWithoutLot.label', 'Items with an expiry date must also have a lot number'),
      path: ['lotNumber'],
    })
    .refine((data) => {
      if (data?.product && data?.product?.id) {
        return data?.quantityRequested !== undefined && data?.quantityRequested !== null;
      }
      return true;
    }, {
      message: translate('react.stockMovement.error.enterQuantity.label', 'Enter proper quantity'),
      path: ['quantityRequested'],
    });

  const validationSchema = z.object({
    values: z.object({
      lineItems: z.array(lineItemSchema),
    }),
  });

  return {
    validationSchema,
  };
};

export default useInboundAddItemsV2Validation;
