import org.joda.time.LocalDate
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */
class SellFromTheStartSellStrategy {
    Double totalTaxPaid = 0.0

    def doSell(investmentDatas, fund, date, amount) {
        def sharePrice = fund.getSharePriceForDate(date)
        def sharesToSell = amount / sharePrice
        def investmentData = investmentDatas

        def investment = amount

        def iter = investmentData.entries.entrySet().iterator()

        Double taxPaid = 0.0

        Double currentSharePrice = fund.getSharePriceForDate(date)

        while (iter.hasNext()) {
            def ent = iter.next()
            LocalDate batchDate = ent.key
            Entry entry = ent.value
            if (entry.shares > sharesToSell) {
                entry.shares = entry.shares - sharesToSell

                def sharePriceForBatch = fund.getSharePriceForDate(batchDate)

                def batchValue = sharesToSell * sharePriceForBatch
                entry.investment -= batchValue

                def currentValue = currentSharePrice * sharesToSell

                taxPaid += (currentValue - batchValue) * 0.30

                totalTaxPaid += taxPaid

                return amount - taxPaid
            } else {
                sharesToSell -= entry.shares
                investment -= entry.investment

                def currentValue = currentSharePrice * entry.shares

                taxPaid += (currentValue - entry.investment) * 0.30

                iter.remove()
            }
        }

        return amount - taxPaid
    }
}
