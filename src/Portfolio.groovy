import org.joda.time.LocalDate
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 25.11.2012
 * Time: 15.52
 * To change this template use File | Settings | File Templates.
 */
class Portfolio {
    List funds
    InvestmentStrategy investmentStrategy
    Double investmentAmountPerPeriod

    Map portfolioData = [:]
    def buffer = 0.0
    def sellStrategy

    Portfolio(List funds, InvestmentStrategy investmentStrategy, Double investmentAmountPerPeriod, sellStrategy) {
        this.funds = funds
        this.investmentStrategy = investmentStrategy
        this.investmentAmountPerPeriod = investmentAmountPerPeriod
        this.sellStrategy = sellStrategy

        funds.each {
            portfolioData[it] = new InvestmentsData()
        }
    }

    def doIntestmentRound(LocalDate date) {
        def suggestions = []
        def portfolioValue = calculatePortfolioValue(date)

        funds.each { fund ->
            def investmentSuggestion = investmentStrategy.invest(fund, date, investmentAmountPerPeriod, portfolioValue, portfolioData[fund])
            suggestions << investmentSuggestion
        }

        suggestions.sort().each {
            if (it < 0 ) {
                sellStrategy.doSell(portfolioData, fund, date, investmentAmountPerPeriod)
            }
        }

    }

    Double calculatePortfolioValue(LocalDate date) {
        portfolioData.collect { fund, data ->
            data.getTotalValueByDate(date, fund.getSharePriceForDate(date))
        }.sum() as Double
    }


}
