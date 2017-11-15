package nl._42.beanie;

import nl._42.beanie.domain.SimpleBeanResult;

public interface SimpleBeanResultBuildCommand extends BeanBuildCommand<SimpleBeanResult> {
    SimpleBeanResultBuildCommand withUniqueId(String uniqueId);
}
