package ru.edu.pr05;

import java.time.LocalDate;

public final class BusinessTrip {
    private static final int MIN_REASON_LENGTH = 10;
    static final CancellationPolicy VARIANT_8 = CancellationPolicy.minimumNotice(45);

    private final String id;
    private final LocalDate start;
    private final LocalDate end;
    private final CancellationPolicy cancellationPolicy;
    private TripStatus status = TripStatus.DRAFT;
    private String rejectionReason;

    public BusinessTrip(String id, LocalDate start, LocalDate end) {
        this(id, start, end, VARIANT_8);
    }

    public BusinessTrip(String id, LocalDate start, LocalDate end, CancellationPolicy cancellationPolicy) {
        if (trimWhitespace(id).isEmpty()) {
            throw new IllegalArgumentException("Идентификатор обязателен");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты начала и окончания обязательны");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Окончание раньше начала");
        }
        if (cancellationPolicy == null) {
            throw new IllegalArgumentException("Правило отмены обязательно");
        }
        this.id = id;
        this.start = start;
        this.end = end;
        this.cancellationPolicy = cancellationPolicy;
    }

    public void submit() {
        requireStatus(TripStatus.DRAFT);
        status = TripStatus.SUBMITTED;
    }

    public void approve() {
        requireStatus(TripStatus.SUBMITTED);
        status = TripStatus.APPROVED;
    }

    public void reject(String reason) {
        requireStatus(TripStatus.SUBMITTED);
        String text = trimWhitespace(reason);
        if (text.codePointCount(0, text.length()) < MIN_REASON_LENGTH) {
            throw new IllegalArgumentException("В причине должно быть хотя бы "
                    + MIN_REASON_LENGTH + " символов");
        }
        rejectionReason = text;
        status = TripStatus.REJECTED;
    }

    public void cancel() {
        cancel(LocalDate.now());
    }

    public void cancel(LocalDate today) {
        if (status != TripStatus.DRAFT && status != TripStatus.SUBMITTED
                && status != TripStatus.APPROVED) {
            throw new IllegalStateException("Отмена недоступна из " + status);
        }
        requireToday(today);
        cancellationPolicy.checkDate(today, start);
        status = TripStatus.CANCELLED;
    }

    public void complete(LocalDate today) {
        requireStatus(TripStatus.APPROVED);
        requireToday(today);
        if (!today.isAfter(end)) {
            throw new IllegalArgumentException("Завершение допустимо только после " + end);
        }
        status = TripStatus.COMPLETED;
    }

    private void requireStatus(TripStatus required) {
        if (status != required) {
            throw new IllegalStateException("Требуется " + required + ", текущий статус " + status);
        }
    }

    private static void requireToday(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("Текущая дата обязательна");
        }
    }

    private static String trimWhitespace(String value) {
        if (value == null) {
            return "";
        }
        int first = 0;
        int last = value.length();
        while (first < last && isWhitespace(value.charAt(first))) {
            first++;
        }
        while (last > first && isWhitespace(value.charAt(last - 1))) {
            last--;
        }
        return value.substring(first, last);
    }

    private static boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch) || Character.isSpaceChar(ch);
    }

    public String getId() {
        return id;
    }

    public TripStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
