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
        fileType      | fields                || expectedResult          | scenario
        FileType.NONE | []                    || ""                      | "Blank without extension"
        FileType.XLS  | []                    || ".xls"                  | "Blank with extension"
        FileType.XLS  | ["a"]                 || "a.xls"                 | "Plain Ascii text"
        FileType.CSV  | ["a"]                 || "a.csv"                 | "Different file type"
        FileType.XLS  | ["a", "b"]            || "a_b.xls"               | "Multiple params"
        FileType.XLS  | ["\\/:*?a\"<>|"]      || "a.xls"                 | "Windows banned characters are removed"
        FileType.XLS  | ["1()&\$#@ +=_-.,'"]  || "1()&\$#@ +=_-.,'.xls"  | "Other special characters are not removed"
        FileType.XLS  | [Instant.now()]       || "2000-01-01.xls"        | "using Instants"
        FileType.XLS  | [LocalDate.now()]     || "2000-01-01.xls"        | "using LocalDates"
        FileType.XLS  | [ZonedDateTime.now()] || "2000-01-01.xls"        | "using ZonedDateTimes"
        FileType.XLS  | [new Date()]          || "2000-01-01.xls"        | "using Java.util.Date"
        FileType.XLS  | ["你好"]               || "你好.xls"              | "Unicode characters"
    }
}
