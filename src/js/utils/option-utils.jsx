import _ from 'lodash';
import queryString from 'query-string';
import apiClient from './apiClient';

export const debouncedUsersFetch = _.debounce((searchTerm, callback) => {
  if (searchTerm) {
    apiClient.get(`/openboxes/api/persons?name=${searchTerm}`)
      .then(result => callback(
        null,
        {
          complete: true,
          options: _.map(result.data.data, obj => (
            {
              value: {
                ...obj,
              },
              name: obj.name,
              label: obj.name,
            }
          )),
        },
      ))
      .catch(error => callback(error, { options: [] }));
  } else {
    callback(null, { options: [] });
  }
}, 500);

export const debouncedLocationsFetch = _.debounce((searchTerm, callback) => {
  if (searchTerm) {
    apiClient.get(`/openboxes/api/locations?direction=${queryString.parse(window.location.search).direction}&name=${searchTerm}`)
      .then(result => callback(
        null,
        {
          complete: true,
          options: _.map(result.data.data, obj => (
            {
              value: {
                id: obj.id,
                type: obj.locationType.locationTypeCode,
                name: obj.name,
                label: `${obj.name} [${obj.locationType.description}]`,
              },
              label: `${obj.name} [${obj.locationType.description}]`,
            }
          )),
        },
      ))
      .catch(error => callback(error, { options: [] }));
  } else {
    callback(null, { options: [] });
  }
}, 500);
