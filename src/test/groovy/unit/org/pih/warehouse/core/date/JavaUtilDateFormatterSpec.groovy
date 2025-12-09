package unit.org.pih.warehouse.core.date

import java.text.SimpleDateFormat
import java.time.ZoneOffset
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.date.JavaUtilDateFormatter

@Unroll
class JavaUtilDateFormatterSpec extends Specification {

    @Shared
    JavaUtilDateFormatter javaUtilDateFormatter

    void setup() {
        javaUtilDateFormatter = new JavaUtilDateFormatter()
    }

    void 'formatAsDateTime should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert javaUtilDateFormatter.formatAsDateTime(date) == ''
    }

    void 'formatAsDateTime should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()
        Date date = newDate("2000-01-01T00:00" + offset)

        expect:
        assert javaUtilDateFormatter.formatAsDateTime(date) == "01/Jan/2000 00:00:00 " + offset
    }

    void 'formatAsDate should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert javaUtilDateFormatter.formatAsDate(date) == ''
    }

    void 'formatAsDate should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()
        Date date = newDate("2000-01-01T00:00" + offset)

        expect:
        assert javaUtilDateFormatter.formatAsDate(date) == "01/Jan/2000"
    }

    void 'formatAsTime should successfully convert a null Date to a String'() {
        given:
        Date date = null

        expect:
        assert javaUtilDateFormatter.formatAsTime(date) == ''
    }

    void 'formatAsTime should successfully convert a Date to a String'() {
        given: 'a Date in the current timezone offset of the system'
        ZoneOffset offset = DateUtil.getSystemZoneOffset()
        Date date = newDate("2000-01-01T12:34" + offset)

        expect:
        assert javaUtilDateFormatter.formatAsTime(date) == "12:34:00"
    }

    /**
     * Convenience method to build a Date for tests.
     */
    Date newDate(String date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX").parse(date)
    }
}
