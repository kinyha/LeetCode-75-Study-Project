package exercise.yandex.dev.tasks.postamat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public record Postamat(
        String id,
        Map<Integer, String> cells
) {

    public Postamat {
        if (id == null) {
            throw new IllegalArgumentException("Id cant be null");
        }
        if (cells == null) {
            throw new IllegalArgumentException("Cant be without cells");
        }

        int randomCells =  new Random().nextInt(5,10);
        cells = new HashMap<>(randomCells);
        for (int i = 0; i < randomCells; i++) {
            cells.put(i, null);
        }
    }
}
