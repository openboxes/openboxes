import { renderHook } from '@testing-library/react-hooks';

import useProductSupplierValidation from 'hooks/productSupplier/form/useProductSupplierValidation';

jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({
    productSupplier: {
      attributes: [
        {
          id: 'color', name: 'Color', options: ['Red'], allowOther: false, required: true,
        },
        {
          id: 'note', name: 'Note', options: [], allowOther: true, required: true,
        },
      ],
    },
  })),
  useDispatch: () => jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('components/form-elements/v2/SelectField', () => 'SelectField');
jest.mock('components/form-elements/v2/TextInput', () => 'TextInput');

const option = (id) => ({ id, value: id, label: id });

const validValues = () => ({
  basicDetails: {
    product: option('product'),
    supplier: option('supplier'),
    name: 'Source name',
    active: true,
  },
  additionalDetails: { manufacturer: null, brandName: 'Brand' },
  defaultPreferenceType: {},
  productSupplierPreferences: [
    { destinationParty: option('org-1'), preferenceType: option('pt') },
    { destinationParty: option('org-2'), preferenceType: option('pt'), comments: 'bid' },
  ],
  packageSpecification: { uom: option('EA'), productPackageQuantity: 1 },
  fixedPrice: { contractPricePrice: 3, tieredPricing: false },
  attributes: { color: { value: 'Red', label: 'Red' }, note: 'text' },
});

const issues = (values) => {
  const { result } = renderHook(() => useProductSupplierValidation());
  const parsed = result.current.validationSchema(values).safeParse(values);
  if (parsed.success) {
    return {};
  }
  return parsed.error.issues.reduce((acc, issue) => ({ ...acc, [issue.path.join('.')]: issue.message }), {});
};

describe('useProductSupplierValidation', () => {
  it('accepts a complete product source', () => {
    expect(issues(validValues())).toEqual({});
  });

  it('requires product, supplier, name, package and the required attributes', () => {
    const values = validValues();
    values.basicDetails = { name: '', active: true };
    values.packageSpecification = { uom: null };
    values.attributes = { note: '  ' };

    expect(issues(values)).toEqual({
      'basicDetails.product': 'Product is required',
      'basicDetails.supplier': 'Supplier is required',
      'basicDetails.name': '(Source) Name is required',
      'packageSpecification.uom': 'Default Source Package is required',
      'packageSpecification.productPackageQuantity': 'Package size is required',
      'attributes.color': 'Color is required',
      'attributes.note': 'Note is required',
    });
  });

  it('rejects duplicated organizations in the preference variations', () => {
    const values = validValues();
    values.productSupplierPreferences[1].destinationParty = option('org-1');

    expect(issues(values)).toEqual({
      'productSupplierPreferences.0.destinationParty': 'Destination party should be unique',
      'productSupplierPreferences.1.destinationParty': 'Destination party should be unique',
    });
  });

  it('requires the organization and preference type on every variation', () => {
    const values = validValues();
    values.productSupplierPreferences = [{ comments: 'x'.repeat(256) }];

    expect(issues(values)).toEqual({
      'productSupplierPreferences.0.destinationParty': 'Organization is required',
      'productSupplierPreferences.0.preferenceType': 'Preference type is required',
      'productSupplierPreferences.0.comments': 'Max length of bid name is 255',
    });
  });

  it('requires a default preference type once any of its other fields is filled', () => {
    const values = validValues();
    values.defaultPreferenceType = { comments: 'note' };

    expect(issues(values)).toEqual({
      'defaultPreferenceType.preferenceType': 'Default preference type must also be selected',
    });

    values.defaultPreferenceType.preferenceType = option('pt');
    expect(issues(values)).toEqual({});
  });

  it('requires a contract price when a valid-until date is set', () => {
    const values = validValues();
    values.fixedPrice = { contractPriceValidUntil: '2026-01-01' };

    expect(issues(values)).toEqual({ 'fixedPrice.contractPricePrice': 'Contract price is required' });

    values.fixedPrice = {};
    expect(issues(values)).toEqual({});
  });

  it('rejects package quantities below one', () => {
    const values = validValues();
    values.packageSpecification.productPackageQuantity = 0;
    values.packageSpecification.minOrderQuantity = 0;

    expect(Object.keys(issues(values))).toEqual([
      'packageSpecification.productPackageQuantity',
      'packageSpecification.minOrderQuantity',
    ]);
  });
});
