/* eslint-disable no-undef,react/prop-types */

import React from 'react';
import renderer from 'react-test-renderer';
import CheckboxField from '../../components/form-elements/CheckboxField';
import { renderFormField } from '../../utils/form-utils';

jest.mock('redux-form', () => ({
  Field: (props) => {
    const { component: Component, name, ...others } = props;
    const input = { onChange: () => {}, name };
    const meta = {};
    return <Component input={input} meta={meta} {...others} />;
  },
}));

describe('CheckboxField component is correctly rendering', () => {
  it('renders correctly', () => {
    const fieldConfig = {
      type: CheckboxField,
      label: 'test label',
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });
});

