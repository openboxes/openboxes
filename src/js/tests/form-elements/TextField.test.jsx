import React from 'react';
import renderer from 'react-test-renderer';
import TextField from '../../components/form-elements/TextField';
import { renderFormField } from '../../utils/form-utils';

jest.mock('react-final-form', () => ({
  Field: (props) => {
    const { component: Component, name, ...others } = props;
    const input = { onChange: () => {}, name };
    const meta = {};
    return <Component input={input} meta={meta} {...others} />;
  },
}));

jest.mock('react-localize-redux', () => ({
  Translate: (props) => {
    const { id } = props;

    return `${id}`;
  },
}));

xdescribe('TextField component is correctly rendering', () => {
  it('renders correctly', () => {
    const fieldConfig = {
      type: TextField,
      label: 'test label',
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });
});

