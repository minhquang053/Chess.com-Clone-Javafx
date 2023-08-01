package com.yelaco.common;

import com.yelaco.piece.*;

import java.util.ArrayList;

public class Game {
    private Player[] players;
    private Board board;
    private Player currentTurn;
    private GameStatus status;
    private ArrayList<Move> movesPlayed;

    private Spot[] twoKings;

    public Game() {
        players = new Player[2];
        board = new Board();
        movesPlayed = new ArrayList<>();

        twoKings = new Spot[2];
        try {
            twoKings[0] = (Spot) board.getBox(4, 0);
            twoKings[1] = (Spot) board.getBox(4, 7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Player p1, Player p2) {
        players[0] = p1;
        players[1] = p2;

        board.setBoard();

        if (p1.isWhiteSide()) {
            this.currentTurn = p1;
        } else {
            this.currentTurn = p2;
        }

        setStatus(GameStatus.ACTIVE);

        movesPlayed.clear();
    }

    public void updateTwokings() {
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece instanceof King) {
                        if (piece.isWhite()) {
                            twoKings[0] = box;
                        } else {
                            twoKings[1] = box;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOver() {
        return this.getStatus() != GameStatus.ACTIVE;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameStatus getStatus() {
        return this.status;
    }

    public ArrayList<String> getAvailiableMove(int mX, int mY) {
        ArrayList<String> availMove = new ArrayList<>();
        try {
            var startSpot = board.getBox(mX, mY);
            var piece = startSpot.getPiece();
            if (piece == null) {
                return availMove;
            }
            Piece oldPiece = null;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var endSpot = board.getBox(i, j);
                    if (piece.canMove(board, startSpot, endSpot)) {
                        oldPiece = endSpot.getPiece();
                        startSpot.setPiece(null);
                        endSpot.setPiece(piece);
                        if (!kingInDanger(piece.isWhite())) {
                            availMove.add(String.format("%d%d", i, j));
                        }
                        startSpot.setPiece(piece);
                        endSpot.setPiece(oldPiece);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availMove;
    }

    public Player getCurrentTurn() {
        if (isStalemate()) {
            setStatus(GameStatus.STALEMATE);
        }
        if (kingInDanger(currentTurn.isWhiteSide) && kingInCheckmate(currentTurn.isWhiteSide)) {
            if (currentTurn.isWhiteSide) {
                System.out.println("- White king is checkmated -");
                setStatus(GameStatus.BLACK_WIN);
            } else {
                System.out.println("- Black king is Checkmated -");
                setStatus(GameStatus.WHITE_WIN);
            }
        }
        return this.currentTurn;
    }

    public ArrayList<Move> getMovesPlayed() {
        return movesPlayed;
    }

    private boolean isStalemate() {
        ArrayList<String> pieceMoves = null;
        Piece piece = null;
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    piece = board.getBox(i, j).getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() != currentTurn.isWhiteSide()) {
                        continue;
                    }
                    pieceMoves = getAvailiableMove(i, j);
                    if (!pieceMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    private boolean kingInDanger(boolean isWhite) {
        updateTwokings();
        Spot spotKing;
        if (isWhite) {
            spotKing = twoKings[0];
        } else {
            spotKing = twoKings[1];
        }
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() == isWhite) {
                        continue;
                    }
                    if (piece.canMove(board, box, spotKing)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private boolean kingInCheckmate(boolean isWhite) {
        ArrayList<Spot> threatPieceSpot = new ArrayList<>();
        Spot spotKing;
        if (isWhite) {
            spotKing = twoKings[0];
            System.out.println("* White king in Check *");
        } else {
            spotKing = twoKings[1];
            System.out.println("* Black king in Check *");
        }
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() == isWhite) {
                        continue;
                    }
                    if (piece.canMove(board, box, spotKing)) {
                        threatPieceSpot.add(box);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // if there are multiple threats, the King must run (can't block attack)
        // we check if the King can run or not with any number of threats
        var king = spotKing.getPiece();
        int xKing = spotKing.getX();
        int yKing = spotKing.getY();
        try {
            for (int i = xKing - 1; i <= xKing + 1; i++) {
                for (int j = yKing - 1; j <= yKing + 1; j++) {
                    if (i < 0 || i > 7 || j < 0 || j > 7) {
                        continue;
                    }
                    if (king.canMove(board, spotKing, board.getBox(i, j))) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (threatPieceSpot.size() > 1) {
            return true;
        }

        // if there is single threat and the King can't run, the threatening piece must be stopped
        // first by eliminate the threatening piece
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() != isWhite) {
                        continue;
                    }
                    if (piece.canMove(board, box, threatPieceSpot.get(0))) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // second by block the attack
        // with knight or pawn checking, there aren't any spots in between to block the attack
        var threatSpot = threatPieceSpot.get(0);
        var threatPiece = threatSpot.getPiece();
        if (threatPiece instanceof Knight || threatPiece instanceof Pawn) {
            return true;
        }

        // get pieces in between the threatPiece and the King
        ArrayList<Spot> inBetweens = new ArrayList<>();
        int i = threatSpot.getX();
        int j = threatSpot.getY();
        int bex = xKing;
        int bey = yKing;
        if (i < bex) {
            bex--;
        } else if (i > bex){
            bex++;
        }
        if (j < bey) {
            bey--;
        } else if (j > bey){
            bey++;
        }

        while (i != bex || j != bey) {
            if (i < bex) {
                i++;
            } else if (i > bex){
                i--;
            }
            if (j < bey) {
                j++;
            } else if (j > bey){
                j--;
            }

            try {
                inBetweens.add(board.getBox(i, j));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        // Check chockablock
        System.out.println(inBetweens.size());
        try {
            for (Spot theSpot : inBetweens) {
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        var box = board.getBox(x, y);
                        var piece = box.getPiece();
                        if (piece == null) {
                            continue;
                        }
                        if (piece.isWhite() != king.isWhite()) {
                            continue;
                        }
                        if (piece.canMove(board, box, theSpot)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // if none of these work, your king dead as hell
        return true;
    }

    public MoveStatus playerMove(Player player, int startX, int startY, int endX, int endY) throws Exception {
        Spot startBox = board.getBox(startX, startY);
        Spot endBox = board.getBox(endX, endY);
        Move move = new Move(player, startBox, endBox);
        return this.makeMove(move, player);
    }

    private MoveStatus makeMove(Move move, Player player) {
        Piece srcPiece = move.getStart().getPiece();
        if (srcPiece == null) {
            return MoveStatus.NULL_PIECE;
        }
        Piece dstPiece = move.getEnd().getPiece();

        System.out.println("> " + srcPiece.getClass().getSimpleName() + " moved");

        // check valid player
        if ( player != currentTurn || (srcPiece.isWhite() != player.isWhiteSide()) ) {
            System.out.print("Wrong turn -> ");
            return MoveStatus.WRONG_TURN;
        }

        // check valid move
        if (!srcPiece.canMove(board, move.getStart(), move.getEnd())) {
            System.out.print("Can't move here -> ");
            if (dstPiece != null && srcPiece.isWhite() == dstPiece.isWhite()) {
                return MoveStatus.SAME_SIDE;
            }
            return MoveStatus.CANT_MOVE;
        }

        move.setPieceMoved(srcPiece);

        if (dstPiece != null) {
            move.setPieceKilled(dstPiece);
        }

        if (srcPiece instanceof King) {
            if (!((King) srcPiece).isCastlingDone()) {
                ((King) srcPiece).setCastlingDone(true);
            }
            if (((King) srcPiece).isCastlingMove(move.getStart(), move.getEnd())) {
                move.setCastlingMove(true);
            }
        }
        //

        // Pawn promotion and init move
        if (srcPiece instanceof Pawn) {
            if (!((Pawn) srcPiece).getInitMoved()) {
                if (Math.abs(move.getEnd().getY() - move.getStart().getY()) == 2) {
                    ((Pawn) srcPiece).setCanEnpassant(true);
                }
                ((Pawn) srcPiece).setInitMoved(true);
            }

            if (move.getEnd().getY() == 7) {
                srcPiece = new Queen(true);
                move.setPromotion(true);
            }
            if (move.getEnd().getY() == 0) {
                srcPiece = new Queen(false);
                move.setPromotion(true);
            }

            // En passant
            if (move.getEnd().getX() != move.getStart().getX() && move.getEnd().getPiece() == null) {
                try {
                    var spotKilled = board.getBox(move.getEnd().getX(), move.getStart().getY());
                    spotKilled.setPiece(null);
                    move.setEnpassant(true);
                    move.setSpotKilled(spotKilled);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Castling
        if (move.isCastlingMove()) {
            System.out.println("King castled");
            try {
                move.getEnd().setPiece(null);
                move.getStart().setPiece(null);
                if (move.getEnd().getX() == 0) {
                    board.getBox(2, move.getEnd().getY()).setPiece(srcPiece);
                    board.getBox(3, move.getEnd().getY()).setPiece(dstPiece);
                    move.setRookCastled(board.getBox(0, move.getEnd().getY()), board.getBox(3, move.getEnd().getY()));
                    move.setEnd(board.getBox(2, move.getEnd().getY()));
                } else if (move.getEnd().getX() == 7) {
                    board.getBox(6, move.getEnd().getY()).setPiece(srcPiece);
                    board.getBox(5, move.getEnd().getY()).setPiece(dstPiece);
                    move.setRookCastled(board.getBox(7, move.getEnd().getY()), board.getBox(5, move.getEnd().getY()));
                    move.setEnd(board.getBox(6, move.getEnd().getY()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return MoveStatus.CANT_MOVE;
            }
        } else {
            move.getEnd().setPiece(srcPiece);
            move.getStart().setPiece(null);
            if (kingInDanger(srcPiece.isWhite())) {
                move.getStart().setPiece(srcPiece);
                move.getEnd().setPiece(dstPiece);
                System.out.print("King is checked --> ");
                return MoveStatus.CANT_MOVE;
            }
        }

        // Turn off en passant for the last pawn move
        if (!movesPlayed.isEmpty()) {
            Piece lastPieceMove = movesPlayed.get(movesPlayed.size() - 1).getPieceMoved();
            if (lastPieceMove instanceof Pawn && ((Pawn) lastPieceMove).getCanEnpassant()) {
                ((Pawn) lastPieceMove).setCanEnpassant(false);
            }
        }

        if (kingInDanger(!srcPiece.isWhite())) {
            move.setCheckMove(true);
        }

        movesPlayed.add(move);

        // Game winning
        if (dstPiece instanceof King) {
            if (player.isWhiteSide()) {
                this.setStatus(GameStatus.WHITE_WIN);
            }
            else {
                this.setStatus(GameStatus.BLACK_WIN);
            }
        }

        if (this.currentTurn == players[0]) {
            this.currentTurn = players[1];
        } else {
            this.currentTurn = players[0];
        }

        return MoveStatus.SUCCESS;
    }
}