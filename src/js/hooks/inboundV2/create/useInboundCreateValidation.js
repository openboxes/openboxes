import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';
import { validateFutureDateFns, validateMinYear } from 'utils/dateUtils';

const useInboundCreateValidation = () => {
  const translate = useTranslate();

  const requiredFieldMessage = translate(
    'react.default.error.requiredField.label',
    'This field is required',
  );

  const requestedBySchema = z.object({
    id: z.string(),
    label: z.string(),
    name: z.string(),
  }, {
    invalid_type_error: requiredFieldMessage,
    required_error: requiredFieldMessage,
  }).required();

  const originSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }, {
    invalid_type_error: requiredFieldMessage,
    required_error: requiredFieldMessage,
  }).required();

  const destinationSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }, {
    invalid_type_error: translate('react.default.error.requiredField.label', 'Destination is required'),
    required_error: translate('react.default.error.requiredField.label', 'Destination is required'),
  }).required();

  const stocklistSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }).optional().nullable();

  const dateRequestedSchema = z
    .string({
      invalid_type_error: requiredFieldMessage,
      required_error: requiredFieldMessage,
    })
    .refine((pickedDate) => validateFutureDateFns(pickedDate), {
      message: translate('react.default.error.futureDate.label', 'The date cannot be in the future'),
    })
    .refine((pickedDate) => validateMinYear(pickedDate, 2000), {
      message: translate('react.stockMovement.error.invalidDate.label', 'This date is invalid. Please enter a date after 2000.'),
    });

  const validationSchema = () => z.object({
    description: z
      .string({
        invalid_type_error: requiredFieldMessage,
        required_error: requiredFieldMessage,
      })
      .min(1, requiredFieldMessage),
    origin: originSchema,
    destination: destinationSchema,
    stocklist: stocklistSchema,
    requestedBy: requestedBySchema,
    dateRequested: dateRequestedSchema,
  });

  return {
    validationSchema,
  };
};

export default useInboundCreateValidation;
