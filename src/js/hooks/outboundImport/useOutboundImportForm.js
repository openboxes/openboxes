import { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import { HttpStatusCode } from 'axios';
import _ from 'lodash';
import moment from 'moment/moment';
import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';

import fulfillmentApi from 'api/services/FulfillmentApi';
import packingListApi from 'api/services/PackingListApi';
import notification from 'components/Layout/notifications/notification';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import NotificationType from 'consts/notificationTypes';
import OutboundImportStep from 'consts/OutboundImportStep';
import { DateFormat } from 'consts/timeFormat';
import useOutboundImportValidation from 'hooks/outboundImport/useOutboundImportValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSessionStorage from 'hooks/useSessionStorage';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';

const useOutboundImportForm = ({ next }) => {
  const translate = useTranslate();
  const spinner = useSpinner();
  const queryParams = useQueryParams();

  const { validationSchema } = useOutboundImportValidation();
  const { currentLocation } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));
  const [cachedData, setCachedData, clearCachedData] = useSessionStorage('outbound-import', {});

  const defaultValues = useMemo(() => {
    const values = {
      description: undefined,
      origin: {
        id: currentLocation?.id,
        label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      },
      destination: undefined,
      requestedBy: undefined,
      dateRequested: undefined,
      dateShipped: moment(new Date()).format(DateFormat.MMM_DD_YYYY_HH_MM_SS),
      shipmentType: undefined,
      trackingNumber: undefined,
      comments: undefined,
      expectedDeliveryDate: undefined,
      packingList: undefined,
    };

    /** Load cached data:
     * If we have saved cached data in the session-storage
     * we want to load cached data only on CONFIRM step,
     * otherwise we clear the session-storage data since that means that user has abandoned the form
     * and that cached data is not relevant anymore
    * */
    if (!_.isEmpty(cachedData)) {
      if (OutboundImportStep.CONFIRM === queryParams?.step) {
        const { fulfillmentDetails, sendingOptions } = cachedData;
        values.description = fulfillmentDetails?.description;
        values.destination = fulfillmentDetails?.destination;
        values.requestedBy = fulfillmentDetails?.requestedBy;
        values.dateRequested = fulfillmentDetails?.dateRequested;
        values.dateShipped = sendingOptions?.expectedShippingDate;
        values.shipmentType = sendingOptions?.shipmentType;
        values.trackingNumber = sendingOptions?.trackingNumber;
        values.comments = sendingOptions?.comments;
        values.expectedDeliveryDate = sendingOptions?.expectedDeliveryDate;
      } else {
        clearCachedData();
      }
    }

    return values;
  }, [currentLocation?.id]);

  /**
   * State to store data that is displayed in the table
   * (this data is a bit different than the one sent to the backend for save)
   * Do not use this one to send the data for save/validation, but use the packingListData
   */
  const [tableData, setTableData] = useState([]);

  // State to store data that is sent to the API for validation/save
  const [packingListData, setPackingListData] = useState([]);
  const [errorsData, setErrorsData] = useState({
    errors: {},
    // Store validation status to easily maintain the disabled prop of the Finish button
    validateStatus: HttpStatusCode.Ok,
  });

  const buildDetailsPayload = (values) => {
    const basicDetails = {
      // Omit values from sending options
      ..._.omit(values, 'shipmentType', 'trackingNumber', 'dateShipped', 'expectedDeliveryDate', 'packingList'),
      dateRequested: moment(values.dateRequested).format(DateFormat.MM_DD_YYYY),
    };
    const sendingOptions = {
      ..._.pick(values, ['shipmentType', 'trackingNumber']),
      expectedShippingDate: moment(values.dateShipped).format(DateFormat.MM_DD_YYYY_HH_MM_Z),
      expectedDeliveryDate: moment(values.expectedDeliveryDate).format(DateFormat.MM_DD_YYYY),
    };
    return { basicDetails, sendingOptions };
  };

  const {
    control,
    getValues,
    handleSubmit,
    formState: { errors, isValid },
    trigger,
    setValue,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  /**
   * Method to group items to two arrays - first - containing items that have validation errors,
   * and second - containing the items without validation errors
   */
  const groupTableDataByErrors = (data) => data?.data?.reduce?.((acc, item) => {
    const itemWithRowNumber = { ...item, fileRowNumber: acc.fileRowNumber };
    if (data?.errors?.packingList?.[item.rowId]) {
      return {
        ...acc,
        itemsWithErrors: [...acc.itemsWithErrors, itemWithRowNumber],
        itemsInOrder: [...acc.itemsInOrder, itemWithRowNumber],
        fileRowNumber: acc.fileRowNumber + 1,
      };
    }
    return {
      ...acc,
      itemsWithoutErrors: [...acc.itemsWithoutErrors, itemWithRowNumber],
      itemsInOrder: [...acc.itemsInOrder, itemWithRowNumber],
      fileRowNumber: acc.fileRowNumber + 1,
    };
  }, {
    itemsWithErrors: [],
    itemsWithoutErrors: [],
    itemsInOrder: [],
    // Rows in the excel file start on 2nd row (first row is the header)
    fileRowNumber: 2,
  });

  const validateOutboundData = async (fulfilmentPayload) => {
    try {
      const validationResponse = await fulfillmentApi.validateOutbound(fulfilmentPayload);
      const tableDataGrouped = groupTableDataByErrors(validationResponse.data);
      // Set the table data that is used to display the items in the React table
      setTableData({
        itemsWithErrors: tableDataGrouped.itemsWithErrors,
        itemsWithoutErrors: tableDataGrouped.itemsWithoutErrors,
        itemsInOrder: tableDataGrouped.itemsInOrder,
      });
      setErrorsData({ errors: {}, validateStatus: validationResponse.status });
    } catch (e) {
      setErrorsData({ errors: e.response.data.errors, validateStatus: e.response.status });
      // Group errors by errors and make the items with errors appear at the top,
      // by merging two grouped arrays
      const tableDataGrouped = groupTableDataByErrors(e.response.data);
      setTableData({
        itemsWithErrors: tableDataGrouped.itemsWithErrors,
        itemsWithoutErrors: tableDataGrouped.itemsWithoutErrors,
        itemsInOrder: tableDataGrouped.itemsInOrder,
      });
      notification(NotificationType.ERROR_OUTLINED)({
        message: translate('react.outboundImport.validationException.label', 'Validation exception'),
        details: `${tableDataGrouped.itemsWithErrors.length} ${translate('react.outboundImport.invalidRows.label', 'rows in the import are invalid. Review the table below to correct errors in the import.')}`,
      });
    }
  };

  // onSubmit method that is run on the first step (import file + validation of outbound)
  const onSubmitStockMovementDetails = async (values) => {
    spinner.show();
    const importPackingListResponse = await packingListApi.importPackingList(values.packingList);
    const { basicDetails, sendingOptions } = buildDetailsPayload(values);

    const packingList = importPackingListResponse.data.data.map((item) => ({
      // !!! A hack to make the origin being bound first in the command object on backend !!!
      // Origin needs to be bound first, as the binLocation is bound using the just bound origin
      // Do not remove this
      origin: basicDetails.origin.id,
      ...item,
      product: item.productCode,
      rowId: _.uniqueId(),
    }));
    if (!packingList.length) {
      spinner.hide();
      notification(NotificationType.ERROR_OUTLINED)({
        message: translate('react.outboundImport.packingList.empty.label', 'Packing list cannot be empty'),
      });
      return;
    }
    // Set the packing list data that is sent to API
    setPackingListData(packingList);
    const fulfilmentPayload = {
      fulfillmentDetails: basicDetails,
      packingList,
      sendingOptions,
    };
    setCachedData(fulfilmentPayload);
    await validateOutboundData(fulfilmentPayload);
    spinner.hide();
    next();
  };

  /**
   * Method that is run on confirm step when attempting to save the outbound (Finish button)
   */
  const onConfirmImport = async (values) => {
    const { basicDetails, sendingOptions } = buildDetailsPayload(values);

    spinner.show();
    try {
      const response = await fulfillmentApi.createOutbound({
        fulfillmentDetails: basicDetails,
        packingList: packingListData,
        sendingOptions,
      });
      notification(NotificationType.SUCCESS)({
        message: translate('react.outboundImport.form.created.success.label', 'Stock Movement has been created successfully'),
      });
      // after a successful creation of outbound stock movement,
      // this cached data is no longer relevant and can be cleared
      clearCachedData();
      // If the save went sucessfully, redirect to the stock movement view page
      window.location = STOCK_MOVEMENT_URL.show(response.data?.data?.id);
    } catch (e) {
      spinner.hide();
      // If in response there is errors property, it means we want to populate errors with the table
      if (e.response.data?.errors) {
        setErrorsData({ errors: e.response?.data?.errors, validateStatus: e.response?.status });
        // Group errors by errors and make the items with errors appear at the top,
        // by merging two grouped arrays
        const tableDataGrouped = groupTableDataByErrors(e.response?.data);
        setTableData({
          itemsWithErrors: tableDataGrouped.itemsWithErrors,
          itemsWithoutErrors: tableDataGrouped.itemsWithoutErrors,
          itemsInOrder: tableDataGrouped.itemsInOrder,
        });
        notification(NotificationType.ERROR_OUTLINED)({
          message: translate('react.outboundImport.validationException.label', 'Validation exception'),
          details: translate('react.outboundImport.validationException.details.label', 'Check out the table for the validation exceptions'),
        });
        return;
      }
      // If there is not errors property in the error response, this means we got an unexpected,
      // not caught validation error
      notification(NotificationType.ERROR_OUTLINED)({
        message: 'Bad request',
        details: e.response?.data?.errorMessages?.join(', '),
      });
    }
  };

  const loadCachedData = async () => {
    if (!_.isEmpty(cachedData)) {
      spinner.show();
      setPackingListData(cachedData.packingList);
      await validateOutboundData(cachedData);
      spinner.hide();
    }
  };

  useEffect(() => {
    loadCachedData();

    if (currentLocation) {
      setValue('origin', {
        id: currentLocation?.id,
        name: currentLocation?.name,
        label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      });
    }
  }, [currentLocation?.id]);

  return {
    control,
    getValues,
    setValue,
    handleSubmit,
    errors,
    isValid,
    onSubmitStockMovementDetails,
    onConfirmImport,
    trigger,
    lineItemErrors: errorsData.errors,
    lineItems: tableData,
    validateStatus: errorsData.validateStatus,
  };
};

export default useOutboundImportForm;
