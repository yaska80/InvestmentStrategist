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

    def ValueAveragingInvestmentStrategy(startDate, annualGrowthRate, rebalancingStrategy = null) {
        growthRatePerPeriod = (1 + annualGrowthRate) ** 1/12
        this.startDate = startDate
        this.rebalancingStrategy = rebalancingStrategy
    }


    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        def currentPeriod = Months.monthsBetween(startDate, date).getMonths()
        def target = amount * currentPeriod * (growthRatePerPeriod ** currentPeriod)

        return rebalancingStrategy.rebalance(fund, fundValue, target)
    }
}
