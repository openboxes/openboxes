import { useForm } from 'react-hook-form';

const useConfirmReceiptForm = () => {
  const { control } = useForm({
    defaultValues: {
      dateDelivered: null,
    },
  });

  return { control };
};

export default useConfirmReceiptForm;
