import { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import moment from 'moment';
import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';

import stockListApi from 'api/services/StockListApi';
import stockMovementApi from 'api/services/StockMovementApi';
import { DateFormat } from 'consts/timeFormat';
import useInboundV2Validation from 'hooks/inboundV2/useInboundV2Validation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';

const useInboundV2Form = ({ next }) => {
  const [stockLists, setStockLists] = useState([]);
  const { currentLocation } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));
  const spinner = useSpinner();
  const { validationSchema } = useInboundV2Validation();
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

  // eslint-disable-next-line consistent-return
  const onSubmitStockMovementDetails = async (values) => {
    const formattedValues = {
      ...values,
      name: '',
      dateRequested: moment(values.dateRequested).format(DateFormat.MM_DD_YYYY),
    };
    try {
      let response;
      if (queryParams.id) {
        response = await stockMovementApi.updateStockMovement(queryParams.id,
          formattedValues);
      } else {
        response = await stockMovementApi.createStockMovement(formattedValues);
      }

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
  //
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

      const newStockLists = _.map(response.data.data, (stocklist) => ({
        id: stocklist.id,
        name: stocklist.name,
        value: stocklist.id,
        label: stocklist.name,
      }));

      const currentStocklistId = _.get(getValues(), 'stocklist.id');
      const stocklistChanged = !_.find(newStockLists, (item) => item.id === currentStocklistId);

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
  }, [origin, destination]);

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

export default useInboundV2Form;
