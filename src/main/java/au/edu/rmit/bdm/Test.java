package au.edu.rmit.bdm;

import au.edu.rmit.bdm.TTorch.base.Instance;
import au.edu.rmit.bdm.TTorch.base.Torch;
import au.edu.rmit.bdm.TTorch.base.helper.MemoryUsage;
import au.edu.rmit.bdm.TTorch.base.invertedIndex.EdgeInvertedIndex;
import au.edu.rmit.bdm.TTorch.base.invertedIndex.VertexInvertedIndex;
import au.edu.rmit.bdm.TTorch.base.model.*;
import au.edu.rmit.bdm.TTorch.mapMatching.model.TowerVertex;
import au.edu.rmit.bdm.TTorch.queryEngine.Engine;
import au.edu.rmit.bdm.TTorch.queryEngine.query.QueryResult;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args)throws IOException{
//        MapMatching mm = MapMatching.getBuilder().build("Resources/porto_raw_trajectory.txt","Resources/porto.osm.pbf");
//        mm.start();

        List<List<TrajEntry>> queries = read();
        Engine engine = Engine.getBuilder().preferedIndex(Torch.Index.LEVI).preferedSimilarityMeasure(Torch.Algorithms.DTW).build();
        QueryResult ret = engine.findTopK(queries.get(1), 3);
        //System.out.println(ret.getRetTrajMapVFormat());
        System.out.println(ret.getMappedQueryMapVformat());
        System.out.println(ret.getRetMapVformat());
        System.out.println(ret.toJSON());

//        getAfew();
//        genEdgeInvertedIndex();
//        genVertexInvertedIndex();
//        addLenToEdgeLookuptable();
    }

    private static List<List<TrajEntry>> read() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("abc/T-Torch/query.txt"));
        List<List<TrajEntry>> list = new ArrayList<>(3);

        String line;
        while((line = reader.readLine())!=null){

            String[] temp = line.split("\t");
            String trajId = temp[0];
            String trajContent = temp[1];

            trajContent = trajContent.substring(2, trajContent.length() - 2); //remove head "[[" and tail "]]"
            String[] trajTuples = trajContent.split("],\\[");
            List<TrajEntry> query = new ArrayList<>();

            String[] latLng;
            for (int i = 0; i < trajTuples.length; i++){

                double lat = 0.;
                double lon = 0.;

                    latLng = trajTuples[i].split(",");
                    lat = Double.parseDouble(latLng[1]);
                    lon = Double.parseDouble(latLng[0]);

                Coordinate node = new Coordinate(lat, lon);

                query.add(node);
            }
            list.add(query);
        }
        return list;
    }

    private static void addLenToEdgeLookuptable() throws IOException{
        BufferedReader edgeReader = new BufferedReader(new FileReader(Instance.fileSetting.ID_EDGE_LOOKUP));
        BufferedReader rawReader = new BufferedReader(new FileReader(Instance.fileSetting.ID_EDGE_RAW));

        List<String> edges = new ArrayList<>(10000);
        String line;
        String raw[];
        double dist;
        while((line = edgeReader.readLine())!=null){
            raw = rawReader.readLine().split(";");
            dist = Double.parseDouble(raw[raw.length - 3]);
            edges.add(line + ";" + dist);
        }

        edgeReader.close();
        rawReader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(Instance.fileSetting.ID_EDGE_LOOKUP));
        for (String edge : edges){

            writer.write(edge);
            writer.newLine();
        }

        writer.flush();
        writer.close();
    }

    private static void getAfew() throws IOException {

        BufferedReader edgeReader = new BufferedReader(new FileReader(Instance.fileSetting.TRAJECTORY_EDGE_REPRESENTATION_PATH+".txt"));
        BufferedReader vertexReader = new BufferedReader(new FileReader(Instance.fileSetting.TRAJECTORY_VERTEX_REPRESENTATION_PATH+".txt"));

        List<String> edgeList = new ArrayList<>(200001);
        List<String> vertexList = new ArrayList<>(200001);
        String line1, line2;
        int i = 0;
        while((line1 = edgeReader.readLine()) != null){
            edgeList.add(line1);
            line2 = vertexReader.readLine();
            vertexList.add(line2);
            if (++i % 200000 == 0){
                break;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(Instance.fileSetting.TRAJECTORY_VERTEX_REPRESENTATION_PATH+"_"+200000+".txt"));
        for (String line : vertexList){
            writer.write(line);
            writer.newLine();
        }
        writer.flush();
        writer.close();

        writer = new BufferedWriter(new FileWriter(Instance.fileSetting.TRAJECTORY_EDGE_REPRESENTATION_PATH+"_"+200000+".txt"));
        for (String line:edgeList){
            writer.write(line);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private static void genEdgeInvertedIndex() throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(Instance.fileSetting.TRAJECTORY_EDGE_REPRESENTATION_PATH_200000));
        EdgeInvertedIndex edgeInvertedIndex = new EdgeInvertedIndex();

        String line;
        String[] tokens;
        String[] edges;

        MemoryUsage.start();

        int i = 0;
        while((line = bufferedReader.readLine()) != null){

            if (++i % 100000 == 0){
                System.err.println("current progress: "+i);
                MemoryUsage.printCurrentMemUsage("");
                if (i == 200000) break;
            }
            tokens = line.split("\t");
            edges = tokens[1].split(",");

            Trajectory<TrajEntry> t = new Trajectory<>();
            t.id = tokens[0];

            for (String edge : edges)
                t.edges.add(new TorEdge(Integer.parseInt(edge), null, null, 0));


            edgeInvertedIndex.index(t);
        }
        MemoryUsage.printCurrentMemUsage("");

        edgeInvertedIndex.saveCompressed(Instance.fileSetting.EDGE_INVERTED_INDEX);
    }

    private static void genVertexInvertedIndex() throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(Instance.fileSetting.TRAJECTORY_VERTEX_REPRESENTATION_PATH_200000));
        VertexInvertedIndex vertexInvertedIndex= new VertexInvertedIndex();

        String line;
        String[] tokens;
        String[] vertices;

        MemoryUsage.start();

        int i = 0;
        while((line = bufferedReader.readLine()) != null){

            if (++i % 100000 == 0){
                System.err.println("current progress: "+i);
                MemoryUsage.printCurrentMemUsage("");
                if (i == 200000) break;
            }
            tokens = line.split("\t");
            vertices = tokens[1].split(",");

            Trajectory<TrajEntry> t = new Trajectory<>();
            t.id = tokens[0];

            for (String vertex : vertices)
               t.add(new TowerVertex(0,0, Integer.valueOf(vertex)));


            vertexInvertedIndex.index(t);
        }
        MemoryUsage.printCurrentMemUsage("");

        vertexInvertedIndex.saveCompressed(Instance.fileSetting.VERTEX_INVERTED_INDEX);
    }
}
