import org.joda.time.LocalDate
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 22.11.2012
 * Time: 17.05
 * To change this template use File | Settings | File Templates.
 */
public interface InvestmentStrategy {
    def invest(FundData fund, LocalDate date, Double amount, Double portfolioTotal, InvestmentsData data)

}