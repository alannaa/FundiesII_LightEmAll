import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


class ExamplesWorld {

  // Examples of GamePieces:
  GamePiece gp1;
  GamePiece gp2;
  GamePiece gp3;
  GamePiece gp4;
  GamePiece gp5;
  GamePiece gp6;

  // Examples of a board
  ArrayList<ArrayList<GamePiece>> board1;
  ArrayList<GamePiece> row1;
  ArrayList<GamePiece> row2;

  // Initial Game Conditions:
  // (Basically creates an upside-down U shape when solved)
  void initConditions() {
    this.gp1 = new GamePiece(1, 1, true, true, true, true);
    this.gp2 = new GamePiece(2, 1, true, false, true, false,true);
    this.gp3 = new GamePiece(1, 2, true, true, true, true);
    this.gp4 = new GamePiece(2, 2, true, true, true, true);
    this.gp5 = new GamePiece(1, 2, true, true, true, true);
    this.gp6 = new GamePiece(2, 2, true, true, true, true);

    this.board1 = new ArrayList<ArrayList<GamePiece>>();
    this.row1 = new ArrayList<GamePiece>();
    this.row1.add(this.gp1);
    this.row1.add(this.gp2);
    this.row1.add(this.gp3);
    this.row2 = new ArrayList<GamePiece>();
    this.row2.add(this.gp4);
    this.row2.add(this.gp5);
    this.row2.add(this.gp6);
    this.board1.add(row1);
    this.board1.add(row2);
  }

  // Tests for rotateClockwise in GamePiece class
  void testRotateClockwise(Tester t) {
    this.initConditions();
    t.checkExpect(this.gp1.left, false);
    t.checkExpect(this.gp1.right, true);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.bottom, true);
    this.gp1.rotateClockwise();
    t.checkExpect(this.gp1.left, true);
    t.checkExpect(this.gp1.right, false);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.bottom, true);
  }

  // Tests for designatePowerStation in GamePiece class
  void testDesignatePS(Tester t) {
    this.initConditions();
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp2.powerStation, false);
    this.gp1.designatePowerStation();
    t.checkExpect(this.gp1.powerStation, true);
    t.checkExpect(this.gp2.powerStation, false);
    this.gp1.losePowerStation();
    this.gp2.designatePowerStation();
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp2.powerStation, true);
  }

  // Tests for whichNeighbor in GamePiece class
  // ***FOR TESTING ONLY - REMOVE BEFORE SUBMITTING
  void testWhichNeighbor(Tester t) {
    this.initConditions();
    t.checkExpect(this.gp1.whichNeighbor(this.gp2), "right");
    t.checkExpect(this.gp1.whichNeighbor(this.gp3), "bottom");
    t.checkExpect(this.gp1.whichNeighbor(this.gp4), "notNeighbors");
    t.checkExpect(this.gp2.whichNeighbor(this.gp1), "left");
    t.checkExpect(this.gp2.whichNeighbor(this.gp3), "notNeighbors");
    t.checkExpect(this.gp2.whichNeighbor(this.gp4), "bottom");
    t.checkExpect(this.gp3.whichNeighbor(this.gp1), "top");
  }

  // Tests for wireNeighborsWith in GamePiece class
  void testWireNeighborsWith(Tester t) {
    this.initConditions();
    // If called on itself:
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp1), false);
    // If not neighbors:
    GamePiece aNewGP = new GamePiece(3, 3, false, true, false, true);
    t.checkExpect(this.gp1.wireNeighborsWith(aNewGP), false);
    // And testing that the game works as expected:
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp2), true);
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp3), true);
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp4), false);
    t.checkExpect(this.gp2.wireNeighborsWith(this.gp1), true);
    t.checkExpect(this.gp2.wireNeighborsWith(this.gp3), false);
    t.checkExpect(this.gp2.wireNeighborsWith(this.gp4), true);
    t.checkExpect(this.gp3.wireNeighborsWith(this.gp4), false);
  }

  // Tests for light in GamePiece class
  void testLight(Tester t) {
    this.gp1.designatePowerStation();
  }

  // Test for big bang
  void testBigBang(Tester t) {
    this.initConditions();
    LightEmAll l1 = new LightEmAll(3, 2, this.board1);
    l1.bigBang(1000, 1000,.1);
  }

}