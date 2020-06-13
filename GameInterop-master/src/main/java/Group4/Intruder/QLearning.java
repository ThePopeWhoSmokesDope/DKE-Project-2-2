package Group4.Intruder;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Group4.Agent;
import Group4.OurInterop.*;
import Group9.Game;
import Group9.agent.container.GuardContainer;
import Group9.agent.container.IntruderContainer;
import Group9.math.Vector2;
import Group9.tree.PointContainer;
import Group9.agent.container.AgentContainer;
import Group9.agent.container.IntruderContainer;
import Interop.Action.IntruderAction;
import Interop.Action.Move;
import Interop.Action.Rotate;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Scenario.SlowDownModifiers;
import Interop.Percept.Vision.FieldOfView;

import static java.lang.Math.abs;

public class QLearning implements Intruder{
    static double threshold = 0.1;
    double targetDirection = 0;
    static int targetX = 0;
    static int targetY = 0;
    private int numberOfMoves = 0;
    static int maximumMovesBeforeThresholdChange = 50;
    public File mapFile = new File("C:/Users/Me/Dropbox/Uni/Project/2-2/DKE-Project-2-2/GameInterop-master/src/main/java/Group9/map/maps/test_2.map");
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final Path filePath = Paths.get(String.valueOf(mapFile));


    @Override
    public IntruderAction getAction(IntruderPercepts percepts) {
        if(!percepts.wasLastActionExecuted())
        {
            return new Rotate(Angle.fromRadians(percepts.getScenarioIntruderPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble()));
        }
        else
        {
            return new Move(new Distance(percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder().getValue() * getSpeedModifier(percepts)));
        }
    }

    private double getSpeedModifier(IntruderPercepts guardPercepts)
    {
        SlowDownModifiers slowDownModifiers =  guardPercepts.getScenarioIntruderPercepts().getScenarioPercepts().getSlowDownModifiers();
        if(guardPercepts.getAreaPercepts().isInWindow())
        {
            return slowDownModifiers.getInWindow();
        }
        else if(guardPercepts.getAreaPercepts().isInSentryTower())
        {
            return slowDownModifiers.getInSentryTower();
        }
        else if(guardPercepts.getAreaPercepts().isInDoor())
        {
            return slowDownModifiers.getInDoor();
        }

        return 1;
    }


    public void readMap(){
        try (Scanner scanner = new Scanner(filePath, ENCODING.name())){
            while (scanner.hasNextLine()){
                parseLine(scanner.nextLine());
            }
        }

        catch(Exception e){

        }
    }

    protected void parseLine(String line){
        try(Scanner scanner = new Scanner(line)){
            scanner.useDelimiter("=");
            if(scanner.hasNext()){
                String id = scanner.next();
                String value = scanner.next();
                value = value.trim();
                id = id.trim();
                String[] items = value.split(" ");
                switch(id){
                    case "targetArea":
                        targetX = Integer.parseInt(items[0]);
                        targetY = Integer.parseInt(items[1]);
                }
            }
        }
    }


/*
    public AMove Qlearning(ArrayList<AMove> moves, IntruderContainer agent){
        //first check, if there are many moves made. If there are too many moves done, increase the chance of doing a random move.
        //This is useful when we need to teleport to get to the right room. With a higher chance of doing random moves,
        //we increase the chance of finding the teleport
        if (numberOfMoves > maximumMovesBeforeThresholdChange && threshold < 1){
            //prevent threshold from increasing > 1
            double residue = 1-threshold;
            double growth = 0.1 * residue;
            threshold = threshold + growth;
            //set the number of moves to 0 again
            numberOfMoves = 0;
        }


        //If the random number is smaller than the threshold:
        //Pick a random move out of the set of all moves
        Random generator = new Random();
        double epsilon = Math.random();
        if (epsilon < threshold) {
            int randomIndex = generator.nextInt(moves.size());
            AMove randomMove = moves.get(randomIndex);
            numberOfMoves++;
            return randomMove;
        }
        //Else, evaluate all moves and pick the move with the highest evaluation (lowest error in this case)
        else{
            int bestIndex = 0;
            double lowestError = Double.POSITIVE_INFINITY;
            //Loop through all moves
            for (int i=0; i<moves.size();i++){
                //Calculate evaluation
                double currentAbsoluteError = evaluateMove(moves.get(i),targetDirection);
                //If evaluation is better than current best, set new current best
                if(currentAbsoluteError<lowestError){
                    bestIndex = i;
                    lowestError = currentAbsoluteError;
                }
            }
            numberOfMoves++;
            return moves.get(bestIndex);
        }

    }
*/


    public double evaluateMove(AMove m, double targetDirection) {
        double absoluteError;
        double movingDirection;
        double xVector = getCurrentXLocation() - m.getX();
        double yVector = getCurrentYLocation() - m.getY();
        if (yVector == 0) {
            if (xVector > 0) {
                movingDirection = 0;
            } else {
                movingDirection = 180;
            }
        } else if (xVector == 0) {
            if (yVector > 0) {
                movingDirection = 90;
            } else {
                movingDirection = 270;
            }
        } else {
            movingDirection = Math.toDegrees(Math.atan(yVector / xVector));
        }
        absoluteError = abs(targetDirection - movingDirection);

        return absoluteError;

    }

    public void updateDirection(){
        double xVector = targetX - getCurrentXLocation();
        double yVector = targetY - getCurrentYLocation();

        if (yVector == 0) {
            if (xVector > 0) {
                this.targetDirection = 0;
            } else {
                this.targetDirection = 180;
            }
        } else if (xVector == 0) {
            if (yVector > 0) {
                this.targetDirection = 90;
            } else {
                this.targetDirection = 270;
            }
        } else {

            this.targetDirection = Math.toDegrees(Math.atan(yVector / xVector));
        }
    }

    static double x = 1;
    static double y = 1;



//    public static void main(String[] args){
//        QLearning q = new QLearning();
//        String mapD = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"GameControllerSample"+System.getProperty("file.separator")+"testmap.txt";
//        Scenario scenario = new Scenario(mapD);
//        //System.out.println(scenario.spawnAreaIntruders.getLeftBoundary());
//        q.setCurrentLocation(scenario.spawnAreaIntruders.getLeftBoundary() + (scenario.spawnAreaIntruders.getRightBoundary()-scenario.spawnAreaIntruders.getLeftBoundary())/2,
//                scenario.spawnAreaIntruders.getTopBoundary() + (scenario.spawnAreaIntruders.getBottomBoundary()-scenario.spawnAreaIntruders.getTopBoundary())/2);
//
//        //@Matt please check if this is the correct way to get the position out of the file
//        q.setTargetLocation(scenario.targetArea.getLeftBoundary() + (scenario.targetArea.getRightBoundary()-scenario.targetArea.getLeftBoundary())/2,
//                scenario.targetArea.getTopBoundary() + (scenario.targetArea.getBottomBoundary()-scenario.targetArea.getTopBoundary())/2);
//
//        //Set initial target direction
//        q.updateDirection();
//
//
//        Runnable run = new Runnable() {
//            public void run() {
//                ArrayList<AMove> moves = ActionsManager.radiusMoves(q.getCurrentXLocation(),q.getCurrentYLocation());
//                Move m =  q.Qlearning(moves);
//                double[] teleportCheck =  checkTeleport(q);
//                if(teleportCheck[0] != -1){
//                    moveExplorer(teleportCheck[1],teleportCheck[2],q);
//                    System.out.println("teleported");
//                }else{
//                    moveExplorer(m.x,m.y,q);
//                }
//                //The goal is to call this after every move, because direction to target changes
//                q.updateDirection();
//            }
//        };
//
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//        executor.scheduleAtFixedRate(run, 0, 5, TimeUnit.MILLISECONDS);
//
//    }

//    public static void moveExplorer(double x, double y,QLearning q){
//        String mapD = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"GameControllerSample"+System.getProperty("file.separator")+"testmap.txt";
//        String gameFileD = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"GameControllerSample"+System.getProperty("file.separator")+"gamefile.txt";
//        Scenario scenario = new Scenario(mapD);
//        double[][] guardPositions = new double[scenario.getNumGuards()][4];
//        int[] guardStates = new int[scenario.getNumGuards()];
//        //System.out.println(scenario.getGameFile().toString());
//        try{
//            //System.out.println("working");
//            FileWriter write = new FileWriter(gameFileD,false);
//            PrintWriter prtln = new PrintWriter(write);
//            prtln.println("signal = 1"); // semaphore code   0-> game controller, 1-> guards, 2-> intruder
//            prtln.println("scenario = " + scenario.getMapDoc());
//            for(int i=0;i<scenario.getNumGuards();i++){
//                prtln.println("guard test = "+String.valueOf(i)+" "+String.valueOf(guardStates[i])+" "+String.valueOf(guardPositions[i][0])+" "+String.valueOf(guardPositions[i][1])+" "+String.valueOf(guardPositions[i][2])+" "+String.valueOf(guardPositions[i][3]));
//            }
//            prtln.println("explorer = " + x + " " + y);
//            prtln.close();
//        }
//        catch(Exception e){
//            System.out.println("failed");
//            // we ar in trouble
//        }
//        q.setCurrentLocation(x,y);
//        System.out.println("agent moved " + x + " " + y);
//    }

//    public static double[] checkTeleport(QLearning q){
//        System.out.println("checking portals");
//        String mapD = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"GameControllerSample"+System.getProperty("file.separator")+"testmap.txt";
//        Scenario scenario = new Scenario(mapD);
//
//        double x = q.getCurrentLocation().getX();
//        double y = q.getCurrentLocation().getY();
//
//        ArrayList<TelePortal> portals = scenario.getTeleportals();
//
//        //System.out.println(scenario.getWalls().size());
//        for(TelePortal p: portals){
//            Rectangle2D portalRect = new Rectangle2D.Double(p.getLeftBoundary(),p.getBottomBoundary(), p.getRightBoundary() -p.getLeftBoundary() , p.getTopBoundary()-p.getBottomBoundary());
//            if(portalRect.contains(x,y)){
//                System.out.println("in portal");
//                return new double[]{1,Double.valueOf(p.getNewLocation()[0]),Double.valueOf(p.getNewLocation()[1])};
//                //moveExplorer(Double.valueOf(p.getNewLocation()[0]),Double.valueOf(p.getNewLocation()[1]),q);
//            }
//        }
//        return new double[]{-1,-1,-1};
//    }


}
