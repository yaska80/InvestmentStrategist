
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
@GrabExclude('org.codehaus.groovy:groovy')
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT

@groovy.lang.Grab(group='joda-time', module='joda-time', version='2.1')
import org.joda.time.LocalDate;

class FundData {
    def httpAddress
    def name
    def startDate
    def data

    static def pattern = ~/(\d{2})\.(\d{2})\.(\d{4});(\d+\.\d+)/


    FundData (httpAddress, name, startDate) {
        this.httpAddress = httpAddress
        this.name = name
        this.startDate = startDate
    }

    def init() {
        def http = new HTTPBuilder(httpAddress)
        data = {}

        http.request(GET, TEXT) {
            response.sucess { resp, reader ->
                reader.eachLine { line ->
                    def parsed = parseLine(line)
                    data.put(parsed[0], parsed[1])
                }
            }

            response.'404' { resp ->
                throw new IllegalArgumentException("Couldn't get data from address ${httpAddress}")
            }
        }
    }

    def parseLine(line) {
        def matcher = pattern.matcher(line)

        matcher.find()
        assert matcher.groupCount() == 4

        def date = new LocalDate(matcher.group(1), matcher.group(2), matcher.group(3))
        def value = Double.parseDouble(matcher.group(4))

        [date, value]
    }
}
