package ru.edu.pr03;

/** Ускоренный маршрут применим только ниже финансового порога */
public final class UrgentRule implements ApprovalRule {
    private final long financeLimit;

    public UrgentRule(long financeLimit) {
        if (financeLimit < 0) {
            throw new IllegalArgumentException("limit must be non-negative");
        }
        this.financeLimit = financeLimit;
    }

    @Override
    public boolean matches(ApprovalRequest request) {
        return request.isUrgent() && request.getAmount() < financeLimit;
    }

    @Override
    public ApprovalRoute getRoute() { return ApprovalRoute.FAST_TRACK; }
}
