import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import store from 'tests/__mocks__/formStore';

import DateField from 'components/form-elements/DateField';
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

describe('DateField component is correctly rendering', () => {
  it('renders an empty date picker input with its label', () => {
    const fieldConfig = {
      type: DateField,
      label: 'test label',
      attributes: {
        dateFormat: 'YYYY-MM-DD',
        viewDate: '2018-01-01',
      },
    };

    const { container } = render(
      <Provider store={store}>{renderFormField(fieldConfig, 'test-field')}</Provider>,
    );

    expect(screen.getByText('test label')).toBeInTheDocument();
    const dateInput = container.querySelector('.react-datepicker__input-container input');
    expect(dateInput).toHaveAttribute('type', 'text');
    expect(dateInput).toHaveValue('');
  });
});
