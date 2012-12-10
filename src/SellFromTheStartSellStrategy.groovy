import org.joda.time.LocalDate
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */
class SellFromTheStartSellStrategy {
    Double totalTaxPaid = 0.0
    Double totalNet = 0.0

    def doSell(investmentDatas, fund, date, amount) {
        def sharePrice = fund.getSharePriceForDate(date)
        def sharesToSell = amount / sharePrice
        def investmentData = investmentDatas

        def investment = amount

        def iter = investmentData.entries.entrySet().iterator()

        Double taxPaid = 0.0

        Double currentSharePrice = fund.getSharePriceForDate(date)
        Double totalProfit = 0.0

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

                totalProfit += (currentValue - batchValue)
                def taxProposition = (currentValue - batchValue) * 0.30
                taxPaid += taxProposition < 0.0 ? 0.0 : taxProposition

                totalTaxPaid += taxPaid
                def net = amount - taxPaid
                totalNet += net


                println "on ${date} sold from fund ${fund.name} amount ${net} (profit: ${totalProfit}) tax ${taxPaid}, total net ${totalNet} and tax ${totalTaxPaid}"

                return net
            } else {
                sharesToSell -= entry.shares
                investment -= entry.investment

                def currentValue = currentSharePrice * entry.shares
                totalProfit += currentValue - entry.investment
                def taxProposition = (currentValue - entry.investment) * 0.30
                taxPaid += taxProposition < 0.0 ? 0.0 : taxProposition

                iter.remove()
            }
        }

        return amount - taxPaid
    }
}

class LeastAmountOfProfitSellStrategy {
    Double totalTaxPaid = 0.0
    Double totalNet = 0.0

    def doSell(investmentDatas, fund, date, amount) {
        def sharePrice = fund.getSharePriceForDate(date)
        def sharesToSell = amount / sharePrice
        def investmentData = investmentDatas as InvestmentsData

        def investment = amount

        Map data = [:] + investmentData.entries

        data = data.findAll {key, value ->
            value.sharePrice <= sharePrice
        }.sort {a, b ->
            (a.value.sharePrice <=> b.value.sharePrice) * -1
        }

        def iter = data.entrySet().iterator()

        Double taxPaid = 0.0
        Double totalProfit = 0.0

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

                totalProfit += (currentValue - batchValue)
                totalTaxPaid += taxPaid
                def net = amount - taxPaid
                totalNet += net


                println "on ${date} sold from fund ${fund.name} amount ${net}(profit: $totalProfit) tax ${taxPaid}, total net ${totalNet} and tax ${totalTaxPaid}"

                return net
            } else {
                sharesToSell -= entry.shares
                investment -= entry.investment

                def currentValue = currentSharePrice * entry.shares
                totalProfit += currentValue - entry.investment
                taxPaid += (currentValue - entry.investment) * 0.30

                iter.remove()
            }
        }

        return amount - taxPaid
    }
}
