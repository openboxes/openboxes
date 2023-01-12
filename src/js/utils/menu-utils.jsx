import _ from 'lodash';
import queryString from 'query-string';

export const checkActiveSection = ({
  menuUrls,
  path,
  params,
}) => {
  const { pathname, search } = path;
  // removing custom params from URL fe. stockMovementId
  const pathParams = _.drop(Object.values(params), 2);
  const pathnameWithoutParams =
    pathParams
      .reduce((acc, param) => acc.replace(param, ''), pathname)
      .replace(/\/$/, '');
  const matchedPath = Object.keys(menuUrls)
    .find((section) => {
      // find matching URL from sections
      const foundURL = menuUrls[section].find((url) => {
        const [sectionPath, sectionSearch] = url.split('?');
        // if current pathname is longer than section path and pathname doesn't end with '/'
        // it means that we didn't match the whole url section
        if (pathnameWithoutParams.length > sectionPath.length && pathnameWithoutParams[sectionPath.length] !== '/') {
          return false;
        }
        // if current pathname doesn't contain section path
        // it means that pathname doesn't match section
        if (!pathnameWithoutParams.includes(sectionPath.replace(/\/index$/, ''))) {
          return false;
        }
        // if found matching pathname
        // then check if all parameters of section path match with current path parameters
        if (!_.isEmpty(sectionSearch)) {
          const {
            direction,
            ...otherParams
          } = queryString.parse(search.substring(1, search.length));
          // if direction is not specified
          // then compare current url with sectionPath without direction
          if (!direction) {
            return Object.values(otherParams).every(param => sectionSearch.includes(param));
          }
          return sectionSearch.split('&').every(param => search.includes(param));
        }
        return true;
      });
      return !!foundURL;
    });
  if (pathname.includes('partialReceiving')) {
    return 'Inbound';
  }

  return matchedPath || 'Dashboard';
};

export const getAllMenuUrls = menuConfig => Object.entries(menuConfig)
  .reduce((acc, [, section]) => {
    if (!acc[section.label]) {
      if (section.href) {
        return {
          ...acc,
          [section.id]: [section.href],
        };
      }
      if (section.subsections) {
        return {
          ...acc,
          // eslint-disable-next-line max-len
          [section.label]: section.subsections.flatMap(subsection => subsection.menuItems.map(item => item.href)),
        };
      }
      if (section.menuItems) {
        return {
          ...acc,
          [section.label]: section.menuItems.flatMap(({ href }) => href),
        };
      }
    }
    return acc;
  }, {});
