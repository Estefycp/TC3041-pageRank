/**
 * Created by Enrro on 08/3/2017.
 * for more information regarding page rank use the reference https://en.wikipedia.org/wiki/PageRank
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JOptionPane;

public class PageRank {


    //files csv to subtract information by commas
    private static final File NODES=new File("data/RedditNodes.csv");
    private static final File EDGES=new File("data/RedditEdges.csv");

    private static final String DEFAULT_SEPARATOR=",";

    //information regarding the Page rank formula
    private static final int NODE=0,EDGE=1;
    private static final double DEFAULT_FACTOR=0.85;
    private static final int DEFAULT_ITERATIONS=100;

    private HashMap<Long,String> nodes;
    private HashMap<Long,Integer> linksOutTable;
    private HashMap<Long,String> linksTable;
    private double factor;
    private String separator;
    private int iterations,edges;
    private String fileNameNodes,fileNameEdges;

    private double avg;

    //arraylist to show data that is better than average
    private ArrayList<String> betterThanAvg;

    private String max,min;

    public static void main(String[] args){
        int iterations[]={10,50,100};
        String result="";
        for(int i=0;i<iterations.length;i++){
            PageRank pg=new PageRank(DEFAULT_FACTOR,DEFAULT_SEPARATOR,iterations[i],NODES.getAbsolutePath(),EDGES.getAbsolutePath());

            HashMap<Long,String> nodes=new HashMap<Long,String>(pg.getNodes());


            System.out.println(pg.betterThanAvg.toString());

            String tmp[]=pg.max.split(pg.getSeparator());
            String tmp2[]=pg.min.split(pg.getSeparator());

            String maxS=tmp[1];
            String maxPR=tmp[2];

            String minS=tmp2[1];
            String minPR=tmp2[2];
            String avg=pg.avg+"";

            result+="Number of iterations: "+iterations[i]+"\n";
            result+="Number of nodes analyzed: "+nodes.size()+"\n";
            result+="Number of edges analyzed: "+pg.amountOfEdges()+"\n";
            result+="Max PageRank found: "+maxS+" : "+maxPR+"\n";
            result+="Minimum PageRank found: "+minS+" : "+minPR+"\n";
            result+="Average PageRank = "+avg+"\n\n";

        }

        JOptionPane.showMessageDialog(null, result);
    }
    /*
    page input is the factor, separator, iterations and the files where the data is located.
    the output of the constructor is an object of the type PageRank.
    An object of type pageRank has the filename of the nodes and edges of the database
    and three different hash maps to take care of the
     */

    public PageRank(double factor, String separator,int iterations,String fileNameNodes,String fileNameEdges){
        this.fileNameNodes=fileNameNodes;
        this.fileNameEdges=fileNameEdges;
        this.nodes=new HashMap<Long,String>();
        this.linksOutTable=new HashMap<Long,Integer>();
        this.linksTable=new HashMap<Long,String>();
        this.factor=factor;
        this.separator=separator;
        this.iterations=iterations;
        this.edges=0;
        this.avg=0.0;
        this.max="";
        this.min="";
        this.betterThanAvg=new ArrayList<String>();
        try {
            this.readFile(/*DEFAULT_NODES.getAbsolutePath()*/this.fileNameNodes, DEFAULT_SEPARATOR, NODE);
            this.readFile(/*DEFAULT_EDGES.getAbsolutePath()*/this.fileNameEdges, DEFAULT_SEPARATOR, EDGE);
            this.generatePageRanks();
            this.getBestAndWorstPR();
            this.getBetterAverages();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void generatePageRanks (){
        if(iterations<0) throw new IllegalArgumentException();

        while(iterations>=0){
            for(Long id : this.nodes.keySet()){

                String[] currentValue=this.nodes.get(id).split(separator);

                String linkedNodes[]=null;

                String line=this.linksTable.get(id);

                if(line!=null){
                    linkedNodes=line.split(separator);
                }
                else{
                    String result=currentValue[0]+separator+currentValue[1]+separator+Double.toString(1.0-this.factor);
                    this.nodes.put(id, result);
                    continue;
                }

                double sumPR=0.0;
                for(String node : linkedNodes){
                    double pr=Double.parseDouble(this.nodes.get(Long.parseLong(node)).split(separator)[2]);

                    Integer j=this.linksOutTable.get(Long.parseLong(node));

                    if(j!=null){

                        double ci=(double)j;

                        if(Math.abs(ci)>0.00001){
                            sumPR+=pr/ci;
                        }

                    }

                }

                sumPR*=this.factor;
                sumPR+=(1-this.factor);

                String result=currentValue[0]+separator+currentValue[1]+separator+Double.toString(sumPR);
                this.nodes.put(id, result);
            }
            iterations--;
        }
    }

    /*
    readFile
    A private helper method.
    Input. Path from the data, the kind of separator and the mode, the mode can be node (0) or edge(1)
     */
    private void readFile(String path, String separator, int mode) throws IOException{
        BufferedReader bf=new BufferedReader(new FileReader(new File(path)));

        String line=bf.readLine();

        //Reed the first line of the file.
        //Every file has the name of the column in the first line.
        ArrayList<String> columns=new ArrayList<String>(Arrays.asList(line.split(separator)));

        //Initialise columns.
        int col1, col2=0,col3=0;

        col1=columns.indexOf("id");

        //Identify if we are dealing with a node or an edge
        if(mode==NODE){
            col2=columns.indexOf("username");
            col3=columns.indexOf("category");
        }
        else if(mode==EDGE){
            col2=columns.indexOf("Source");
            col3=columns.indexOf("Target");
        }

        line=bf.readLine();

        String[] tmp;

        //we now iterate from top to bottom looking for the appropriate column
        while(line!=null){

            tmp=line.split(separator);

            long id=Long.parseLong(tmp[col1].replaceAll("\"", ""));

            if(mode==NODE){
                this.nodes.put(id, tmp[col2]+separator+tmp[col3]+separator+"0.0");
            }
            else if(mode==EDGE){
                edges++;
                long source=Long.parseLong(tmp[col2]);

                String value = this.linksTable.get(source);

                Integer i=this.linksOutTable.get(source);

                value = (value==null) ? tmp[col3] : value+separator+tmp[col3];

                i = (i==null) ? 1 : i+1;

                this.linksOutTable.put(source, i);
                this.linksTable.put(source, value);
            }

            line=bf.readLine();
        }


        bf.close();
    }

    public double getAvg(){
        return this.avg;
    }

    public void getBestAndWorstPR(){
        double currentPR;

        double max=Double.NEGATIVE_INFINITY,min=Double.POSITIVE_INFINITY;

        String idMax="",idMin="",categoryMax="",categoryMin="";

        Long id1=0l,id2=0l;

        for(Long node : nodes.keySet()){
            String[] tmp=nodes.get(node).split(this.separator);
            currentPR=Double.parseDouble(tmp[2]);

            if(max<currentPR){
                max=currentPR;
                idMax=tmp[0];
                categoryMax=tmp[1];
                id1=node;
            }

            if(min>currentPR){
                min=currentPR;
                idMin=tmp[0];
                categoryMin=tmp[1];
                id2=node;
            }
        }
        this.max=Long.toString(id1)+this.separator+idMax+this.separator+categoryMax+separator+Double.toString(max);
        this.min=Long.toString(id2)+this.separator+idMin+this.separator+categoryMin+separator+Double.toString(min);
    }

    public String getSeparator(){
        return this.separator;
    }

    public String getMax(){
        return this.max;
    }

    public String getMin(){
        return this.min;
    }

    public void getBetterAverages(){
        double currentPR;
        for(Long node : nodes.keySet()){

            String[] tmp=nodes.get(node).split(this.separator);

            currentPR=Double.parseDouble(tmp[2]);

            this.avg+=currentPR;
        }

        this.avg=avg/nodes.size();


        for(Long node : nodes.keySet()){
            String[] tmp=nodes.get(node).split(this.separator);

            double actual=Double.parseDouble(tmp[2]);

            if(actual>=this.avg){
                this.betterThanAvg.add(Long.toString(node)+this.separator+tmp[0]+this.separator+tmp[1]+separator+tmp[2]);
            }
        }

    }

    public Map<Long,String> getNodes(){
        return Collections.unmodifiableMap(this.nodes);
    }

    public Map<Long,String> getLinks(){
        return Collections.unmodifiableMap(this.linksTable);
    }

    public Map<Long,Integer> getLinksOut(){
        return Collections.unmodifiableMap(this.linksOutTable);
    }

    public int amountOfEdges(){
        return this.edges;
    }

    public int amountOfNodes(){
        return this.nodes.size();
    }
}
