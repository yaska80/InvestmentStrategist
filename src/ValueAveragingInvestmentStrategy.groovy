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

    def growthRatePerPeriod
    def startDate

    def ValueAveragingInvestmentStrategy(startDate, annualGrowthRate) {
        growthRatePerPeriod = Math.pow(1.0 + annualGrowthRate, 1.0/12)
        this.startDate = startDate
    }


    @Override
    def invest(FundData fund, LocalDate date, Double amount, Double allocation) {
        def currentPeriod = Months.monthsBetween(startDate, date)
        def target = amount * currentPeriod * Math.pow(growthRatePerPeriod, currentPeriod)

        def fundsTarget = target * allocation
        def fundValue = investmentDatas[fund].getTotalValueByDate(date)
        def investment = fundsTarget - fundValue
        //investment = investment < 0.0 ? 0.0 : investment

        buyOrSell(fund, date, investment)
    }
}
