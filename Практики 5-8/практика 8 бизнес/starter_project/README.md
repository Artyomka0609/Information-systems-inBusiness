# Практическая работа №8 — вариант 8

Весь Java-код находится в трёх исходных файлах:
- `BusinessTrip.java`: модель заявки, сервис, обработка ввода и тип Employee из исходной диаграммы.
- `TripStatus.java`: состояния заявки.
- `SelfCheck.java`: граничные проверки.

Для сборки нужен JDK. Команды javac и java должны быть доступны через PATH.
В Windows скрипт также учитывает JAVA_HOME.

Сборка и проверка в Windows из папки starter_project:

```bat
run_selfcheck.bat
```

Запуск приложения после сборки:

```bat
java -Dfile.encoding=UTF-8 -cp out ru.edu.pr05.Application
java -Dfile.encoding=UTF-8 -cp out ru.edu.pr05.Application data/sample.csv
```

В Linux и macOS сборка и проверка: `bash run_selfcheck.sh`.

Без аргументов приложение запрашивает id, даты начала и окончания, затем команды
SUBMIT, APPROVE, REJECT, CANCEL и COMPLETE. Для REJECT вводится причина, для CANCEL
и COMPLETE — текущая дата. Пустая команда завершает ввод. Формат дат: ГГГГ-ММ-ДД.
CSV сохраняет исходные четыре столбца id;start;end;action. Каждая строка создаёт
отдельную заявку DRAFT; несколько переходов одной заявки выполняются через консоль.

Для варианта 8 принято толкование: отмена разрешена из DRAFT, SUBMITTED и APPROVED,
если до начала поездки осталось не менее 45 календарных дней. Ровно 45 дней разрешены,
44 дня запрещены. В задании способ применения числа 45 не уточнён.
Порог MIN_CANCEL_DAYS находится в BusinessTrip. Метод cancel() использует системную
дату, cancel(LocalDate) принимает текущую дату явно для воспроизводимых проверок.
