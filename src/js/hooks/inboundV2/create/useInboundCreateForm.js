import { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import queryString from 'query-string';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams } from 'react-router-dom';
import { getCurrentLocation, getDebounceTime, getMinSearchLength } from 'selectors';

import { fetchUsers, updateWorkflowHeader } from 'actions';
import stockListApi from 'api/services/StockListApi';
import stockMovementApi from 'api/services/StockMovementApi';
import { STOCK_MOVEMENT_BY_ID } from 'api/urls';
import StockMovementDirection from 'consts/StockMovementDirection';
import { DateFormat, DateFormatDateFns } from 'consts/timeFormat';
import useInboundCreateValidation from 'hooks/inboundV2/create/useInboundCreateValidation';
import useSpinner from 'hooks/useSpinner';
import apiClient from 'utils/apiClient';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone, { formatDateToString } from 'utils/dateUtils';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';

const useInboundCreateForm = ({ next }) => {
  const [stockLists, setStockLists] = useState([]);
  const currentLocation = useSelector(getCurrentLocation);
  const debounceTime = useSelector(getDebounceTime);
  const minSearchLength = useSelector(getMinSearchLength);
  const spinner = useSpinner();
  const { validationSchema } = useInboundCreateValidation();
  const dispatch = useDispatch();
  const history = useHistory();
  const { stockMovementId } = useParams();

  const debouncedOriginFetch = useMemo(
    () => debounceLocationsFetch(
      debounceTime,
      minSearchLength,
      null,
      false,
      false,
      true,
      false,
      StockMovementDirection.INBOUND,
    ),
    [debounceTime, minSearchLength],
  );

  const debouncedPeopleFetch = useMemo(
    () => debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const defaultValues = useMemo(() => {
    const values = {
      description: '',
      origin: undefined,
      destination: {
        id: currentLocation?.id,
        label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
        name: currentLocation?.name,
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
    setValue,
    watch,
    reset,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  const origin = watch('origin');
  const destination = watch('destination');

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
      destination: { id: values.destination.id },
      requestedBy: { id: values.requestedBy.id },
    };
    try {
      const response = stockMovementId
        ? await stockMovementApi.updateStockMovement(stockMovementId, formattedValues)
        : await stockMovementApi.createStockMovement(formattedValues);

      next({ pathId: response.data.data.id });
    } finally {
      spinner.hide();
    }
  };

  useEffect(() => {
    if (currentLocation && !destination?.id) {
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

  useEffect(() => {
    if (origin?.id && destination?.id) {
      fetchStockLists();
    }
  }, [origin?.id, destination?.id]);

  const fetchData = async () => {
    if (!stockMovementId) {
      dispatch(updateWorkflowHeader([], null));
      return;
    }
    spinner.show();
    try {
      const response = await apiClient.get(STOCK_MOVEMENT_BY_ID(stockMovementId));
      const { data } = response.data;

      reset({
        description: data.description,
        origin: data.origin
          ? {
            id: data.origin.id,
            name: data.origin.name,
            label: `${data.origin.name} [${data.origin.locationType?.description ?? ''}]`,
          }
          : null,
        destination: data.destination
          ? {
            id: data.destination.id,
            name: data.destination.name,
            label: `${data.destination.name} [${data.destination.locationType?.description ?? ''}]`,
          }
          : null,
        requestedBy: data.requestedBy ? {
          id: data.requestedBy.id,
          name: data.requestedBy.name,
          label: data.requestedBy.name,
        } : null,
        dateRequested: formatDateToString({
          date: data.dateRequested,
          dateFormat: DateFormatDateFns.DD_MMM_YYYY,
        }),
        stocklist: data.stocklist ? {
          id: data.stocklist.id,
          name: data.stocklist.name,
          label: data.stocklist.name,
        } : null,
      });

      // We set {} for headerStatus in the create step because we only want to display it on the
      // last step
      dispatch(
        updateWorkflowHeader(createInboundWorkflowHeader(data), data.displayStatus?.name),
      );
    } catch {
      dispatch(updateWorkflowHeader([], null));
      history.push({
        pathname: '/openboxes/stockMovement/createInbound',
        search: queryString.stringify({ direction: 'INBOUND' }),
      });
    } finally {
      spinner.hide();
    }
  };

  useEffect(() => {
    // Fetching data for "requested by" dropdown
    dispatch(fetchUsers());
    fetchData();
  }, []);

  return {
    form: {
      control,
      errors,
      handleSubmit,
    },
    data: {
      stockLists,
      origin,
      debouncedOriginFetch,
      debouncedPeopleFetch,
    },
    actions: {
      onSubmitStockMovementDetails,
    },
  };
};

export default useInboundCreateForm;
