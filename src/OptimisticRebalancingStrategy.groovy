
/**
 * Created with IntelliJ IDEA.
 * User: jaakkosjoholm
 * Date: 29.11.2012
 * Time: 22.17
 * To change this template use File | Settings | File Templates.
 */
class OptimisticRebalancingStrategy {
    Map fundSettings

    Double rebalance(FundData fund, Double fundCurrentValue, Double portfolioTargetValue) {
        FundRebalancingSettings settings = fundSettings[fund] as FundRebalancingSettings

        Double fundTargetValue = portfolioTargetValue * settings.allocation
        Double rebalancedInvestment = 0.0
        Double highLimitTarget = fundTargetValue * (1 + settings.highLimit)
        Double lowLimitTarget = fundTargetValue * (1 - settings.lowLimit)

        if (fundCurrentValue > highLimitTarget) {
            rebalancedInvestment = fundTargetValue - fundCurrentValue
        } else if (fundCurrentValue < lowLimitTarget) {
            rebalancedInvestment = fundTargetValue - fundCurrentValue
        }

        return rebalancedInvestment
    }
}

class ToAllocationRebalancingStrategy {
    Map fundSettings

    Double rebalance(FundData fund, Double fundCurrentValue, Double portfolioTargetValue) {
        FundRebalancingSettings settings = fundSettings[fund] as FundRebalancingSettings

        Double fundTargetValue = portfolioTargetValue * settings.allocation
        return fundTargetValue - fundCurrentValue
    }
}

class NoSellRebalancingStrategy {
    Map fundSettings

    Double rebalance(FundData fund, Double fundCurrentValue, Double portfolioTargetValue) {
        FundRebalancingSettings settings = fundSettings[fund] as FundRebalancingSettings
        Double fundTargetValue = portfolioTargetValue * settings.allocation
        Double investment = fundTargetValue - fundCurrentValue

        return investment < 0.0 ? 0.0 : investment
    }
}

class FundRebalancingSettings {
    Double highLimit, lowLimit
    Double allocation
}
