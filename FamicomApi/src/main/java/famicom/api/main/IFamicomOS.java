package famicom.api.main;

import famicom.api.annotation.FamicomApi;
import famicom.api.core.RomManager;

import java.util.List;

/**
 * Created by hkoba on 2017/01/02.
 */
@FamicomApi
public interface IFamicomOS {
    void execute(String[] args, List<RomManager.RomData> romDataList);
}
