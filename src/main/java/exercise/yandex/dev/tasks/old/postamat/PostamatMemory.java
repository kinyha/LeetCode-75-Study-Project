package exercise.yandex.dev.tasks.old.postamat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostamatMemory {
    List<Postamat> postamats;

    public PostamatMemory() {
        postamats = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            postamats.add(new Postamat("id" + i, Map.of()));
        }
    }
}
