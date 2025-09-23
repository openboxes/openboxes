import moment from 'moment';
import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';
import { validateFutureDate } from 'utils/form-utils';

const useInboundSendValidation = () => {
  const translate = useTranslate();

  const requiredFieldMessage = translate(
    'react.default.error.requiredField.label',
    'This field is required',
  );

  // Reusable schema for SelectField components
  const selectFieldSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }, {
    invalid_type_error: requiredFieldMessage,
    required_error: requiredFieldMessage,
  }).required();

  const shipDateSchema = z
    .string({
      required_error: requiredFieldMessage,
      invalid_type_error: requiredFieldMessage,
    })
    .refine((pickedDate) => validateFutureDate(pickedDate), {
      message: translate('react.default.error.futureDate.label', 'The date cannot be in the future'),
    });

  const expectedDeliveryDateSchema = z
    .string({
      required_error: requiredFieldMessage,
      invalid_type_error: requiredFieldMessage,
    });

  const validationSchema = () =>
    z.object({
      origin: selectFieldSchema,
      destination: selectFieldSchema,
      shipDate: shipDateSchema,
      shipmentType: selectFieldSchema,
      trackingNumber: z.string().optional(),
      driverName: z.string().optional(),
      comments: z.string().optional(),
      expectedDeliveryDate: expectedDeliveryDateSchema,
    }).superRefine((data, ctx) => {
      if (!moment(data.expectedDeliveryDate).isSameOrAfter(moment(data.shipDate), 'day')) {
        const errorMessage = translate(
          'react.stockMovement.error.deliveryDateBeforeShipDate.label',
          'Please verify timeline. Delivery date cannot be before Ship date.',
        );

        ctx.addIssue({ code: z.ZodIssueCode.custom, message: errorMessage, path: ['shipDate'] });
        ctx.addIssue({ code: z.ZodIssueCode.custom, message: errorMessage, path: ['expectedDeliveryDate'] });
      }
    });

  return {
    validationSchema,
  };
};

export default useInboundSendValidation;
