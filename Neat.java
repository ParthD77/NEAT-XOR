import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.awt.*;
import java.awt.event.*;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import draw.DrawPane;

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
    int id, type, y;
    
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
    int currentId = 0, inputNum, outputNum, creatureId, improve = 0;
    float scoresPrev = 0, scoresCur = 0;
    
    // constructor for base minimal creature
    public Creature(int inputs, int outputs) {
        this.inputNum = inputs;
        this.outputNum = outputs;
        // generates all inputs nodes
        for (int i = 0; i < inputs; i++) { 
            this.nodes.add(new Node(0, currentId, 0));
            this.nodes.get(i).layer = 0;
            this.nodes.get(i).y = i;
            currentId++;
        }
        
        // generates all output nodes
        for (int i = 0; i < outputs; i++) {
            this.nodes.add(new Node(0, currentId, 1));
            this.nodes.get(i+inputs).layer = 1;
            this.nodes.get(i).y = i;
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
        // 0 = new node                     2%
        // 1 = new connection               10%
        // 2 = remove a connection          3%
        // 3 = adjust 1 connection value    85%
        int generate = rand.nextInt(0,100);
        
        // choose  a random connection to add a node between
        // create a new connection from new node to end of old connection
        // move the end of old connection to new node
        if (generate == 0 || generate == 1){
            
            // choose a random connection and add a node
            int create = this.rand.nextInt(0, (connections.size())); 
            nodes.add(new Node(0, currentId, 2));
            nodes.get(nodes.size()-1).y = nodes.get(connections.get(create).start).y;
            
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
        
        else if (generate >= 2 && generate <= 11){
            
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
                int choice = rand.nextInt(0, start.size());
                connections.add(new Connection(rand.nextFloat() * 2.0f - 1.0f , start.get(choice), end.get(choice)));
            }
        }
        
        //If a there is a connection that can be removed
        else if (generate >= 12 && generate <= 14 &&  connections.size() > 1) {
            int change = rand.nextInt(0, connections.size());
            connections.remove(change);
        }
        
        //Adjust a random connections weight to a random number
        else if (generate >= 15 && generate <= 99) {
            int change = rand.nextInt(0, connections.size());
            connections.get(change).weight = rand.nextFloat() * 2.0f - 1.0f;
        }
        
        return null;
    }
    
    
    public ArrayList<Float> Calculate(ArrayList<Float> inputs) {
        totLayers.sort(null);

        // set each of the inputs nodes values
        for (int i = 0; i < inputNum; i++) { 
            nodes.get(i).value = inputs.get(i);
            
        }
        
        
        // for every layer get all nodes in it, for every node multiply value by its connection weight and send to ending node
        for (int i = 0; i < totLayers.size(); i++){
            for (int s = 0; s < nodes.size(); s++){
                if (nodes.get(s).layer == totLayers.get(i)){
                    for (int w = 0; w < connections.size(); w++){
                        if (connections.get(w).start==s){
                            System.out.println(connections.get(w).start+" "+nodes.get(s).id + " " + s);
                            nodes.get(connections.get(w).end).value += nodes.get(s).value*connections.get(w).weight;
                        }
                    }
                }
            }
        }
        
        // set the outputs
        ArrayList<Float> outputs = new ArrayList<Float>();
        for (int i = 0; i < outputNum; i++) { 
            outputs.add(nodes.get(inputNum+i).value);
            System.out.println(outputs.get(i));
        }

        
        return outputs;
    }
}


class run{
    static final int NUM_INPUTS = 2;
    static final int NUM_OUTPUTS = 1;
    static int NUM_AGENTS = 10;
    static int NUM_GENERATIONS = 500;
    static int get = 1;
    static ArrayList<Creature> currentGeneration = new ArrayList<Creature>();
    static ArrayList<Float> scores = new ArrayList<Float>();
    static ArrayList<Creature> best = new ArrayList<Creature>();
    
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
    private static void speciate(Creature c1, Creature c2){
        int excess = 0, count = 0, disjoint = 0;
        float diff = 0, average = 0;
        
        // gets all the excess connections
        excess = Math.abs(c1.connections.size()-c2.connections.size());
        
        // gets all the non-similar connections and  average weight
        for (int i = 0; i < c1.connections.size(); i++){
            for (int s = 0; s < c2.connections.size(); s++){
                if (c1.connections.get(i).inoId == c2.connections.get(s).inoId){
                    average += Math.abs(c1.connections.get(i).weight) + Math.abs(c2.connections.get(s).weight);
                    count++;
                    break;
                }
            }
        }
        if (count==0) count = 1;
        
        average = average / count;
        disjoint = c1.connections.size()-count;
        disjoint += c2.connections.size()-count;
        
        
        diff = excess + disjoint + average;
        // System.out.println("diff: " +diff+" "+excess+" "+disjoint+" "+average + "\n");
        
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
            double val =  Math.round(1/(1+Math.exp(specimen.Calculate(input).get(0)*-1)));  //sigmoid function
            if (val == xor(input.get(0), input.get(1))) {
                score++;
            }
        }
        if (score == 4){
            System.out.println("PERECT SCOREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        }
        return (float) score;
    }
    
    static void ai() throws IOException {
        int name = 0;
        currentGeneration.clear();
        scores.clear();
        
        // generate inital population
        for (int i = 0; i < NUM_AGENTS; i++){
            // each creature has random connection weights, with minimal nodes
            currentGeneration.add(new Creature(NUM_INPUTS, NUM_OUTPUTS));
            currentGeneration.get(i).creatureId = name;
            name++;
            scores.add(getFitness(currentGeneration.get(i)));
            currentGeneration.get(i).scoresCur = scores.get(i);
        }
        
        sort(currentGeneration, scores);
        best.add(currentGeneration.get(currentGeneration.size()-1));
        
        System.out.println("START" + scores);
        for (int i = 0; i < currentGeneration.size(); i++){
            System.out.print("s:"+scores.get(i) +" ID:"+ currentGeneration.get(i).creatureId + "   ");
        }
        System.out.println("");
        
        // for every generation 
        for (int s = 0; s < NUM_GENERATIONS-1; s++){
            speciate(currentGeneration.get(0), currentGeneration.get(9));
            sort(currentGeneration, scores);
            
            for (int i = 0; i < currentGeneration.size(); i++){
                System.out.print("s:"+scores.get(i) +" ID:"+ currentGeneration.get(i).creatureId + "  ");
            }
            System.out.println(s);
            
            // keep the top 20%
            for (int w = 0; w < Math.round(NUM_AGENTS*0.8); w++){
                scores.remove(0);
                currentGeneration.remove(0);
            }
            
            // remove the agent if it hasnt improved over 15 generations
            for (int w = 0; w < currentGeneration.size(); w++){ 
                if (currentGeneration.get(w).scoresCur == currentGeneration.get(w).scoresPrev){
                    currentGeneration.get(w).improve++;
                }
                else{
                    currentGeneration.get(w).scoresPrev = currentGeneration.get(w).scoresCur;
                    currentGeneration.get(w).improve = 0;
                }
                
                if (currentGeneration.get(w).improve >= 50 && scores.get(w) != 4){
                    System.out.println("reset nodes: " + currentGeneration.get(w).nodes.size() + " from agent " + currentGeneration.get(w).creatureId);
                    currentGeneration.set(w, new Creature(NUM_INPUTS, NUM_OUTPUTS));
                    scores.set(w, getFitness(currentGeneration.get(w)));
                    currentGeneration.get(w).improve = 0;
                    currentGeneration.get(w).creatureId = name;
                }
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
            for (int i = 0; i < get; i++){
                best.add(currentGeneration.get(currentGeneration.size()-1+i));
            }
        }
        
        sort(currentGeneration, scores);
        for (int i =0; i < currentGeneration.size(); i++){
            //if (scores.get(i) == 4){
                System.out.print(currentGeneration.get(i).nodes.size() + " ");
                //  }
            }
            System.out.println();
            System.out.println("END SCORE " + scores);
            // 1. conduct tests
            // 2. take 20 highest scorers
            // 3. for each mutuate 4 times and add to new population
            // 4. repeat steps 1-4 for num of generations
        }
    }
    
    
    class draw implements ActionListener {
        int disGen = 0;
        JFrame frame = new JFrame();
        
        JTextField track = new JTextField(run.get);
        JTextField agents = new JTextField(run.NUM_AGENTS);
        JTextField gens = new JTextField(run.NUM_GENERATIONS);
        JTextField showGens = new JTextField(0);
        
        
        JLabel trackLabel = new JLabel("Top agents shown:");
        JLabel agentsLabel = new JLabel("Agent Count:");
        JLabel genLabel = new JLabel("Generation Count:");
        JLabel showGenLabel = new JLabel("Generation Shown:");
        
        
        JButton trackBut = new JButton("Set");
        JButton agentBut = new JButton("Set");
        JButton genBut = new JButton("Set");
        JButton showGenBut = new JButton("Show");
        JButton runBut = new JButton("Run");
        JButton cheatBut  = new JButton("");
        DrawPane n = new DrawPane();
        
        
        draw(){
            // set the details
            frame.setTitle("NEAT");
            frame.add(n);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 1000);
            frame.setVisible(true);
            
            
            // set the dimensions
            trackLabel.setBounds(0, 0, 250, 20);
            track.setBounds(0, 20, 100, 20);
            trackBut.setBounds(102, 20, 60, 20);
            
            agentsLabel.setBounds(0, 40, 250, 20);
            agents.setBounds(0, 60, 100, 20);
            agentBut.setBounds(102, 60, 60, 20);
            
            genLabel.setBounds(0, 80, 250, 20);
            gens.setBounds(0, 100, 100, 20);
            genBut.setBounds(102, 100, 60, 20);
            
            runBut.setBounds(0, 130, 162, 70);
            
            showGenLabel.setBounds(0, 210, 250, 20);
            showGens.setBounds(0, 230, 100, 20);
            showGenBut.setBounds(102, 230, 70, 20);
            cheatBut.setBounds(0, 0, 0, 0);
            
            // make it so you can see when it was pressed
            trackBut.addActionListener(this);
            agentBut.addActionListener(this);
            genBut.addActionListener(this);
            showGenBut.addActionListener(this);
            runBut.addActionListener(this);
            
            // cheat button added last so no button fills upo the whole screen due to no layout manager
            cheatBut.setEnabled(false);
            cheatBut.setVisible(false);
            
            // add to the pannel
            frame.add(track);
            frame.add(trackLabel);
            frame.add(trackBut);
            frame.add(agentsLabel);
            frame.add(agents);
            frame.add(agentBut);
            frame.add(genLabel);
            frame.add(gens);
            frame.add(genBut);
            frame.add(runBut);
            frame.add(showGenLabel);
            frame.add(showGens);
            frame.add(showGenBut);
            frame.add(cheatBut);
        }
        
        // tests if a valid number was entered
        public int test(String text){
            int num;
            try{
                num = Integer.parseInt(text);
            }
            catch(Exception w){
                num = -1;
            }
            return num;
        }
        Boolean draws = false;
        // read key presses
        public void actionPerformed(ActionEvent e) {
            
            // if button was pressed
            if (e.getSource() == trackBut) {
                if (test(track.getText()) != -1) run.get = Integer.parseInt(track.getText());
                else track.setText("Whole # Only");
            }
            if (e.getSource() == agentBut) {
                if (test(agents.getText()) != -1) run.NUM_AGENTS = Integer.parseInt(agents.getText());
                else agents.setText("Whole # Only");
            }
            if (e.getSource() == genBut) {
                if (test(gens.getText()) != -1) run.NUM_GENERATIONS = Integer.parseInt(gens.getText());
                else gens.setText("Whole # Only");
            }
            // decides which generation to show 
            if (e.getSource() == showGenBut) {
                if (test(showGens.getText()) != -1){
                    if (Integer.parseInt(showGens.getText()) <= run.NUM_GENERATIONS){
                        disGen = Integer.parseInt(showGens.getText()); 
                        frame.repaint(); // repaints the new net
                    }
                    else{
                        disGen = run.NUM_GENERATIONS;
                        showGens.setText(run.NUM_GENERATIONS+"");
                    }
                } 
                else showGens.setText("Whole # Only");
            }
            
            if (e.getSource() == runBut){
                try {
                    run.ai();
                    frame.repaint(); // repaints the new net
                    
                }
                catch(Exception w){
                }
            }
        }
        
        // draws everything
        class DrawPane extends JPanel {
            public void paint(Graphics g) {
                DecimalFormat f = new DecimalFormat("#0.0000");
                Creature n = run.currentGeneration.get(disGen);
                int startx = 200, endx = 200, starty = 80, endy = 80;
                
                for (int i = 0; i < n.totLayers.size(); i++){
                    for (int w = 0; w < n.nodes.size(); w++){
                        
                        g.drawOval((int)(200+300*n.nodes.get(w).layer),
                        80+(int)(80*n.nodes.get(w).y), 50, 50);
                        
                        g.drawString(f.format(n.nodes.get(w).value)+"", 
                        (int)(200+300*n.nodes.get(w).layer),
                        80+(int)(80*n.nodes.get(w).y));
                    }   
                }
                for (int i = 0; i < n.connections.size(); i++){
                    for (int w = 0; w < n.nodes.size(); w++){
                        if (n.connections.get(i).start == n.nodes.get(w).id){
                            startx = (int)(225+300*n.nodes.get(w).layer);
                            starty = 105+(int)(80*n.nodes.get(w).y);
                        }
                        else if(n.connections.get(i).end == n.nodes.get(w).id){
                            endx = (int)(225+300*n.nodes.get(w).layer);
                            endy = 105+(int)(80*n.nodes.get(w).y);

                        }
                    }
                    g.drawLine(startx, starty, endx, endy);
                    g.drawString(f.format(n.connections.get(i).weight)+"", (int)(startx+endx)/2-20, (int)(starty+endy)/2);

                }
            }
        }
    }
    
    
    public class Neat {
        public static void main(String[] args) throws IOException {
            run.ai();
            int test = 0;
            
            for (int i = 0; i < run.currentGeneration.get(test).totLayers.size(); i++){
                System.out.println(run.currentGeneration.get(test).totLayers.get(i));
            }
            
            new draw();
        }
    }
    
