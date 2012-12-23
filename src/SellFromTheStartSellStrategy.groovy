import org.joda.time.LocalDate
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */
abstract class SellStrategySupport {
    Double totalTaxPaid = 0.0
    Double totalNet = 0.0

    def doSell(investmentDatas, fund, date, amount) {
        def sharePrice = fund.getSharePriceForDate(date)
        def sharesToSell = amount / sharePrice
        def investmentData = investmentDatas

        def investment = amount

        def originalData = investmentData.entries
        def data = getDataInSellingOrder(originalData, sharePrice, date)

        def iter = data.entrySet().iterator()

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
                originalData.remove(ent.key)
            }
        }

        return amount - taxPaid
    }

    protected abstract Map getDataInSellingOrder(Map map, Double sharePrice, LocalDate date);
}

class SellFromTheStartSellStrategy extends SellStrategySupport {


    protected Map getDataInSellingOrder(Map map, Double aDouble, LocalDate date) {
        [:] + map
    }
}

class LeastAmountOfProfitSellStrategy extends SellStrategySupport{
    @Override
    protected Map getDataInSellingOrder(Map data, Double sharePrice, LocalDate date) {

        data.findAll {key, value ->
            value.sharePrice <= sharePrice &&  key < date.minusYears(1)
        }.sort {a, b ->
            (a.value.sharePrice <=> b.value.sharePrice) * -1
        }
    }
}

class MaxAmountOfProfitSellStrategy extends SellStrategySupport{
    @Override
    protected Map getDataInSellingOrder(Map data, Double sharePrice, LocalDate date) {
        data.findAll {key, value ->
            value.sharePrice <= sharePrice
        }.sort {a, b ->
            (a.value.sharePrice <=> b.value.sharePrice)
        }
    }
}
