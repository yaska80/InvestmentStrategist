
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
    def periodicalData

    private static final pattern = ~/(\d{2})\.(\d{2})\.(\d{4});(\d+\.\d+)/


    FundData (httpAddress, name, startDate) {
        this.httpAddress = httpAddress
        this.name = name
        this.startDate = startDate
    }

    def load(Period nextPeriod) {
        def http = new HTTPBuilder(httpAddress)
        data = [:]

        http.request(GET, TEXT) { req ->
            response.success = { resp, reader ->
                reader.eachLine { line ->
                    def parsed = parseLine(line)
                    data[parsed[0]] = parsed[1]
                }
            }

            response.'404' = { resp ->
                throw new IllegalArgumentException("Couldn't get data from address ${httpAddress}")
            }
        }

/*
        data.entrySet().each {it ->
            println "${it.key};${it.value}"
        }
*/

        initMonthlyData(data, nextPeriod)

        /*monthlyData.entrySet().each {it ->
            println "${it.key};${it.value}"
        }*/
    }

    private initMonthlyData(def data, Period nextPeriod) {
        def firstEntryDate = data.keySet().iterator().next()

        def currentTargetDate = startDate.compareTo(firstEntryDate) >= 0 ? startDate : firstEntryDate
        periodicalData = [:]

        for (def it : data.entrySet()) {
            def comparison = it.key.compareTo(currentTargetDate)
            if (comparison >= 0) {
                periodicalData[it.key] = it.value
                currentTargetDate = nextPeriod.getNextPeriodDate(currentTargetDate)
            }
        }
    }

    def shares = [:]
    def investedAmounts = [:]

    public investToFund(amount, date, strategy) {
        def entry = periodicalData.entrySet().find {it.key.compareTo(date) >= 0}

        if (shares[strategy] == null) shares[strategy] = [:]
        if (investedAmounts[strategy] == null) investedAmounts[strategy] = [:]

        if (entry) {
            investedAmounts[strategy][entry.key] = amount
            shares[strategy][entry.key] = amount / entry.value
        }

        return getValueForDate(date, strategy)
    }

    public getValueForDate(date, strategy) {
        def entry = periodicalData.entrySet().find {it.key.compareTo(date) >= 0}
        def sharePrice = entry.value

        if (entry == null) return 0.0
        if (shares[strategy] == null) shares[strategy] = [:]

        def sum = shares[strategy].values().sum()
        sum = sum ?: 0.0
        sum * sharePrice
    }

    public getInvestedAmount(date, strategy) {
        def sum = 0.0
        for (entry in investedAmounts[strategy].entrySet()) {
            if (entry.key.compareTo(date) <= 0) {
                sum += entry.value
            }
        }

        return sum
    }

    Double getSharePriceForDate(date) {
        def entry = periodicalData.entrySet().find {it.key >= date}
        return entry.value
    }

    Double getLastSharePrice() {
        def maxEntry = periodicalData.max { it.key }
        maxEntry.value
    }

    private parseLine(line) {
        def matcher = pattern.matcher(line)

        matcher.find()
        assert matcher.groupCount() == 4

        def year = Integer.parseInt(matcher.group(3))
        def month = Integer.parseInt(matcher.group(2))
        def day = Integer.parseInt(matcher.group(1))

        def date = new LocalDate(year, month, day)
        def value = Double.parseDouble(matcher.group(4))

        [date, value]
    }
}
