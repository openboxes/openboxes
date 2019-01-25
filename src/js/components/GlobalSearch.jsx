import React, { Component } from 'react';
import _ from 'lodash';

import Select from '../utils/Select';
import apiClient from '../utils/apiClient';

const debouncedGlobalSearch = _.debounce((searchTerm, callback) => {
  if (searchTerm) {
    apiClient.get(`/openboxes/json/globalSearch?term=${searchTerm}`)
      .then(result => callback(
        null,
        {
          complete: true,
          options: _.map(result.data, obj => (
            {
              value: {
                url: obj.url,
              },
              label: obj.label,
            }
          )),
        },
      ))
      .catch(error => callback(error, { options: [] }));
  } else {
    callback(null, { options: [] });
  }
}, 500);


class GlobalSearch extends Component {
  constructor(props) {
    super(props);

    this.state = {
      inputValue: '',
    };

    this.onInputChange = this.onInputChange.bind(this);
  }

  onInputChange(inputValue) {
    this.setState({ inputValue });

    return inputValue;
  }

  render() {
    return (
      <div className="global-search ">
        <Select
          async
          placeholder="Search..."
          loadOptions={debouncedGlobalSearch}
          cache={false}
          options={[]}
          showValueTooltip
          menuContainerStyle={{ maxHeight: '500px' }}
          menuStyle={{ maxHeight: '500px' }}
          filterOptions={options => options}
          onChange={(value) => {
            window.location = value.url;
          }}
          onInputChange={this.onInputChange}
          onEnterPress={() => {
            window.location = `/openboxes/dashboard/globalSearch?searchTerms=${this.state.inputValue}`;
          }}
        />
      </div>);
  }
}

export default GlobalSearch;
