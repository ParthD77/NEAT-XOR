import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


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
    ArrayList<Double> scores = new ArrayList<Double>();
    ArrayList<Connection> connections = new ArrayList<Connection>();
    Random rand = new Random();
    int currentId = 0, inputNum, outputNum, creatureId, improve = 0;
    float scoresPrev = 0, scoresCur = 0;
    
    // constructor for base minimal creature
    public Creature(int inputs, int outputs) {
        scores.add(0.0);                                                          //TEST
        scores.add(0.0);
        scores.add(0.0);
        scores.add(0.0);

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
            connections.add(new Connection((float)(Math.random()), currentId, connections.get(create).end)); 
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
        for (int i = 0; i < nodes.size(); i++){
            nodes.get(i).value = 0;
        }
        
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
         //   System.out.println(outputs.get(i));
        }
    //    System.out.println("");
        
        return outputs;
    }
}


class run{
    static final int NUM_INPUTS = 2;
    static final int NUM_OUTPUTS = 1;
    static int NUM_AGENTS = 10;
    static int NUM_GENERATIONS = 500;
    static int get = 1;
    static int cutoff = 50;
    static int name = 0;
    static ArrayList<Creature> currentGeneration = new ArrayList<Creature>();
    static ArrayList<Float> scores = new ArrayList<Float>();
    
    //  sorts the generation based on their scores
    static private void sort(ArrayList<Creature> currentGeneration){
        // bubble sorting
        Creature temp;
        boolean swapped;
        
        for (int i = 0; i < currentGeneration.size()-1; i++){
            swapped = false;
            for (int s = 0; s < currentGeneration.size()-i-1; s++){
                if (currentGeneration.get(s).scoresCur > currentGeneration.get(s+1).scoresCur){
                    // swap agents
                    temp = currentGeneration.get(s);
                    currentGeneration.set(s, currentGeneration.get(s+1));
                    currentGeneration.set(s+1, temp);
                
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
    static public float getFitness(Creature specimen) {
        int score = 0;
        
        // create the 4 possible test cases
        for (int i = 0; i < 4; i++) {
            ArrayList<Float> input = new ArrayList<Float>();
            input.add(0, (float) Math.round(i/4.0f));
            input.add(1, (float) i % 2);
            double val =  specimen.Calculate(input).get(0); 
        //    if (val == 0) val -= 0.0001;
        //    val = Math.round(1/(1+Math.exp(-val)));  //sigmoid function
        //    val = Math.abs(Math.round(val+0.25));    // testing!??!?!?
            val = Math.round(0.5*Math.sin(Math.PI*(val-0.5))+0.5);
            specimen.scores.set(i, val);
            if (val == xor(input.get(0), input.get(1))) {   
                score++;
            }
        }
        if (score == 4){
        //    System.out.println("PERECT SCOREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        }
        return (float) score;
    }

    public static ArrayList<Creature> initPop(){
        // generate inital population
        for (int i = 0; i < NUM_AGENTS; i++){
            // each creature has random connection weights, with minimal nodes
            currentGeneration.add(new Creature(NUM_INPUTS, NUM_OUTPUTS));
            currentGeneration.get(i).creatureId = name;
            currentGeneration.get(i).scoresCur = getFitness(currentGeneration.get(i));
            name++;
        }
        return currentGeneration;
    }
    
    static void ai() throws IOException {
        currentGeneration.clear();
               // generate inital population
        for (int i = 0; i < NUM_AGENTS; i++){
            // each creature has random connection weights, with minimal nodes
            currentGeneration.add(new Creature(NUM_INPUTS, NUM_OUTPUTS));
            currentGeneration.get(i).creatureId = name;
            currentGeneration.get(i).scoresCur = getFitness(currentGeneration.get(i));
            name++;
        }

        sort(currentGeneration);

        System.out.println("START" + scores);
        for (int i = 0; i < currentGeneration.size(); i++){
            System.out.print("s:"+currentGeneration.get(i).scoresCur +" ID:"+ currentGeneration.get(i).creatureId + "   ");
        }
        System.out.println("");
        
        // for every generation 
        for (int s = 0; s < NUM_GENERATIONS-1; s++){
        //    speciate(currentGeneration.get(0), currentGeneration.get(6));
            sort(currentGeneration);
            
            for (int i = 0; i < currentGeneration.size(); i++){
                System.out.print("s:"+currentGeneration.get(i).scoresCur +" ID:"+ currentGeneration.get(i).creatureId+ "  ");
            }
            System.out.println(s);
            
            // keep the top 20%
            for (int w = 0; w < Math.round(NUM_AGENTS*0.8); w++){
                currentGeneration.remove(0);
            }
            
            // remove the agent if it hasnt improved over 15 generations
            for (int w = 0; w < currentGeneration.size(); w++){ 
                if (currentGeneration.get(w).scoresCur <= currentGeneration.get(w).scoresPrev){
                    currentGeneration.get(w).improve++;
                }
                else{
                    currentGeneration.get(w).scoresPrev = currentGeneration.get(w).scoresCur;
                    currentGeneration.get(w).improve = 0;
                }
                
                if (currentGeneration.get(w).improve >= cutoff && (currentGeneration.get(w).scoresCur != 4)){
                    System.out.println("reset nodes: " + currentGeneration.get(w).nodes.size() + " from agent " + currentGeneration.get(w).creatureId);
                    currentGeneration.set(w, new Creature(NUM_INPUTS, NUM_OUTPUTS));
                    currentGeneration.get(w).scoresCur = getFitness(currentGeneration.get(w));
                    
                    currentGeneration.get(w).improve = 0;
                    currentGeneration.get(w).creatureId = name;
                }
               // System.out.println(currentGeneration.get(w).improve);

            }  
            
            // duplicate the top 20% 4 times and mutate them and test them to fill the 80%
            for (int w = 0; w < Math.round(NUM_AGENTS*0.2); w++){
                for (int i = 0; i < 4; i++){    
                    int max = currentGeneration.size()-1;
                    currentGeneration.add(0, currentGeneration.get(max-w));
                    currentGeneration.get(0).Mutate();

                    currentGeneration.get(0).creatureId = currentGeneration.get(max-w).creatureId+1;
                    currentGeneration.get(0).scoresCur = getFitness(currentGeneration.get(0));
                }

            }

        
            sort(currentGeneration);
        }
        
        sort(currentGeneration);
        for (int i =0; i < currentGeneration.size(); i++){
            //if (scores.get(i) == 4){
            //    System.out.print(currentGeneration.get(i).nodes.size() + " ");
                //  }
            }
           // System.out.println();
          //  System.out.println("END SCORE " + scores);
            // 1. conduct tests
            // 2. take 20 highest scorers
            // 3. for each mutuate 4 times and add to new population
            // 4. repeat steps 1-4 for num of generations
        }
    }
    
    
    class draw implements ActionListener {
        int disAgent = 0;
        JFrame frame = new JFrame();
        
        JTextField cutoff = new JTextField(run.cutoff+"");
        JTextField agents = new JTextField(run.NUM_AGENTS+"");
        JTextField gens = new JTextField(run.NUM_GENERATIONS+"");
        JTextField showAgent = new JTextField();
        
        
        JLabel cutoffLabel = new JLabel("Generation Cutoff: ");
        JLabel agentsLabel = new JLabel("Agent Count:");
        JLabel genLabel = new JLabel("Generation Count:");
        JLabel showAgentLabel = new JLabel("Agent Shown: ");
        
        
        JButton cutoffBut = new JButton("Set");
        JButton agentBut = new JButton("Set");
        JButton genBut = new JButton("Set");
        JButton showAgentBut = new JButton("Show");
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
            cutoffLabel.setBounds(0, 0, 250, 20);
            cutoff.setBounds(0, 20, 100, 20);
            cutoffBut.setBounds(102, 20, 60, 20);
            
            agentsLabel.setBounds(0, 40, 250, 20);
            agents.setBounds(0, 60, 100, 20);
            agentBut.setBounds(102, 60, 60, 20);
            
            genLabel.setBounds(0, 80, 250, 20);
            gens.setBounds(0, 100, 100, 20);
            genBut.setBounds(102, 100, 60, 20);
            
            runBut.setBounds(0, 130, 162, 70);
            
            showAgentLabel.setBounds(0, 210, 250, 20);
            showAgent.setBounds(0, 230, 100, 20);
            showAgentBut.setBounds(102, 230, 70, 20);

            cheatBut.setBounds(0, 0, 0, 0);
            
            // make it so you can see when it was pressed
            cutoffBut.addActionListener(this);
            agentBut.addActionListener(this);
            genBut.addActionListener(this);
            showAgentBut.addActionListener(this);
            runBut.addActionListener(this);
            
            // cheat button added last so no button fills upo the whole screen due to no layout manager
            cheatBut.setEnabled(false);
            cheatBut.setVisible(false);
            
            // add to the pannel
            frame.add(cutoff);
            frame.add(cutoffLabel);
            frame.add(cutoffBut);
            frame.add(agentsLabel);
            frame.add(agents);
            frame.add(agentBut);
            frame.add(genLabel);
            frame.add(gens);
            frame.add(genBut);
            frame.add(runBut);
            frame.add(showAgentLabel);
            frame.add(showAgent);
            frame.add(showAgentBut);
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
            if (e.getSource() == cutoffBut) {
                if (test(cutoff.getText()) != -1) run.cutoff = Integer.parseInt(cutoff.getText());
                else cutoff.setText("Whole # Only");
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
            if (e.getSource() == showAgentBut) {  // make it so it gets the specific agent from the gen rather than gen, and add a button to go to next gen and do the stuff
                if (test(showAgent.getText()) >= 0){
                    if (Integer.parseInt(showAgent.getText()) < run.NUM_AGENTS){
                        disAgent = Integer.parseInt(showAgent.getText()); 
                        frame.repaint(); // repaints the new net
                    }
                    else{
                        disAgent = run.NUM_AGENTS-1;
                        showAgent.setText(run.NUM_AGENTS-1+"");
                    }
                } 
                else showAgent.setText("Whole # Only");
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
                DecimalFormat f = new DecimalFormat("#0.000");
                Creature n = run.currentGeneration.get(disAgent);
                int startx = 200, endx = 200, starty = 80, endy = 80;

                // shows test cases and answers and score
                g.drawString(run.getFitness(n)+"", 790, 120);
                for (int i =0; i < 4; i++){
                g.drawString((float) Math.round(i/4.0f) + ", " + (float) i % 2+ " = "+
                n.scores.get(i), 650, 120+i*30);
                }
                
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
            
            
            new draw();
        }
    }
    
