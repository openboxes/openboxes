import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import store from 'tests/__mocks__/formStore';

import TextField from 'components/form-elements/TextField';
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

describe('TextField component is correctly rendering', () => {
  it('renders a text input with its label', () => {
    const fieldConfig = {
      type: TextField,
      label: 'test label',
    };

    render(<Provider store={store}>{renderFormField(fieldConfig, 'test-field')}</Provider>);

    expect(screen.getByRole('textbox', { name: 'test label' })).toHaveAttribute('type', 'text');
    expect(screen.getByTestId('input')).toHaveAttribute('id', 'test-field');
  });
});
