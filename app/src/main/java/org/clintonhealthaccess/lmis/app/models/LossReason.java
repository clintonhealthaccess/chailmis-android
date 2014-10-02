package org.clintonhealthaccess.lmis.app.models;

import com.google.common.base.Predicate;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;

public enum LossReason {
    WASTED("Wastage"),
    MISSING("Missing"),
    EXPIRED("Expired"),
    VVM_CHANGE("VVM Change"),
    BREAKAGE("Breakage"),
    FROZEN("Frozen"),
    LABEL_REMOVED("Label Removed"),
    OTHERS("Others");

    private String label;

    LossReason(String label) {
        this.label = label;
    }

    public static LossReason of(CommodityAction commodityAction) {
        return valueOf(commodityAction.getActivityType());
    }

    public static List<CommodityAction> getLossCommodityActions(Commodity commodity) {
        return from(commodity.getCommodityActionsSaved()).filter(new Predicate<CommodityAction>() {
            @Override
            public boolean apply(CommodityAction input) {
                String activityType = input.getActivityType();
                return LossReason.isValidLossReason(activityType);
            }
        }).toList();
    }

    private static boolean isValidLossReason(String activityType) {
        try {
            valueOf(activityType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getLabel() {
        return label;
    }
}
