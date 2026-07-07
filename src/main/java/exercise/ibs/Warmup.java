package exercise.ibs;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Разминка перед IBS/ЦФА подготовкой. Без ответов.
 * <p>
 * Часть A — 3 алгоритмические задачи:
 * 1. task1..task3 — заглушки, пиши тело сам, руками, без подсматривания.
 * 2. Запусти main, сверь вывод с комментарием "ожидаем".
 * <p>
 * Часть B — 2 кейса на код-ревью (внизу файла, в комментариях):
 * Код НЕ компилируется специально (Spring/JPA типов нет в classpath) —
 * это текст для чтения и разбора вслух, как на живом ТИ.
 * Правила разбора (см. interview_ti1_ibs_structured.md, п.1.1):
 * сначала молча прочитай весь код, потом пройдись сверху вниз:
 * корректность/NPE -> DI -> типизация -> ошибки -> транзакции ->
 * конкурентность -> API-слой -> тесты. Ответов здесь нет —
 * свои находки запиши отдельно и сверяй сам с docs позже.
 */
public class Warmup {

    public static void main(String[] args) {
        System.out.println("task1 -> " + isSorted(new int[]{1, 2, 2, 5, 9})); // ожидаем: true
        System.out.println("task1 -> " + isSorted(new int[]{3, 1, 2}));       // ожидаем: false

        System.out.println("task2 -> " + countVowels("Interview preparation")); // ожидаем: 9

        System.out.println("task3 -> " + Arrays.toString(mergeSorted(new int[]{1, 3, 5}, new int[]{2, 4, 6})));
        // ожидаем: [1, 2, 3, 4, 5, 6]
    }

    // ── task1 ────────────────────────────────────────────────────────────
    // Проверить, отсортирован ли массив по неубыванию.
    static boolean isSorted(int[] nums) {
        //loop
//        for (int i = 0; i < nums.length - 1; i++) {
//            if (nums[i] > nums[i + 1]) return false;
//        }
        //return true;
        //stream
        /*return Arrays.stream(nums)
                .reduce((a, b) -> {
                    if (a > b) throw new RuntimeException("No sorted");
                    return b;
                }).isPresent();*/

        //workedStream
        var a = IntStream.range(0, nums.length - 1);
        a.forEach(System.out::println);
        return false;
    }

    // ── task2 ────────────────────────────────────────────────────────────
    // Посчитать гласные (a,e,i,o,u без учёта регистра) в строке.
    static int countVowels(String s) {
        char[] vowels = new char[]{'a', 'e', 'i','o','u'};
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            for (char vowel : vowels) {
                if (s.toLowerCase().charAt(i) == vowel) {
                    count = count + 1;
                    System.out.println(s.charAt(i));
                }
            }

        }
        return count; // TODO
    }

    // ── task3 ────────────────────────────────────────────────────────────
    // Слить два УЖЕ отсортированных массива в один отсортированный,
    // без вызова Arrays.sort() на результате (классический merge-step).
    static int[] mergeSorted(int[] a, int[] b) {
        return new int[0]; // TODO
    }
}

/*
============================================================
REVIEW CASE 1 — BidService.placeBid
============================================================
Контекст: сервис приёма ставок на аукционе лота. Прочитай и разбери,
как на живом код-ревью: сначала вопросы по бизнес-логике, потом баги.

@Service
public class BidService {

    @Autowired
    private LotRepository lotRepository;
    @Autowired
    private BidRepository bidRepository;

    public void placeBid(Long lotId, Long bidderId, double amount) {
        Lot lot = lotRepository.findById(lotId).get();
        Bid currentTop = bidRepository.findTopByLotIdOrderByAmountDesc(lotId);

        if (currentTop == null || amount > currentTop.getAmount()) {
            Bid bid = new Bid();
            bid.setLotId(lotId);
            bid.setBidderId(bidderId);
            bid.setAmount(amount);
            bidRepository.save(bid);
            lot.setCurrentPrice(amount);
            lotRepository.save(lot);
        }
    }
}
*/

/*
============================================================
REVIEW CASE 2 — DailyLimitResetJob
============================================================
Контекст: ночная джоба сбрасывает суточный лимит платежей клиента
перед началом нового дня. Прочитай и разбери так же, как кейс 1.

@Component
public class DailyLimitResetJob {

    @Autowired
    private CustomerLimitRepository repo;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetLimits() {
        List<CustomerLimit> limits = repo.findAll();
        for (CustomerLimit l : limits) {
            l.setSpentToday(0.0);
            l.setLastResetDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            repo.save(l);
        }
    }
}
*/
