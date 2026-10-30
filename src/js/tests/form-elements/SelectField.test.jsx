import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import store from 'tests/__mocks__/formStore';

import SelectField from 'components/form-elements/SelectField';
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

describe('SelectField component is correctly rendering', () => {
  it('renders a react-select with its label and no value selected', () => {
    const fieldConfig = {
      type: SelectField,
      label: 'test label',
      attributes: {
        options: ['One', 'Two'],
      },
    };

    const { container } = render(
      <Provider store={store}>{renderFormField(fieldConfig, 'test-field')}</Provider>,
    );

    expect(screen.getByText('test label')).toBeInTheDocument();
    expect(container.querySelector('.select-xs .react-select__control')).toBeInTheDocument();
    expect(container.querySelector('.react-select__single-value')).not.toBeInTheDocument();
  });
});
