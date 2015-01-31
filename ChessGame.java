package ChessLeague.ChessLib;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jacques.heunis
 */

import java.util.LinkedList;
import java.util.Iterator;

import java.awt.Point;

public class ChessGame{

    public static final int PIECE_TYPE_PAWN = 0;
    public static final int PIECE_TYPE_BISHOP = 1;
    public static final int PIECE_TYPE_KNIGHT = 2;
    public static final int PIECE_TYPE_ROOK = 3;
    public static final int PIECE_TYPE_QUEEN = 4;
    public static final int PIECE_TYPE_KING = 5;

    final ChessPlayer PLAYER_WHITE;
    final ChessPlayer PLAYER_BLACK;
    private ChessPlayer winner = null;

    private ChessPiece[][] chessBoard;
    private boolean playerTurnWhite;
    private boolean playing;
    private boolean replay;
    
    private LinkedList<ChessListener> listenerList;

    private int stalemateMoveCount;
    //private Stack<String> commandStack;
    //private Thread commandThread;
    
    public ChessGame(){
        PLAYER_WHITE = new ChessPlayer(true,this);
        PLAYER_BLACK = new ChessPlayer(false,this);

        listenerList = new LinkedList<ChessListener>();
        
        initBoard();
    }
    
    public void setReplay(boolean isReplay){
        replay = isReplay;
    }
    
    public ChessPiece getPiece(int x, int y){
        if(x<0 || x>7 || y<0 || y>7){
            return null;
        }
        
        return chessBoard[x][y];
    }

    public ChessPlayer playerToMove(){
        return playerTurnWhite ? PLAYER_WHITE : PLAYER_BLACK;
    }
    
    public ChessPlayer textToPlayer(String idText){
        if(idText == null){
            throw new IllegalArgumentException("Invalid text given as player id: \""+idText+"\"");
        }
        if(idText.length() < 1){
            throw new IllegalArgumentException("Invalid text given as player id: \""+idText+"\"");
        }
        
        char idChar = idText.charAt(0);
        if(idChar == '0'){
            return PLAYER_WHITE;
        }else if(idChar == '1'){
            return PLAYER_BLACK;
        }else{
            throw new IllegalArgumentException("Invalid text given as player id: \""+idText+"\"");
        }
    }
    
    public String coordToText(int x, int y){
        char cx = (char)(x+'A');
        char cy = (char)(y+'1');
        
        return "" + cx + cy;
    }
    
    private Point textToCoord(String block){
        if(block == null){
            throw new IllegalArgumentException("Invalid block name given: "+block);
        }
        if(block.length() != 2){
            throw new IllegalArgumentException("Invalid block name given: "+block);
        }
        if(!Character.isLetter(block.charAt(0))){
            throw new IllegalArgumentException("Invalid block name given: "+block);
        }
        if(!Character.isDigit(block.charAt(1))){
            throw new IllegalArgumentException("Invalid block name given: "+block);
        }
        
        int x = block.charAt(0)-'A';
        int y = block.charAt(1)-'1';
        
        return new Point(x,y);
    }
    
    public int getPlayerScore(int player){
        if(player < -1 || player > 1){
            throw new IllegalArgumentException("Invalid player");
        }
        //A player's score is the total of his pieces
        ChessPlayer cp = (player == 0) ? PLAYER_WHITE : PLAYER_BLACK;

        return cp.getTotalValue();
    }
    
    private void undoCommandInternal(String cmd, String[] parameters){
        if(cmd.equals("MOVE")){
            //MOVE <FROM LOCATION> <TO LOCATION>
            if(parameters.length != 2){
                throw new IllegalArgumentException("Invalid parameter count for the given command.");
            }
            
            Point cpPoint = textToCoord(parameters[1]);
            Point newPoint = textToCoord(parameters[0]);
            
            moveUnchecked(chessBoard[cpPoint.x][cpPoint.y],newPoint.x,newPoint.y);
            
        }else if(cmd.equals("TAKE")){
            //TAKE <LOCATION> <TAKEN PIECE'S ID> <TAKEN PIECE'S OWNER ID> 
            if(parameters.length != 3){
                throw new IllegalArgumentException("Invalid parameter count for the given command.");
            }
            Point target = textToCoord(parameters[0]);
            
            if(parameters[2].charAt(0) == '0'){
                chessBoard[target.x][target.y] = PLAYER_WHITE.addPiece(Integer.parseInt(parameters[1]),target.x,target.y);
            }else{
                chessBoard[target.x][target.y] = PLAYER_BLACK.addPiece(Integer.parseInt(parameters[1]),target.x,target.y);
            }
            
        }else if(cmd.equals("PROMOTE")){
            //PROMOTE <PIECE LOCATION> <NEW PIECETYPE CHARACTER (Q/K/R/B)>
            if(parameters.length != 2){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            
            Point cpPoint = textToCoord(parameters[0]);
            chessBoard[cpPoint.x][cpPoint.y].promote(PIECE_TYPE_PAWN);
        }
    }
    
    public void gameCommand(String command){
        //commandStack.add(command);
        //notify();
        gameCommandInternal(command);
    }
    
    private void gameCommandInternal(String command){
        if(!(playing)){
            return;
        }
        
        if(command.length() == 0){
            throw new IllegalArgumentException("Empty command string.");
        }
        
        int firstSpaceIndex = command.indexOf(" ");
        String cmd;
        String[] parameters;
        
        if(firstSpaceIndex == -1){
            cmd = command;
            parameters = new String[0]; 
            
        }else{
            cmd = command.substring(0,firstSpaceIndex).toUpperCase();
            
            String tempStorage = command.substring(firstSpaceIndex+1);
            tempStorage = tempStorage.toUpperCase();

            //Split the string on it's spaces (apparently "\\s+" is safer than " ")
            parameters = tempStorage.split("\\s+");
        }

        if(cmd.equals("MOVE")){
            //MOVE <FROM LOCATION> <TO LOCATION>
            if(parameters.length != 2){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            
            Point cpPoint = textToCoord(parameters[0]);
            Point newPoint = textToCoord(parameters[1]);
            
            move(chessBoard[cpPoint.x][cpPoint.y],newPoint.x,newPoint.y);
            
        }else if(cmd.equals("PROMOTE")){
            //PROMOTE <PIECE LOCATION> <NEW PIECETYPE CHARACTER (Q/K/R/B)>
            if(parameters.length != 2){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            
            Point cpPoint = textToCoord(parameters[0]);
            int type = -1;
            
            //Check to see that the pawn is at the opposite side of the board
            if(chessBoard[cpPoint.x][cpPoint.y] != null){
                if(cpPoint.y == (int)(3.5 + 3.5*chessBoard[cpPoint.x][cpPoint.y].owner().directionMultiplier())){
                    if((chessBoard[cpPoint.x][cpPoint.y] != null) && (chessBoard[cpPoint.x][cpPoint.y].piece() == PIECE_TYPE_PAWN)){
                        switch(parameters[1].charAt(0)){
                            case('Q'):
                                type = PIECE_TYPE_QUEEN;
                                break;

                            case('K'):
                                type = PIECE_TYPE_KNIGHT;
                                break;

                            case('R'):
                                type = PIECE_TYPE_ROOK;
                                break;

                            case('B'):
                                type = PIECE_TYPE_BISHOP;
                                break;
                            //There is no need for a default case as the variable is already initialized to -1, so it would simply not change
                        }

                        if(type > -1){
                            chessBoard[cpPoint.x][cpPoint.y].promote(type);
                        }
                    }
                }
            }
            
        }else if(cmd.equals("TAKE")){
            //TAKE <LOCATION> <TAKEN PIECE'S ID> <TAKEN PIECE'S OWNER ID>
            if(parameters.length != 3){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            stalemateMoveCount = 0;

            Point target = textToCoord(parameters[0]);
            
            chessBoard[target.x][target.y].remove();
            chessBoard[target.x][target.y] = null;

        }else if(cmd.equals("UNDO")){
            if(parameters.length < 1){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            String[] undoCmd = new String[parameters.length-1];
            for(int i=1; i<parameters.length;i++){
                undoCmd[i-1] = parameters[i];
            }
            undoCommandInternal(parameters[0],undoCmd);
            
            PLAYER_WHITE.updateMovementPossibility();
            PLAYER_BLACK.updateMovementPossibility();

        }else if(cmd.equals("CONCEDE")){
            //CONCEDE <CONCEDING PLAYER>
            if(parameters.length != 1){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            gameCommandInternal("CHECKMATE "+parameters[0]);
            
        }else if(cmd.equals("CHECKMATE")){
            //CHECKMATE <LOSING PLAYER ID>
            if(parameters.length != 1){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            ChessPlayer loser = textToPlayer(parameters[0]);
            if(loser == PLAYER_WHITE){
                winner = PLAYER_BLACK;
            }else if(loser == PLAYER_BLACK){
                winner = PLAYER_WHITE;
            }
            
            playing = false;
            
        }else if(cmd.equals("STALEMATE")){
            //STALEMATE
            if(parameters.length != 0){
                throw new IllegalArgumentException("Invalid parameter count for command: "+cmd);
            }
            playing = false;
        }
        
        notifyListeners(cmd,parameters);
    }

    private void notifyListeners(String event,String[] parameters){
        if(event.equals("UNDO")){
            return;
        }
        
        ChessEvent evt = new ChessEvent(event,parameters);
        
        Iterator<ChessListener> i = listenerList.iterator();
        while(i.hasNext()){
            i.next().chessEvent(evt);
        }
    }
    
    private void move(ChessPiece cp, int newX, int newY){
        if(cp == null){
            return;
        }
        boolean isWhitesPiece = (cp.owner() == PLAYER_WHITE);
        if(playerTurnWhite == isWhitesPiece || replay){
            //Check to see if the move is legal
            if(cp.canAccessPoint(newX, newY)){
                moveUnchecked(cp,newX,newY);
                playerTurnWhite = !playerTurnWhite;
            }
        }
        
        PLAYER_WHITE.updateMovementPossibility();
        PLAYER_BLACK.updateMovementPossibility();

        //Modify the stalemate move counter (to see when 50 moves have passed) according to the move
        if(cp.piece() == PIECE_TYPE_PAWN){
            stalemateMoveCount = 0;
        }else{
            stalemateMoveCount++;
            if(stalemateMoveCount == 50){
                notifyListeners("50MOVES",null);
            }
        }

        //Check to see if it is a stalemate
        if(isStalemate()){
            //Stalemate
            gameCommandInternal("STALEMATE");
        }
        
        if(PLAYER_WHITE.canAccessPoint(PLAYER_BLACK.king().x(), PLAYER_BLACK.king().y())){
            PLAYER_BLACK.check(true);
            PLAYER_BLACK.updateMovementPossibility();
            notifyListeners("CHECK",new String[]{""+PLAYER_BLACK.id()});
            
            if(PLAYER_BLACK.moveCount() == 0){
                //Checkmate
                gameCommandInternal("CHECKMATE "+PLAYER_BLACK.id());
            }
            
        }else if(PLAYER_BLACK.canAccessPoint(PLAYER_WHITE.king().x(), PLAYER_WHITE.king().y())){
            PLAYER_WHITE.check(true);
            PLAYER_WHITE.updateMovementPossibility();
            notifyListeners("CHECK",new String[]{""+PLAYER_WHITE.id()});
            
            if(PLAYER_WHITE.moveCount() == 0){
                //Checkmate
                gameCommandInternal("CHECKMATE "+PLAYER_BLACK.id());
            }
        }
        
        return;
    }
    
    private void moveUnchecked(ChessPiece cp, int newX, int newY){
        boolean castling = false;
        if(cp == null){
            return;
        }
        if(chessBoard[newX][newY] != null){
            //Check to see if it can be an occurrance of castling
            if((cp.piece() == PIECE_TYPE_ROOK) && (chessBoard[newX][newY].piece() == PIECE_TYPE_KING)){
                if(chessBoard[newX][newY].owner() == cp.owner()){
                    //Castling!
                    castling = true;
                    int kingCastleDirection = (cp.x()-newX)/Math.abs(cp.x()-newX);

                    //The Rook must move here first, because the rook's possible moves will get re-updated when this block exits (yes recursion gets confusing)
                    moveUnchecked(cp,newX+kingCastleDirection,newY);    //Move the Rook
                    moveUnchecked(chessBoard[newX][newY],newX+(kingCastleDirection*2),newY);    //Move the King
                }
                
            }else{ //The following should only NOT happen if we are castling
                
               //take the piece that is already there
                String location = coordToText(newX,newY);
                gameCommandInternal("TAKE " + location +" "+ chessBoard[newX][newY].piece() +" "+chessBoard[newX][newY].owner().id() );
            }
            
            //Check for taking pawns en passant
        }else if((cp.piece() == PIECE_TYPE_PAWN) && (newX != cp.x()) && (chessBoard[newX][newY] == null) && (chessBoard[newX][cp.y()] != null) && (chessBoard[newX][cp.y()].owner() != cp.owner())){
            //Before we take the piece, we just need to check that the piece is moving forward
            //Yes pawns can normally only move forward, but it could be undo'ing a move, in which case it'd be moving backwards
            if((newY * cp.owner().directionMultiplier() > cp.y() * cp.owner().directionMultiplier())){
                //take the piece en passant
                gameCommandInternal("TAKE " + coordToText(newX,cp.y()) +" "+ chessBoard[newX][cp.y()].piece() +" "+chessBoard[newX][cp.y()].owner().id());
            }
        }
        
        if(!(castling)){
            chessBoard[newX][newY] = cp;
            chessBoard[cp.x()][cp.y()] = null;
            cp.move(newX,newY);
        }
        
        //Check to see if the move will bring a pawn to the other side of the board (for promotion)
        if(cp.piece() == PIECE_TYPE_PAWN){
            if(newY == 3.5 + 3.5*cp.owner().directionMultiplier()){
                gameCommandInternal("CANPROMOTE "+coordToText(newX,newY));
            }
        }
    }
    
    public boolean isStalemate(){
        if(PLAYER_WHITE.inCheck() || PLAYER_BLACK.inCheck()){
            return false;
            
        }else if((PLAYER_WHITE.moveCount() == 0) || (PLAYER_BLACK.moveCount() == 0)){
            return true;
        }
        
        return false;
    }

    public LinkedList<Point> listMoves(ChessPiece cp){
        LinkedList<Point> moves = new LinkedList<Point>();
        int tempX;
        int tempY;
        
        if(cp.owner() == PLAYER_BLACK){
            if(cp.piece() == 0){
                if(cp.x() == 1){
                    System.out.print("");
                }
            }
        }
        
        switch(cp.piece()){
            case (PIECE_TYPE_PAWN): 
                for(int x=-1;x<=1;x++){
                    if(isMoveLegal(cp,cp.x()+x,cp.y()+1*cp.owner().directionMultiplier())){
                        moves.add(new Point(cp.x()+x,cp.y()+1*cp.owner().directionMultiplier()));
                    }
                }
                if( (!cp.hasMoved()) || replay ){
                    if(isMoveLegal(cp,cp.x(),cp.y()+2*cp.owner().directionMultiplier())){
                        moves.add(new Point(cp.x(),cp.y()+2*cp.owner().directionMultiplier()));
                    }
                }
                break;
                
            case (PIECE_TYPE_BISHOP):
                for(int i=-1;i<=1;i+=2){
                    for(int j=-1;j<=1;j+=2){
                        tempX = cp.x()+i; //i and j must be added here so that the check will pick it up for the first value if it is out of bounds
                        tempY = cp.y()+j; //e.g if the piece is in the bottom left corner then going -1 will be problematic

                        while(tempX<8 && tempX>=0 && tempY<8 && tempY>=0){
                            if(isMoveLegal(cp,tempX,tempY)){
                                moves.add(new Point(tempX,tempY));
                            }else{
                                if(!cp.owner().inCheck()){
                                    break;
                                }
                            }
                            tempX += i;
                            tempY += j;
                        }   
                    }
                }
                break;

            case (PIECE_TYPE_KNIGHT):
                for(int x=-2;x<=2;x++){
                    for(int y=-2;y<=2;y++){
                        if(x != 0 && y != 0 && x != y){
                            if(isMoveLegal(cp,cp.x()+x,cp.y()+y)){
                                moves.add(new Point(cp.x()+x,cp.y()+y));
                            }
                        }
                    }
                }
                break;

            case (PIECE_TYPE_ROOK):
                for(int i=-1;i<=1;i+=2){
                    tempX = cp.x()+i; //i and j must be added here so that the check will pick it up for the first value if it is out of bounds
                    tempY = cp.y()+i; //e.g if the piece is in the bottom left corner then going -1 will be problematic
                    
                    //Check all possible moves in a horizantal line
                    while(tempX<8 && tempX>=0){
                        if(isMoveLegal(cp,tempX,cp.y())){
                            moves.add(new Point(tempX,cp.y()));
                        }else{
                            if(!cp.owner().inCheck()){
                                break;
                            }
                        }
                        tempX += i;
                    }
                    
                    //Check all possible moves in a vertical line
                    while(tempY<8 && tempY>=0){
                        if(isMoveLegal(cp,cp.x(),tempY)){
                            moves.add(new Point(cp.x(),tempY));
                        }else{
                            if(!cp.owner().inCheck()){
                                break;
                            }
                        }
                        tempY += i;
                    }
                }
                break;

            case (PIECE_TYPE_QUEEN):
                for(int i=-1;i<=1;i+=2){
                    tempX = cp.x()+i; //i and j must be added here so that the check will pick it up for the first value if it is out of bounds
                    tempY = cp.y()+i; //e.g if the piece is in the bottom left corner then going -1 will be problematic
                    
                    //Check all possible moves in a horizantal line
                    while(tempX<8 && tempX>=0){
                        if(isMoveLegal(cp,tempX,cp.y())){
                            moves.add(new Point(tempX,cp.y()));
                        }else{
                            if(!cp.owner().inCheck()){
                                break;
                            }
                        }
                        tempX += i;
                    }
                    
                    //Check all possible moves in a vertical line
                    while(tempY<8 && tempY>=0){
                        if(isMoveLegal(cp,cp.x(),tempY)){
                            moves.add(new Point(cp.x(),tempY));
                        }else{
                            if(!cp.owner().inCheck()){
                                break;
                            }
                        }
                        tempY += i;
                    }
                }
                
                for(int i=-1;i<=1;i+=2){
                    for(int j=-1;j<=1;j+=2){
                        tempX = cp.x()+i; //i and j must be added here so that the check will pick it up for the first value if it is out of bounds
                        tempY = cp.y()+j; //e.g if the piece is in the bottom left corner then going -1 will be problematic

                        while(tempX<8 && tempX>=0 && tempY<8 && tempY>=0){
                            if(isMoveLegal(cp,tempX,tempY)){
                                moves.add(new Point(tempX,tempY));
                            }else{
                                if(!cp.owner().inCheck()){
                                    break;
                                }
                            }
                            tempX += i;
                            tempY += j;
                        }   
                    }
                }
                break;

            case (PIECE_TYPE_KING):
                for(int x=-1;x<=1;x++){
                    for(int y=-1;y<=1;y++){
                        if(x != 0 || y != 0){
                            if(isMoveLegal(cp,cp.x()+x,cp.y()+y)){
                                moves.add(new Point(cp.x()+x,cp.y()+y));
                            }
                        }
                    }
                }
                break;

        }
        
        //Make a list of all the possible moves according to the piece type
        //For every different direction in which the piece can move, check every block in that direction (except for with pawn/king) until it meets another piece
        //Lastly, iterate over all of the opponent's pieces (excluding king/pawn/knight) to see if moving would cause the player to be in check
        return moves;
    }

    private boolean isMoveLegal(ChessPiece cp, int newX, int newY){
        //Check to see that the new location can possibly fall into the board
        if( (newX < 0) || (newX > 7) || (newY < 0) || (newY > 7) ){
            return false;
        }

        //Check to see that the new location isnt the piece's current location
        if((cp.x() == newX) && (cp.y() == newY)){
            return false;
        }

        //Check to see if there is a friendly piece at the target, unless it is a rook, in which case it might be a target for castling
        if(chessBoard[newX][newY] != null){
            if(chessBoard[newX][newY].owner() == cp.owner()){
                if((cp.piece() != PIECE_TYPE_ROOK)|| (chessBoard[newX][newY].piece() != PIECE_TYPE_KING)){
                    return false;
                }
            }
        }

        //Check for check-ness
        //If it is a legal move then check for....check
        if(cp.owner().inCheck()){
            //The player wanting to move is in check, see if the target move would take a piece
            boolean willPreventCheck = false; //Stores whether or not the proposed move will take the king out of check
            ChessPiece takenPiece = null;
            String[] moveCmd;//Store commands to be undone
            ChessPlayer playerToCheck = (cp.owner() != PLAYER_WHITE) ? PLAYER_WHITE : PLAYER_BLACK; //Check to see if this player can access the king

            //Store this command for later use
            moveCmd = new String[]{coordToText(cp.x(),cp.y()),coordToText(newX,newY)};
            if(chessBoard[newX][newY] != null){
                //We know that the piece that is at the target is an enemy because this line would only
                //Run if the current piece could access the target location - IE not if there is a friendly piece
                //Store the piece for future reference and nullify it's board reference (make it "invisible" on the board
                takenPiece = chessBoard[newX][newY];
                chessBoard[newX][newY] = null;
            }

            //There is (now) nothing at the target position
            moveUnchecked(cp,newX,newY);
            cp.moveMinusOne();
            playerToCheck.updateMovementPossibility();
            if(!playerToCheck.canAccessPoint(cp.owner().king().x(), cp.owner().king().y())){
                willPreventCheck = true; //The piece will take it's owner out of check if it makes this move, so it may do so
            }

            //Undo the commands executed
            undoCommandInternal("MOVE",moveCmd);
            cp.moveMinusOne();
            if(takenPiece != null){
                chessBoard[newX][newY] = takenPiece;
            }
            playerToCheck.updateMovementPossibility();
            
            if(!willPreventCheck){
                return false;
            }
        }
        
        //Ensure that piece specific requirements are met (e.g not moving a rook diagonally, not moving a king into check etc)
        int xMove = newX-cp.x();
        int yMove = newY-cp.y();
        int dX = Math.abs(xMove);
        int dY = Math.abs(yMove);

        switch(cp.piece()){

            case (PIECE_TYPE_ROOK):
                //Check that the movement is only in 1 direction
                if(dX != 0 && dY != 0){
                    return false;
                }
                
                //Check to see if the target friendly piece is a king, for castling (only a rook will be able to check for friendly pieces)
                if((chessBoard[newX][newY] != null) && (chessBoard[newX][newY].owner() == cp.owner())){
                        //No need to check to see if its a king because we check that before doing piece-specific checks
                        int kingCastleDirection = (cp.x()-newX)/Math.abs(cp.x()-newX);
                        ChessPlayer enemyPlayer = (cp.owner() == PLAYER_WHITE) ? PLAYER_BLACK : PLAYER_WHITE;

                        //Ensure that neither the castle nor the king has moved yet
                        if(cp.hasMoved() || chessBoard[newX][newY].hasMoved()){
                            return false;
                            
                        //Ensure that both spots between the castle and the king are open
                        }else if( (chessBoard[newX+kingCastleDirection][newY] != null) || (chessBoard[newX+kingCastleDirection*2][newY] != null) ){
                            return false;
                        
                        }else if( enemyPlayer.canAccessPoint(newX+kingCastleDirection,newY) || enemyPlayer.canAccessPoint(newX+kingCastleDirection*2,newY) ){
                            return false;
                        }
                }
                break;
            
            case (PIECE_TYPE_BISHOP):
                if(dX != dY){
                    return false;
                }
                break;
                
            case (PIECE_TYPE_KNIGHT):
                //Check that it is a valid knight move (2x;1y) or (1x;2y)
                //This could be thought (or pronounced) as:
                //If dX is 2 and dY 1; or if dX and dY is 2; then it is correct, therefore if it is not true, then return false (hence the NOT)
                if(!( ((dX == 2) && (dY == 1)) || ((dX == 1) && (dY == 2)) )){
                    return false;
                }
                break;

            case (PIECE_TYPE_KING):
                if((dX != 1) || (dY != 1) ){
                    return false;
                }
                
                //Check the new location doesnt fall within the range of any enemy pieces and would therefore put him in check
                if(cp.owner() == PLAYER_WHITE){
                    if(PLAYER_BLACK.canAccessPoint(newX,newY)){
                        return false;
                    }
                }else{
                    if(PLAYER_WHITE.canAccessPoint(newX,newY)){
                        return false;
                    }
                }
                break;

            case (PIECE_TYPE_PAWN):
                //Check that it's moving forwards
                if(newY*cp.owner().directionMultiplier() < cp.y()*cp.owner().directionMultiplier()){
                    return false;
                }

                //Check that it is moving forwards by only 1 (or 2 if it's the first move of this pawn)
                if(dY != 1){
                    if(dY == 2){
                        if((cp.y() != 1) && (cp.y() != 6)){
                            return false;
                        }
                    }else{
                        return false;
                    }
                }

                //If it's attacking (IE moving diagonally)
                if(newX != cp.x()){
                    if(chessBoard[newX][newY] == null){
                        
                        if(chessBoard[newX][cp.y()] == null || chessBoard[newX][cp.y()].owner() == cp.owner() || chessBoard[newX][cp.y()].piece() != PIECE_TYPE_PAWN){
                            //Return false if you're moving diagonally and not taking a piece
                            //Also only return false if a piece is not being taken en passant
                            return false;
                        
                        }else if(chessBoard[newX][cp.y()].moveCount() != 1 || (cp.y() != (int)(3.5 + 0.5*cp.owner().directionMultiplier()))){
                            //Ensure that the piece to be taken as just moved 2 blocks at once
                            return false;
                        }

                    }else{
                        //Check that you're not moving more than 1 to either side
                        if((dX != 1) || (dY != 1) ){
                            return false;
                        }
                    }
                
                }else if(chessBoard[newX][newY] != null){
                    //Check that it cant attack forwards
                    return false;
                }
                break;
        }

        

        //Check to see if there is anything blocking the path (excluding knight because it can jump, and pawn/king because they can only move 1 anyways)
        if((cp.piece() != PIECE_TYPE_KNIGHT) && ((cp.piece() != PIECE_TYPE_PAWN) || (cp.piece() == PIECE_TYPE_PAWN && dY == 2)) && (cp.piece() != PIECE_TYPE_KING)){
            //check to see if a piece is blocking
            int tempX = cp.x();
            int tempY = cp.y();
            int xIncr = 0;
            int yIncr = 0;
            if(dX != 0){
                xIncr = xMove/dX;
                tempX += xIncr;  //dX is the absolute value of xMove, so it will always be 1, or -1
            }
            if(dY != 0){
                yIncr = yMove/dY;
                tempY += yIncr;  //dY is the absolute value of yMove, so it will always be 1, or -1
            }
            
            while( (tempX != newX) || (tempY != newY) ){
                if(chessBoard[tempX][tempY] != null){
                    return false;
                }
                
                tempX += xIncr;
                tempY += yIncr;
            }
            
        }

        return true;
    }
    
    private void initBoard(){
        chessBoard = new ChessPiece[8][8];
        playerTurnWhite = true;
        playing = true;
        stalemateMoveCount = 0;

        ChessPlayer own = PLAYER_BLACK;
        for(int y=7;y>=0;y--){

            if(y==0 || y==7){
                //Create strong pieces
                for(int x=0;x<8;x++){
                    switch(x){
                        case 0: case 7:
                            chessBoard[x][y] = own.addPiece(PIECE_TYPE_ROOK,x,y);
                            break;
                        case 1: case 6:
                            chessBoard[x][y] = own.addPiece(PIECE_TYPE_KNIGHT,x,y);
                            break;
                        case 2: case 5:
                            chessBoard[x][y] = own.addPiece(PIECE_TYPE_BISHOP,x,y);
                            break;
                        case 3:
                            chessBoard[x][y] = own.addPiece(PIECE_TYPE_QUEEN,x,y);
                            break;
                        case 4:
                            chessBoard[x][y] = own.addPiece(PIECE_TYPE_KING,x,y);
                            break;
                    }
                }
            } else if(y==1 || y==6){
                //Create pawns
                for(int x=0;x<8;x++){
                    chessBoard[x][y] = own.addPiece(PIECE_TYPE_PAWN,x,y);
                }
            }

            if(y == 6){
                own = PLAYER_WHITE;
            }
        }
    }

    public void addEventListener(ChessListener listener){
        if(!listenerList.contains(listener)){
            listenerList.add(listener);
        }
    }
}
