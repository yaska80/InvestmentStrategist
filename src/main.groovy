import org.joda.time.LocalDate
import groovy.swing.SwingBuilder
import javax.swing.WindowConstants
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.data.xy.XYDataset
import org.jfree.chart.ChartFactory
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.axis.NumberAxis
import java.text.DecimalFormat
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.labels.StandardXYToolTipGenerator
import java.text.SimpleDateFormat
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.Day
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import java.text.NumberFormat
/**
 * 
 * @author jaakkosj / Solita Oy Last changed by: $LastChangedBy$
 */


def startDate = new LocalDate(2002,1,7)

List vaResults = []
List dcaResults = []

for (def i = 0; i < 200; i++) {
    SimulatedFundData russia
    java.util.Map funds
    (funds, russia) = setupFunds(startDate)

    Map fundSettings = [:]

    Period period = new ForthnightlyPeriod()
    def periodicalInvestment = 300
//    Period period = new MonthlyPeriod()
//    def periodicalInvestment = 600

    Map fundMeans = [:]
    Map fundStdDevs = [:]

    funds.each { fund, allocation ->
        fund.load(period)

        //if (!fund instanceof SimulatedFundData){
        fundMeans[fund] = fund.mean
        fundStdDevs[fund] = fund.standardDeviation

//            println "Fund ${fund.name},  myMean=${fundMeans[fund]}, stddev=${fundStdDevs[fund]}"
        //}

        fundSettings[fund] = new FundRebalancingSettings(highLimit: 0.2, lowLimit: 0.05, allocation: allocation as Double)
    }
    fundSettings[russia] = new FundRebalancingSettings(highLimit: 0.3, lowLimit: 0.05, allocation: 0.1)

    OptimisticRebalancingStrategy ors = new OptimisticRebalancingStrategy(fundSettings: fundSettings)
    //NoSellRebalancingStrategy ors = new NoSellRebalancingStrategy(fundSettings: fundSettings)
    ToAllocationRebalancingStrategy orsTa = new ToAllocationRebalancingStrategy(fundSettings: fundSettings)

    ValueAveragingInvestmentStrategy invStrat = new ValueAveragingInvestmentStrategy(startDate, 0.08, period.periodsPerYear, ors)
    DollarCostAveregingInvestmentStrategy dcaStrat = new DollarCostAveregingInvestmentStrategy(orsTa, period.periodsPerYear, 0.03)

    //SellFromTheStartSellStrategy sellStrat = new SellFromTheStartSellStrategy()
    LeastAmountOfProfitSellStrategy sellStratVa = new LeastAmountOfProfitSellStrategy()
    LeastAmountOfProfitSellStrategy sellStratDca = new LeastAmountOfProfitSellStrategy()
    //MaxAmountOfProfitSellStrategy sellStrat = new MaxAmountOfProfitSellStrategy()


    Portfolio portfolioVa = new Portfolio(funds.keySet().asList(), invStrat, periodicalInvestment, sellStratVa, period.periodsPerYear)
    Portfolio portfolioDca = new Portfolio(funds.keySet().asList(), dcaStrat, periodicalInvestment, sellStratDca, period.periodsPerYear)

    LocalDate currentDate = startDate
    def endDate = new LocalDate()
    def data = [["va":[:],"dca":[:]],["va":[:],"dca":[:]]]
    def lastInvestedVa = 0.0
    def lastInvestedDca = 0.0
    def lastValueVa = 0.0
    def lastValueDca = 0.0

    while (currentDate.compareTo(endDate) < 0) {
        portfolioVa.doInvestmentRound(currentDate)
        portfolioDca.doInvestmentRound(currentDate)

        lastInvestedVa = portfolioVa.calculatePortfolioInvested(currentDate)
        lastInvestedDca = portfolioDca.calculatePortfolioInvested(currentDate)
        lastValueVa = portfolioVa.calculatePortfolioValue(currentDate)
        lastValueDca = portfolioDca.calculatePortfolioValue(currentDate)
        data[0]["va"][currentDate] = lastInvestedVa
        data[0]["dca"][currentDate] = lastInvestedDca
        data[1]["va"][currentDate] = lastValueVa
        data[1]["dca"][currentDate] = lastValueDca

        currentDate = period.getNextPeriodDate(currentDate)
    }

    def profit = ((lastValueVa) / lastInvestedVa - 1) * 100
    def profitWithBuffer = ((lastValueVa + portfolioVa.buffer) / lastInvestedVa - 1) * 100
    vaResults << [profit, profitWithBuffer]
//    println "\n[VA] Buffer was at the end ${portfolioVa.buffer}"
//    println "[VA] Total Tax Paid ${sellStratVa.totalTaxPaid}"
//    println "[VA] End invested $lastInvestedVa"
//    println "[VA] End value $lastValueVa"
//    println "[VA] End value with buffer ${lastValueVa + portfolioVa.buffer}"
//    println "[VA] Total profit at the end ${profit}%"
//    println "[VA] Total profit at the end with buffer ${profitWithBuffer}%"
//    println "[VA] End total period investment $portfolioVa.currentPeriodInvestment"


    profit = ((lastValueDca) / lastInvestedDca - 1) * 100
    profitWithBuffer = ((lastValueDca + portfolioDca.buffer) / lastInvestedDca - 1) * 100
    dcaResults << [profit, profitWithBuffer]
//    println "\n[DCA] Buffer was at the end ${portfolioDca.buffer}"
//    println "[DCA] Total Tax Paid ${sellStratDca.totalTaxPaid}"
//    println "[DCA] End invested $lastInvestedDca"
//    println "[DCA] End value $lastValueDca"
//    println "[DCA] End value with buffer ${lastValueDca + portfolioDca.buffer}"
//    println "[DCA] Total profit at the end ${profit}%"
//    println "[DCA] Total profit at the end with buffer ${profitWithBuffer}%"
//    println "[DCA] End total period investment $portfolioDca.currentPeriodInvestment"

    print(".")
}

println "\n\nVA profit\t| DCA profit\t| -DIFF-\t\t| VA buffer\t| DCA buffer\t| -DIFF-"
println "-_" * 40
for (def i = 0; i < vaResults.size(); i++) {
    println "${toP(vaResults[i][0])}%\t\t| ${toP(dcaResults[i][0])}%\t\t| ${toP(vaResults[i][0] - dcaResults[i][0])}%\t| ${toP(vaResults[i][1])}%\t| ${toP(dcaResults[i][1])}%\t\t| ${toP(vaResults[i][1] - dcaResults[i][1])}%"
}

def toP(Double value) {
    String.format("%05.2f", value)
}


def profits = vaResults.collect {it[0]}
def profitsWithBuffer = vaResults.collect {it[1]}

println("\nVA profit mean: ${toP(profits.sum()/profits.size())}%")
println("VA profit with buffer mean: ${toP(profitsWithBuffer.sum()/profitsWithBuffer.size())}%")

profits = dcaResults.collect {it[0]}
profitsWithBuffer = dcaResults.collect {it[1]}

println("DCA profit mean: ${profits.sum()/profits.size()}%")
println("DCA profit with buffer mean: ${profitsWithBuffer.sum()/profitsWithBuffer.size()}%")

private List setupFunds(LocalDate startDate) {
//FundData europe = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=eurooppa", "Seligson Eurooppa", startDate)
    FundData europe = new SimulatedFundData("Eurooppa", startDate, 0.0064, 0.06099246257923363)

    //FundData corporate = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=eurocorporate", "Seligson Corporate Bond", startDate)
    FundData corporate = new SimulatedFundData("Corporate Bond", startDate, 0.00430228641396697, 0.01210366428170987)

    //def obligaatio = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=euroobligaatio", "Seligson Obligaatio", startDate)
    def obligaatio = new SimulatedFundData("Obligaatio", startDate, 0.004213307822527416, 0.013641529992514539)

    //def pharma = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=global-pharma", "Seligson Pharma", startDate)
    def pharma = new SimulatedFundData("Pharma", startDate, 0.002495694347109639, 0.03942556517235173)

    //def russia = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=russia", "Seligson Russia", startDate)
    def russia = new SimulatedFundData("Russia", startDate, 0.01764216832366826, 0.09616196805363253)

    //def brands = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=global-brands", "Seligson Brands", startDate)
    def brands = new SimulatedFundData("Brands", startDate, 0.004366179752845425, 0.04461666496923974)

    //def aasia = new FundData("http://www.seligson.fi/graafit/data.asp?op=aasia", "Seligson Aasia", startDate)
    def aasia = new SimulatedFundData("Aasia", startDate, 0.004020501D, 0.05882721986195309)

    //def phoebus = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=phoebus", "Seligson Phoebus", startDate)
    def phoebus = new SimulatedFundData("Phoebus", startDate, 0.008687441460610443, 0.05452897459762416)

    def kehittyva = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=kehittyva", "Seligson Kehittyvät", startDate)
    def amerikka = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=pohjoisamerikka", "Seligson Pohjois-amerikka", startDate)


    Map funds = [:]
    // Osakkeet
    funds.put(europe, 0.3)
    funds.put(russia, 0.1)
    funds.put(phoebus, 0.3)
    funds.put(aasia, 0.1)
    //funds.put(amerikka, 0.2)
    funds.put(brands, 0.05)
    funds.put(pharma, 0.05)
    // Pitkät korot
    funds.put(corporate, 0.05)
    funds.put(obligaatio, 0.05)
    [funds, russia]
}

XYDataset createVolumeDataset(data) {
    return new TimeSeriesCollection(mapToTimeSeries(data, "Volume"))
}

def mapToTimeSeries(map, name) {
    TimeSeries timeseries = new TimeSeries(name, org.jfree.data.time.Day.class);

    map.each { date, value ->
        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), value)
    }

    return timeseries
}

XYDataset createPriceDataset(data) {
    return new TimeSeriesCollection(mapToTimeSeries(data, "Price"))
}


JFreeChart createChart(FundData fund, String strategy, Map volumeData)
{
    XYDataset xydataset = createPriceDataset(fund.periodicalData);
    String s = "${fund.name} (${strategy.toUpperCase()})";
    JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(s, "Date", "Price", xydataset, true, true, false);
    XYPlot xyplot = (XYPlot)jfreechart.getPlot();
    NumberAxis numberaxis = (NumberAxis)xyplot.getRangeAxis();
    numberaxis.setLowerMargin(0.40000000000000002D);
    DecimalFormat decimalformat = new DecimalFormat("00.00");
    numberaxis.setNumberFormatOverride(decimalformat);
    XYItemRenderer xyitemrenderer = xyplot.getRenderer();
    xyitemrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));


    NumberAxis numberaxis1 = new NumberAxis("Volume");
    numberaxis1.setUpperMargin(1.0D);
    xyplot.setRangeAxis(1, numberaxis1);
    xyplot.setDataset(1, createVolumeDataset(volumeData));
    xyplot.setRangeAxis(1, numberaxis1);
    xyplot.mapDatasetToRangeAxis(1, 1);
    XYBarRenderer xybarrenderer = new XYBarRenderer(0.20000000000000001D);
    xybarrenderer.shadowVisible = false
    xybarrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.00")));
    xyplot.setRenderer(1, xybarrenderer);
    return jfreechart;
}

JFreeChart createSummaryChart(data, bufferData) {
    TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

    timeSeriesCollection.addSeries(mapToTimeSeries(data[0]["dca"], "Invested-DCA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[0]["va"], "Invested-VA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[1]["dca"], "NVA-DCA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[1]["va"], "NVA-VA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(bufferData, "Buffer"))

    String s = "Summary";
    JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(s, "Date", "Price", timeSeriesCollection, true, true, false);
    XYPlot xyplot = (XYPlot)jfreechart.getPlot();
    NumberAxis numberaxis = (NumberAxis)xyplot.getRangeAxis();
    numberaxis.setLowerMargin(0.40000000000000002D);
    DecimalFormat decimalformat = new DecimalFormat("00.00");
    numberaxis.setNumberFormatOverride(decimalformat);
    XYItemRenderer xyitemrenderer = xyplot.getRenderer();
    xyitemrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));

    return jfreechart;
}

JFreeChart createAllocationChart(Portfolio portfolio) {
    def dataset = new DefaultPieDataset()


    portfolio.portfolioData.each {fund, data ->
        dataset.setValue(fund.name, data.getTotalValueByDate(new LocalDate(), fund.getLastSharePrice()))
    }

    JFreeChart chart = ChartFactory.createPieChart(
            "Allocations",  // chart title
            dataset,             // data
            true,               // include legend
            true,
            false
    );

    PiePlot plot = (PiePlot) chart.getPlot();
    //plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
    plot.setNoDataMessage("No data available");
    plot.setCircular(false);
    plot.setLabelGap(0.02);
    plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
            "{0} ({2})", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
    ));

    return chart;
}

//def swing = new SwingBuilder()
//def frame = swing.frame(title:'Groovy PieChart',
//        defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
//    panel(id:'MainPanel') {
//        scrollPane(preferredSize: [1000,700], constraints: context.CENTER) {
//            vbox {
//                panel(id:"Summary") { widget(new ChartPanel(createSummaryChart(data, portfolioVa.bufferData)))}
//                panel(id:"Allocation") { widget(new ChartPanel(createAllocationChart(portfolioVa)))}
//
//                portfolioVa.funds.eachWithIndex { fund, index ->
//                    if (fund != null) {
//                        //println "Creating panel for ${fund.name}"
//                        Map fundData = portfolioVa.portfolioData[fund].entries
//                        def volumeData = fundData.collectEntries { date, value ->
//                            def out = [:]
//                            out.put(date, value.investment)
//                            return out
//                        }
//
//                        panel(id:fund.name + index) {
//                            widget(new ChartPanel(createChart(fund, "va", volumeData)))
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

//frame.pack()
//frame.show()

//println europeData