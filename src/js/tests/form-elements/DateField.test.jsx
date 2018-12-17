import React from 'react';
import renderer from 'react-test-renderer';
import DateField from '../../components/form-elements/DateField';
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

xdescribe('DateField component is correctly rendering', () => {
  it('renders correctly', () => {
    const fieldConfig = {
      type: DateField,
      label: 'test label',
      attributes: {
        dateFormat: 'YYYY-MM-DD',
        viewDate: '2018-01-01',
      },
    };

    const rendered = renderer.create(renderFormField(fieldConfig, 'test-field'));

    expect(rendered.toJSON()).toMatchSnapshot();
  });
});

