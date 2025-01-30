import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';
import { validateFutureDate } from 'utils/form-utils';

const useInboundValidation = () => {
  const translate = useTranslate();

  const requestedBySchema = z.object({
    id: z.string(),
    label: z.string(),
    name: z.string(),
  }, {
    invalid_type_error: translate('react.default.error.requiredField.label', 'Requested by is required'),
    required_error: translate('react.default.error.requiredField.label', 'Requested by is required'),
  }).required();

  const originSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }, {
    invalid_type_error: translate('react.default.error.requiredField.label', 'Origin is required'),
    required_error: translate('react.default.error.requiredField.label', 'Origin is required'),
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
  }).optional();

  const validationSchema = () => z.object({
    description: z
      .string({
        required_error: translate('react.default.error.requiredField.label', 'Description is required'),
      })
      .min(1, translate('react.default.error.requiredField.label', 'Description is required')),
    origin: originSchema,
    destination: destinationSchema,
    stocklist: stocklistSchema,
    requestedBy: requestedBySchema,
    dateRequested: z
      .string({
        required_error: translate('react.default.error.requiredField.label', 'Date requested is required'),
        invalid_type_error: translate('react.default.error.requiredField.label', 'Date requested is required'),
      })
      .refine((pickedDate) => validateFutureDate(pickedDate), {
        message: translate('react.default.error.futureDate.label', 'The date cannot be in the future'),
      }),
  });

  return {
    validationSchema,
  };
};

export default useInboundValidation;
