import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import store from 'tests/__mocks__/formStore';

import BaseField from 'components/form-elements/BaseField';

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

describe('BaseField component is correctly rendering', () => {
  it('text label', () => {
    const props = {
      fieldName: 'test',
      fieldConfig: {
        label: 'test label',
      },
      renderInput: (attr) => (<input id={attr.id} />),
    };

    render(<Provider store={store}><BaseField {...props} /></Provider>);

    expect(screen.getByRole('textbox', { name: 'test label' })).toBeInTheDocument();
    expect(screen.getByTestId('form-field')).toHaveAttribute('aria-label', 'test label');
  });

  it('component label', () => {
    const props = {
      fieldName: 'test',
      fieldConfig: {
        label: () => (<span>test</span>),
      },
      renderInput: () => (<input />),
    };

    render(<Provider store={store}><BaseField {...props} /></Provider>);

    expect(screen.getByText('test').tagName).toBe('SPAN');
    expect(screen.getByRole('textbox')).toBeInTheDocument();
  });

  it('attributes', () => {
    const props = {
      fieldName: 'test',
      fieldConfig: {
        label: 'test label',
        attributes: {
          required: true,
          placeholder: 'test placeholder',
        },
        getDynamicAttr: ({ dynamicProp }) => ({ hidden: true, title: dynamicProp }),
      },
      dynamicProp: 'dynamic property',
      renderInput: (attr) => (<input {...attr} />),
    };

    render(<Provider store={store}><BaseField {...props} /></Provider>);

    const formField = screen.getByTestId('form-field');
    expect(formField).toHaveClass('required', 'd-none');
    const input = formField.querySelector('input');
    expect(input).toHaveAttribute('id', 'test');
    expect(input).toHaveAttribute('placeholder', 'test placeholder');
    expect(input).toHaveAttribute('title', 'dynamic property');
  });
});
