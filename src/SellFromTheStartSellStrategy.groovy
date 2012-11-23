
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */
class SellFromTheStartSellStrategy {
    def doSell(investmentDatas, fund, date, amount) {
        def sharePrice = fund.getSharePriceForDate(date)
        def sharesToSell = amount / sharePrice
        def investmentData = investmentDatas[fund]

        def iter = investmentData.entrySet.iterator

        while (iter.hasNext) {
            def entry = iter.next
            if (entry.value > sharesToSell) {
                entry.value = entry.value - sharesToSell
                return amount
            } else {
                sharesToSell -= entry.value
                iter.remove
            }
        }

        return amount
    }
}
