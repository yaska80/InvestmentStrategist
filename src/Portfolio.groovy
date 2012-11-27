
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 25.11.2012
 * Time: 15.52
 * To change this template use File | Settings | File Templates.
 */
class Portfolio {
    def fundsAndAllocations
    def investmentStrategy
    def rebalanceStrategy
    def investmentAmount

    def portfolioData = [:]
    def buffer = 0.0


    def Portfolio(def fundsAndAllocations, def investmentStrategy, def rebalanceStrategy, def investmentAmount) {
        this.fundsAndAllocations = fundsAndAllocations

        fundsAndAllocations.each {
            portfolioData[it[0]] = new InvestmentsData()
        }


        this.investmentStrategy = investmentStrategy
        this.rebalanceStrategy = rebalanceStrategy
        this.investmentAmount = investmentAmount
    }

    def doIntestmentRound(date) {
        def suggestions = [:]
        fundsAndAllocations.each {
            def fund = it[0]
            def allocation = it[1]

            def investmentSuggestion = investmentStrategy.invest(fund, date, investmentAmount, allocation, portfolioData[fund])
            suggestions[fund] = investmentSuggestion
        }

        def actualActs = rebalanceStrategy.doRebalancing(suggestions)

    }





}
