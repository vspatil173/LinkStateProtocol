package edu.cmu.ece.helper;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.models.LinkStateMessage;


import java.util.LinkedList;
import java.util.List;

public class Optimizer {
    final static boolean DEBUG_MODE=true;
    private static int minDistance(double[] dist, Boolean[] sptSet, int V)
    {
        double min = Double.MAX_VALUE;
        int min_index = -1;

        for (int v = 0; v < V; v++)
            if (!sptSet[v] && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }

        return min_index;
    }

    private static void printSolution(double[] dist, int V)
    {
        System.out.println("Vertex \t\t Distance from Source");
        for (int i = 0; i < V; i++)
            System.out.println(i + " \t\t " + dist[i]);
    }

    private static double[] dijkstra(double[][] graph, int src, int V)
    {
        double[] dist = new double[V];
        Boolean[] sptSet = new Boolean[V];

        for (int i = 0; i < V; i++) {
            dist[i] = Double.MAX_VALUE;
            sptSet[i] = false;
        }

        dist[src] = 0;

        for (int count = 0; count < V - 1; count++) {
            int u = minDistance(dist, sptSet,V);
            sptSet[u] = true;
            for (int v = 0; v < V; v++)
                if (!sptSet[v] && graph[u][v] != 0 && dist[u] != Double.MAX_VALUE && dist[u] + graph[u][v] < dist[v])
                    dist[v] = dist[u] + graph[u][v];
        }
        printSolution(dist,V);
        return dist;
    }

    public static void calculate_min_distances() {

        LinkStateMessage localMsg = SharedResources.getServerConfig().getLinkStateMessage();
        int no_of_peers = localMsg.getDistance_vector().size();

        int peers = localMsg.getDistance_vector().get(SharedResources.serverConfig.getHostName()).size();
//        int peers = localMsg.getDistance_vector().get("node1900").size();
        if(no_of_peers!=peers) {System.out.println("nah nah kuch toh gadbad hey daya"); return;}

        //convert to array format
        double [][]graph = new double[peers][peers];
        List<String> peer_names = new LinkedList<>(localMsg.getDistance_vector()
                .get(SharedResources.serverConfig.getHostName()).keySet());
//        List<String> peer_names = new LinkedList<>(localMsg.getDistance_vector()
//                .get("node1900").keySet());
        for(int i = 0;i < peer_names.size();i++){
            for(int j = 0;j < peer_names.size();j++){
                graph[i][j] =
                        localMsg.getDistance_vector().get(peer_names.get(i)).get(peer_names.get(j)) == null
                                ? 10000
                                : localMsg.getDistance_vector().get(peer_names.get(i)).get(peer_names.get(j));
                System.out.print(graph[i][j]+"\t");
            }
            System.out.println();
        }
        System.out.println(peer_names);
        int srcIndex = peer_names.indexOf(SharedResources.getServerConfig().getHostName());
        //call dijkstra
//        int srcIndex = peer_names.indexOf("node1900");
        System.out.println(peer_names.get(srcIndex));
        double[] dist = Optimizer.dijkstra(graph,srcIndex,peers);
        //update local msg with new values
        if(DEBUG_MODE) System.out.println(dist);
        for(int j = 0;j < peer_names.size();j++){
            localMsg.getDistance_vector().get(SharedResources.serverConfig.getHostName()).put(peer_names.get(j),dist[j]);
//            localMsg.getDistance_vector().get("node1900").put(peer_names.get(j),dist[j]);
        }
        System.out.println(localMsg);
    }
    /*public static void main(String[] args) throws Exception {
        StringBuilder container = new StringBuilder();
        try {
            FileReader reader = new FileReader("resources/temp.json");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                container.append(line);
            }
        } catch (IOException e) { e.printStackTrace(); }

        LinkStateMessage linkStateMessage = JsonParser.generateMapFromString(container.toString());
        Optimizer t = new Optimizer();
        t.calculate_min_distances(linkStateMessage);
    }*/
}
