/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 6.11.2012
 * Time: 18.31
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.WindowConstants as WC

import groovy.swing.SwingBuilder
@groovy.lang.Grab(group = 'jfree', module = 'jfreechart', version = '1.0.13') import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYDataset
import org.joda.time.LocalDate

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.joda.time.Months
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import java.text.NumberFormat

private static JFreeChart createChart(def fund, strategy)
{
    XYDataset xydataset = createPriceDataset(fund.monthlyData);
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
    xyplot.setDataset(1, createVolumeDataset(fund.investedAmounts[strategy]));
    xyplot.setRangeAxis(1, numberaxis1);
    xyplot.mapDatasetToRangeAxis(1, 1);
    XYBarRenderer xybarrenderer = new XYBarRenderer(0.20000000000000001D);
    xybarrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.00")));
    xyplot.setRenderer(1, xybarrenderer);
    return jfreechart;
}

private static JFreeChart createSummaryChart(data) {
    TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

    timeSeriesCollection.addSeries(mapToTimeSeries(data[0]["dca"], "Invested-DCA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[0]["va"], "Invested-VA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[1]["dca"], "NVA-DCA"))
    timeSeriesCollection.addSeries(mapToTimeSeries(data[1]["va"], "NVA-VA"))

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

static XYDataset createVolumeDataset(data) {
//    TimeSeries timeseries = new TimeSeries("Volume", org.jfree.data.time.Day.class);
//
//    data.entrySet().each { it ->
//        def date = it.key
//        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), it.value)
//    }

    return new TimeSeriesCollection(mapToTimeSeries(data, "Volume"))
}

private static mapToTimeSeries(map, name) {
    TimeSeries timeseries = new TimeSeries(name, org.jfree.data.time.Day.class);

    map.entrySet().each { it ->
        def date = it.key
        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), it.value)
    }

    return timeseries
}

static XYDataset createPriceDataset(data) {
//    TimeSeries timeseries = new TimeSeries("Price", org.jfree.data.time.Day.class);
//
//    data.entrySet().each { it ->
//        def date = it.key
//        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), it.value)
//    }
    return new TimeSeriesCollection(mapToTimeSeries(data, "Price"))
}


def startDate = new LocalDate(2002,1,20)

def europe = new FundData("http://www.seligson.fi/graafit/data.asp?op=eurooppa", "Seligson Eurooppa", startDate)
europe.load()
def corporate = new FundData("http://www.seligson.fi/graafit/data.asp?op=eurocorporate", "Seligson Corporate Bond", startDate)
corporate.load()
def obligaatio = new FundData("http://www.seligson.fi/graafit/data.asp?op=euroobligaatio", "Seligson Obligaatio", startDate)
obligaatio.load()
def pharma = new FundData("http://www.seligson.fi/graafit/data.asp?op=global-pharma", "Seligson Pharma", startDate)
pharma.load()
def russia = new FundData("http://www.seligson.fi/graafit/data.asp?op=russia", "Seligson Russia", startDate)
russia.load()
def brands = new FundData("http://www.seligson.fi/graafit/data.asp?op=global-brands", "Seligson Brands", startDate)
brands.load()
def aasia = new FundData("http://www.seligson.fi/graafit/data.asp?op=aasia", "Seligson Aasia", startDate)
aasia.load()
def phoebus = new FundData("http://www.seligson.fi/graafit/data.asp?op=phoebus", "Seligson Phoebus", startDate)
phoebus.load()
def kehittyva = new FundData("http://www.seligson.fi/graafit/data.asp?op=kehittyva", "Seligson Kehittyvät", startDate)
kehittyva.load()
def amerikka = new FundData("http://www.seligson.fi/graafit/data.asp?op=pohjoisamerikka", "Seligson Pohjois-amerikka", startDate)
amerikka.load()

// eurooppa 15.06.1998
//eurocorporate 14.09.2001
//euroobligaatio 14.10.1998
//http://www.seligson.fi/graafit/data.asp?op=global-pharma 17.01.2000
//http://www.seligson.fi/graafit/data.asp?op=russia 08.03.2000
//http://www.seligson.fi/graafit/data.asp?op=global-brands    18.06.1998
//http://www.seligson.fi/graafit/data.asp?op=aasia 29.12.1999
//http://www.seligson.fi/graafit/data.asp?op=phoebus 10.10.2001
//http://www.seligson.fi/graafit/data.asp?op=kehittyva 03.09.2010
//http://www.seligson.fi/graafit/data.asp?op=pohjoisamerikka 29.12.2006


def funds = [:]
// Osakkeet
funds.put(europe, 0.2)
funds.put(russia, 0.2)
funds.put(phoebus, 0.2)
funds.put(aasia, 0.2)
funds.put(brands, 0.05)
funds.put(pharma, 0.05)
// Pitkät korot
funds.put(corporate, 0.05)
funds.put(obligaatio, 0.05)


def dollarCostAveraging(date, fund, fundAllocation, totalValue, totalMonthlyInvestment, target) {
    def investment = totalMonthlyInvestment * fundAllocation

    fund.investToFund(investment, date, "dca")
}

def valueAveraging(date, fund, fundAllocation, totalValue, totalMonthlyInvestment, target) {
    def fundsTarget = target * fundAllocation
    def fundValue = fund.getValueForDate(date, "va")
    def investment = fundsTarget - fundValue
    investment = investment < 0.0 ? 0.0 : investment

    fund.investToFund(investment, date, "va")
}



def swing = new SwingBuilder()
def frame = swing.frame(title:'Groovy PieChart',
        defaultCloseOperation:WC.EXIT_ON_CLOSE) {
    //gridLayout(cols: 1, rows: 1)


    //flowLayout()
    def data = doInvestments(funds, 600, startDate)

    panel() {
        scrollPane(preferredSize: [1000,1000], constraints: context.CENTER) {
            vbox {
                funds.keySet().each { iter ->
                    def fund = iter
                    if (fund != null) {

                        println "Creating panel for ${fund.name}"
                        panel(id:fund.name) {widget(new ChartPanel(createChart(fund, "va")))}
                    }
                }

                panel(id:"Allocations") { widget(new ChartPanel(createAllocationChart(funds)))}
                panel(id:"Summary") { widget(new ChartPanel(createSummaryChart(data)))}
            }
        }
    }


    //panel(id:europe.name + "_va") { widget(new ChartPanel(createChart(europe,"va"))) }
    //panel(id:corporate.name + "_va") { widget(new ChartPanel(createChart(corporate,"va")))}

    //panel(id:europe.name + "_dca") { widget(new ChartPanel(createChart(europe,"dca"))) }
    //panel(id:corporate.name + "_dca") { widget(new ChartPanel(createChart(corporate,"dca")))}

    // Summary
}

private static JFreeChart createAllocationChart(def funds) {
    def dataset = new DefaultPieDataset()


    funds.keySet().each {
        dataset.setValue(it.name, it.getValueForDate(new LocalDate(2012,9,20),"va"))
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

def doInvestments(funds, totalMonthlyInvestment, startDate) {
    def currentDate = startDate
    def endDay = new LocalDate()
    def target = totalMonthlyInvestment
    def portfolioProgress = ["dca":[:], "va":[:]]
    def totalInvestments = ["dca":[:], "va":[:]]

    // VA related
    def monthsBetween = Months.monthsBetween(startDate, endDay).getMonths()
    def r = (0.8/monthsBetween)
    def period = 1

    while (currentDate.compareTo(endDay) < 0) {
        def dca = 0.0
        def va = 0.0

        target = totalMonthlyInvestment * period * Math.pow(1.0 + r, period)

        println "Calculatin for date ${currentDate} with period of ${period} and target of ${target}"

        totalInvestments["dca"][currentDate] = 0.0
        totalInvestments["va"][currentDate] = 0.0

        for (it in funds.entrySet()) {
            dca += dollarCostAveraging(currentDate, it.key, it.value, 0, totalMonthlyInvestment, target)
            va += valueAveraging(currentDate, it.key, it.value, 0, totalMonthlyInvestment, target)

            totalInvestments["dca"][currentDate] += it.key.getInvestedAmount(currentDate, "dca")
            totalInvestments["va"][currentDate] += it.key.getInvestedAmount(currentDate, "va")
        }

        portfolioProgress["dca"][currentDate] = dca
        portfolioProgress["va"][currentDate] = va

        //target += totalMonthlyInvestment

        currentDate = currentDate.plusMonths(1)
        period++
    }

    [totalInvestments, portfolioProgress]

}

frame.pack()
frame.show()