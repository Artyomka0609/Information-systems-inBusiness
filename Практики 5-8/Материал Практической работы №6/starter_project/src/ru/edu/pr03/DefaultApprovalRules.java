package ru.edu.pr03;

import java.util.Arrays;
import java.util.List;

public final class DefaultApprovalRules {
    public static final long FINANCE_LIMIT = 500_000L;

    private DefaultApprovalRules() { }

    public static List<ApprovalRule> create() {
        return create(FINANCE_LIMIT);
    }

    public static List<ApprovalRule> create(long financeLimit) {
        return Arrays.asList(
                new SecurityRule(),
                new HighRiskRule(),
                new FinanceRule(financeLimit),
                new UrgentRule(financeLimit));
    }
}
