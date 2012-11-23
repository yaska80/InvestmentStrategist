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
    def sellStrategy
    def buffer

    def AbstractInvestmentStrategy() {
        sellStrategy = new SellFromTheStartSellStrategy();
    }

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

    protected buyOrSell(fund, date, amount) {
        if (amount >= 0) {
            buy(fund, date, amount)
        } else {
            buffer += sell(fund, date, amount)
        }

    }

    def sell(fund, date, amount) {
        sellStrategy.doSell(investmentDatas, fund, date, amount)
    }

    private buy(fund, date, investment) {
        def sharePrice = fund.getSharePriceForDate(date)
        def nbrShares = investment / sharePrice

        investmentDatas[fund].put(date, nbrShares, investment)
    }
}
