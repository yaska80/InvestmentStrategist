import org.joda.time.LocalDate
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 22.11.2012
 * Time: 17.28
 * To change this template use File | Settings | File Templates.
 */
class InvestmentsData {
    Map entries = [:]

    def put(LocalDate date, Double sharesAquired, Double invested, Double sharePrice) {
        entries[date] = new Entry(shares: sharesAquired, investment: invested, sharePrice: sharePrice)
    }

    Double getTotalInvestedByDate(LocalDate date) {
        entries.findAll { it.key.compareTo(date) <= 0 }.values().sum { it.investment } as Double
    }

    Double getTotalSharesByDate(LocalDate date) {
        entries.findAll {
            it.key.compareTo(date) <= 0
        }.values().sum {
            it.shares
        } as Double
    }

    Double getTotalValueByDate(date, Double sharePrice) {
        Double totalShares = getTotalSharesByDate(date) ?: 0.0D
        totalShares * sharePrice
    }

}

private class Entry {
    Double shares
    Double investment
    Double sharePrice
}
