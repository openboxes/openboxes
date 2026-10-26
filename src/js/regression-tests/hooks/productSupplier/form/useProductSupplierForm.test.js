import { act, renderHook } from '@testing-library/react-hooks';

import {
  GENERIC_API,
  PREFERENCE_TYPE_OPTIONS,
  PRODUCT_PACKAGE_API,
  PRODUCT_SUPPLIER_API,
  PRODUCT_SUPPLIER_ATTRIBUTE_BATCH,
  PRODUCT_SUPPLIER_BY_ID,
  PRODUCT_SUPPLIER_PREFERENCE_BATCH,
  RATING_TYPE_OPTIONS,
} from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useProductSupplierForm from 'hooks/productSupplier/form/useProductSupplierForm';
import useQueryParams from 'hooks/useQueryParams';
import apiClient from 'utils/apiClient';

const mockDispatch = jest.fn();
const mockHistory = { push: jest.fn() };
let mockParams;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({
    session: { currentLocation: { id: 'loc' }, activeLanguage: 'en' },
    productSupplier: {
      attributes: [{
        id: 'color', name: 'Color', options: ['Red'], allowOther: false,
      }, {
        id: 'note', name: 'Note', options: [], allowOther: true,
      }],
    },
  })),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: () => mockParams,
  useHistory: () => mockHistory,
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('hooks/useTranslateWithRedirect', () => () => ({ defaultLabel }) => defaultLabel);
jest.mock('hooks/useQueryParams', () => jest.fn(() => ({})));
const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockShowNotification));
jest.mock('components/form-elements/v2/SelectField', () => 'SelectField');
jest.mock('components/form-elements/v2/TextInput', () => 'TextInput');

const flushPromises = async () => {
  for (let i = 0; i < 30; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const fetchedSupplier = {
  id: 'ps-1',
  code: 'PS1',
  product: { id: 'prod-1', productCode: 'P1', name: 'Gloves' },
  productCode: 'SUP-P1',
  supplier: { id: 'org-1', displayName: 'Acme' },
  supplierCode: 'AC',
  name: 'Acme gloves',
  active: false,
  createdBy: { id: 'u1', name: 'Ann' },
  manufacturer: { id: 'org-2', displayName: 'Maker' },
  ratingTypeCode: 'GOOD',
  manufacturerCode: 'MC',
  brandName: 'Brand',
  defaultProductPackage: { uom: { id: 'BX', name: 'Box' }, quantity: 10, productPrice: { price: 25 } },
  minOrderQuantity: 2,
  eachPrice: 2.5,
  contractPrice: { price: 20, validUntil: '2027-01-01' },
  tieredPricing: true,
  attributes: [{ attribute: { id: 'color' }, value: 'Red' }, { attribute: { id: 'note' }, value: 'soft' }],
  productSupplierPreferences: [
    { id: 'pref-default', preferenceType: { id: 'pt-1', name: 'Preferred' }, comments: 'default' },
    {
      id: 'pref-2',
      destinationParty: { id: 'org-9', displayName: 'Clinic' },
      preferenceType: { id: 'pt-2', name: 'Backup' },
    },
  ],
};

const submitValues = () => ({
  basicDetails: {
    product: { id: 'prod-1' }, supplier: { id: 'org-1' }, name: 'Acme gloves', active: true, code: '',
  },
  additionalDetails: { manufacturer: null, ratingTypeCode: { id: 'GOOD' }, brandName: 'Brand' },
  defaultPreferenceType: {
    id: 'pref-default', preferenceType: { id: 'pt-1' }, validityStartDate: '2026-01-02T00:00:00',
  },
  productSupplierPreferences: [{
    id: 'pref-2',
    destinationParty: { id: 'org-9' },
    preferenceType: { id: 'pt-2' },
    validityEndDate: '2026-12-31T00:00:00',
  }],
  packageSpecification: {
    uom: { id: 'BX' }, productPackageQuantity: 10, productPackagePrice: 25, minOrderQuantity: undefined,
  },
  fixedPrice: { contractPricePrice: 20, contractPriceValidUntil: '2027-01-01T00:00:00', tieredPricing: true },
  attributes: { color: { value: 'Red' }, note: 'soft', empty: undefined },
});

describe('useProductSupplierForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockParams = {};
    useQueryParams.mockReturnValue({});
    apiClient.post.mockResolvedValue({ data: { data: { id: 'ps-new', code: 'PS9' } } });
    apiClient.put.mockResolvedValue({ data: { data: { id: 'ps-1', code: 'PS1' } } });
  });

  it('starts an empty active form and fetches rating types, preference types and attributes', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [] } });
    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();

    expect(result.current.getValues()).toEqual(expect.objectContaining({
      basicDetails: { active: true },
      fixedPrice: { tieredPricing: false },
      productSupplierPreferences: [],
    }));

    await Promise.all(mockDispatch.mock.calls
      .filter(([action]) => typeof action === 'function')
      .map(([thunk]) => thunk(jest.fn())));
    const urls = apiClient.get.mock.calls.map(([url]) => url);
    expect(urls).toEqual(expect.arrayContaining([RATING_TYPE_OPTIONS, PREFERENCE_TYPE_OPTIONS]));
  });

  it('prefills the product when the page is opened for a product', async () => {
    useQueryParams.mockReturnValue({ productId: 'prod-7' });
    apiClient.get.mockResolvedValue({ data: { data: { id: 'prod-7', productCode: 'P7', name: 'Mask' } } });

    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(`${GENERIC_API}/product/prod-7`);
    expect(result.current.getValues('basicDetails')).toEqual({
      active: true,
      product: { id: 'prod-7', value: 'prod-7', label: 'P7 - Mask' },
    });
  });

  it('maps a fetched product source into form values when editing', async () => {
    mockParams = { productSupplierId: 'ps-1' };
    apiClient.get.mockResolvedValue({ data: { data: fetchedSupplier } });

    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(PRODUCT_SUPPLIER_BY_ID('ps-1'));
    const values = result.current.getValues();
    expect(values.id).toBe('ps-1');
    expect(values.basicDetails).toEqual(expect.objectContaining({
      code: 'PS1',
      product: { id: 'prod-1', value: 'prod-1', label: 'P1 - Gloves' },
      supplier: { id: 'org-1', value: 'org-1', label: 'Acme' },
      active: false,
      createdBy: { id: 'u1', name: 'Ann' },
      updatedBy: { id: undefined, name: undefined },
    }));
    expect(values.additionalDetails).toEqual({
      manufacturer: { id: 'org-2', value: 'org-2', label: 'Maker' },
      ratingTypeCode: { id: 'GOOD', value: 'GOOD', label: 'GOOD' },
      manufacturerCode: 'MC',
      brandName: 'Brand',
    });
    expect(values.productSupplierPreferences).toEqual([expect.objectContaining({
      id: 'pref-2',
      destinationParty: { id: 'org-9', label: 'Clinic', value: 'org-9' },
      preferenceType: { id: 'pt-2', label: 'Backup', value: 'pt-2' },
    })]);
    expect(values.defaultPreferenceType).toEqual(expect.objectContaining({
      id: 'pref-default',
      preferenceType: { id: 'pt-1', label: 'Preferred', value: 'pt-1' },
      comments: 'default',
    }));
    expect(values.packageSpecification).toEqual(expect.objectContaining({
      uom: { id: 'BX', value: 'BX', label: 'Box' },
      productPackageQuantity: 10,
      minOrderQuantity: 2,
      productPackagePrice: 25,
    }));
    expect(values.fixedPrice).toEqual({
      contractPricePrice: 20, contractPriceValidUntil: '2027-01-01', tieredPricing: true,
    });
    expect(values.attributes).toEqual({ color: { id: 'color', value: 'Red', label: 'Red' }, note: 'soft' });
  });

  it('leaves optional relations empty for a sparse product source', async () => {
    mockParams = { productSupplierId: 'ps-2' };
    apiClient.get.mockResolvedValue({
      data: { data: { id: 'ps-2', attributes: [], productSupplierPreferences: [] } },
    });

    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();

    const values = result.current.getValues();
    expect(values.basicDetails.supplier).toBeUndefined();
    expect(values.additionalDetails.manufacturer).toBeUndefined();
    expect(values.packageSpecification.uom).toBeUndefined();
    expect(values.defaultPreferenceType.preferenceType).toBeUndefined();
  });

  it('creates a product source and saves package, preferences and attributes for it', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [] } });
    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();

    await result.current.onSubmit(submitValues());

    expect(apiClient.post).toHaveBeenNthCalledWith(1, PRODUCT_SUPPLIER_API, {
      product: 'prod-1',
      supplier: 'org-1',
      name: 'Acme gloves',
      active: true,
      manufacturer: null,
      ratingTypeCode: 'GOOD',
      brandName: 'Brand',
      tieredPricing: true,
    });
    expect(apiClient.post).toHaveBeenNthCalledWith(2, PRODUCT_PACKAGE_API, {
      uom: 'BX',
      productPackageQuantity: 10,
      productPackagePrice: 25,
      contractPricePrice: 20,
      contractPriceValidUntil: '01/01/2027',
      tieredPricing: true,
      productSupplier: 'ps-new',
    });
    expect(apiClient.post).toHaveBeenNthCalledWith(3, PRODUCT_SUPPLIER_PREFERENCE_BATCH, {
      productSupplierPreferences: [
        {
          id: 'pref-default',
          preferenceType: 'pt-1',
          validityStartDate: '01/02/2026',
          validityEndDate: null,
          productSupplier: 'ps-new',
        },
        {
          id: 'pref-2',
          destinationParty: { id: 'org-9' },
          preferenceType: 'pt-2',
          validityStartDate: null,
          validityEndDate: '12/31/2026',
          productSupplier: 'ps-new',
        },
      ],
    });
    expect(apiClient.post).toHaveBeenNthCalledWith(4, PRODUCT_SUPPLIER_ATTRIBUTE_BATCH, {
      productAttributes: [
        { attribute: 'color', productSupplier: 'ps-new', value: 'Red' },
        { attribute: 'note', productSupplier: 'ps-new', value: 'soft' },
        { attribute: 'empty', productSupplier: 'ps-new', value: '' },
      ],
    });
    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
    expect(mockShowNotification).toHaveBeenCalledWith({
      message: 'Product source PS9 has been created successfully',
    });
    expect(mockHistory.push).toHaveBeenCalledWith(expect.stringMatching(/\/productSupplier\/list$/));
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SHOW_SPINNER', payload: true });
    expect(mockDispatch).toHaveBeenLastCalledWith({ type: 'HIDE_SPINNER', payload: false });
  });

  it('updates an existing source and skips the preference request when no preference is filled', async () => {
    mockParams = { productSupplierId: 'ps-1' };
    apiClient.get.mockResolvedValue({ data: { data: fetchedSupplier } });
    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();
    const values = submitValues();
    values.defaultPreferenceType = { id: 'pref-default' };
    values.productSupplierPreferences = [];
    values.fixedPrice = { tieredPricing: false };

    await result.current.onSubmit(values);

    expect(apiClient.put).toHaveBeenCalledWith(PRODUCT_SUPPLIER_BY_ID('ps-1'), expect.objectContaining({
      product: 'prod-1',
      tieredPricing: false,
    }));
    expect(apiClient.post.mock.calls.map(([url]) => url)).toEqual([
      PRODUCT_PACKAGE_API,
      PRODUCT_SUPPLIER_ATTRIBUTE_BATCH,
    ]);
    expect(apiClient.post.mock.calls[0][1]).toEqual(expect.objectContaining({
      contractPricePrice: null,
      contractPriceValidUntil: null,
    }));
    expect(mockShowNotification).toHaveBeenCalledWith({
      message: 'Product source PS1 has been updated successfully',
    });
  });

  it('hides the spinner and does not redirect when saving fails', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [] } });
    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();
    apiClient.post.mockRejectedValueOnce(new Error('save failed'));

    await expect(result.current.onSubmit(submitValues())).rejects.toThrow('save failed');

    expect(mockHistory.push).not.toHaveBeenCalled();
    expect(mockDispatch).toHaveBeenLastCalledWith({ type: 'HIDE_SPINNER', payload: false });
  });

  it('sets the package size to one for the each unit and clears it otherwise', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [] } });
    const { result } = renderHook(() => useProductSupplierForm());
    await flushPromises();

    act(() => result.current.setProductPackageQuantity({ id: 'EA' }));
    expect(result.current.getValues('packageSpecification.productPackageQuantity')).toBe(1);

    act(() => result.current.setProductPackageQuantity({ id: 'BX' }));
    expect(result.current.getValues('packageSpecification.productPackageQuantity')).toBe('');
  });
});
