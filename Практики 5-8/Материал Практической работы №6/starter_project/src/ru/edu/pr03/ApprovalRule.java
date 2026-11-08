package ru.edu.pr03;

public interface ApprovalRule {
    boolean matches(ApprovalRequest request);
    ApprovalRoute getRoute();
}
