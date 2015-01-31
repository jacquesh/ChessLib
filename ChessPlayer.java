/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ChessLeague.ChessLib;

/**
 *
 * @author D3zmodos
 */
import java.util.Iterator;
import java.util.ArrayList;

public class ChessPlayer {

    private ChessGame owner;
    private int playerID;

    private int forwardDirectionMultiplier; //This exists to allow each player to have his/her own sense of "forward" IE if you're comparing 2 as an inequality, multiply both sides by this

    private boolean inCheck;
    private ArrayList<ChessPiece> pieces;
    private ChessPiece king;
    
    ChessPlayer(boolean movesUp, ChessGame parentGame){
        pieces = new ArrayList<ChessPiece>(16);
        owner = parentGame;
        playerID = new Boolean(!movesUp).compareTo(false);//Boolean.compare(!movesUp,false); -> This is more efficient but is only available in java 1.7
        
        if(movesUp){
            forwardDirectionMultiplier = 1;
        }else{
            forwardDirectionMultiplier = -1;
        }
    }
    
    ChessPiece addPiece(int pieceType, int x, int y){
        ChessPiece cp = new ChessPiece(pieceType,this,x,y);
        
        if(pieceType == ChessGame.PIECE_TYPE_KING){
            king = cp;
        }
        
        pieces.add(cp);
        return cp;
    }

    void updateMovementPossibility(){
        //Refresh each piece's movements
        Iterator<ChessPiece> i = pieces.iterator();
        while(i.hasNext()){
            ChessPiece cp = i.next();
            cp.updateMoves();
        }
    }
    
    void check(boolean isCheck){
        inCheck = isCheck;
    }
    
    void removePiece(ChessPiece cp){
        pieces.remove(cp);
    }
    
    public int getTotalValue(){
        int result = 0;
        Iterator<ChessPiece> i = pieces.iterator();
        
        while(i.hasNext()){
            ChessPiece cp = i.next();
            switch(cp.piece()){
                case(ChessGame.PIECE_TYPE_PAWN):
                    result += 1;
                    break;
                case(ChessGame.PIECE_TYPE_BISHOP):
                case(ChessGame.PIECE_TYPE_KNIGHT):
                    result += 3;
                    break;
                case(ChessGame.PIECE_TYPE_ROOK):
                    result += 5;
                    break;
                case(ChessGame.PIECE_TYPE_QUEEN):
                    result += 9;
                    break;
                default:
            }
        }
        
        return result;
    }
    
    public boolean canAccessPoint(int x, int y){
        //Check each piece to see if it can access the given point
        Iterator<ChessPiece> i = pieces.iterator();
        
        while(i.hasNext()){
            ChessPiece cp = i.next();
            if(cp.canAccessPoint(x,y)){
                return true;
            }
        }
        
        return false;
    }
    
    public int pieceCount(){
        return pieces.size();
    }
    
    public int moveCount(){
        //Add together all the total moves for each piece
        Iterator<ChessPiece> it = pieces.iterator();
        int result = 0;
        
        while(it.hasNext()){
            result += it.next().possibleMoves().size();
        }
        
        return result;
    }

    public boolean inCheck(){
        return inCheck;
    }
    
    public ChessGame game(){
        return owner;
    }
    
    public ChessPiece king(){
        return king;
    }
    
    public int id(){
        return playerID;
    }

    public int directionMultiplier(){
        return forwardDirectionMultiplier;
    }
    
}
