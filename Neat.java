import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class Connection {
    int start, end, inoId;
    float weight;
    
    public Connection(float weight, int start, int end){
        this.weight = weight;
        this.start = start;
        this.end = end;
        this.inoId = Integer.parseInt(start + "" + end);
    }
}
class Node {
    float value, layer;
    int id, type;
    
    public Node(float value, int id, int type) {
        //TYPES OF NODES
        // 0 - input
        // 1 - output
        // 2 - hidden
        this.value = value;
        this.id = id;
        this.type = type;
    }
}

class Creature {
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<Connection> connections = new ArrayList<Connection>();
    Random rand = new Random();
    int currentId = 0;
    int inputNum;
    int outputNum;
    int creatureId;
    
    // constructor for base minimal creature
    public Creature(int inputs, int outputs) {
        this.inputNum = inputs;
        this.outputNum = outputs;
        // generates all inputs nodes
        for (int i = 0; i < inputs; i++) { 
            this.nodes.add(new Node(0, currentId, 0));
            this.nodes.get(i).layer = 0;
            currentId++;
        }
        
        // generates all output nodes
        for (int i = 0; i < outputs; i++) {
            this.nodes.add(new Node(0, currentId, 1));
            this.nodes.get(i+inputs).layer = 1;
            currentId++;
        }
        
        // connects all inputs to all outputs
        for (int i = 0; i < inputs; i++) {
            for (int j = 0; j < outputs; j++) {
                this.connections.add(new Connection(this.rand.nextFloat() * 2.0f - 1.0f, i, inputs+j));
            }
        }
    }
    
    ArrayList<Float> totLayers = new ArrayList<Float>();
    Creature Mutate() {
        // have a chance of adding a node or connection
        // 0 = new node
        // 1 = new connection
        // 2 = remove a connection
        // 3 = adjust 1 connection value
        int generate = rand.nextInt(0,4);
        
        
        // choose  a random connection to add a node between
        // create a new connection from new node to end of old connection
        // move the end of old connection to new node
        if (generate == 0){
            if (rand.nextInt(0, 80) == 0) { // 1 in 50 chance of new node muation
                System.out.println("Node added: " + currentId);
                
                // choose a random connection and add a node
                int create = this.rand.nextInt(0, (connections.size())); 
                nodes.add(new Node(0, currentId, 2));
                
                // set its layer to the average of its start and end connection layers
                float s = nodes.get(connections.get(create).start).layer;
                float x = nodes.get(connections.get(create).end).layer;
                nodes.get(currentId).layer = (s+x)/2;
                
                // create a new connection from the new node to previous end node and set its inno id
                connections.add(new Connection(1, currentId, connections.get(create).end)); 
                connections.get(connections.size()-1).inoId = Integer.parseInt(currentId + "" + connections.get(create).end);
                
                // move the connection from old end node to the new node and set its inno id
                connections.get(create).end = currentId; 
                connections.get(create).inoId = Integer.parseInt(connections.get(create).start + "" + connections.get(create).end);
                currentId++;
                
                
                // add every unqie layer number to a list
                for(int i = 0; i < nodes.size(); i++){
                    if (!(totLayers.contains(nodes.get(i).layer))){
                        totLayers.add(nodes.get(i).layer);
                    } 
                } 
                
                totLayers.sort(null);
            }  
        }
        
        else if (generate == 1){
            
            // for every node check which node is in the layer ahead to create a new connection
            ArrayList<Integer> start = new ArrayList<Integer>();
            ArrayList<Integer> end = new ArrayList<Integer>();

            // for every node check all possible other nodes
            for (int i  = 0; i < nodes.size(); i++){
                for (int s = 0; s < nodes.size(); s++){
                    // if the end connection is ahead of start connection in layers
                    if (nodes.get(i).layer < nodes.get(s).layer){
                        // if the connection already exists
                        boolean exists = false;
                        for (int w  = 0; w < connections.size(); w++){
                            if (connections.get(w).start == i && connections.get(w).end == s){
                                exists = true;
                            }
                        }
                        // dont add if it already exists
                        if (exists == false){
                            start.add(nodes.get(i).id);
                            end.add(nodes.get(s).id);
                        }
                    }
                }
            }
            
            //If possible connections can be added then add
            if (start.size() != 0){
                System.out.println("new node: "+nodes.size());
                int choice = rand.nextInt(0, start.size());
                connections.add(new Connection(rand.nextFloat() * 2.0f - 1.0f , start.get(choice), end.get(choice)));
            }
        }
        
        //If a there is a connection that can be removed
        else if (generate == 2 &&  connections.size() > 1) {
            int change = rand.nextInt(0, connections.size());
            System.out.println("Connection removed: "+connections.get(change).inoId);
            connections.remove(change);
        }
        
        //Adjust a random connections weight to a random number
        else if (generate == 3) {
            int change = rand.nextInt(0, connections.size());
            System.out.println("Connection changed: "+connections.get(change).inoId);
            connections.get(change).weight = rand.nextFloat() * 2.0f - 1.0f;
        }
        
        return null;
    }
    
    
    public ArrayList<Float> Calculate(ArrayList<Float> inputs) {
        
        // set each of the inputs nodes values
        for (int i = 0; i < inputNum; i++) { 
            nodes.get(i).value = inputs.get(i);
            
        }
        
        // for every node, find which connections start at it and multiply the value with the weight and then set that to the value of the ending node
        for (int i = 0; i < nodes.size(); i++){
            for (int s = 0; s < connections.size(); s++){
                if (connections.get(s).start==i){
                    nodes.get(connections.get(s).end).value +=  nodes.get(i).value*connections.get(s).weight;
                }
            }
        }
        
        // for every layer get all nodes in it, for every node multiply value by its connection weight and send to ending node
        for (int i = 0; i < totLayers.size(); i++){
            for (int s = 0; s < nodes.size(); s++){
                if (nodes.get(s).layer == totLayers.get(i)){
                    for (int w = 0; w < connections.size(); w++){
                        if (connections.get(w).start==i){
                            nodes.get(connections.get(w).end).value += nodes.get(s).value*connections.get(w).weight;
                        }
                    }
                }
            }
        }
        
        //set the outputs
        ArrayList<Float> outputs = new ArrayList<Float>();
        for (int i = 0; i < outputNum; i++) { 
            outputs.add(nodes.get(inputNum+i).value);
        }
        
        return outputs;
    }
}

public class Neat {
    static final int NUM_INPUTS = 2;
    static final int NUM_OUTPUTS = 1;
    static final int NUM_AGENTS = 50;
    static final int NUM_GENERATIONS = 1000;
    
    //  sorts the scores and the creatures in parallel
    static private void sort(ArrayList<Creature> currentGeneration, ArrayList<Float> scores){
        
        // sorts the scores and moves the creature with the bubble sorting
        float temp;
        Creature tempCreature;
        boolean swapped;
        
        for (int i = 0; i < scores.size()-1; i++){
            swapped = false;
            for (int s = 0; s < scores.size()-i-1; s++){
                if (scores.get(s) > scores.get(s+1)){
                    // swap scores
                    temp = scores.get(s);
                    scores.set(s, scores.get(s+1));
                    scores.set(s+1, temp);
                    
                    // swap creatures
                    tempCreature = currentGeneration.get(s);
                    currentGeneration.set(s, currentGeneration.get(s+1));
                    currentGeneration.set(s+1, tempCreature);
                    
                    swapped = true;
                }
            }
            if (swapped == false){
                break;
            }
        }
    }
    
    // compares two species and  see how different they are
    static void speciate(Creature c1, Creature c2){
        int excess = 0, count = 0, disjoint = 0;
        float diff = 0, average = 0;
        
        // gets all the excess connections
        excess = Math.abs(c1.connections.size()-c2.connections.size());
        
        // gets all the non-similar connections and  average weight
        if (c1.connections.size() > c2.connections.size()){
            for (int i = 0; i < c1.connections.size(); i++){
                for (int s = 0; s < c2.connections.size(); s++){
                    if (c1.connections.get(i).inoId == c2.connections.get(s).inoId){
                        average += Math.abs(c1.connections.get(i).weight) + Math.abs(c2.connections.get(s).weight);
                        count++;
                        break;
                    }
                }
            }
        }
        else{
            for (int i = 0; i < c2.connections.size(); i++){
                for (int s = 0; s < c1.connections.size(); s++){
                    if (c2.connections.get(i).inoId == c1.connections.get(s).inoId){
                        average += Math.abs(c1.connections.get(i).weight) + Math.abs(c2.connections.get(s).weight);
                        count++;
                        break;
                    }
                }
            }
        }

        average = average / count;
        disjoint = c1.connections.size()-count;
        disjoint += c2.connections.size()-count;
        
        diff = excess + disjoint + average;
        System.out.println(diff+" "+excess+" "+disjoint+" "+average);

    }
    
    // specifically designed for XOR
    // verifys test results
    static private float xor(float num1, float num2) {
        if (num1 != num2) {
            return 1;
        }
        return 0;
    }
    
    // specifically designed for XOR
    static private float getFitness(Creature specimen) {
        int score = 0;
        
        // create the 4 possible test cases
        for (int i = 0; i < 4; i++) {
            ArrayList<Float> input = new ArrayList<Float>();
            input.add(0, (float) Math.round(i/4.0f));
            input.add(1, (float) i % 2);
            double val =  Math.round(1/(1+Math.exp(specimen.Calculate(input).get(0)*-1)));//sigmoid function
            if (val == xor(input.get(0), input.get(1))) {
                score++;
                // System.out.println("Test was: " + input.get(0)+" "+input.get(1));
                //   System.out.println("reply: "+ val);
                
                
            }
            else{
                //  System.out.println("wrong Test was: " +input.get(0)+" "+input.get(1));
                //  System.out.println("wrong reply: "+val);
                
            }
        }
        if (score == 4){
            System.out.println("PERECT SCOREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        }
        return (float) score;
    }
    
    
    public static void main(String[] args) throws IOException {
        ArrayList<Float> scores = new ArrayList<Float>();
        int name = 0;

        // generate initial population
        ArrayList<Creature> currentGeneration = new ArrayList<Creature>();
        for (int i = 0; i < NUM_AGENTS; i++){
            // each creature has random connection weights, with minimal nodes
            currentGeneration.add(new Creature(NUM_INPUTS, NUM_OUTPUTS));
            scores.add(getFitness(currentGeneration.get(i)));
            currentGeneration.get(i).creatureId = name;
            name++;
        }
       
        
        sort(currentGeneration, scores);
        
        System.out.println(scores);
         for (int i = 0; i < currentGeneration.size(); i++){
                System.out.print(currentGeneration.get(i).creatureId + " ");
            }

        // for every generation 
        for (int s = 0; s < NUM_GENERATIONS-1; s++){
             speciate(currentGeneration.get(0), currentGeneration.get(1));
            //System.out.println("NEW GENERATION: " + s);
            sort(currentGeneration, scores);
            // keep the top 20%
            for (int w = 0; w < Math.round(NUM_AGENTS*0.8); w++){
                scores.remove(0);
                currentGeneration.remove(0);
            }
            
            // duplicate the top 20% 4 times and mutate them and test them to fill the 80%
            for (int w = 0; w < Math.round(NUM_AGENTS*0.2); w++){
                for (int i = 0; i < 4; i++){
                    currentGeneration.add(currentGeneration.get(w));
                    currentGeneration.get(currentGeneration.size()-1).Mutate();
                    scores.add(getFitness(currentGeneration.get(currentGeneration.size()-1)));   
                }
            }
            
            sort(currentGeneration, scores);
            System.out.println(scores);
            for (int i = 0; i < currentGeneration.size(); i++){
                System.out.print(currentGeneration.get(i).creatureId + " ");
            }
        }
        //   System.out.println("END SCORE " + scores);
        // 1. conduct tests
        // 2. take 20 highest scorers
        // 3. for each mutuate 4 times and add to new population
        // 4. repeat steps 1-4 for num of generations
    }
}

