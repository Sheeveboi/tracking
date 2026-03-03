package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Shapes.Box;

import java.util.*;

public class Node extends Box {

    public enum NodeType {
        NORMAL,
        ICEROAD,
        DOOR,
        INTERACTABLE
    }

    public String tag;
    public ArrayList<Integer> connections;
    public NodeType type;
    public Map<Integer, Integer> distanceMap = new HashMap<>();

    public void setColor() {
        this.a = .3f;
        switch (this.type) {
            case NORMAL -> {
                this.r = 0;
                this.g = 0;
                this.b = 1;
            }

            case ICEROAD -> {
                this.r = .5f;
                this.g = .5f;
                this.b = 1f;
            }

            case DOOR -> {
                this.r = 0f;
                this.g = 1f;
                this.b = 0f;
            }

            case INTERACTABLE -> {
                this.r = 1f;
                this.g = 0f;
                this.b = 0f;
            }
        }
    }

    public Node(int x, int y, int z) {
        super(x, y, z, String.valueOf(java.util.UUID.randomUUID()));
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type) {
        super(x, y, z, String.valueOf(java.util.UUID.randomUUID()));
        this.type = type;
        this.setColor();
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type, String tag) {
        super(x, y, z, tag);
        this.type = type;
        this.setColor();
        this.tag = tag;
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type, String tag, ArrayList<Integer> connections) {
        super(x, y, z, tag);
        this.type = type;
        this.setColor();
        this.tag = tag;
        this.connections = connections;
    }

    public void calculateDistances() {

        Map<Integer, ArrayList<Integer>> potentialDistances = new HashMap<>();

        //gather distances of all neighbors
        for (int index : this.connections) {

            Node neighbor = Navigation.nodes.get(index);

            //set distances to self to one on parent node
            Navigation.nodes.get(index).distanceMap.put(Navigation.nodes.indexOf(this), 1);
            this.distanceMap.put(index, 1);

            //copy neighbors distance map
            for (int distanceKey : neighbor.distanceMap.keySet()) {

                if (distanceKey != Navigation.nodes.indexOf(this)) {

                    int distance = neighbor.distanceMap.get(distanceKey);

                    if (!potentialDistances.containsKey(distanceKey))
                        potentialDistances.put(distanceKey, new ArrayList<>());

                    potentialDistances.get(distanceKey).add(distance);

                }

            }

        }

        class key implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {

                int distance1 = (int) o1;
                int distance2 = (int) o2;

                if      (distance1 > distance2) return 1;
                else if (distance1 == distance2) return 0;
                return -1;

            }
        }

        //assign the closest distance of all the nodes the neighbors can see + 1
        for (int distanceKey : potentialDistances.keySet()) {

            ArrayList<Integer> sortedDistances = potentialDistances.get(distanceKey);
            sortedDistances.sort(new key());

            int finalDistance = sortedDistances.getLast() + 1;

            if (!distanceMap.containsKey(distanceKey)) distanceMap.put(distanceKey, finalDistance);

            else if (distanceMap.get(distanceKey) > finalDistance) {
                distanceMap.put(distanceKey, finalDistance);
                Navigation.nodes.get(distanceKey).distanceMap.put(distanceKey, finalDistance);
            }

        }

    }
}
