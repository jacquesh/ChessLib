/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ChessLeague.ChessLib;

/**
 *
 * @author D3zmodos
 */

import java.awt.Point;
import java.awt.Image;

import javax.swing.ImageIcon;

import java.util.Iterator;
import java.util.LinkedList;

public class ChessPiece {

    private LinkedList<Point> possibleMoves;
    
    private int piece;

    private final ChessGame game;
    private final ChessPlayer owner;

    private int moveCount;
    private int x = 0;
    private int y = 0;
    private Point previousLoc;

    ChessPiece(int pieceType, ChessPlayer pieceOwner, int startX, int startY){
        piece = pieceType;
        owner = pieceOwner;
        game = owner.game();
        moveCount = 0;
        x = startX;
        y = startY;
        previousLoc = new Point(x,y);
        
        if(pieceType != ChessGame.PIECE_TYPE_PAWN
                && pieceType != ChessGame.PIECE_TYPE_BISHOP
                && pieceType != ChessGame.PIECE_TYPE_KNIGHT
                && pieceType != ChessGame.PIECE_TYPE_ROOK
                && pieceType != ChessGame.PIECE_TYPE_QUEEN
                && pieceType != ChessGame.PIECE_TYPE_KING){
            
                throw new IllegalArgumentException("Invalid chess piece type specified.");
        }
        
        updateMoves();
    }

    void move(int newX, int newY){
        previousLoc.x = x;
        previousLoc.y = y;
        moveCount++;
        x = newX;
        y = newY;
    }
    
    void updateMoves(){
        possibleMoves = game.listMoves(this);
    }

    void remove(){
        owner.removePiece(this);
    }
    
    void promote(int pieceType){
        piece = pieceType;
        //Recalculate Possible moves
        possibleMoves = game.listMoves(this);
    }
    
    public LinkedList<Point> possibleMoves(){
        return (LinkedList<Point>)possibleMoves.clone();
    }

    public Image getImage(){
        String filename = "images/";
        
        if(owner == game.PLAYER_WHITE){
            filename += "white";
        }else if(owner == game.PLAYER_BLACK){
            filename += "black";
            
        }else{
            return null;
        }
        
        switch(piece){
            case (ChessGame.PIECE_TYPE_PAWN):
                filename += "pawn";
                break;
                
            case (ChessGame.PIECE_TYPE_BISHOP):
                filename += "bishop";
                break;
                
            case (ChessGame.PIECE_TYPE_KNIGHT):
                filename += "knight";
                break;
                
            case (ChessGame.PIECE_TYPE_ROOK):
                filename += "rook";
                break;
                
            case (ChessGame.PIECE_TYPE_QUEEN):
                filename += "queen";
                break;
                
            case (ChessGame.PIECE_TYPE_KING):
                filename += "king";
                break;
                
            default:
                return null;
        }
        
        return new ImageIcon(filename+".png").getImage();
    }

    public int previousX(){
        return previousLoc.x;
    }
    
    public int previousY(){
        return previousLoc.y;
    }
    
    public int x(){
        return x;
    }

    public int y(){
        return y;
    }

    public int piece(){
        return piece;
    }

    public ChessPlayer owner(){
        return owner;
    }
    
    public int moveCount(){
        return moveCount;
    }

    void moveMinusOne(){
        moveCount--;
    }
    
    public boolean hasMoved(){
        return moveCount != 0;
    }
    
    public boolean canAccessPoint(int x, int y){
        Iterator<Point> j = possibleMoves.iterator();
            
        while(j.hasNext()){
            Point p = j.next();
            if(p.x == x && p.y == y){
                return true;
            }
        }
        return false;
    }
}
