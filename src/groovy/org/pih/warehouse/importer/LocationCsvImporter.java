package org.pih.warehouse.importer;

import org.grails.plugins.csv.CSVMapReader;
import org.pih.warehouse.data.LocationDataService;

import java.io.Reader;

public class LocationCsvImporter {

  private final CSVMapReader csvReader;
  private final LocationDataService locationDataService;

  private final String[] fieldKeys = {
      "id",
      "name",
      "active",
      "locationType",
      "organization",
      "parentLocation",
      "locationNumber",
      "locationGroup",
      "streetAddress",
      "streetAddress2",
      "city",
      "stateProvince",
      "postalCode",
      "country",
      "description",
  };

  public LocationCsvImporter(Reader csvReader, LocationDataService locationDataService) {
    this.csvReader = new CSVMapReader(csvReader);
    this.csvReader.setFieldKeys(this.fieldKeys);
    this.locationDataService = locationDataService;
  }

  public void importData() {
    ImportDataCommand command = new ImportDataCommand();
    command.setData(csvReader.readAll());

    locationDataService.validateData(command);
    locationDataService.importData(command);
  }
}
