import org.joda.time.LocalDate
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 29.11.2012
 * Time: 22.47
 * To change this template use File | Settings | File Templates.
 */
class DollarCostAveregingInvestmentStrategy implements InvestmentStrategy
{
    def rebalancingStrategy
    def period = 0
    def lastPeriodDate = new LocalDate(1900,1,1)
    def target = 0
    def growthRate = 1.03 ** (1/26)

    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        if (date > lastPeriodDate) {
            target = amount * (growthRate ** period)
            lastPeriodDate = date
            period++
        }
        Double sharePrice = fund.getSharePriceForDate(date)
        def fundCurValue = data.getTotalValueByDate(date, sharePrice)

        //def result = rebalancingStrategy.rebalance(fund, fundCurValue, target)
        //period += 1

        //def fundTarget = target * rebalancingStrategy.fundSettings[fund].allocation
        def result = target * rebalancingStrategy.fundSettings[fund].allocation

        return result
    }
}
