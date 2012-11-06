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

import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import org.jfree.chart.renderer.xy.XYBarRenderer

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

static XYDataset createVolumeDataset(data) {
    TimeSeries timeseries = new TimeSeries("Volume", org.jfree.data.time.Day.class);

    data.entrySet().each { it ->
        def date = it.key
        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), it.value)
    }

    return new TimeSeriesCollection(timeseries)
}

static XYDataset createPriceDataset(data) {
    TimeSeries timeseries = new TimeSeries("Price", org.jfree.data.time.Day.class);

    data.entrySet().each { it ->
        def date = it.key
        timeseries.add(new Day(date.dayOfMonth, date.monthOfYear, date.year), it.value)
    }
    return new TimeSeriesCollection(timeseries)
}


def startDate = new LocalDate(2002,1,20)

def europe = new FundData("http://www.seligson.fi/graafit/data.asp?op=eurooppa", "Seligson Eurooppa", startDate)
europe.load()
def corporate = new FundData("http://www.seligson.fi/graafit/data.asp?op=eurocorporate", "Seligson Corporate Bond", startDate)
corporate.load()

def funds = [:]
funds.put(europe, 0.5)
funds.put(corporate, 0.5)


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
    gridLayout(cols: 2, rows: 2)

    def data = doInvestments(funds, 600, startDate)

    panel(id:europe.name + "_va") { widget(new ChartPanel(createChart(europe,"va"))) }
    panel(id:corporate.name + "_va") { widget(new ChartPanel(createChart(corporate,"va")))}

    panel(id:europe.name + "_dca") { widget(new ChartPanel(createChart(europe,"dca"))) }
    panel(id:corporate.name + "_dca") { widget(new ChartPanel(createChart(corporate,"dca")))}
}

def doInvestments(funds, totalMonthlyInvestment, startDate) {
    def currentDate = startDate
    def endDay = new LocalDate()
    def target = totalMonthlyInvestment
    def portfolioProgress = ["dca":[:], "va":[:]]
    def totalInvestments = ["dca":[:], "va":[:]]



    while (currentDate.compareTo(endDay) < 0) {
        def dca = 0.0
        def va = 0.0

        for (it in funds.entrySet()) {
            dca += dollarCostAveraging(currentDate, it.key, it.value, 0, totalMonthlyInvestment, target)
            va += valueAveraging(currentDate, it.key, it.value, 0, totalMonthlyInvestment, target)

            if (totalInvestments["dca"][currentDate] == null) totalInvestments["dca"][currentDate] = 0.0
            if (totalInvestments["va"][currentDate] == null) totalInvestments["va"][currentDate] = 0.0

            totalInvestments["dca"][currentDate] += it.key.getInvestedAmount(currentDate, "dca")
            totalInvestments["va"][currentDate] += it.key.getInvestedAmount(currentDate, "va")
        }

        portfolioProgress["dca"][currentDate] = dca
        portfolioProgress["va"][currentDate] = va

        target += totalMonthlyInvestment

        currentDate = currentDate.plusMonths(1)
    }

    [totalInvestments, portfolioProgress]

}

frame.pack()
frame.show()