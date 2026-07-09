package exercise.ibsNew.tasks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generator {

    private static final Path SOURCE_FILE = Path.of(
            "src",
            "main",
            "java",
            "exercise",
            "ibs",
            "theori",
            "claude",
            "news",
            "extracted_excel_interview_info (1) (1).md"
    );

    // Можно указать: "all", "4", "2-5", "2,4,8", "2-10".
    private static final String CHAPTERS = "6";

    private static final int QUESTIONS_TO_PRINT = 1;

    private static final Pattern CHAPTER_HEADER = Pattern.compile("^####\\s+(\\d+)\\.\\s+(.+)$");

    public static void main(String[] args) throws IOException {
        String chapterSpec = args.length > 0 ? args[0] : CHAPTERS;
        int count = args.length > 1 ? Integer.parseInt(args[1]) : QUESTIONS_TO_PRINT;

        Set<Integer> selectedChapters = parseChapters(chapterSpec);
        List<Question> questions = loadQuestions();
        List<Question> filteredQuestions = questions.stream()
                .filter(question -> selectedChapters.contains(question.chapter))
                .toList();

        if (filteredQuestions.isEmpty()) {
            throw new IllegalArgumentException("Не найдено вопросов для глав: " + chapterSpec);
        }

        System.out.println("Источник: " + SOURCE_FILE);
        System.out.println("Главы: " + chapterSpec);
        System.out.println("Вопросов найдено: " + questions.size());
        System.out.println("После фильтра: " + filteredQuestions.size());
        System.out.println();

        ArrayList<Question> pool = new ArrayList<>(filteredQuestions);
        int resultCount = Math.min(count, pool.size());
        for (int i = 1; i <= resultCount; i++) {
            Question question = pickWeighted(pool);
            pool.remove(question);
            printQuestion(i, question);
        }
    }

    private static List<Question> loadQuestions() throws IOException {
        if (!Files.exists(SOURCE_FILE)) {
            throw new IllegalStateException("Не найден файл с вопросами: " + SOURCE_FILE.toAbsolutePath());
        }

        List<String> lines = Files.readAllLines(SOURCE_FILE, StandardCharsets.UTF_8);
        Map<String, Question> questionsByKey = new LinkedHashMap<>();

        String candidate = "unknown";
        boolean inJavaSection = false;
        Integer chapter = null;
        String chapterTitle = "";

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("## ")) {
                candidate = trimmed.substring(3).replace(".xlsx", "").trim();
                inJavaSection = false;
                chapter = null;
                continue;
            }

            if (trimmed.startsWith("| Кандидат |")) {
                List<String> cells = splitMarkdownRow(trimmed);
                if (cells.size() > 1 && !cells.get(1).isBlank()) {
                    candidate = cells.get(1);
                }
                continue;
            }

            if (trimmed.equals("### Java")) {
                inJavaSection = true;
                chapter = null;
                continue;
            }

            if (inJavaSection && trimmed.startsWith("### ")) {
                inJavaSection = false;
                chapter = null;
                continue;
            }

            if (!inJavaSection) {
                continue;
            }

            if (trimmed.startsWith("#### ")) {
                Matcher matcher = CHAPTER_HEADER.matcher(trimmed);
                if (matcher.matches()) {
                    int chapterNumber = Integer.parseInt(matcher.group(1));
                    if (chapterNumber >= 2 && chapterNumber <= 10) {
                        chapter = chapterNumber;
                        chapterTitle = matcher.group(2);
                    } else {
                        chapter = null;
                    }
                } else {
                    chapter = null;
                }
                continue;
            }

            if (chapter == null || !trimmed.startsWith("|")) {
                continue;
            }

            List<String> cells = splitMarkdownRow(trimmed);
            if (cells.size() < 3) {
                continue;
            }

            Integer row = parseIntOrNull(cells.get(0));
            String questionText = normalizeQuestion(cells.get(1));
            if (row == null || shouldSkipQuestion(questionText)) {
                continue;
            }

            double score = parseScore(cells.get(2));
            String key = chapter + ":" + row;
            int questionChapter = chapter;
            String questionChapterTitle = chapterTitle;
            int questionRow = row;
            Question question = questionsByKey.computeIfAbsent(
                    key,
                    ignored -> new Question(questionChapter, questionChapterTitle, questionRow, questionText)
            );

            if (question.text.length() < questionText.length()) {
                question.text = questionText;
            }
            if (score > 0) {
                question.askedBy.add(candidate);
            }
        }

        List<Question> questions = new ArrayList<>(questionsByKey.values());
        assignNumbersInsideChapters(questions);
        return questions;
    }

    private static void assignNumbersInsideChapters(List<Question> questions) {
        Map<Integer, Integer> countersByChapter = new LinkedHashMap<>();
        for (Question question : questions) {
            int nextNumber = countersByChapter.getOrDefault(question.chapter, 0) + 1;
            countersByChapter.put(question.chapter, nextNumber);
            question.numberInChapter = nextNumber;
        }
    }

    private static Question pickWeighted(List<Question> questions) {
        int totalWeight = questions.stream()
                .mapToInt(Question::weight)
                .sum();
        int ticket = ThreadLocalRandom.current().nextInt(totalWeight);

        int current = 0;
        for (Question question : questions) {
            current += question.weight();
            if (ticket < current) {
                return question;
            }
        }
        return questions.getLast();
    }

    private static void printQuestion(int index, Question question) {
        System.out.println("--- Вопрос " + index + " ---");
        System.out.println("Номер: " + question.number());
        System.out.println("Глава: " + question.chapter + ". " + question.chapterTitle);
        System.out.println("Вес: x" + question.weight() + " (" + question.askedLabel() + ")");
        System.out.println("Вопрос:");
        System.out.println(question.number() + " " + question.text);
        System.out.println();
    }

    private static Set<Integer> parseChapters(String spec) {
        String normalized = spec.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.equals("all") || normalized.equals("все")) {
            return range(2, 10);
        }

        Set<Integer> chapters = new LinkedHashSet<>();
        for (String token : normalized.split(",")) {
            String part = token.trim();
            if (part.isBlank()) {
                continue;
            }

            if (part.contains("-")) {
                String[] bounds = part.split("-", 2);
                int start = Integer.parseInt(bounds[0].trim());
                int end = Integer.parseInt(bounds[1].trim());
                chapters.addAll(range(start, end));
            } else {
                chapters.add(Integer.parseInt(part));
            }
        }

        for (Integer chapter : chapters) {
            if (chapter < 2 || chapter > 10) {
                throw new IllegalArgumentException("Глава должна быть в диапазоне 2-10: " + chapter);
            }
        }
        return chapters;
    }

    private static Set<Integer> range(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Некорректный диапазон глав: " + start + "-" + end);
        }
        Set<Integer> result = new LinkedHashSet<>();
        for (int chapter = start; chapter <= end; chapter++) {
            result.add(chapter);
        }
        return result;
    }

    private static List<String> splitMarkdownRow(String line) {
        String text = line.trim();
        if (text.startsWith("|")) {
            text = text.substring(1);
        }
        if (endsWithUnescapedPipe(text)) {
            text = text.substring(0, text.length() - 1);
        }

        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            boolean escaped = i > 0 && text.charAt(i - 1) == '\\';
            if (current == '|' && !escaped) {
                cells.add(cleanCell(cell.toString()));
                cell.setLength(0);
            } else {
                cell.append(current);
            }
        }
        cells.add(cleanCell(cell.toString()));
        return cells;
    }

    private static boolean endsWithUnescapedPipe(String text) {
        if (!text.endsWith("|")) {
            return false;
        }
        return text.length() == 1 || text.charAt(text.length() - 2) != '\\';
    }

    private static String cleanCell(String cell) {
        return cell
                .replace("\\|", "|")
                .replace("<br>", "\n")
                .replace("&nbsp;", " ")
                .trim();
    }

    private static String normalizeQuestion(String question) {
        return question
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean shouldSkipQuestion(String question) {
        return question.isBlank()
                || question.equals("Вопрос / пункт")
                || question.equals("Прочие вопросы");
    }

    private static Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static double parseScore(String value) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static final class Question {
        private final int chapter;
        private final String chapterTitle;
        private final int row;
        private int numberInChapter;
        private String text;
        private final Set<String> askedBy = new LinkedHashSet<>();

        private Question(int chapter, String chapterTitle, int row, String text) {
            this.chapter = chapter;
            this.chapterTitle = chapterTitle;
            this.row = row;
            this.text = text;
        }

        private String number() {
            return chapter + "." + numberInChapter;
        }

        private int weight() {
            return switch (askedBy.size()) {
                case 0 -> 1;
                case 1 -> 2;
                default -> 3;
            };
        }

        private String askedLabel() {
            if (askedBy.isEmpty()) {
                return "не задавался";
            }
            return "задавался: " + String.join(", ", askedBy);
        }
    }
}
