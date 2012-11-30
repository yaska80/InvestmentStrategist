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

    def doInvestmentRound(LocalDate date) {
        def suggestions = [:]
        def portfolioValue = calculatePortfolioValue(date)

        funds.each { fund ->
            def investmentSuggestion =
                investmentStrategy.invest(fund as FundData, date,
                        investmentAmountPerPeriod, portfolioValue,
                        portfolioData[fund] as InvestmentsData)

            suggestions[fund] = investmentSuggestion
        }

        suggestions.sort {a, b ->
            a.value <=> b.value
        }.each { fund, suggestion ->
            if (suggestion < 0 ) {
                buffer += sellStrategy.doSell(portfolioData[fund], fund, date, suggestion * -1)
            } else {
                def sharePrice = (fund as FundData).getSharePriceForDate(date)
                Double shares = suggestion / sharePrice

                (portfolioData[fund] as InvestmentsData).put(date as LocalDate, shares, suggestion as Double)
            }
        }

    }

    Double calculatePortfolioValue(LocalDate date) {
        portfolioData.collect { fund, data ->
            data.getTotalValueByDate(date, fund.getSharePriceForDate(date))
        }.sum() as Double
    }

    Double calculatePortfolioInvested(LocalDate date) {
        portfolioData.collect { fund, data ->
            data.getTotalInvestedByDate(date)
        }.sum() as Double
    }
}
