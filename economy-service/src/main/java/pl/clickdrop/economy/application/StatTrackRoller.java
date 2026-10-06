package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatTrackRoller {

    private record WeightedStatTrack(boolean statTrack, int weight) { }

    private static final List<WeightedStatTrack> POOL = List.of(
            new WeightedStatTrack(true, 10),
            new WeightedStatTrack(false, 90)
    );

    private final WeightedPicker weightedPicker;

    public boolean roll() {
        return weightedPicker.pick(POOL, WeightedStatTrack::weight).statTrack();
    }
}
