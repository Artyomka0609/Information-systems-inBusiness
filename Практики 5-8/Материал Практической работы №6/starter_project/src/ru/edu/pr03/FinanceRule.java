package ru.edu.pr03;

/** Проверка включительной границы финансового согласования */
public final class FinanceRule implements ApprovalRule {
    private final long limit;

    public FinanceRule(long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be non-negative");
        }
        this.limit = limit;
    }

    @Override
    public boolean matches(ApprovalRequest request) {
        return request.getAmount() >= limit;
    }

    @Override
    public ApprovalRoute getRoute() { return ApprovalRoute.FINANCE_REVIEW; }
}
