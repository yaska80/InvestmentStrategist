import org.joda.time.LocalDate
import org.joda.time.Months
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 22.11.2012
 * Time: 17.36
 * To change this template use File | Settings | File Templates.
 */
class ValueAveragingInvestmentStrategy implements InvestmentStrategy {

    def growthRatePerPeriod = 0.0D
    def startDate = new LocalDate()
    def rebalancingStrategy
    def currentTargetValue

    def ValueAveragingInvestmentStrategy(startDate, annualGrowthRate, rebalancingStrategy = null) {
        growthRatePerPeriod = (1.0 + annualGrowthRate) ** (1.0/12.0)
        this.startDate = startDate
        this.rebalancingStrategy = rebalancingStrategy
    }


    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        def currentPeriod = Months.monthsBetween(startDate, date).getMonths()
        currentTargetValue = amount * currentPeriod * (growthRatePerPeriod ** currentPeriod)
        def sharePrice = fund.getSharePriceForDate(date)
        def fundValue = data.getTotalValueByDate(date, sharePrice)

        return rebalancingStrategy.rebalance(fund, fundValue, currentTargetValue)
    }
}
