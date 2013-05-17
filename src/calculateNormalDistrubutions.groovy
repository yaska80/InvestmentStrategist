import org.joda.time.LocalDate

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException

/**
 *
 * @author jaakkosj / Solita Oy<br>
 * Last changed by: $LastChangedBy: jaakkosj $
 *
 */



def periods = [new ForthnightlyPeriod(),
               new MonthlyPeriod(),
               new QuaterlyPeriod(),
               new BiAnnualPeriod(),
               new YearlyPeriod()]

def periodStrings = periods.collect {
    def name = it.class.getSimpleName().toUpperCase()
    name.substring(0, name.length() - "PERIOD".length())
}

if (args.length != 2){
    println "Please provide period and standard deviation multiplier (floating point number)\n\t" +
            "possible periods are: ${periodStrings}"
    System.exit(-1)
}
def userPeriod = args[0].toUpperCase().replaceAll("\\s", "") + "PERIOD"

def userMultiplier
try {
    NumberFormat format = NumberFormat.getNumberInstance();
    userMultiplier = format.parse(args[1]).doubleValue()
} catch (ParseException pe) {
    println "Provided standard deviation multiplier '${args[1]}' doesn't seem be a floating point number. Your decimal separator is ${DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator()}"
    System.exit(-1)
}

def period = periods.find {
    def className = it.class.getSimpleName().toUpperCase()
    className == userPeriod
}

if (period == null) {
    println "Unknown user provided period: ${args[0]}\n\tpossible values are: ${periodStrings}"
    System.exit(-1)
}


def startDate = new LocalDate(1990,1,7)
//Period period = new ForthnightlyPeriod()
//Period period = new MonthlyPeriod()
//Period period = new QuaterlyPeriod()
//Period period = new BiAnnualPeriod()
//Period period = new YearlyPeriod()


FundData europe = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=eurooppa", "Seligson Eurooppa", startDate)
FundData corporate = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=eurocorporate", "Seligson Corporate Bond", startDate)
def obligaatio = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=euroobligaatio", "Seligson Obligaatio", startDate)
def pharma = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=global-pharma", "Seligson Pharma", startDate)
def russia = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=russia", "Seligson Russia", startDate)
def brands = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=global-brands", "Seligson Brands", startDate)
def aasia = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=aasia", "Seligson Aasia", startDate)
def phoebus = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=phoebus", "Seligson Phoebus", startDate)
def kehittyva = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=kehittyva", "Seligson Kehittyvät", startDate)
def amerikka = new SeligsonFundData("http://www.seligson.fi/graafit/data.asp?op=pohjoisamerikka", "Seligson Pohjois-amerikka", startDate)

def funds = [europe,
            corporate,
            obligaatio,
            pharma,
            russia,
            brands,
            aasia,
            phoebus,
            kehittyva,
            amerikka]

def stdDevMultiplier = userMultiplier

println "Printing for period ${period.class.getSimpleName()} with StdDev multiplier ${stdDevMultiplier} and locale ${Locale.getDefault()}\n"


funds.each { fund ->
    fund.load(period)


    def negatMean = fund.getMeanOfNegativeChange()
    def negatStdDev = fund.getStandardDeviationOnNegativeChange() * stdDevMultiplier
    def posMean = fund.getMeanOfPositiveChange()
    def posStdDev = fund.getStandardDeviationOnPositiveChange() * stdDevMultiplier

    def negatUpper = negatMean + negatStdDev
    def negatLower = negatMean - negatStdDev
    def posUpper = posMean + posStdDev
    def posLower = posMean - posStdDev

    println "Fund ${fund.name}:\n" +
            "Mean: ${toP(fund.getMean())} StdDev: ${toP(fund.getStandardDeviation())} DropMean: ${toP(negatMean)} DropStdDev: ${toP(negatStdDev)} PlusMean: ${toP(posMean)} PlusStdDev: ${toP(posStdDev)}\n" +
            "NUP: ${toP(negatUpper)} NLOW: ${toP(negatLower)} PUP: ${toP(posUpper)} PLOW: ${posLower}\n"
}

def toP(Double value) {
    String.format("%05.2f", value*100.0)
}