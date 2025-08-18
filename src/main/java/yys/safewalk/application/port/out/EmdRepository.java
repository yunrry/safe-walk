package yys.safewalk.application.port.out;

import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.domain.model.Emd;
import java.util.List;

public interface EmdRepository {
    List<Emd> findEmdInBounds(Coordinate swCoordinate, Coordinate neCoordinate);
}