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

    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        Double sharePrice = fund.getSharePriceForDate(date)
        def fundCurValue = data.getTotalValueByDate(date, sharePrice)

        return rebalancingStrategy.rebalance(fund, fundCurValue, portfolioTotal + amount)
    }
}
