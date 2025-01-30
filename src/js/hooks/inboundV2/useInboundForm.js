import { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import moment from 'moment';
import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';

import stockListApi from 'api/services/StockListApi';
import stockMovementApi from 'api/services/StockMovementApi';
import { DateFormat } from 'consts/timeFormat';
import useInboundValidation from 'hooks/inboundV2/useInboundValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';

const useInboundForm = ({ next }) => {
  const [stockLists, setStockLists] = useState([]);
  const { currentLocation } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));
  const spinner = useSpinner();
  const { validationSchema } = useInboundValidation();
  const queryParams = useQueryParams();

  const defaultValues = useMemo(() => {
    const values = {
      description: undefined,
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
    formState: { errors, isValid },
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
      dateRequested: moment(values.dateRequested).format(DateFormat.MM_DD_YYYY),
      origin: { id: values.origin.id },
      destination: { id: values.destination.id },
      requestedBy: { id: values.requestedBy.id },
    };
    try {
      const response = await (queryParams.id
        ? stockMovementApi.updateStockMovement(queryParams.id, formattedValues)
        : stockMovementApi.createStockMovement(formattedValues));

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

  return {
    control,
    getValues,
    setValue,
    handleSubmit,
    errors,
    isValid,
    trigger,
    onSubmitStockMovementDetails,
    stockLists,
  };
};

export default useInboundForm;
