
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
import groovyx.net.http.HTTPBuilder
import com.sun.org.apache.bcel.internal.generic.GETFIELD;


class FundData {
    def httpAddress
    def name
    def startDate
    FundData (httpAddress, name, startDate) {
        this.httpAddress = httpAddress
        this.name = name
        this.startDate = startDate
    }

    def init() {
        def http = new HTTPBuilder(httpAddress)

        http.request(GET, TEXT)
    }
}
