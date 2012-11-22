
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 22.11.2012
 * Time: 17.28
 * To change this template use File | Settings | File Templates.
 */
class InvestmentsData {
    def shares = [:]
    def investments = [:]

    def put(date, sharesAquired, invested) {
        shares[date] = sharesAquired
        investments[date] = invested
    }

    def getTotalInvestedByDate(date) {
        def entry = investments.entrySet().find {it.key.compareTo(date) >= 0}
        entry.value
    }

    def getTotalSharesByDate(date) {
        def entry = shares.entrySet().find {it.key.compareTo(date) >= 0}
        entry.value
    }

    def getTotalValueByDate(date, sharePrice) {
        def entry = shares.entrySet().find {it.key.compareTo(date) >= 0}
        entry.value * sharePrice
    }

}
