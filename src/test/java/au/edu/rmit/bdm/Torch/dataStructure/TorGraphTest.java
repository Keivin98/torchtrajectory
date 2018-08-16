package au.edu.rmit.bdm.Torch.dataStructure;

import au.edu.rmit.bdm.Torch.base.helper.MemoryUsage;
import au.edu.rmit.bdm.Torch.base.Torch;
import au.edu.rmit.bdm.Torch.mapMatching.algorithm.TorGraph;
import org.junit.Test;

public class TorGraphTest {

    /**
     * memory used:
     * graph hopper + tor virtual graph + shortest path cache 900m - 1100m
     *
     *
     */
    @Test
    public void test(){

        MemoryUsage.start();
        TorGraph graph = TorGraph.getInstance().initGH("Torch/HopperMeta_car", "map-data/Beijing.osm.pbf",Torch.vehicleType.CAR);
        MemoryUsage.printCurrentMemUsage("after loading graph-hopper");

//        graph.build();

        MemoryUsage.printCurrentMemUsage("after building torGraph");
    }
}