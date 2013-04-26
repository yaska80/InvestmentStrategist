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
    def buffer = 4000
    def sellStrategy
    def period = 1
    def growthPerPeriod = 1.03 ** (1/12)
    def currentPeriodInvestment = 0.0
    def savedMoneyCumulativeSum = 0.0
    Map bufferData = [:]

    Double TWRIndex = 100.0
    Double previousPortfolioValue = 0.0

    Portfolio(List funds, InvestmentStrategy investmentStrategy, Double investmentAmountPerPeriod, sellStrategy, periodsPerYear) {
        this.funds = funds
        this.investmentStrategy = investmentStrategy
        this.investmentAmountPerPeriod = investmentAmountPerPeriod
        this.sellStrategy = sellStrategy
        this.currentPeriodInvestment = investmentAmountPerPeriod
        this.growthPerPeriod = 1.03 ** (1/periodsPerYear)

        funds.each {
            portfolioData[it] = new InvestmentsData()
        }
    }

    def doInvestmentRound(LocalDate date) {
        def suggestions = [:]
        def portfolioValue = calculatePortfolioValue(date)

        currentPeriodInvestment = currentPeriodInvestment * growthPerPeriod
        buffer += currentPeriodInvestment

        funds.each { fund ->
            def investmentSuggestion =
                investmentStrategy.invest(fund as FundData, date,
                        currentPeriodInvestment, portfolioValue,
                        portfolioData[fund] as InvestmentsData)

            suggestions[fund] = investmentSuggestion
        }

        def bufferBeforeTransactions = buffer
        def suggestionsTotal = suggestions.values().sum()
        //savedMoneyCumulativeSum += suggestionsTotal
        def savedMoneyThisPeriod = currentPeriodInvestment - suggestionsTotal
        if (savedMoneyThisPeriod > 0.005) {
            savedMoneyCumulativeSum += savedMoneyThisPeriod
//            println "on $date Whee, we are saving money ${savedMoneyThisPeriod}, cumulative sum $savedMoneyCumulativeSum"
        }

        def realPeriodInvestmentsTotal = []

        suggestions.sort {a, b -> a.value <=> b.value }
            .each { fund, investment ->
                if (investment < 0 ) {
                    buffer += sellStrategy.doSell(portfolioData[fund], fund, date, investment * -1)
                    realPeriodInvestmentsTotal << (investment * -1)
                } else {
                    def sharePrice = (fund as FundData).getSharePriceForDate(date)

                    if (buffer - investment < 0.0) {
//                        println "On $date buffer would have ran out! Buffer = $buffer, investment=$investment to $fund.name buffer before transactions $bufferBeforeTransactions"
                    }
                    investment = buffer - investment < 0.0 ? buffer : investment
                    Double shares = investment / sharePrice

                    (portfolioData[fund] as InvestmentsData).put(date as LocalDate, shares, investment as Double, sharePrice as Double)
                    buffer -= investment
                    realPeriodInvestmentsTotal << investment
                }
            }

        portfolioValue = calculatePortfolioValue(date)
        //edellisen päivän indeksiluku*(1+(salkun arvo - edellisen päivän arvo - rahavirta)/edellisen päivän salkun arvolla)
        def realPeriodInvestmentsTotalSum = realPeriodInvestmentsTotal.sum()
        def curPortfolioValue = portfolioValue - previousPortfolioValue - realPeriodInvestmentsTotalSum
        if (previousPortfolioValue > 0.0) {
            TWRIndex = TWRIndex * (1.0 + curPortfolioValue / previousPortfolioValue)
        }
        previousPortfolioValue = portfolioValue

        period++
        bufferData[date] = buffer

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
