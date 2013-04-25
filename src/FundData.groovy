
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

abstract class FundData {
    def name
    def startDate
    Map periodicalData = [:]

    FundData(name, startDate) {
        this.name = name
        this.startDate = startDate
    }

    abstract def load(Period period);
//    def shares = [:]
//    def investedAmounts = [:]

//    public investToFund(amount, date, strategy) {
//        def entry = periodicalData.entrySet().find {it.key.compareTo(date) >= 0}
//
//        if (shares[strategy] == null) shares[strategy] = [:]
//        if (investedAmounts[strategy] == null) investedAmounts[strategy] = [:]
//
//        if (entry) {
//            investedAmounts[strategy][entry.key] = amount
//            shares[strategy][entry.key] = amount / entry.value
//        }
//
//        return getValueForDate(date, strategy)
//    }

    private calculateMean(Collection collection) {
        collection.sum() / collection.size()
    }

    def getMean() {
        def priceChanges = collectSiblings(periodicalData.values()) { prev, current ->
            current / prev - 1
        } as List

        calculateMean(priceChanges)
    }

    private collectSiblings(Collection collection, Closure transformer) {
        def results = []

        if (collection.isEmpty()) return results
        assert transformer.maximumNumberOfParameters == 2

        def iter = collection.iterator()
        Double prev = iter.next()

        while (iter.hasNext()) {
            Double current = iter.next()

            results << transformer.call(prev, current)
            prev = current
        }

        return results
    }




    def getStandardDeviation() {
        def mean = getMean()
        def priceChanges = collectSiblings(periodicalData.values()) { prev, current ->
            prev / current - 1
        } as List


        def differencesFromMean = priceChanges.collect { mean - it }

        def squaredDifferences = differencesFromMean.collect {
            it ** 2
        }

        def variance = calculateMean(squaredDifferences)

        def standardDeviation = Math.sqrt(variance)

        return standardDeviation
    }

//    public getValueForDate(date, strategy) {
//        def entry = periodicalData.entrySet().find {it.key.compareTo(date) >= 0}
//        def sharePrice = entry.value
//
//        if (entry == null) return 0.0
//        if (shares[strategy] == null) shares[strategy] = [:]
//
//        def sum = shares[strategy].values().sum()
//        sum = sum ?: 0.0
//        sum * sharePrice
//    }

//    public getInvestedAmount(date, strategy) {
//        def sum = 0.0
//        for (entry in investedAmounts[strategy].entrySet()) {
//            if (entry.key.compareTo(date) <= 0) {
//                sum += entry.value
//            }
//        }
//
//        return sum
//    }

    Double getSharePriceForDate(date) {
        periodicalData.entrySet().find {it.key >= date}.value
    }

    Double getLastSharePrice() {
        periodicalData.max { it.key }.value
    }

}

class SimulatedFundData extends FundData{
    private Double myMean, stdDev
    private random = new Random()
    //private static final random = new Random()

    SimulatedFundData(name, startDate, Double mean, Double stdDev) {
        super(name, startDate)
        this.myMean = mean
        this.stdDev = stdDev
    }

    def load(Period period) {
        def currentTargetDate = startDate
        def endDate = new LocalDate()
        def prevValue = 1.0D
        //def random = new Random()

        while (currentTargetDate <= endDate) {
            def priceChange = (random.nextGaussian() * stdDev + myMean) + 1
            prevValue = priceChange * prevValue
            periodicalData[currentTargetDate] = prevValue

            currentTargetDate = period.getNextPeriodDate(currentTargetDate)
        }
    }
}

class SeligsonFundData extends FundData {
    def httpAddress

    private static final pattern = ~/(\d{2})\.(\d{2})\.(\d{4});(\d+\.\d+)/

    SeligsonFundData(httpAddress, name, startDate) {
        super(name, startDate)
        this.httpAddress = httpAddress
    }

    def load(Period nextPeriod) {
        def http = new HTTPBuilder(httpAddress)
        def data = [:]

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

        initMonthlyData(data, nextPeriod)
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
