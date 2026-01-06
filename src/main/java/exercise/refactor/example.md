qweГазпромбанк

Сделать ревью
/**
* Метод работает не всегда корректно.
* Как его можно отрефакторить или переписать, и как проверить, что ничего не сломалось?
*
* Метод возвращает индекс элемента в последовательности чисел, который соответствует дубликату.
*
* @param numbers
* @return
*
* 1,2,3,4,4,5,6
* 4
*
*/
public int findDuplicateIndex(int... numbers) {

    int[] countArray = new int[nubmers.length];
    for (int i = 0; i < numbers.length; i++) {
        int current = numbers[i];
        if (countArray[current] > 0) {
            return i;
        } else {
            countArray[current] += 1;
        }
    }
    throw new CustomException("Duplicate not found!");
}

Сбер

Ревью
class CodeProcessor {

    public void process(List<Code> codes) {
        for (Code code : codes) {
            if (CodeType.ITCP == code.getCodeType()) {
                doSmthngITCP();
            } 
            else if (CodeType.TLS == code.getCodeType()) {
                doSmthngTLS();
            } 
            else if (CodeType.OTHER == code.getCodeType()) {
                doSmthngOther();
            } 
            else {
                doDefault();
            }
        }
    }

    private void doSmthngITCP() {
        System.out.println("Handling ITCP");
    }

    private void doSmthngTLS() {
        System.out.println("Handling TLS");
    }

    private void doSmthngOther() {
        System.out.println("Handling Other");
    }

    private void doDefault() {
        System.out.println("Handling Default Case");
    }
}

enum CodeType {
ITCP, TLS, OTHER
}

class Code {
private final CodeType codeType;

    public Code(CodeType codeType) {
        this.codeType = codeType;
    }

    public CodeType getCodeType() {
        return codeType;
    }
}

public class CodeProcessingApp {
public static void main(String[] args) {
List<Code> codes = Arrays.asList(
new Code(CodeType.ITCP),
new Code(CodeType.TLS),
new Code(CodeType.OTHER)
);

        CodeProcessor processor = new CodeProcessor();
        processor.process(codes);
    }
}
#sber


СБЕР

Сделать рефакторинг кода

@Transactional
public void process(String oldName, String newName) {
Long id = exec("select id from file where name='" + oldName + "'"); //выполнение запроса к БД
insert
processFile(oldName, newName); //переименование файла на диске
exec("update file set name='" + newName + "' where id = " + id);  
}

Точка банк (стажировка)

Провести код-ревью

boolean containsStringInData(String csvFile, String str) throw IOException {

BufferedReader reader = new BufferedReader(new FileReader(csvFile);
    ArrayList<String> list = new ArrayList();

String line;
    while ((line = br.readLine()) != null) {
        list.add(line);
    }

boolean result;
    for (String s : list) {
        if (s == str) {
            result = true;
        }
    }

return result;
}
