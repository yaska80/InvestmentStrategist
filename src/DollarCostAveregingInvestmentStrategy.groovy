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
    def lastRebalancingDate = new LocalDate(1900,1,1)
    def target = 0
    def growthRate = 1.03 ** (1/26)

    DollarCostAveregingInvestmentStrategy(rebalancingStrategy, periodsPerYear, growthPerYear) {
        this.rebalancingStrategy = rebalancingStrategy
        growthRate = 1 + growthPerYear ** (1/periodsPerYear)
    }

    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        def sharePrice = fund.getSharePriceForDate(date)

        if (date > lastRebalancingDate.plusMonths(4)) {
            def result = rebalancingStrategy.rebalance(fund, data.getTotalValueByDate(date,sharePrice), portfolioTotal)
            lastRebalancingDate = date
            return result
        } else if (lastRebalancingDate == date) {
            return rebalancingStrategy.rebalance(fund, data.getTotalValueByDate(date,sharePrice), portfolioTotal)
        } else {
            def allocation = rebalancingStrategy.fundSettings[fund].allocation
            def result = amount * allocation

            return result
        }
    }
}
