import {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

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
import confirmationModal from 'utils/confirmationModalUtils';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone, { formatDateToString } from 'utils/dateUtils';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';

const useInboundCreateForm = ({ next }) => {
  const [stockLists, setStockLists] = useState([]);
  // Store initial form values from backend to detect changes in origin or stocklist
  // before proceeding to the next step. If changes are detected, a confirmation
  // modal 'Confirm change' is shown. If user declines, form is reset to these initial values
  const initialValuesRef = useRef({
    description: '',
    origin: undefined,
    destination: undefined,
    requestedBy: undefined,
    dateRequested: undefined,
    stocklist: undefined,
  });

  const currentLocation = useSelector(getCurrentLocation);
  const debounceTime = useSelector(getDebounceTime);
  const minSearchLength = useSelector(getMinSearchLength);

  const spinner = useSpinner();
  const { validationSchema } = useInboundCreateValidation();
  const dispatch = useDispatch();
  const history = useHistory();
  const { stockMovementId } = useParams();

  const debouncedOriginLocationsFetch = useMemo(
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
      destination: mapToFormSelectOption(currentLocation, {
        customLabel: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      }),
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
    formState: { errors, dirtyFields },
    setValue,
    watch,
    reset,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  const destination = watch('destination');
  const origin = watch('origin');

  const checkStockMovementChange = () => {
    // If no stock movement id, it's a new stock movement, so no need to check for changes
    if (!stockMovementId) {
      return false;
    }

    return dirtyFields.origin || dirtyFields.stocklist;
  };

  const confirmStockMovementChange = (onConfirm) => {
    const modalLabels = {
      title: {
        label: 'react.stockMovement.message.confirmChange.label',
        default: 'Confirm change',
      },
      content: {
        label: 'react.stockMovement.confirmUpdateInboundChange.message',
        default: 'Do you want to change stock movement data? Changing origin or stock list can cause loss of your current work',
      },
    };

    const modalButtons = (onClose) => [
      {
        variant: 'transparent',
        defaultLabel: 'No',
        label: 'react.default.no.label',
        onClick: () => {
          reset(initialValuesRef.current);
          onClose();
        },
      },
      {
        variant: 'primary',
        defaultLabel: 'Yes',
        label: 'react.default.yes.label',
        onClick: () => {
          onConfirm();
          onClose();
        },
      },
    ];

    confirmationModal({ buttons: modalButtons, ...modalLabels });
  };

  const saveStockMovement = async (values) => {
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
      stocklist: values.stocklist?.id ? { id: values.stocklist.id } : null,
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

  const onSubmitStockMovementDetails = async (values) => {
    const showModal = checkStockMovementChange(values);

    if (!showModal) {
      await saveStockMovement(values);
      return;
    }

    confirmStockMovementChange(() => saveStockMovement(values));
  };

  useEffect(() => {
    if (currentLocation && !destination?.id) {
      setValue('destination', mapToFormSelectOption(currentLocation, {
        customLabel: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      }));
    }
  }, [currentLocation?.id, destination]);

  const fetchStockLists = async () => {
    spinner.show();
    const config = {
      params: {
        origin: origin.id,
        destination: destination.id,
      },
    };
    try {
      const response = await stockListApi.getStockLists(config);

      const newStockLists = response.data.data.map((stocklist) => mapToFormSelectOption(stocklist));

      const currentStocklistId = getValues()?.stocklist?.id;
      const stocklistChanged = !newStockLists.find((item) => item.id === currentStocklistId);

      if (stocklistChanged) {
        setValue('stocklist', null);
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

      const formValues = {
        description: data.description,
        origin: mapToFormSelectOption(data.origin, {
          customLabel: `${data.origin.name} [${data.origin.locationType?.description ?? ''}]`,
        }),
        destination: mapToFormSelectOption(data.destination, {
          customLabel: `${data.destination.name} [${data.destination.locationType?.description ?? ''}]`,
        }),
        requestedBy: mapToFormSelectOption(data.requestedBy),
        stocklist: mapToFormSelectOption(data.stocklist),
        dateRequested: formatDateToString({
          date: data.dateRequested,
          dateFormat: DateFormatDateFns.DD_MMM_YYYY,
        }),
      };

      initialValuesRef.current = formValues;
      reset(formValues);

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
      debouncedOriginLocationsFetch,
      debouncedPeopleFetch,
    },
    actions: {
      onSubmitStockMovementDetails,
    },
  };
};

export default useInboundCreateForm;
