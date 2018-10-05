import React from 'react';
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


const GlobalSearch = () => (
  <div className="global-search ">
    <Select
      async
      placeholder="Search..."
      loadOptions={debouncedGlobalSearch}
      cache={false}
      options={[]}
      showValueTooltip
      filterOptions={options => options}
      onChange={(value) => { window.location = value.url; }}
    />
  </div>
);

export default GlobalSearch;
