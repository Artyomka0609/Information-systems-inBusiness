package ru.edu.pr03;

/** Высокий риск требует согласования директором */
public final class HighRiskRule implements ApprovalRule {
    @Override
    public boolean matches(ApprovalRequest request) {
        switch (request.getRisk()) {
            case HIGH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public ApprovalRoute getRoute() { return ApprovalRoute.DIRECTOR_REVIEW; }
}
