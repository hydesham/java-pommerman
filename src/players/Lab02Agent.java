/* OSLA Extended from lab02 */

package players;

import core.GameState;
import players.heuristics.CustomHeuristic;
import players.heuristics.StateHeuristic;
import utils.Types;
import utils.Utils;
import utils.Vector2d;

import java.util.*;

public class Lab02Agent extends Player {
    private Random random;
    private StateHeuristic rootStateHeuristic;
    private boolean rndOpponentModel;
    public double epsilon = 1e-6;

    public Lab02Agent(long seed, int id) {
        super(seed, id);
        reset(seed, id);
    }

    @Override
    public void reset(long seed, int playerID) {
        super.reset(seed, playerID);
        random = new Random(seed);
    }

    @Override
    public Types.ACTIONS act(GameState gs) {
        rootStateHeuristic = new CustomHeuristic(gs);
        rndOpponentModel = true;
        ArrayList<Types.ACTIONS> actionsList = Types.ACTIONS.all();
        Types.ACTIONS bestAction = null;
        double maxQ = Double.NEGATIVE_INFINITY;

        for (Types.ACTIONS act : actionsList) {
            GameState gsCopy = gs.copy();
            rollRnd(gsCopy, act);

            Vector2d myPosition = gsCopy.getPosition();

            boolean isDangerZone = false;
            // Find all bombs that may hit the player. Not fullproof and can still trap itself. Can be written better.
            for(int i = 0; i < gsCopy.getBoard().length; i++){
                isDangerZone = isDangerZoneFromBomb(i, myPosition.y, myPosition, gsCopy);
                isDangerZone = isDangerZoneFromBomb(myPosition.x, i, myPosition, gsCopy);
                isDangerZone = isDangerZoneFromBomb(i, myPosition.y, myPosition, gs);
                isDangerZone = isDangerZoneFromBomb(myPosition.x, i, myPosition, gs);
                if(isDangerZone){
                    break;
                }
            }

            double valState = rootStateHeuristic.evaluateState(gsCopy);

            // System.out.println("ValState:" + valState);
            double Q = Utils.noise(valState, this.epsilon, this.random.nextDouble());

            // System.out.println("Action:" + act + " score:" + Q);
            if (Q > maxQ && !(isDangerZone)) {
                maxQ = Q;
                bestAction = act;
            }
            // System.out.println(bestAction);
        }
        return bestAction;
    }

    private void rollRnd(GameState gs, Types.ACTIONS act)
    {
        //Simple, all random first, then my position. (This is from OSLA)
        int nPlayers = 4;
        Types.ACTIONS[] actionsAll = new Types.ACTIONS[4];

        for(int i = 0; i < nPlayers; ++i)
        {
            if(i == getPlayerID() - Types.TILETYPE.AGENT0.getKey())
            {
                actionsAll[i] = act;
            }else{
                if(rndOpponentModel){
                    int actionIdx = random.nextInt(gs.nActions());
                    actionsAll[i] = Types.ACTIONS.all().get(actionIdx);
                } else
                {
                    actionsAll[i] = Types.ACTIONS.ACTION_STOP;
                }
            }
        }

        gs.next(actionsAll);
    }

    private boolean isDangerZoneFromBomb(int x, int y, Vector2d myPosition, GameState gs) {
        boolean isDangerZoneFromBomb = false;
        Types.TILETYPE[][] board = gs.getBoard();
        Types.TILETYPE type = board[y][x];

        if (type == Types.TILETYPE.BOMB) {
            // Get strength
            int bombBlastStrength = gs.getBombBlastStrength()[y][x];

            if (bombBlastStrength > 0) {
                // System.out.println(bombBlastStrength);
                // Calculate Manhattan distance
                int distance = Math.abs(x - myPosition.x) + Math.abs(y - myPosition.y);
                //System.out.println(distance);

                // Can further be improved by setting isDangerZoneFromBomb based on timeleft of the bomb.
                if (distance <= bombBlastStrength) {
                    isDangerZoneFromBomb = true;
                }
            }
        }
        return isDangerZoneFromBomb;
    }

    @Override
    public int[] getMessage() {
        // default message
        return new int[Types.MESSAGE_LENGTH];
    }

    @Override
    public Player copy() {
        return new Lab02Agent(seed, playerID);
    }

}
