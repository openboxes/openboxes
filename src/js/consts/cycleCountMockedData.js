const data = [
  {
    // other properties
    lastCountDate: '12/11/2024',
    product: {
      // other properties
      productCode: '10002',
      name: 'Methyldopa, 250mg film coated tablet',
      category: 'DRUG: Cardiovascular System',
      tags: ['Vital', 'Oral', 'Essential', 'Medication'],
      catalogs: ['ZL Formulary', 'MNH', 'Liberia Formulary'],
      abcClass: 'A',
      quantity: 2832,
    },
    binLocation: {
      // other properties
      name: 'ZL-01-DD-4A',
    },
  },
  {
    // other properties
    lastCountDate: '12/09/2024',
    product: {
      // other properties
      productCode: '10009',
      name: 'Tape, Identification, for surgical instruments, Blue, 0.25in x 250in roll',
      category: 'MedSupply: Surgical',
      tags: ['Vital', 'Oral', 'Essential', 'Medication'],
      catalogs: ['Sierra Leone Formularity Formulary', 'Liberia Formulary'],
      abcClass: 'A',
      quantity: 322,
    },
    binLocation: {
      // other properties
      name: 'R-640FNQ',
    },
  },
  {
    // other properties
    lastCountDate: '',
    product: {
      // other properties
      productCode: '10008',
      name: 'Arm sling, with shoulder strap, Medium',
      category: 'DefaultMedSupply: Rehabilitation',
      tags: ['Vital', 'Oral', 'Essential', 'Medication'],
      catalogs: ['Sierra Leone Formularity Formulary', 'MNH', 'Liberia Formulary'],
      abcClass: 'C',
      quantity: 421,
    },
    binLocation: {
      // other properties
      name: 'ZL-01-DD-4A',
    },
  },
  {
    // other properties
    lastCountDate: '12/11/2024',
    product: {
      // other properties
      productCode: '10002',
      name: 'Methyldopa, 250mg film coated tablet',
      category: 'DRUG: Cardiovascular System',
      tags: ['Vital', 'Oral', 'Essential', 'Medication'],
      catalogs: ['ZL Formulary', 'MNH', 'Liberia Formulary'],
      abcClass: 'A',
      quantity: 2832,
    },
    binLocation: {
      // other properties
      name: 'ZL-01-DD-4A',
    },
  },
  {
    // other properties
    lastCountDate: '12/09/2024',
    product: {
      // other properties
      productCode: '10009',
      name: 'Tape, Identification, for surgical instruments, Blue, 0.25in x 250in roll',
      category: 'MedSupply: Surgical',
      tags: ['Vital', 'Oral', 'Essential', 'Medication'],
      catalogs: ['Sierra Leone Formularity Formulary', 'Liberia Formulary'],
      abcClass: 'A',
      quantity: 322,
    },
    binLocation: {
      // other properties
      name: 'R-640FNQ',
    },
  },
  {
    // other properties
    lastCountDate: '',
    product: {
      // other properties
      productCode: '10008',
      name: 'Arm sling, with shoulder strap, Medium',
      category: 'DefaultMedSupply: Rehabilitation',
      tags: ['Vital', 'Oral', 'Essential', 'Medication'],
      catalogs: ['Sierra Leone Formularity Formulary', 'MNH', 'Liberia Formulary'],
      abcClass: 'C',
      quantity: 421,
    },
    binLocation: {
      // other properties
      name: 'ZL-01-DD-4A',
    },
  },
];

const csvData = `'lastCountDate','product__productCode','product__name','product__category','product__tags__001','product__tags__002','product__tags__003','product__tags__004','product__catalogs__001','product__catalogs__002','product__catalogs__003','product__abcClass','product__quantity','binLocation__name'
'12/11/2024','10002','Methyldopa, 250mg film coated tablet','DRUG: Cardiovascular System','Vital','Oral','Essential','Medication','ZL Formulary','MNH','Liberia Formulary','A','2832','ZL-01-DD-4A'
'12/09/2024','10009','Tape, Identification, for surgical instruments, Blue, 0.25in x 250in roll','MedSupply: Surgical','Vital','Oral','Essential','Medication','Sierra Leone Formularity Formulary','Liberia Formulary','','A','322','R-640FNQ'
'','10008','Arm sling, with shoulder strap, Medium','DefaultMedSupply: Rehabilitation','Vital','Oral','Essential','Medication','Sierra Leone Formularity Formulary','MNH','Liberia Formulary','C','421','ZL-01-DD-4A'
'12/11/2024','10002','Methyldopa, 250mg film coated tablet','DRUG: Cardiovascular System','Vital','Oral','Essential','Medication','ZL Formulary','MNH','Liberia Formulary','A','2832','ZL-01-DD-4A'
'12/09/2024','10009','Tape, Identification, for surgical instruments, Blue, 0.25in x 250in roll','MedSupply: Surgical','Vital','Oral','Essential','Medication','Sierra Leone Formularity Formulary','Liberia Formulary','','A','322','R-640FNQ'
'','10008','Arm sling, with shoulder strap, Medium','DefaultMedSupply: Rehabilitation','Vital','Oral','Essential','Medication','Sierra Leone Formularity Formulary','MNH','Liberia Formulary','C','421','ZL-01-DD-4A'`;

export default {
  data,
  csvData,
};
