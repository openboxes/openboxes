import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';

import useInboundAddItemsValidation from './useInboundAddItemsValidation';

const useInboundAddItemsFormState = () => {
  const { validationSchema } = useInboundAddItemsValidation();

  const defaultTableRow = [{
    palletName: '',
    boxName: '',
    product: undefined,
    lotNumber: '',
    expirationDate: '',
    quantityRequested: undefined,
    recipient: undefined,
  }];

  const defaultValues = {
    currentLineItems: [],
    values: {
      lineItems: defaultTableRow,
    },
  };

  const {
    control,
    getValues,
    handleSubmit,
    formState: { errors },
    trigger,
    setValue,
  } = useForm({
    mode: 'onChange',
    defaultValues,
    resolver: zodResolver(validationSchema),
  });

  return {
    control,
    getValues,
    handleSubmit,
    errors,
    trigger,
    setValue,
    defaultTableRow,
  };
};

export default useInboundAddItemsFormState;
