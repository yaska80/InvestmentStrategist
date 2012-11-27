import org.joda.time.LocalDate
import org.joda.time.Months
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 22.11.2012
 * Time: 17.36
 * To change this template use File | Settings | File Templates.
 */
class ValueAveragingInvestmentStrategy extends AbstractInvestmentStrategy {

    def growthRatePerPeriod = 0.0
    def startDate = new LocalDate()
    def rebalancingStrategy

    def ValueAveragingInvestmentStrategy(startDate, annualGrowthRate, rebalancingStrategy = null) {
        growthRatePerPeriod = Math.pow(1 + annualGrowthRate, 1/12)
        this.startDate = startDate
        this.rebalancingStrategy = rebalancingStrategy
    }


    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double allocation, Double portfolioTotal, InvestmentsData data) {
        def currentPeriod = Months.monthsBetween(startDate, date).getMonths()
        def target = amount * currentPeriod * Math.pow(growthRatePerPeriod, currentPeriod)

        def fundsTarget = target * allocation
        def fundValue = data.getTotalValueByDate(date)
        def investment = fundsTarget - fundValue

        //rebalancingStrategy.rebalance()


        //investment = investment < 0.0 ? 0.0 : investment

        //buyOrSell(fund, date, investment)
        return investment
    }
}
