import org.joda.time.LocalDate

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
    def currentTargetValue = 0.0D
    def period = 0
    def lastPeriodDate

    def ValueAveragingInvestmentStrategy(startDate, annualGrowthRate, periodsPerYear, rebalancingStrategy = null) {
        growthRatePerPeriod = (1.0 + annualGrowthRate) ** (1.0/periodsPerYear)
        this.startDate = startDate
        this.rebalancingStrategy = rebalancingStrategy
        this.lastPeriodDate = startDate
    }


    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data) {
        if (date > lastPeriodDate) {
            period++
            currentTargetValue = amount * period * (growthRatePerPeriod ** period)
            lastPeriodDate = date
        }
        //period++
        def sharePrice = fund.getSharePriceForDate(date)
        def fundValue = data.getTotalValueByDate(date, sharePrice)


        return rebalancingStrategy.rebalance(fund, fundValue, currentTargetValue)
    }
}
