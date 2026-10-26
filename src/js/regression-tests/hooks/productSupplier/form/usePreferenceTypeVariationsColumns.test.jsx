import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { useForm } from 'react-hook-form';

import { FETCH_BUYERS } from 'actions/types';
import { PRODUCT_SUPPLIER_PREFERENCES_BY_ID } from 'api/urls';
import usePreferenceTypeVariationsColumns from 'hooks/productSupplier/form/usePreferenceTypeVariationsColumns';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';

import '@testing-library/jest-dom';

const mockDispatch = jest.fn();
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({
    productSupplier: { preferenceTypes: [{ id: 'pt-1', label: 'Preferred' }] },
    organizations: { buyers: [{ id: 'org-1', label: 'Clinic' }] },
  })),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('utils/confirmationModalUtils', () => jest.fn());
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('utils/Translate', () => ({ defaultMessage }) => <span>{defaultMessage}</span>);
jest.mock('react-tippy', () => ({ Tooltip: ({ children }) => <div>{children}</div> }));
/* eslint-disable react/prop-types */
function mockField(testId) {
  return ({
    name, value, onChange, hasErrors, errorMessage, placeholder, options,
  }) => (
    <div data-testid={`${testId}-${name}`} data-has-errors={String(Boolean(hasErrors))}>
      <span>{errorMessage}</span>
      {placeholder}
      <span data-testid="options">{(options || []).map((option) => option.label).join(',')}</span>
      <span data-testid="value">{JSON.stringify(value ?? null)}</span>
      <button type="button" onClick={() => onChange(`${testId}-new`)}>change</button>
    </div>
  );
}
jest.mock('components/form-elements/v2/SelectField', () => mockField('select'));
jest.mock('components/form-elements/v2/DateField', () => mockField('date'));
jest.mock('components/form-elements/v2/TextInput', () => mockField('text'));
/* eslint-enable react/prop-types */

const rows = [
  { id: 'pref-1', destinationParty: { id: 'org-1' }, preferenceType: null },
  { id: null, comments: '' },
];

const Table = ({ hookProps, onColumns }) => {
  const { control } = useForm({ defaultValues: { productSupplierPreferences: rows } });
  const { columns } = usePreferenceTypeVariationsColumns({ control, ...hookProps });
  onColumns(columns);
  return (
    <div>
      {rows.map((row, index) => (
        <div key={row.id ?? index} data-testid={`row-${index}`}>
          {columns.map(({ Header, Cell }) => (
            <Cell key={Header} index={index} original={row} />
          ))}
        </div>
      ))}
    </div>
  );
};

describe('usePreferenceTypeVariationsColumns', () => {
  let hookProps;
  let columns;

  beforeEach(() => {
    jest.clearAllMocks();
    apiClient.delete.mockResolvedValue({});
    hookProps = {
      errors: {
        0: { preferenceType: { message: 'Preference type is required' }, comments: { message: 'Too long' } },
        1: { destinationParty: { id: { message: 'Organization is required' } } },
      },
      remove: jest.fn(),
      updatedRows: [{ id: 'pref-1', destinationParty: { id: 'org-1' } }, { comments: '' }],
      triggerValidation: jest.fn(),
    };
  });

  const renderTable = () => render(
    <Table hookProps={hookProps} onColumns={(value) => { columns = value; }} />,
  );

  it('fetches the buyer organizations on mount', async () => {
    renderTable();
    apiClient.get.mockResolvedValue({ data: { data: [{ id: 'o1', name: 'Org', displayName: 'Org One' }] } });
    const innerDispatch = jest.fn();

    mockDispatch.mock.calls[0][0](innerDispatch);
    await Promise.resolve();
    await Promise.resolve();

    expect(apiClient.get).toHaveBeenCalledWith('/api/organizations?roleType=ROLE_BUYER&active=false');
    expect(innerDispatch).toHaveBeenCalledWith({
      type: FETCH_BUYERS,
      payload: [{
        id: 'o1', value: 'o1', name: 'Org', label: 'Org One',
      }],
    });
  });

  it('defines the six columns with translated headers', () => {
    renderTable();

    expect(columns.map((column) => column.Header)).toEqual([
      'Organization', 'Preference Type', 'Valid Start Date', 'Valid End Date', 'Bid Name', 'Actions',
    ]);
  });

  it('flags errors only on rows the user has filled in', () => {
    renderTable();

    expect(screen.getByTestId('select-productSupplierPreferences.0.preferenceType'))
      .toHaveAttribute('data-has-errors', 'true');
    expect(screen.getByTestId('select-productSupplierPreferences.0.destinationParty'))
      .toHaveAttribute('data-has-errors', 'false');
    // Row 1 has an organization error, but it is still empty so it is not highlighted
    expect(screen.getByTestId('select-productSupplierPreferences.1.destinationParty'))
      .toHaveAttribute('data-has-errors', 'false');
    expect(screen.getByText('Too long')).toBeInTheDocument();
    expect(screen.getAllByText('Select Organization')).toHaveLength(2);
  });

  it('feeds the select options from the store and binds field values', () => {
    renderTable();
    const organization = screen.getByTestId('select-productSupplierPreferences.0.destinationParty');

    expect(organization).toHaveTextContent('Clinic');
    expect(organization).toHaveTextContent('{"id":"org-1"}');
    expect(screen.getByTestId('select-productSupplierPreferences.0.preferenceType')).toHaveTextContent('Preferred');
  });

  it('updates the field and revalidates the preferences on every change', () => {
    renderTable();
    const fields = [
      'select-productSupplierPreferences.0.destinationParty',
      'select-productSupplierPreferences.0.preferenceType',
      'date-productSupplierPreferences.0.validityStartDate',
      'date-productSupplierPreferences.0.validityEndDate',
      'text-productSupplierPreferences.0.comments',
    ];

    fields.forEach((testId) => {
      fireEvent.click(screen.getByTestId(testId).querySelector('button'));
    });

    expect(hookProps.triggerValidation).toHaveBeenCalledTimes(5);
    expect(hookProps.triggerValidation).toHaveBeenCalledWith('productSupplierPreferences');
    expect(screen.getByTestId('text-productSupplierPreferences.0.comments')).toHaveTextContent('"text-new"');
  });

  it('confirms and deletes a saved row, then removes it from the form', async () => {
    renderTable();

    fireEvent.click(screen.getByTestId('row-0').querySelector('.preference-type-bin'));

    expect(confirmationModal).toHaveBeenCalledTimes(1);
    const { buttons } = confirmationModal.mock.calls[0][0];
    await buttons(jest.fn())[1].onClick();

    expect(apiClient.delete).toHaveBeenCalledWith(PRODUCT_SUPPLIER_PREFERENCES_BY_ID('pref-1'));
    expect(hookProps.remove).toHaveBeenCalledWith(0);
    expect(hookProps.triggerValidation).toHaveBeenCalledWith('productSupplierPreferences');
  });

  it('removes an unsaved row without confirmation', () => {
    renderTable();

    fireEvent.click(screen.getByTestId('row-1').querySelector('.preference-type-bin'));

    expect(confirmationModal).not.toHaveBeenCalled();
    expect(apiClient.delete).not.toHaveBeenCalled();
    expect(hookProps.remove).toHaveBeenCalledWith(1);
  });

  it('lets the user cancel the deletion and pick the same row again', () => {
    renderTable();
    const bin = () => screen.getByTestId('row-0').querySelector('.preference-type-bin');

    fireEvent.click(bin());
    confirmationModal.mock.calls[0][0].buttons(jest.fn())[0].onClick();
    fireEvent.click(bin());

    expect(confirmationModal).toHaveBeenCalledTimes(2);
    expect(hookProps.remove).not.toHaveBeenCalled();
  });
});
