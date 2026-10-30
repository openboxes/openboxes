import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import store from 'tests/__mocks__/formStore';

import CheckboxField from 'components/form-elements/CheckboxField';
import { renderFormField } from 'utils/form-utils';

import '@testing-library/jest-dom';

jest.mock('react-final-form', () => ({
  Field: (props) => {
    const { component: Component, name, ...others } = props;
    const input = { onChange: () => {}, name };
    const meta = {};
    return <Component input={input} meta={meta} {...others} />;
  },
}));

jest.mock('react-localize-redux', () => ({
  ...jest.requireActual('react-localize-redux'),
  Translate: (props) => {
    const { id } = props;

    return `${id}`;
  },
}));

describe('CheckboxField component is correctly rendering', () => {
  it('renders an unchecked checkbox with its label', () => {
    const fieldConfig = {
      type: CheckboxField,
      label: 'test label',
    };

    render(<Provider store={store}>{renderFormField(fieldConfig, 'test-field')}</Provider>);

    const checkbox = screen.getByRole('checkbox', { name: 'test label' });
    expect(checkbox).not.toBeChecked();
  });
});
