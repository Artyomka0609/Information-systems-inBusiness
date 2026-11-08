package ru.edu.pr05;

import java.time.LocalDate;
import java.time.YearMonth;

public class SelfCheck {
    private static final LocalDate START = LocalDate.of(2026, 10, 10);
    private static final LocalDate END = LocalDate.of(2026, 10, 12);
    private static int passed;
    private static int failed;

    private static void check(boolean condition) {
        if (!condition) { throw new AssertionError("Неверный результат"); }
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable operation) {
        try { operation.run(); }
        catch (RuntimeException ex) {
            check(type.isInstance(ex));
            return;
        }
        throw new AssertionError("Ожидалось " + type.getSimpleName());
    }

    private static void test(String name, Runnable operation) {
        try {
            operation.run(); passed++;
            System.out.println("PASS " + name);
        } catch (AssertionError | RuntimeException ex) {
            failed++;
            System.out.println("FAIL " + name + ": " + ex.getMessage());
        }
    }

    private static BusinessTrip submitted() {
        BusinessTrip trip = new BusinessTrip("T", START, END);
        trip.submit();
        return trip;
    }

    private static BusinessTrip approved() {
        BusinessTrip trip = submitted();
        trip.approve();
        return trip;
    }

    private static void checkPolicyContract(CancellationPolicy policy, LocalDate allowed, LocalDate denied) {
        BusinessTripService service = new BusinessTripService(policy);
        BusinessTrip trip = service.create("POLICY", START, END);
        trip.submit();
        expect(IllegalArgumentException.class, () -> trip.cancel(null));
        expect(IllegalArgumentException.class, () -> trip.cancel(denied));
        check(trip.getStatus() == TripStatus.SUBMITTED && trip.getRejectionReason() == null);
        trip.cancel(allowed);
        check(trip.getStatus() == TripStatus.CANCELLED);
        expect(IllegalStateException.class, () -> trip.cancel(allowed));
    }

    public static void main(String[] args) {
        test("Типовая заявка и поиск по статусу", () -> {
            BusinessTripService service = new BusinessTripService();
            BusinessTrip trip = service.create("T", START, END);
            check(trip.getStatus() == TripStatus.DRAFT);
            trip.submit();
            check(service.findByStatus(TripStatus.SUBMITTED).contains(trip));
        });
        test("Начало и окончание в один день", () -> {
            BusinessTrip trip = new BusinessTrip("T", START, START);
            trip.submit(); check(trip.getStatus() == TripStatus.SUBMITTED);
        });
        test("Окончание раньше начала", () -> expect(IllegalArgumentException.class,
                () -> new BusinessTrip("T", START, START.minusDays(1))));
        test("Отсутствующие обязательные данные", () -> {
            expect(IllegalArgumentException.class, () -> new BusinessTrip(null, START, END));
            expect(IllegalArgumentException.class, () -> new BusinessTrip("", START, END));
            expect(IllegalArgumentException.class, () -> new BusinessTrip("T", null, END));
            expect(IllegalArgumentException.class, () -> new BusinessTrip("T", START, null));
        });
        test("Неверный формат и календарная дата", () -> {
            expect(IllegalArgumentException.class, () -> Application.parseDate("10.10.2026"));
            expect(IllegalArgumentException.class, () -> Application.parseDate("2026-02-29"));
        });
        test("Причина из 9 символов", () -> {
            BusinessTrip trip = submitted();
            expect(IllegalArgumentException.class, () -> trip.reject("123456789"));
            check(trip.getStatus() == TripStatus.SUBMITTED && trip.getRejectionReason() == null);
        });
        test("Причина из 10 символов", () -> {
            BusinessTrip trip = submitted(); trip.reject("1234567890");
            check(trip.getStatus() == TripStatus.REJECTED);
            check("1234567890".equals(trip.getRejectionReason()));
        });
        test("Пустая или отсутствующая причина", () -> {
            BusinessTrip trip = submitted();
            expect(IllegalArgumentException.class, () -> trip.reject(null));
            expect(IllegalArgumentException.class, () -> trip.reject(""));
        });
        test("Одобрение и отклонение только из SUBMITTED", () -> {
            BusinessTrip draft = new BusinessTrip("T", START, END);
            expect(IllegalStateException.class, () -> draft.approve());
            expect(IllegalStateException.class, () -> draft.reject("1234567890"));
            BusinessTrip trip = approved();
            expect(IllegalStateException.class, () -> trip.reject("1234567890"));
        });
        test("Отмена из DRAFT, SUBMITTED и APPROVED за 45 дней", () -> {
            BusinessTrip[] trips = {new BusinessTrip("T", START, END), submitted(), approved()};
            for (BusinessTrip trip : trips) {
                trip.cancel(START.minusDays(45)); check(trip.getStatus() == TripStatus.CANCELLED);
            }
        });
        test("Завершение до окончания и в дату окончания", () -> {
            BusinessTrip trip = approved();
            expect(IllegalArgumentException.class, () -> trip.complete(END.minusDays(1)));
            expect(IllegalArgumentException.class, () -> trip.complete(END));
            expect(IllegalArgumentException.class, () -> trip.complete(null));
            check(trip.getStatus() == TripStatus.APPROVED);
        });
        test("Завершение на следующий день", () -> {
            BusinessTrip trip = approved(); trip.complete(END.plusDays(1));
            check(trip.getStatus() == TripStatus.COMPLETED);
            expect(IllegalStateException.class, () -> submitted().complete(END.plusDays(1)));
        });
        test("Запрет отмены из конечных состояний", () -> {
            BusinessTrip completed = approved(); completed.complete(END.plusDays(1));
            BusinessTrip rejected = submitted(); rejected.reject("1234567890");
            BusinessTrip cancelled = submitted(); cancelled.cancel(START.minusDays(45));
            for (BusinessTrip trip : new BusinessTrip[]{completed, rejected, cancelled}) {
                expect(IllegalStateException.class, () -> trip.cancel(START.minusDays(45)));
            }
        });
        test("Повторная подача заявки", () -> {
            BusinessTrip trip = submitted();
            expect(IllegalStateException.class, () -> trip.submit());
            check(trip.getStatus() == TripStatus.SUBMITTED);
        });
        test("Отмена за 46 дней", () -> {
            BusinessTrip trip = submitted();
            trip.cancel(START.minusDays(46));
            check(trip.getStatus() == TripStatus.CANCELLED);
        });
        test("Запрет отмены за 44 дня во всех допустимых статусах", () -> {
            BusinessTrip[] trips = {new BusinessTrip("T", START, END), submitted(), approved()};
            for (BusinessTrip trip : trips) {
                TripStatus before = trip.getStatus();
                expect(IllegalArgumentException.class, () -> trip.cancel(START.minusDays(44)));
                check(trip.getStatus() == before && trip.getRejectionReason() == null);
            }
        });
        test("Запрет отмены в день начала и после начала", () -> {
            BusinessTrip trip = approved();
            expect(IllegalArgumentException.class, () -> trip.cancel(START));
            expect(IllegalArgumentException.class, () -> trip.cancel(START.plusDays(1)));
            check(trip.getStatus() == TripStatus.APPROVED);
        });
        test("Отмена без текущей даты", () -> {
            BusinessTrip trip = submitted();
            expect(IllegalArgumentException.class, () -> trip.cancel(null));
            check(trip.getStatus() == TripStatus.SUBMITTED);
        });
        test("Отмена без аргумента также проверяет срок", () -> {
            LocalDate now = LocalDate.now();
            BusinessTrip allowed = new BusinessTrip("OK", now.plusDays(46), now.plusDays(47));
            allowed.cancel();
            check(allowed.getStatus() == TripStatus.CANCELLED);
            BusinessTrip denied = new BusinessTrip("NO", now.plusDays(44), now.plusDays(45));
            expect(IllegalArgumentException.class, () -> denied.cancel());
            check(denied.getStatus() == TripStatus.DRAFT);
        });
        test("45 календарных дней через границу года", () -> {
            BusinessTrip trip = new BusinessTrip("YEAR", LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 12));
            trip.submit();
            trip.cancel(LocalDate.of(2026, 11, 26));
            check(trip.getStatus() == TripStatus.CANCELLED);
        });
        test("Пробелы по краям не удлиняют причину", () -> {
            BusinessTrip trip = submitted();
            expect(IllegalArgumentException.class, () -> trip.reject("   123456789   "));
            expect(IllegalArgumentException.class, () -> trip.reject("\u00a0коротко\u2003\u2003"));
            check(trip.getStatus() == TripStatus.SUBMITTED && trip.getRejectionReason() == null);
            trip.reject("  Нет бюджета  ");
            check("Нет бюджета".equals(trip.getRejectionReason()));
        });
        test("Один символ вне BMP считается один раз", () -> {
            BusinessTrip trip = submitted();
            expect(IllegalArgumentException.class, () -> trip.reject("12345678🚆"));
            trip.reject("123456789🚆");
            check(trip.getStatus() == TripStatus.REJECTED);
        });
        test("Идентификатор из пробельных символов", () ->
                expect(IllegalArgumentException.class, () -> new BusinessTrip(" \t\u00a0", START, END)));
        test("Контракт отмены для стандартного правила", () ->
                checkPolicyContract(BusinessTrip.VARIANT_8, START.minusDays(45), START.minusDays(44)));
        test("Тот же контракт для иной реализации правила", () -> {
            CancellationPolicy sameMonth = (today, start) -> {
                if (!YearMonth.from(today).equals(YearMonth.from(start)) || today.isAfter(start)) {
                    throw new IllegalArgumentException("Отмена доступна в месяце поездки до её начала включительно");
                }
            };
            checkPolicyContract(sameMonth, START, START.withDayOfMonth(1).minusDays(1));
        });
        test("Настройка порога и проверка конфигурации", () -> {
            checkPolicyContract(CancellationPolicy.minimumNotice(15), START.minusDays(15), START.minusDays(14));
            expect(IllegalArgumentException.class, () -> CancellationPolicy.minimumNotice(-1));
            expect(IllegalArgumentException.class, () -> new BusinessTripService(null));
            expect(IllegalArgumentException.class, () -> new BusinessTrip("T", START, END, null));
        });
        test("Новую команду можно зарегистрировать без изменения диспетчера", () -> {
            TripCommands commands = new TripCommands(() -> null, () -> START);
            commands.register("SEND", BusinessTrip::submit);
            expect(IllegalArgumentException.class, () -> commands.register("SEND", BusinessTrip::approve));
            BusinessTrip trip = new BusinessTrip("T", START, END);
            commands.execute("SEND", trip);
            check(trip.getStatus() == TripStatus.SUBMITTED);
        });
        test("Ошибка в строке CSV не оставляет заявку в сервисе", () -> {
            BusinessTripService service = new BusinessTripService();
            TripCommands commands = new TripCommands(() -> null, () -> START);
            CsvTripReader csv = new CsvTripReader(service, commands, System.out);
            String row = "CSV;" + START + ";" + END + ";";
            expect(IllegalStateException.class, () -> csv.importRow(row + "APPROVE"));
            expect(IllegalArgumentException.class, () -> csv.importRow(row + "UNKNOWN"));
            check(service.findByStatus(TripStatus.DRAFT).isEmpty());
            BusinessTrip saved = csv.importRow(row + "SUBMIT");
            check("CSV".equals(saved.getId()));
            check(service.findByStatus(TripStatus.SUBMITTED).size() == 1);
            check(service.findByStatus(TripStatus.SUBMITTED).get(0) == saved);
        });
        System.out.println("TOTAL: " + passed + " PASS, " + failed + " FAIL");
        if (failed != 0) { throw new AssertionError("Проверки не пройдены"); }
    }
}
