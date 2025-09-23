import { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import queryString from 'query-string';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';

import { fetchUsers, updateWorkflowHeader } from 'actions';
import stockListApi from 'api/services/StockListApi';
import stockMovementApi from 'api/services/StockMovementApi';
import { STOCK_MOVEMENT_BY_ID } from 'api/urls';
import InboundV2Step from 'consts/InboundV2Step';
import { DateFormat } from 'consts/timeFormat';
import useInboundCreateValidation from 'hooks/inboundV2/create/useInboundCreateValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import apiClient from 'utils/apiClient';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone from 'utils/dateUtils';

const useInboundCreateForm = ({ next }) => {
  const [stockLists, setStockLists] = useState([]);
  const { currentLocation } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));
  const spinner = useSpinner();
  const { validationSchema } = useInboundCreateValidation();
  const queryParams = useQueryParams();
  const dispatch = useDispatch();
  const history = useHistory();
  const location = useLocation();

  const defaultValues = useMemo(() => {
    const values = {
      description: '',
      origin: undefined,
      destination: {
        id: currentLocation?.id,
        label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      },
      stocklist: undefined,
      requestedBy: undefined,
      dateRequested: undefined,
    };

    return values;
  }, [currentLocation?.id]);

  const {
    control,
    getValues,
    handleSubmit,
    formState: { errors },
    trigger,
    setValue,
    watch,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  const onSubmitStockMovementDetails = async (values) => {
    spinner.show();
    const formattedValues = {
      ...values,
      name: '',
      dateRequested: dateWithoutTimeZone({
        date: values.dateRequested,
        outputDateFormat: DateFormat.MM_DD_YYYY,
      }),
      origin: { id: values.origin.id },
      destination: { id: currentLocation.id },
      requestedBy: { id: values.requestedBy.id },
    };
    try {
      const response = queryParams.id
        ? await stockMovementApi.updateStockMovement(queryParams.id, formattedValues)
        : await stockMovementApi.createStockMovement(formattedValues);

      next({ id: response.data.data.id });
    } finally {
      spinner.hide();
    }
  };

  useEffect(() => {
    if (currentLocation) {
      setValue('destination', {
        id: currentLocation?.id,
        name: currentLocation?.name,
        label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      });
    }
  }, [currentLocation?.id]);

  const fetchStockLists = async () => {
    spinner.show();
    const config = {
      params: {
        origin: getValues().origin.id,
        destination: getValues().destination.id,
      },
    };
    try {
      const response = await stockListApi.getStockLists(config);

      const newStockLists = response.data.data.map((stocklist) => ({
        id: stocklist.id,
        name: stocklist.name,
        value: stocklist.id,
        label: stocklist.name,
      }));

      const currentStocklistId = getValues()?.stocklist?.id;
      const stocklistChanged = !newStockLists.find((item) => item.id === currentStocklistId);

      if (stocklistChanged) {
        setValue('stocklist', undefined);
      }

      setStockLists(newStockLists);
    } finally {
      spinner.hide();
    }
  };

  const origin = watch('origin');
  const destination = watch('destination');

  useEffect(() => {
    if (origin?.id && destination?.id) {
      fetchStockLists();
    }
  }, [origin?.id, destination?.id]);

  const fetchData = async () => {
    if (!queryParams.id) {
      dispatch(updateWorkflowHeader([], null));
      return;
    }
    spinner.show();
    try {
      const response = await apiClient.get(STOCK_MOVEMENT_BY_ID(queryParams.id));

      const { data } = response.data;
      setValue('description', data.description);
      setValue('origin', {
        id: data.origin.id,
        name: data.origin.name,
        label: `${data.origin.name} [${data.origin.locationType.description}]`,
      });
      setValue('requestedBy', {
        id: data.requestedBy.id,
        name: data.requestedBy.name,
        label: data.requestedBy.name,
      });
      setValue('dateRequested', data.dateRequested);

      // We set {} for headerStatus in the create step because we only want to display it on the
      // last step
      dispatch(
        updateWorkflowHeader(createInboundWorkflowHeader(data), data.displayStatus?.name),
      );
    } catch {
      dispatch(updateWorkflowHeader([], null));
      history.push({
        pathname: location.pathname,
        search: queryString.stringify({
          ...queryParams,
          id: undefined,
        }, { skipNull: true }),
      });
    } finally {
      spinner.hide();
    }
  };

  useEffect(() => {
    if (queryParams.step !== InboundV2Step.ADD_ITEMS && queryParams.step !== InboundV2Step.SEND) {
      // Fetching data for "requested by" dropdown
      dispatch(fetchUsers());
      fetchData();
    }
  }, [queryParams.step]);
  return {
    control,
    getValues,
    setValue,
    handleSubmit,
    errors,
    trigger,
    onSubmitStockMovementDetails,
    stockLists,
  };
};

export default useInboundCreateForm;
