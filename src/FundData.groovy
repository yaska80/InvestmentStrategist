
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
@GrabExclude('org.codehaus.groovy:groovy')
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import static groovyx.net.http.Method.GET
import groovyx.net.http.ContentType

import static groovyx.net.http.ContentType.TEXT

class FundData {
    def httpAddress
    def name
    def startDate
    def data


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

    }
}
