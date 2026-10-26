import { act, renderHook } from '@testing-library/react-hooks';
import { useForm } from 'react-hook-form';

import useCalculateEachPrice from 'hooks/productSupplier/form/useCalculateEachPrice';

const renderWithForm = (packageSpecification) => renderHook(() => {
  const form = useForm({ defaultValues: { packageSpecification } });
  useCalculateEachPrice({ control: form.control, setValue: form.setValue });
  return form;
});

const eachPrice = (result) => result.current.getValues('packageSpecification.eachPrice');

describe('useCalculateEachPrice', () => {
  it('computes the each price from package price and quantity, rounded to 4 places', () => {
    const { result } = renderWithForm({ productPackagePrice: 10, productPackageQuantity: 3 });

    expect(eachPrice(result)).toBe(3.3333);
  });

  it('recomputes when the package values change', () => {
    const { result } = renderWithForm({ productPackagePrice: 10, productPackageQuantity: 2 });

    act(() => result.current.setValue('packageSpecification.productPackageQuantity', 4));

    expect(eachPrice(result)).toBe(2.5);
  });

  it.each([
    ['price is missing', { productPackageQuantity: 2 }],
    ['quantity is missing', { productPackagePrice: 5 }],
    ['quantity is zero', { productPackagePrice: 5, productPackageQuantity: 0 }],
  ])('clears the each price when the %s', (label, values) => {
    const { result } = renderWithForm({ ...values, eachPrice: 99 });

    expect(eachPrice(result)).toBe('');
  });
});
