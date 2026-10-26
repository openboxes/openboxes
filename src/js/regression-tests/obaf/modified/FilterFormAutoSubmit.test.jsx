// OBAF-modified: FilterForm's autoSubmitOnFilterChange submits as soon as a filter value changes,
// but never for the free-text search field (that still waits for Search or Enter).
import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Field } from 'react-final-form';
import { Provider } from 'react-redux';
import { answer, sessionStore } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

/* eslint-disable import/first,import/order */
import FilterForm from 'components/Filter/FilterForm';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

// eslint-disable-next-line react/prop-types
const PlainField = ({ fieldName }) => (
  <Field name={fieldName}>
    {({ input }) => <input aria-label={fieldName} {...input} value={input.value || ''} />}
  </Field>
);

const filterFields = { color: { type: PlainField, attributes: {} } };

const renderForm = (props = {}) => {
  const updateFilterParams = jest.fn();
  render(
    <Provider store={sessionStore()}>
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={updateFilterParams}
        defaultValues={{ color: '' }}
        searchFieldId="searchTerm"
        searchFieldPlaceholder="react.test.search"
        searchFieldDefaultPlaceholder="Search things"
        allowEmptySubmit
        hidden={false}
        {...props}
      />
    </Provider>,
  );
  return { updateFilterParams };
};

const submittedValues = (fn) => fn.mock.calls.map(([values]) => values);

describe('FilterForm autoSubmitOnFilterChange (OBAF)', () => {
  beforeEach(() => answer(apiClient.get));
  afterEach(() => jest.clearAllMocks());

  it('submits when a filter value changes', () => {
    const { updateFilterParams } = renderForm({ autoSubmitOnFilterChange: true });
    updateFilterParams.mockClear();

    fireEvent.change(screen.getByLabelText('color'), { target: { value: 'red' } });

    expect(submittedValues(updateFilterParams)).toEqual([{ color: 'red' }]);
  });

  it('does not submit when only the search field changes', () => {
    const { updateFilterParams } = renderForm({ autoSubmitOnFilterChange: true });
    updateFilterParams.mockClear();

    fireEvent.change(screen.getByPlaceholderText('Search things'), { target: { value: 'abc' } });
    expect(updateFilterParams).not.toHaveBeenCalled();

    fireEvent.click(screen.getByText('Search').closest('button'));
    expect(submittedValues(updateFilterParams)).toEqual([{ color: '', searchTerm: 'abc' }]);
  });

  it('keeps the classic behaviour without the flag', () => {
    const { updateFilterParams } = renderForm();
    updateFilterParams.mockClear();

    fireEvent.change(screen.getByLabelText('color'), { target: { value: 'red' } });
    expect(updateFilterParams).not.toHaveBeenCalled();
  });
});
