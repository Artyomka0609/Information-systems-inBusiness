package ru.edu.pr03;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SelfCheck {
    private static int passed;
    private static int failed;

    private SelfCheck() { }

    public static void main(String[] args) throws IOException {
        checkRoutingAndContracts();
        checkInvalidInput();
        checkExtensionAndConfiguration();
        checkCsvApplication();
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void checkRoutingAndContracts() {
        ApprovalRouter router = new ApprovalRouter();
        List<ApprovalRule> reversed = new ArrayList<ApprovalRule>(DefaultApprovalRules.create());
        Collections.reverse(reversed);
        ApprovalRouter reverseRouter = new ApprovalRouter(reversed);
        long limit = ApprovalRouter.FINANCE_LIMIT;
        long[] amounts = {0, 1, limit - 1, limit, limit + 1, Long.MAX_VALUE};
        boolean[] flags = {false, true};
        int combinations = 0;
        for (long amount : amounts) {
            for (boolean urgent : flags) {
                for (RiskLevel risk : RiskLevel.values()) {
                    for (boolean personal : flags) {
                        ApprovalRequest request = new ApprovalRequest(amount, urgent, risk, personal);
                      
                        ApprovalRoute expected;
                        if (personal) {
                            expected = ApprovalRoute.SECURITY_REVIEW;
                        } else if (risk == RiskLevel.HIGH) {
                            expected = ApprovalRoute.DIRECTOR_REVIEW;
                        } else if (amount >= limit) {
                            expected = ApprovalRoute.FINANCE_REVIEW;
                        } else if (urgent) {
                            expected = ApprovalRoute.FAST_TRACK;
                        } else {
                            expected = ApprovalRoute.STANDARD;
                        }
                        String name = amount + "/" + urgent + "/" + risk + "/" + personal;
                        check("route " + name, router.route(amount, urgent, risk, personal) == expected);
                        check("reverse order " + name, reverseRouter.route(request) == expected);
                        for (ApprovalRule rule : reversed) {
                            checkContract(rule, request, name);
                        }
                        combinations++;
                    }
                }
            }
        }
        System.out.println("Checked " + combinations
                + " input combinations, both rule orders and all 4 rule contracts");
    }

    private static void checkContract(ApprovalRule rule, ApprovalRequest request, String name) {
        try {
            long amount = request.getAmount();
            boolean urgent = request.isUrgent();
            RiskLevel risk = request.getRisk();
            boolean personal = request.containsPersonalData();
            boolean firstMatch = rule.matches(request);
            ApprovalRoute firstRoute = rule.getRoute();
            check("contract " + rule.getClass().getSimpleName() + " " + name,
                    firstMatch == rule.matches(request)
                    && firstRoute != null && firstRoute == rule.getRoute()
                    && amount == request.getAmount() && urgent == request.isUrgent()
                    && risk == request.getRisk() && personal == request.containsPersonalData());
        } catch (RuntimeException e) {
            check("contract threw " + rule.getClass().getSimpleName() + " " + name, false);
        }
    }

    private static void checkInvalidInput() {
        RequestCsvParser parser = new RequestCsvParser();
        ApprovalRequest spaced = parser.parse(" 100 ; TRUE ; low ; false ");
        check("CSV whitespace and case", new ApprovalRouter().route(spaced) == ApprovalRoute.FAST_TRACK);
        check("CSV maximum long", parser.parse("9223372036854775807;false;LOW;false")
                .getAmount() == Long.MAX_VALUE);
        String[] invalidLines = {
                "-1;false;LOW;false", ";false;LOW;false", "abc;false;LOW;false",
                "9223372036854775808;false;LOW;false", "100;yes;LOW;false",
                "100;;LOW;false", "100;false;UNKNOWN;false", "100;false;;false",
                "100;false;LOW;", "100;false;LOW;1", "100;false;LOW",
                "100;false;LOW;false;extra", "1.5;false;LOW;false",
                "+1;false;LOW;false", "", null
        };
        for (String line : invalidLines) {
            expectError("invalid CSV " + line, () -> parser.parse(line));
        }
        expectError("negative amount via API", () -> new ApprovalRouter().route(-1, false, RiskLevel.LOW, false));
        expectError("null risk via API", () -> new ApprovalRouter().route(0, false, null, false));
        expectError("null request", () -> new ApprovalRouter().route((ApprovalRequest) null));
        expectError("null rules", () -> new ApprovalRouter(null));
        expectError("null rule", () -> new ApprovalRouter(Collections.singletonList(null)));
        expectError("negative threshold", () -> DefaultApprovalRules.create(-1));
        expectError("negative finance threshold", () -> new FinanceRule(-1));
        expectError("negative urgent threshold", () -> new UrgentRule(-1));
        expectError("null amount text", () -> RequestCsvParser.parseAmount(null));
        expectError("null boolean text", () -> RequestCsvParser.parseBoolean(null, "urgent"));
        expectError("null risk text", () -> RequestCsvParser.parseRisk(null));
        expectError("wrong header", () -> parser.validateHeader("risk;amount;urgent;personal"));
        expectError("missing header", () -> parser.validateHeader(null));
    }

    private static void checkExtensionAndConfiguration() {
        List<ApprovalRule> rules = new ArrayList<ApprovalRule>(DefaultApprovalRules.create());
        ApprovalRouter original = new ApprovalRouter(rules);
        // Новое условие подключается через общий контракт
        rules.add(new ApprovalRule() {
            @Override
            public boolean matches(ApprovalRequest request) {
                return request.getRisk() == RiskLevel.MEDIUM;
            }

            @Override
            public ApprovalRoute getRoute() { return ApprovalRoute.FINANCE_REVIEW; }
        });
        ApprovalRouter extended = new ApprovalRouter(rules);
        check("new rule changes result", extended.route(100, false, RiskLevel.MEDIUM, false)
                == ApprovalRoute.FINANCE_REVIEW);
        check("new rule keeps security priority", extended.route(100, true, RiskLevel.MEDIUM, true)
                == ApprovalRoute.SECURITY_REVIEW);
        check("original list is copied", original.route(100, false, RiskLevel.MEDIUM, false)
                == ApprovalRoute.STANDARD);
        rules.clear();
        check("clearing caller list cannot change router", extended.route(100, false, RiskLevel.MEDIUM, false)
                == ApprovalRoute.FINANCE_REVIEW);
        check("no matching rules means standard", new ApprovalRouter(Collections.<ApprovalRule>emptyList())
                .route(0, false, RiskLevel.LOW, false) == ApprovalRoute.STANDARD);

        ApprovalRouter configured = new ApprovalRouter(DefaultApprovalRules.create(100));
        check("custom threshold below", configured.route(99, true, RiskLevel.LOW, false)
                == ApprovalRoute.FAST_TRACK);
        check("custom threshold exact", configured.route(100, true, RiskLevel.LOW, false)
                == ApprovalRoute.FINANCE_REVIEW);
        check("custom threshold above", configured.route(101, false, RiskLevel.LOW, false)
                == ApprovalRoute.FINANCE_REVIEW);
        check("custom threshold keeps risk priority", configured.route(100, true, RiskLevel.HIGH, false)
                == ApprovalRoute.DIRECTOR_REVIEW);
        check("zero threshold", new ApprovalRouter(DefaultApprovalRules.create(0))
                .route(0, true, RiskLevel.LOW, false) == ApprovalRoute.FINANCE_REVIEW);
    }

    private static void checkCsvApplication() throws IOException {
        String header = "amount;urgent;risk;personal\n";
        checkFile("BOM header and valid rows", "\uFEFF" + header
                + "499999;true;LOW;false\n500000;true;LOW;false\n", 0,
                "line;route\n2;FAST_TRACK\n3;FINANCE_REVIEW\n", "");
        checkFile("continue after invalid row", header
                + "100;yes;LOW;false\n100;false;LOW;false\n", 1,
                "line;route\n3;STANDARD\n", "ERROR line 2: urgent must be true or false\n");
        checkFile("empty file", "", 2, "",
                "ERROR: expected header: amount;urgent;risk;personal\n");
        checkFile("header only", header, 2, "", "ERROR: CSV contains no requests\n");
        checkFile("bad header", "wrong\n100;false;LOW;false\n", 2, "",
                "ERROR: expected header: amount;urgent;risk;personal\n");
        checkFile("empty data row", header + "\n100;false;LOW;false\n", 1,
                "line;route\n3;STANDARD\n", "ERROR line 2: expected 4 fields\n");
        checkFile("duplicate requests remain separate", header
                + "100;false;LOW;false\n100;false;LOW;false\n", 0,
                "line;route\n2;STANDARD\n3;STANDARD\n", "");
        try (PrintStream sink = new PrintStream(new ByteArrayOutputStream())) {
            ApprovalApplication app = new ApprovalApplication(new ApprovalRouter(), new RequestCsvParser());
            check("too many arguments", app.run(new String[]{"a", "b"}, sink, sink) == 2);
            Path directory = Files.createTempDirectory("approval-selfcheck-");
            try {
                check("missing file", app.run(new String[]{directory.resolve("missing.csv").toString()}, sink, sink) == 2);
            } finally {
                Files.deleteIfExists(directory);
            }
            check("invalid path", app.run(new String[]{"\u0000"}, sink, sink) == 2);
        }
    }

    private static void checkFile(String name, String csv, int expectedCode,
                                  String expectedOut, String expectedErr) throws IOException {
        Path file = Files.createTempFile("approval-selfcheck-", ".csv");
        try {
            Files.write(file, csv.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            try (PrintStream out = new PrintStream(stdout, true, "UTF-8");
                 PrintStream err = new PrintStream(stderr, true, "UTF-8")) {
                ApprovalApplication app = new ApprovalApplication(new ApprovalRouter(), new RequestCsvParser());
                check(name + " exit code", app.run(new String[]{file.toString()}, out, err) == expectedCode);
            }
            check(name + " stdout", stdout.toString("UTF-8").replace("\r\n", "\n").equals(expectedOut));
            check(name + " stderr", stderr.toString("UTF-8").replace("\r\n", "\n").equals(expectedErr));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void expectError(String name, Runnable action) {
        try {
            action.run();
            check(name, false);
        } catch (IllegalArgumentException expected) {
            check(name, true);
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.err.println("FAIL " + name);
        }
    }
}
