import org.joda.time.LocalDate
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 22.11.2012
 * Time: 17.09
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractInvestmentStrategy implements InvestmentStrategy {
    def investmentDatas = [:]

    @Override
    def getInvestedToFundData(FundData fund) {
        investmentDatas[fund].investments
    }

    @Override
    def getPortfolioTotalValueByDate(LocalDate date) {
        def sum = 0
        investmentDatas.values().each {
            sum += it.getTotalValueByDate(date)
        }

        return sum
    }


}
