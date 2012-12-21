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

FundData europe = new FundData("http://www.seligson.fi/graafit/data.asp?op=eurooppa", "Seligson Eurooppa", startDate)
FundData corporate = new FundData("http://www.seligson.fi/graafit/data.asp?op=eurocorporate", "Seligson Corporate Bond", startDate)
def obligaatio = new FundData("http://www.seligson.fi/graafit/data.asp?op=euroobligaatio", "Seligson Obligaatio", startDate)
def pharma = new FundData("http://www.seligson.fi/graafit/data.asp?op=global-pharma", "Seligson Pharma", startDate)
def russia = new FundData("http://www.seligson.fi/graafit/data.asp?op=russia", "Seligson Russia", startDate)
def brands = new FundData("http://www.seligson.fi/graafit/data.asp?op=global-brands", "Seligson Brands", startDate)
def aasia = new FundData("http://www.seligson.fi/graafit/data.asp?op=aasia", "Seligson Aasia", startDate)
def phoebus = new FundData("http://www.seligson.fi/graafit/data.asp?op=phoebus", "Seligson Phoebus", startDate)
def kehittyva = new FundData("http://www.seligson.fi/graafit/data.asp?op=kehittyva", "Seligson Kehittyvät", startDate)
def amerikka = new FundData("http://www.seligson.fi/graafit/data.asp?op=pohjoisamerikka", "Seligson Pohjois-amerikka", startDate)

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

Map fundSettings = [:]

Period period = new ForthnightlyPeriod()
//def period = monthlyPeriod

funds.each { fund, allocation ->
    fund.load(period)
    fundSettings[fund] = new FundRebalancingSettings(highLimit: 5, lowLimit: 0.01, allocation: allocation as Double)
}

OptimisticRebalancingStrategy ors = new OptimisticRebalancingStrategy(fundSettings: fundSettings)
//NoSellRebalancingStrategy ors = new NoSellRebalancingStrategy(fundSettings: fundSettings)

ValueAveragingInvestmentStrategy invStrat = new ValueAveragingInvestmentStrategy(startDate, 0.03, period.periodsPerYear, ors)
//DollarCostAveregingInvestmentStrategy invStrat = new DollarCostAveregingInvestmentStrategy(rebalancingStrategy: ors, period.periodsPerYear, 0.03)

//SellFromTheStartSellStrategy sellStrat = new SellFromTheStartSellStrategy()
LeastAmountOfProfitSellStrategy sellStrat = new LeastAmountOfProfitSellStrategy()
//MaxAmountOfProfitSellStrategy sellStrat = new MaxAmountOfProfitSellStrategy()

Portfolio portfolio = new Portfolio(funds.keySet().asList(), invStrat, 300, sellStrat)

LocalDate currentDate = startDate
def endDate = new LocalDate()
def data = [["va":[:]],["va":[:]]]
def lastInvested = 0.0
def lastValue = 0.0

while (currentDate.compareTo(endDate) < 0) {
    portfolio.doInvestmentRound(currentDate)

    lastInvested = portfolio.calculatePortfolioInvested(currentDate)
    lastValue = portfolio.calculatePortfolioValue(currentDate)
    data[0]["va"][currentDate] = lastInvested
    data[1]["va"][currentDate] = lastValue

    currentDate = period.getNextPeriodDate(currentDate)
}

println "Buffer was at the end ${portfolio.buffer}"
println "Total Tax Paid ${sellStrat.totalTaxPaid}"
println "End invested $lastInvested"
println "End value $lastValue"
println "End value with buffer ${lastValue + portfolio.buffer}"
println "Total profit at the end ${((lastValue)/lastInvested-1)*100}%"
println "Total profit at the end with buffer ${((lastValue+portfolio.buffer)/lastInvested-1)*100}%"
println "End total period investment $portfolio.currentPeriodInvestment"

static XYDataset createVolumeDataset(data) {
    return new TimeSeriesCollection(mapToTimeSeries(data, "Volume"))
}

private static mapToTimeSeries(map, name) {
    TimeSeries timeseries = new TimeSeries(name, org.jfree.data.time.Day.class);

    map.each { date, value ->
        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), value)
    }

    return timeseries
}

static XYDataset createPriceDataset(data) {
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
    xybarrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.00")));
    xyplot.setRenderer(1, xybarrenderer);
    return jfreechart;
}

JFreeChart createSummaryChart(data, bufferData) {
    TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

    //timeSeriesCollection.addSeries(mapToTimeSeries(data[0]["dca"], "Invested-DCA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[0]["va"], "Invested-VA"))
    //timeSeriesCollection.addSeries(mapToTimeSeries(data[1]["dca"], "NVA-DCA"))
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

def swing = new SwingBuilder()
def frame = swing.frame(title:'Groovy PieChart',
        defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
    panel(id:'MainPanel') {
        scrollPane(preferredSize: [1000,700], constraints: context.CENTER) {
            vbox {
                panel(id:"Summary") { widget(new ChartPanel(createSummaryChart(data, portfolio.bufferData)))}
                panel(id:"Allocation") { widget(new ChartPanel(createAllocationChart(portfolio)))}

                portfolio.funds.eachWithIndex { fund, index ->
                    if (fund != null) {
                        println "Creating panel for ${fund.name}"
                        Map fundData = portfolio.portfolioData[fund].entries
                        def volumeData = fundData.collectEntries { date, value ->
                            def out = [:]
                            out.put(date, value.investment)
                            return out
                        }

                        panel(id:fund.name + index) {
                            widget(new ChartPanel(createChart(fund, "va", volumeData)))
                        }
                    }
                }
            }
        }
    }
}

frame.pack()
frame.show()

//println europeData