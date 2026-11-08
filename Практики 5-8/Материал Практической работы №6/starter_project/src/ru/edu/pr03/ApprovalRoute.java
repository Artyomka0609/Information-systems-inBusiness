package ru.edu.pr03;

/** Вариант 8 */
public enum ApprovalRoute {
    STANDARD(0),
    FAST_TRACK(10),
    FINANCE_REVIEW(40),
    DIRECTOR_REVIEW(45),
    SECURITY_REVIEW(100);

    private final int priority;

    ApprovalRoute(int priority) { this.priority = priority; }

    public int getPriority() { return priority; }
}
