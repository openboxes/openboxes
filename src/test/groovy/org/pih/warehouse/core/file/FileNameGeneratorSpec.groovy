package org.pih.warehouse.core.file

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateFormatter

@Unroll
class FileNameGeneratorSpec extends Specification {

    @Shared
    FileNameGenerator fileNameGenerator

    void setup() {
        fileNameGenerator = new FileNameGenerator()
        fileNameGenerator.dateFormatter = Stub(DateFormatter) {
            formatForFileName(_) >> "2000-01-01"
        }
    }

    void "generate builds a file name as expected for scenario: #scenario"() {
        expect:
        assert fileNameGenerator.generate(fileType, fields) == expectedResult

        where:
        fileType           | fields                || expectedResult          | scenario
        FileExtension.NONE | []                    || ""                     | "Blank without extension"
        FileExtension.XLS  | []                    || ".xls"                 | "Blank with extension"
        FileExtension.XLS  | ["a"]                 || "a.xls"                | "Plain Ascii text"
        FileExtension.CSV  | ["a"]                 || "a.csv"                | "Different file type"
        FileExtension.XLS  | ["a", "b"]            || "a_b.xls"              | "Multiple params"
        FileExtension.XLS  | ["\\/:*?a\"<>|"]      || "a.xls"                | "Windows banned characters are removed"
        FileExtension.XLS  | ["a", "*", "b"]       || "a_b.xls"              | "Field with all banned characters middle"
        FileExtension.XLS  | ["*", "b"]            || "b.xls"                | "Field with all banned characters first"
        FileExtension.XLS  | ["a", "*"]            || "a.xls"                | "Field with all banned characters last"
        FileExtension.XLS  | ["1()&\$#@ +=_-.,'"]  || "1()&\$#@ +=_-.,'.xls" | "Other special characters aren't removed"
        FileExtension.XLS  | [Instant.now()]       || "2000-01-01.xls"       | "using Instants"
        FileExtension.XLS  | [LocalDate.now()]     || "2000-01-01.xls"       | "using LocalDates"
        FileExtension.XLS  | [ZonedDateTime.now()] || "2000-01-01.xls"       | "using ZonedDateTimes"
        FileExtension.XLS  | [new Date()]          || "2000-01-01.xls"       | "using Java.util.Date"
        FileExtension.XLS  | ["你好"]               || "你好.xls"             | "Unicode characters"
        FileExtension.XLS  | ["a​b"]            || "a​b.xls"           | "OBS-1960: Zero width joiner"

    }
}
