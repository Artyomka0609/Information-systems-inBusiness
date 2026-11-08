package ru.edu.pr03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/** читает файл, передает строки парсеру и печатает результат */
public final class ApprovalApplication {
    private final ApprovalRouter router;
    private final RequestCsvParser parser;

    public ApprovalApplication(ApprovalRouter router, RequestCsvParser parser) {
        if (router == null || parser == null) {
            throw new IllegalArgumentException("router and parser are required");
        }
        this.router = router;
        this.parser = parser;
    }

    public static void main(String[] args) {
        ApprovalApplication application = new ApprovalApplication(
                new ApprovalRouter(), new RequestCsvParser());
        int exitCode = application.run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /** 0 - успех, 1 - ошибки строк, 2 - ошибка запуска, файла или заголовка. */
    public int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length > 1) {
            err.println("Usage: ApprovalApplication [file.csv]");
            return 2;
        }
        String file = args.length == 0 ? "data/sample.csv" : args[0];
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get(file), StandardCharsets.UTF_8)) {
            parser.validateHeader(reader.readLine());
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("CSV contains no requests");
            }
            out.println("line;route");
            int lineNumber = 2;
            boolean hasErrors = false;
            while (line != null) {
                try {
                    ApprovalRequest request = parser.parse(line);
                    out.println(lineNumber + ";" + router.route(request));
                } catch (IllegalArgumentException e) {
                    hasErrors = true;
                    err.println("ERROR line " + lineNumber + ": " + e.getMessage());
                }
                line = reader.readLine();
                lineNumber++;
            }
            return hasErrors ? 1 : 0;
        } catch (IOException | InvalidPathException e) {
            err.println("ERROR: " + e.getMessage());
            return 2;
        } catch (IllegalArgumentException e) {
            err.println("ERROR: " + e.getMessage());
            return 2;
        }
    }
}
