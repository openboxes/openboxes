import React from 'react';

import Badge from 'utils/Badge';

const NewTable = () => {
  const data = [{
    tags: ['Vital', 'Oral'],
    productCatalogue: ['Sierra Leone Formulary', 'Malawi Formulary', 'ZL Formulary', 'NCD', 'Global Formulary'],
  }, {
    tags: ['Vital', 'Oral'],
    productCatalogue: ['Sierra Leone Formulary', 'Malawi Formulary', 'ZL Formulary', 'NCD', 'Global Formulary'],
  }];

  return (
    <div>
      {data.map((item, dataIndex) => item.tags.map((tag, tagIndex) => (
        // eslint-disable-next-line react/no-array-index-key
        <Badge key={`${dataIndex}-${tagIndex}`} label={tag} variant="light-purple" />)))}

      {data.map((item, dataIndex) => item.productCatalogue.map((cat, catIndex) => (
        // eslint-disable-next-line react/no-array-index-key
        <Badge key={`${dataIndex}-${catIndex}`} label={cat} variant="light-blue" />)))}
    </div>
  );
};

export default NewTable;
