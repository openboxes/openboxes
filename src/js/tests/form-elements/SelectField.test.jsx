import React from 'react';
import renderer from 'react-test-renderer';
import SelectField from '../../components/form-elements/SelectField';
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

xdescribe('SelectField component is correctly rendering', () => {
  it('renders correctly', () => {
    const fieldConfig = {
      type: SelectField,
      label: 'test label',
      attributes: {
        options: ['One', 'Two'],
      },
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });
});

