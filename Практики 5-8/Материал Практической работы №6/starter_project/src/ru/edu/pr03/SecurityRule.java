package ru.edu.pr03;

/** Персональные данные требуют проверки безопасности */
public final class SecurityRule implements ApprovalRule {
    @Override
    public boolean matches(ApprovalRequest request) {
        return request.containsPersonalData();
    }

    @Override
    public ApprovalRoute getRoute() {
         return ApprovalRoute.SECURITY_REVIEW; 
        }
}
