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

    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        Double sharePrice = fund.getSharePriceForDate(date)
        def fundCurValue = data.getTotalValueByDate(date, sharePrice)

        def result = rebalancingStrategy.rebalance(fund, fundCurValue, portfolioTotal + 600)
        period += 1

        return result
    }
}
