package ru.edu.pr03;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Выбирает самый строгий из подходящих маршрутов*/
public final class ApprovalRouter {
    
    public static final long FINANCE_LIMIT = DefaultApprovalRules.FINANCE_LIMIT;
    private final List<ApprovalRule> rules;

    public ApprovalRouter() {
        this(DefaultApprovalRules.create());
    }

    /** Дополнительное правило подключается без изменения алгоритма */
    public ApprovalRouter(List<ApprovalRule> rules) {
        if (rules == null) {
            throw new IllegalArgumentException("rules are required");
        }
        List<ApprovalRule> copy = new ArrayList<ApprovalRule>(rules);
        for (ApprovalRule rule : copy) {
            if (rule == null) {
                throw new IllegalArgumentException("rule must not be null");
            }
        }
        this.rules = Collections.unmodifiableList(copy);
    }

    public ApprovalRoute route(long amount, boolean urgent, RiskLevel risk,
                               boolean containsPersonalData) {
        return route(new ApprovalRequest(amount, urgent, risk, containsPersonalData));
    }

    public ApprovalRoute route(ApprovalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        ApprovalRoute result = ApprovalRoute.STANDARD;
        for (ApprovalRule rule : rules) {
            if (rule.matches(request)) {
                ApprovalRoute candidate = rule.getRoute();
                if (candidate == null) {
                    throw new IllegalStateException("rule returned a null route");
                }
                if (candidate.getPriority() > result.getPriority()) {
                    result = candidate;
                }
            }
        }
        return result;
    }
}
