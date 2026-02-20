import moment from 'moment/moment';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';
import { z } from 'zod';

import { DateFormat } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { validateDateIsSameOrAfter, validateFutureDate } from 'utils/form-utils';

const useOutboundImportValidation = () => {
  const translate = useTranslate();
  const locale = useSelector(getCurrentLocale);
  const requestedBySchema = z.object({
    id: z.string(),
    label: z.string(),
    name: z.string(),
  }, {
    invalid_type_error: translate('react.outboundImport.validation.requestedBy.required.label', 'Requested by is required'),
    required_error: translate('react.outboundImport.validation.requestedBy.required.label', 'Requested by is required'),
  }).required();
  const originSchema = z.object({
    id: z.string(),
    label: z.string(),
    name: z.string(),
  }, {
    invalid_type_error: translate('react.outboundImport.validation.origin.required.label', 'Origin is required'),
    required_error: translate('react.outboundImport.validation.origin.required.label', 'Origin is required'),
  }).required();
  const destinationSchema = z.object({
    id: z.string(),
    label: z.string(),
    name: z.string(),
  }, {
    invalid_type_error: translate('react.outboundImport.validation.destination.required.label', 'Destination is required'),
    required_error: translate('react.outboundImport.validation.destination.required.label', 'Destination is required'),
  }).required();

  const validationSchema = (data) => z.object({
    description: z
      .string({
        required_error: translate('react.outboundImport.validation.description.required.label', 'Description is required'),
      })
      .min(1, translate('react.outboundImport.validation.description.required.label', 'Description is required')),
    origin: originSchema,
    destination: destinationSchema,
    requestedBy: requestedBySchema,
    dateRequested: z.string({
      required_error: translate('react.outboundImport.validation.dateRequested.required.label', 'Date requested is required'),
      invalid_type_error: translate('react.outboundImport.validation.dateRequested.required.label', 'Date requested is required'),
    }).refine((pickedDate) => {
      const pickedDateMoment = moment(pickedDate, DateFormat.MMM_DD_YYYY, locale);
      return validateFutureDate(pickedDateMoment);
    }, {
      message: translate('react.outboundImport.validation.dateRequested.futureDate.label', 'The date cannot be in the future'),
    }),
    dateShipped: z.string({
      required_error: translate('react.outboundImport.validation.dateShipped.required.label', 'Date shipped is required'),
      invalid_type_error: translate('react.outboundImport.validation.dateShipped.required.label', 'Date shipped is required'),
    }).superRefine((pickedDate, ctx) => {
      const pickedDateMoment = moment(pickedDate, DateFormat.MMM_DD_YYYY_HH_MM_SS, locale);
      if (!validateFutureDate(pickedDateMoment)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.outboundImport.validation.dateRequested.futureDate.label', 'The date cannot be in the future'),
        });
      }
      const { expectedDeliveryDate, dateShipped } = data;
      const expectedDeliveryDateMoment = moment(
        expectedDeliveryDate, DateFormat.MMM_DD_YYYY, locale,
      );
      const dateShippedMoment = moment(dateShipped, DateFormat.MMM_DD_YYYY_HH_MM_SS, locale);
      if (!validateDateIsSameOrAfter(expectedDeliveryDateMoment, dateShippedMoment)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.outboundImport.validation.expectedDeliveryDate.afterDateShipped.label',
            'Please verify timeline. Delivery date cannot be before Ship date.'),
        });
      }
    }),
    shipmentType: z.object({
      id: z.string(),
      value: z.string(),
      label: z.string(),
      displayName: z.string(),
      enumKey: z.string(),
      description: z.string().nullish(),
    }, {
      required_error: translate('react.outboundImport.validation.shipmentType.required.label', 'Shipment type is required'),
      invalid_type_error: translate('react.outboundImport.validation.shipmentType.required.label', 'Shipment type is required'),
    }).required(),
    trackingNumber: z.string().nullish(),
    expectedDeliveryDate: z.string({
      required_error: translate('react.outboundImport.validation.expectedDeliveryDate.required.label', 'Expected delivery date is required'),
      invalid_type_error: translate('react.outboundImport.validation.expectedDeliveryDate.required.label', 'Expected delivery date is required'),
    }).refine(() => {
      const { expectedDeliveryDate, dateShipped } = data;
      const deliveryDateMoment = moment(expectedDeliveryDate, DateFormat.MMM_DD_YYYY, locale);
      const dateShippedMoment = moment(dateShipped, DateFormat.MMM_DD_YYYY_HH_MM_SS, locale);
      return validateDateIsSameOrAfter(deliveryDateMoment, dateShippedMoment);
    }, {
      message: translate('react.outboundImport.validation.expectedDeliveryDate.afterDateShipped.label',
        'Please verify timeline. Delivery date cannot be before Ship date.'),
    }),
    packingList: z.instanceof(File),
  });

  return {
    validationSchema,
    destinationSchema,
    originSchema,
    requestedBySchema,
  };
};

export default useOutboundImportValidation;
