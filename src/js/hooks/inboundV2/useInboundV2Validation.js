import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';
import { validateFutureDate } from 'utils/form-utils';

const useOutboundImportValidation = () => {
  const translate = useTranslate();

  const requestedBySchema = z.object({
    id: z.string(),
    label: z.string(),
    name: z.string(),
  }, {
    invalid_type_error: translate('react.inboundV2.validation.requestedBy.required.label', 'Requested by is required'),
    required_error: translate('react.inboundV2.validation.requestedBy.required.label', 'Requested by is required'),
  }).required();

  const originSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }, {
    invalid_type_error: translate('react.inboundV2.validation.origin.required.label', 'Origin is required'),
    required_error: translate('react.inboundV2.validation.origin.required.label', 'Origin is required'),
  }).required();

  const destinationSchema = z.object({
    id: z.string(),
    name: z.string(),
    label: z.string(),
  }, {
    invalid_type_error: translate('react.inboundV2.validation.destination.required.label', 'Destination is required'),
    required_error: translate('react.inboundV2.validation.destination.required.label', 'Destination is required'),
  }).required();

  const stocklistSchema = z.object({
    id: z.string(),
    name: z.string(),
  }).optional();

  const validationSchema = () => z.object({
    description: z
      .string({
        required_error: translate('react.inboundV2.validation.description.required.label', 'Description is required'),
      })
      .min(1, translate('react.inboundV2.validation.description.required.label', 'Description is required')),
    origin: originSchema,
    destination: destinationSchema,
    stocklist: stocklistSchema,
    requestedBy: requestedBySchema,
    dateRequested: z
      .string({
        required_error: translate('react.inboundV2.validation.dateRequested.required.label', 'Date requested is required'),
        invalid_type_error: translate('react.inboundV2.validation.dateRequested.required.label', 'Date requested is required'),
      })
      .refine((pickedDate) => validateFutureDate(pickedDate), {
        message: translate('react.inboundV2.validation.dateRequested.futureDate.label', 'The date cannot be in the future'),
      }),
  });

  return {
    validationSchema,
    destinationSchema,
    originSchema,
    requestedBySchema,
    stocklistSchema,
  };
};

export default useOutboundImportValidation;
