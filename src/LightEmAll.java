import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;



// CODE FLOW:
// - GamePiece class: Contains all information about the GamePiece representation
// - LightEmAll class: Contains all game logic, layout, controls, etc
// - Edge class
// - ArrayUtils
// - GamePieceHex class: Contains all information about a hexagonal representation of a GamePiece
// - LightEmAllHex class: Contains all information about a hexagonal game logic, layout, etc
// - Example class: Contains tests for square game first, then hexagon game

// EXTRA CREDIT ATTEMPTS:
// -gradient coloring (yellow -> orange -> pink)
// -Allowing the player to start a new puzzle without restarting 
// the program: Press "r" at any time to restart the puzzle.
// -Keeping score of the game in two ways: letting the user know
// how many squares remain to be lit until they've won, and also
// how many squares they have lit so far.
// -Time: A time tracker appears in the canvas and pauses when the game 
// has been won. It restarts when a new game is restarted.
// -Construct wiring with a bias in a particular direction
// /a preference for horizontal or vertical wires:
// press "h" for a horizontal bias.
// press "v" for a vertical bias.
// Something that isn't on the extra credit list but works too:
// press "g" at any time to give up (will connect the wires but the user
// should still move the power station to 'win')
// -Press 'x' to open a new canvas with our attempt for the hexagon grid,
// which is entirely functional, except for onClick functionality...
///////////////////////////////////////////////////////////////////////

// When dealing with different sides of a GamePiece, for the sake of uniformity,
// all methods in this program process sides in the following order: 
// left; right; top; bottom;
// Also: We decided from the start to change the orientation of the grid from column-major order 
// to row-major order to make our code more readable and understandable. We believe that row-major
// order is a better reflection of the actual visualized grid, where columns mimic x-coordinates
// and rows mimic y-coordinates, with the origin at the top-left corner. Our coded representation
// of the grid is read from left to right, top to bottom, as is the English language.

class GamePiece {
  // In logical coordinates, with origin at the top-left
  int col;
  int row;
  // whether this GamePiece is connected to the adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // A list of GamePieces of this GamePiece's neighbors
  ArrayList<GamePiece> neighbors;
  // whether the power station is on this piece
  boolean powerStation;
  // Whether the GamePiece is connected to power
  boolean lit;
  // The distance of the GamePiece from the power station.
  // Two GamePieces may have the same powerVals if they both span
  // out from the power station in different directions but are the same
  // distance from it
  int powerVal;
  // The distance field is used only once in the process of finding the diameter,
  // and can represent a more general sense of 'distance' between two arbitrary
  // GamePieces rather than specifically the distance from the power station.
  int distance;
  // A random number that represents the number of times a GamePiece is rotated when a
  // game is initialized
  int orientation;
  
  // Constructor for a general, non-power station GamePiece.
  // To begin, all fields are set to false, and given wires
  // in the connectTheWires() method in the LightEmAll class by delegating the
  // wire assignment to this class
  GamePiece(int col, int row) {
    this.col = col;
    this.row = row;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.neighbors = new ArrayList<GamePiece>();
    this.powerStation = false;
    this.lit = false;
    this.powerVal = 0;
    this.distance = 0;
    Random r = new Random();
    this.orientation = r.nextInt(4);
  }
 
  // Constructor for the power station GamePiece, used to initialize the power
  // station in the makeBoard method in the LightEmAll class
  GamePiece(int col, int row, boolean powerStation) {
    this.col = col;
    this.row = row;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.neighbors = new ArrayList<GamePiece>();
    this.powerStation = powerStation;
    this.lit = true;
    // The power station starts the count of distance at 0, its neighbors have a powerVal of 1
    this.powerVal = 0;
    Random r = new Random();
    this.orientation = r.nextInt(4);
  }

  // Constructor for testing different variations of orientation in examples class:
  GamePiece(int col, int row, boolean left, boolean right, boolean top, boolean bottom) {
    this.col = col;
    this.row = row;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.neighbors = new ArrayList<GamePiece>();
    this.powerStation = false;
    this.lit = false;
    this.powerVal = 0;
    Random r = new Random();
    this.orientation = r.nextInt(4);
  }
  
  // EFFECT: Modifies this neighbors field to add a new neighbor, 
  // and adds this GamePiece to the neighbor's neighbors list,
  // only if the two GamePiece's are not already deemed neighbors
  void addNeighbor(GamePiece gp) {
    if (!this.neighbors.contains(gp)) {
      this.neighbors.add(gp);
      gp.neighbors.add(this);
    }
  }
  
  // After the min spanning tree has been generated, this method determines which neighbors 
  // each GamePiece should be wire-connected to.
  // EFFECT: Modifies the left/right/top/bottom boolean fields of this GamePiece and of its
  // neighbor GamePiece if they ought to be connected by wire
  void connectIfNeighborOf(GamePiece to) {
    // If this is the left neighbor of to
    if (this.row == to.row && this.col == to.col - 1) {
      this.right = true;
      to.left = true;
    }
    // If this is the right neighbor of to
    if (this.row == to.row && this.col == to.col + 1) {
      this.left = true;
      to.right = true;
    }
    // If this is the top neighbor of to
    if (this.col == to.col && this.row == to.row - 1) {
      this.bottom = true;
      to.top = true;
    }
    // If this is the bottom neighbor of to
    if (this.col == to.col && this.row == to.row + 1) {
      this.top = true;
      to.bottom = true;
    }
  }
    
  // EFFECT: rotates the GamePiece a random number of times,
  // which is determined in the GamePiece constructor 
  // This method is used in the initiation of the game to randomize the rotation
  // of the board's GamePieces to start off
  void rotateRandom() {
    for (int i = this.orientation; i >= 0; i = i - 1) {
      this.rotateClockwise();
    }
  }
 
  // Determines if two GamePieces are connected by wires (not only by location)
  // First, checks which of the side neighbors the given piece is, in relation to this one
  // Then, returns whether the given GamePiece is also connected back to this one by wire
  boolean wireNeighborsWith(GamePiece that) {
    // If that is the LEFT neighbor:
    if (that.col == this.col - 1 && that.row == this.row) {
      return this.left && that.right;
    }
    // If that is the RIGHT neighbor:
    if (that.col == this.col + 1 && that.row == this.row) {
      return this.right && that.left;
    }
    // If that is the TOP neighbor:
    if (that.col == this.col && that.row == this.row - 1) {
      return this.top && that.bottom;
    }
    // If that is the BOTTOM neighbor:
    if (that.col == this.col && that.row == this.row + 1) {
      return this.bottom && that.top;
    }
    else {
      return false;
    }
  }
  
  // EFFECT: Uses breadth-first technique to modify the lit fields of the GamePieces
  // This method is only ever called on the PowerStation in the refresh method
  // in the LightEmAll class, and then spans out across the game board passing light across 
  // wire-connected pieces.
  // A convenient side effect of this is visiting specific GamePieces in order of distance
  // from the power station itself, so this method also has the effect of modifying GamePiece's
  // powerVal fields, which is a number representing its distance from the power station.
  // The GamePiece is only lit if it's powerVal is within the radius of light extension.
  void lightEmUp(int radius) {
    ArrayList<GamePiece> seen = new ArrayList<GamePiece>();
    ArrayList<GamePiece> workList = new ArrayList<GamePiece>();
    workList.add(this);
    this.powerVal = 0;
    while (workList.size() > 0) {
      GamePiece curGP = workList.remove(0); // removes and returns
      // only lights the GamePiece if it falls within the radius of the power station,
      // which is a parameter given by the LightEmAll class to this method
      if (curGP.powerVal <= radius) {
        curGP.lit = true;
      }
      for (int i = 0; i < curGP.neighbors.size(); i++) {
        GamePiece eachNeighbor = curGP.neighbors.get(i);
        if (curGP.wireNeighborsWith(eachNeighbor) && !seen.contains(eachNeighbor)) {
          workList.add(eachNeighbor);
          // Each GamePiece's powerVal is one more than that of its neighbor that is
          // closest
          // to the power station
          eachNeighbor.powerVal = curGP.powerVal + 1;
        }
      }
      seen.add(curGP);
    }
  }
  
  // EFFECT: Causes a GamePiece to lose it's light, which also 
  // resets its powerVal to zero by nature. 
  // This method is used in the refresh method in the LightEmAll class and applied to every
  // GP on the board (except the power station) in order to then re-light only the appropriate 
  // GPs in the lightEmUp method above
  void loseLight() {
    this.powerVal = 0;
    this.lit = false;
  }
  
  // Returns the GamePiece that is farthest from the given GamePiece,
  // using breadth-first search to process all GamePieces in breadth-first
  // order, then returns the last item on the seen list.
  // This method is a helper method to the following method: getDiameter();
  GamePiece farthestFrom() {
    ArrayList<GamePiece> seen = new ArrayList<GamePiece>();
    ArrayList<GamePiece> workList = new ArrayList<GamePiece>();
    workList.add(this);
    this.distance = 0;
    while (workList.size() > 0) {
      GamePiece curGP = workList.remove(0); // removes and returns
      for (int i = 0; i < curGP.neighbors.size(); i++) {
        GamePiece eachNeighbor = curGP.neighbors.get(i);
        if (curGP.wireNeighborsWith(eachNeighbor) && !seen.contains(eachNeighbor)) {
          workList.add(eachNeighbor);
          eachNeighbor.distance = curGP.distance + 1;
        }
      }
      seen.add(curGP);
    }
    GamePiece farthestFromThis = seen.remove(seen.size() - 1);
    return farthestFromThis;
  }
  
  // Returns an integer that represents the diameter of the extension of power
  // that the power station can give off. This is calculated by locating the farthest
  // GamePiece from the power station, then locating the farthest GamePiece from that 
  // GamePiece. All the while, the distance of movement across the board is kept track of
  // in the distance field of each GamePiece. Then, the diameter is the distance apart from
  // the two farthest GamePieces.
  // This method is called on the power station in the LightEmAll class.
  int getDiameter() {
    GamePiece farthestFromPS = this.farthestFrom();
    GamePiece farthestFromThat = farthestFromPS.farthestFrom();
    int diameter = farthestFromThat.distance;
    return diameter;
  }
 
  // Draws an individual GamePiece, depending on its properties:
  // if the GP is not lit, it is grey;
  // if it is and it is close to the power station, it is yellow;
  // the further away it gets from the power station, it turns orange;
  // the GamePieces the farthest away are pink;
  // GamePieces that fall outside the radius are not lit.
  // The length of an individual piece will depend on the properties of the full canvas,
  // therefore, the length of each piece will be calculated in the LightEmAll
  // class and given here
  WorldImage draw(int gpWidth, int gpHeight, int radius) {
    WorldImage gPiece = new OverlayImage(
        new RectangleImage(gpWidth - 2, gpHeight - 2, OutlineMode.SOLID, Color.DARK_GRAY), 
        new RectangleImage(gpWidth, gpHeight, OutlineMode.SOLID, Color.BLACK));
    // Each line's end point will always be a position on the outer border of the
    // gPiece image, and will land in the center of the length of the side of the gPiece image.
    int midLengthTopBot = gpWidth / 2;
    int midLengthSide = gpHeight / 2;
    double offsetX = gpWidth / 4;
    double offsetY = gpHeight / 4;
    // EFFECT: The variable clr contains the color for which the lines on the GamePiece should be
    // based on whether or not the GamePiece is connected to power 
    Color clr = Color.LIGHT_GRAY;
    if (this.lit) {
      if (this.powerVal <= radius) {
        clr = Color.YELLOW;
      }
      if (this.powerVal >= radius - 3) {
        clr = Color.ORANGE;
      }
      if (this.powerVal >= radius - 1) {
        clr = Color.PINK;
      }
    }
    // One horizontal and one vertical line to overlay at different positions 
    // over the plank gPiece image above
    WorldImage horizontal = new LineImage(new Posn(midLengthTopBot, 0), clr);
    WorldImage vertical = new LineImage(new Posn(0, midLengthSide), clr);
    if (this.left) {
      gPiece = new OverlayOffsetImage(horizontal, offsetX, 0, gPiece);
    }
    if (this.right) {
      gPiece = new OverlayOffsetImage(horizontal, -offsetX, 0, gPiece);
    }
    if (this.top) {
      gPiece = new OverlayOffsetImage(vertical, 0, offsetY, gPiece);
    }
    if (this.bottom) {
      gPiece = new OverlayOffsetImage(vertical, 0, -offsetY, gPiece);
    }
    if (this.powerStation) {
      WorldImage pwr = new OverlayImage(
          new CircleImage(midLengthSide / 4, OutlineMode.SOLID, Color.YELLOW),
          new StarImage(midLengthSide - 5, 7, OutlineMode.SOLID, Color.RED));
      gPiece = new OverlayImage(pwr, gPiece);
    }
    return gPiece;
  }
  
  // EFFECT: adjusts this GamePiece's properties to reflect a rotation in the clockwise direction.
  // When a GamePiece is clicked, the LightEmAll class delegates the rotation action to here
  void rotateClockwise() {
    boolean storeleft = this.left;
    boolean storetop = this.top;
    boolean storeright = this.right;
    boolean storebottom = this.bottom;
    this.left = storebottom;
    this.top = storeleft;
    this.right = storetop;
    this.bottom = storeright;
  }
  
  // EFFECT: modifies the power station field of two GamePieces,
  // causing one to lose its power station status and the other to
  // gain it, and also becoming lit by nature. This one method to
  // transfer the power station ensures that there can only ever
  // be one designated power station on the game grid, and it can
  // only ever pass it off the one other GamePiece
  void givePowerStationTo(GamePiece newPS) {
    this.powerStation = false;
    newPS.powerStation = true;
    newPS.lit = true;
    newPS.powerVal = 0;
  }
}

///////REPRESENTS THE LIGHTEMALL CLASS WITH GAME LOGIC//////
class LightEmAll extends World {
  // the width and height of the board
  // in terms of the number of GamePieces that go across and down the game board
  int width;
  int height;
  int powerCol;
  int powerRow;
  // a list of rows of GamePieces
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes in game
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree 
  ArrayList<Edge<GamePiece>> mst;
  // the current location of the power station,
  // as well as its effective radius
  int radius;
  int time;
  boolean gameWon;
  int whitespace = 300;
  int bbWidth = 700;
  int bbHeight = 700;
  
  // Constructor takes the number of columns and rows respectively, and generates a playable game
  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    this.powerCol = 0;
    this.powerRow = 0;
    this.board = makeBoard();
    //After constructing the board, populates the neighbors of each GamePiece:
    populateNeighbors();
    this.nodes = getNodes();
    //The int parameters given to getEdges(int int) represent the option to define a 
    //bias in either horizontal or vertical direction. Here, they are equal, representing
    //no bias.
    //The createMST takes in an ArrayList<Edge> as a parameter in order to be abstract
    //enough to create a min spanning tree given any un ordered list of Edge.
    this.mst = this.createMST(this.getEdges(this.nodes.size(), this.nodes.size()));
    //Connects the edges in the min spanning tree by wires:
    this.connectTheWires();
    //Populates the GamePieces with power values to be used in calculateRadius:
    this.refresh();
    this.radius = calculateRadius();
    //Randomly rotates all GamePieces:
    randomRotateAll();
    //Prepares the board for game play:
    this.refresh();
    this.time = 0;
    this.gameWon = false;
    //The width of the canvas has 200 pixels of whitespace on the right side, where game stats
    //will be written to fulfill extra credit portions of the game:
    bigBang(bbWidth + whitespace, bbHeight, 1);
  }
  
  // Constructor for testing that does not open a canvas when called, and allows you to test
  // more properties than the previous constructor:
  LightEmAll(int width, int height, int powerCol, int powerRow) {
    this.width = width;
    this.height = height;
    if (powerCol >= width || powerCol < 0) {
      throw new IllegalArgumentException("Column does not exist on this board.");
    }
    else {
      this.powerCol = powerCol;
    }
    if (powerRow >= height || powerRow < 0) {
      throw new IllegalArgumentException("Row does not exist on this board.");
    }
    else {
      this.powerRow = powerRow;
    }
    this.board = makeBoard();
    this.nodes = getNodes();
    this.mst = this.createMST(this.getEdges(this.nodes.size(), this.nodes.size()));
    this.radius = calculateRadius();
    this.time = 0;
    this.gameWon = false;
  }
  
  ///////Constructs the game board and other initial properties://///////
  // Creates a 2D representation of the LightEmAll game board in row-major orientation
  // Allows coder to access a specific GamePiece, call this.board.get(i).get(j)
  ArrayList<ArrayList<GamePiece>> makeBoard() {
    ArrayList<ArrayList<GamePiece>> allRows = new ArrayList<ArrayList<GamePiece>>();
    for (int i = 0; i < height; i++) {
      ArrayList<GamePiece> aRow = new ArrayList<GamePiece>();
      for (int j = 0; j < width; j++) {
        //Add the power station:
        if (j == this.powerCol && i == this.powerRow) {
          aRow.add(new GamePiece(j, i, true));
        }
        //Add normal GamePieces, with all wire booleans set to false:
        else {
          aRow.add(new GamePiece(j, i));
        }
      }
      allRows.add(aRow);
    }
    return allRows;
  }
  
  // EFFECT: After constructing the board, this method populates the neighbors of each GamePiece
  // by modifying the curGP's neighbor list 
  // If the curGP is along the side of the grid, it will have three neighbors
  // If the curGP is a corner piece, it will have two neighbors
  public void populateNeighbors() {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        int jOfLeftn = j - 1; // the j index of the left neighbor
        if (jOfLeftn >= 0) { // so long as curGP's left neighbor exists...
          GamePiece leftN = this.board.get(i).get(jOfLeftn);
          curGP.addNeighbor(leftN); // add the neighbor
        }
        int jOfRightn = j + 1; // j of right...
        if (jOfRightn <= width - 1) {
          GamePiece rightN = this.board.get(i).get(jOfRightn);
          curGP.addNeighbor(rightN);
        }
        int iOfTopn = i - 1; // i of top...
        if (iOfTopn >= 0) {
          GamePiece topN = this.board.get(iOfTopn).get(j);
          curGP.addNeighbor(topN);
        }
        int iOfBotn = i + 1; // i of bottom...
        if (iOfBotn <= height - 1) {
          GamePiece botN = this.board.get(iOfBotn).get(j);
          curGP.addNeighbor(botN);
        }
      }
    }
  }
  
  // Generates an ArrayList of all nodes in the game
  // by iterating through the board, taking one GamePiece at a time, and 
  // adding it to a new flattened list
  ArrayList<GamePiece> getNodes() {
    ArrayList<GamePiece> nodeList = new ArrayList<GamePiece>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        nodeList.add(curGP);
      }
    }
    return nodeList;
  }
  
  ///////////////KRUSKALS ALGORITHM//////////////////
  // A series of helper methods followed by a method called createMST
  // that brings together all of the helper methods:
  
  // Generates an ArrayList of all weighted edges possible to connect the board
  // by locating the curGP's bottom and right neighbors only (because if we generated edges with 
  // all neighbors, there would be repeat edges) then for each neighbor, if it exists, add a new 
  // Edge to the list of Edges with a randomized weight, which is randomized in the constructor of 
  // the Edge class. 
  // --When the game board has no bias, he maxWeight variables are equal, arbitrarily calculated, 
  // and is just a simple way to generate a max for the random.nextInt() used in the Edge class.
  // --When the game board has a vertical bias, the maximum weight used in the generation of 
  // horizontal is much, much higher than that of the max weight a vertical edge may have. 
  // Therefore, the vertical edges will inevitably come first in the ordering of edges from 
  // smallest to largest, and therefore appear more frequently on the final game, because they 
  // are processed first and have less likelihood to create a cycle then.
  // (Press "v" at any time to start a vertically bias game)
  // --The same but opposite logic applies to a horizontally bias game.
  // (Press "h" at any time to start a horizontally bias game)
  ArrayList<Edge<GamePiece>> getEdges(int verticalBias, int horizontalBias) {
    ArrayList<Edge<GamePiece>> edgeList = new ArrayList<Edge<GamePiece>>();
    int maxWeightHorizontal = horizontalBias;
    int maxWeightVertival = verticalBias;
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        int jOfRightn = j + 1; // the j index of the right neighbor
        if (jOfRightn <= width - 1) { // so long as curGP's right neighbor exists...
          GamePiece rightN = this.board.get(i).get(jOfRightn);
          edgeList.add(new Edge<GamePiece>(curGP, rightN, maxWeightHorizontal));
        }
        int iOfBotn = i + 1; // i of bottom...
        if (iOfBotn <= height - 1) {
          GamePiece botN = this.board.get(iOfBotn).get(j);
          edgeList.add(new Edge<GamePiece>(curGP, botN, maxWeightVertival));
        }
      }
    }
    return edgeList;
  }
 
  // Creates a hashmap of representatives to be used in Krushkal's algorithm
  // Uses unique integers pulled from the GamePiece's unique index in the ArrayList
  // of total game board nodes to be represented in the hashmap. This way, when we are
  // comparing representatives in the createMST method, it is better to compare equality 
  // of integers, rather than compare equality of GamePieces, etc.
  HashMap<Integer, Integer> makeHashMapOfReps() {
    HashMap<Integer, Integer> reps = new HashMap<Integer, Integer>();
    for (int i = 0; i < this.nodes.size(); i++) {
      reps.put(i, i);
    }
    return reps;
  }

  // Searches through the reps HashMap and finds the Key that has the given GamePiece
  // representative Integer as a Value. Returns the Key:
  int find(HashMap<Integer, Integer> reps, GamePiece gp) {
    int find = 0;
    for (int i = 0; i < this.nodes.size(); i++) {
      GamePiece node = this.nodes.get(i);
      if (node.row == gp.row && node.col == gp.col) {
        find = i;
      }
    }
    while (find != reps.get(find)) {
      find = reps.get(find);
    }
    return find;
  }
  
  // Performs Kruskal's algorithm to determine the minimum spanning tree of the solved game:
  // The edges parameter is useful in case you want to generate a board with a vertical or 
  // horizontal bias, you can call getEdges(int verticalBias, int horizontalBias) with varying 
  // values.
  ArrayList<Edge<GamePiece>> createMST(ArrayList<Edge<GamePiece>> edges) {
    //Initialize the hashmap:
    HashMap<Integer, Integer> reps = this.makeHashMapOfReps();
    //Initialize the workList, which is edges sorted from smallest weight to largest weight:
    ArrayUtils au = new ArrayUtils();
    ArrayList<Edge<GamePiece>> edgeListSmToLg = au.heapsort(edges, new CompareWeight());
    //Initialize the min spanning tree:
    ArrayList<Edge<GamePiece>> mst = new ArrayList<Edge<GamePiece>>();
    while (edgeListSmToLg.size() > 0) {
      Edge<GamePiece> curEdge = edgeListSmToLg.remove(0); //removes and returns
      int x = this.find(reps, curEdge.fromNode);
      int y = this.find(reps, curEdge.toNode);
      //If this edge does not create a cycle in the mst:
      if (x != y) {
        //add it to the tree:
        mst.add(curEdge);
        //and union the trees:
        reps.put(x, y); //union
      }
    }
    return mst;
  }
  ////////////End Kruskal's Algorithm////////////////////
  
  // Methods that finish up the initiation of the game board:
  
  // Calculates the finite radius of effectiveness that is given off by 
  // the power station by locating the power station, using breadth-first search
  // to calculate the longest distance from the power station to an end node when
  // the puzzle is solved, then calculating the distance between that GamePiece and its
  // furthest GamePiece (diameter).
  // then calculates the radius using that diameter.
  // This method is called once at the initiation of the game, but before the pieces
  // are randomly rotated (so, as a solved puzzle)
  int calculateRadius() {
    GamePiece powerstation = this.board.get(this.powerRow).get(this.powerCol);
    int diameter = powerstation.getDiameter();
    int rad = (diameter / 2) + 1;
    return rad;
  }
  
  // Traverses through the min spanning tree list of edges that represent the solved
  // game and 'gives wires' to each GamePiece according to their connected neighbors.
  // EFFECT: delegates to the GamePiece class to modify the left/right/top/bottom
  // boolean fields of the GamePieces that need to be connected
  void connectTheWires() {
    for (int i = 0; i < this.mst.size(); i++) {
      Edge<GamePiece> e = this.mst.get(i);
      GamePiece from = e.fromNode;
      GamePiece to = e.toNode;
      to.connectIfNeighborOf(from);
    }
  }
  
  // EFFECT: traverses through the total list of nodes of GamePieces and rotates
  // them a random number of times to finally prepare the board for game play:
  void randomRotateAll() {
    for (int i = 0; i < this.nodes.size(); i++) {
      GamePiece curGP = this.nodes.get(i);
      curGP.rotateRandom();
    }
  }
  //////////////////////////////////////////////////
  
  //////////////////Draw controls/////////////////////
  // Draws the LightEmAll game as a grid of GamePieces 
  // the gpWidth is the width of big bang (minus the whitespace where game stats are kept; 
  // this is done in bigbang above) divided by the number of columns
  // the gpHeight is the height of big bang divided by the number of rows
  public WorldScene makeScene() {
    int gpWidth = bbWidth / width;
    int gpHeight = bbHeight / height;
    WorldScene w = new WorldScene(bbWidth, bbHeight);
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        w.placeImageXY(curGP.draw(gpWidth, gpHeight, this.radius), 
            (j * gpWidth) + (gpWidth / 2), (i * gpHeight) + (gpHeight / 2));
      }
    }
    w.placeImageXY(this.writeGameStats(), bbWidth + (whitespace / 2), bbHeight / 2);
    return w;
  }
  
  // Produces a rectangular world image to be placed on the right side of the canvas and 
  // will contain text that keeps track of the game status throughout the game
  WorldImage writeGameStats() {
    //Initialize the rectangle where the info will be placed:
    WorldImage bckgr = new RectangleImage(whitespace, bbHeight, OutlineMode.OUTLINE,
        Color.BLACK);
    //Creates the Title & Instructions:
    WorldImage pwr = new OverlayImage(
        new CircleImage(8, OutlineMode.SOLID, Color.YELLOW),
        new StarImage(15, 7, OutlineMode.SOLID, Color.RED));
    WorldImage title = new TextImage("LIGHT 'EM ALL", 20, FontStyle.ITALIC, Color.BLACK);
    WorldImage titleImage = new AboveImage(
        new BesideImage(pwr, title, pwr),
        new TextImage("To Win: Connect all the wires and move", 14, FontStyle.ITALIC, Color.BLACK),
        new TextImage("the power station to light the board", 14, FontStyle.ITALIC, Color.BLACK));
    //Keeps track of time:
    WorldImage time = new TextImage("TIME: " + Integer.toString(this.time), 14, FontStyle.REGULAR,
        Color.BLACK);
    //Keeps track of score:
    WorldImage score = new AboveImage(
        new TextImage("You have connected ", 16, FontStyle.BOLD, Color.BLACK),
        new TextImage(Integer.toString(this.keepScore()) + " / "
        + Integer.toString(this.width * this.height) + " squares", 16, FontStyle.BOLD, 
        Color.BLACK),
        new TextImage("Connect " 
            + Integer.toString(this.width * this.height - this.keepScore())
            + " more to win!", 16, FontStyle.REGULAR, 
            Color.BLACK));
    WorldImage instructions = new AboveImage(
        new TextImage("(or press 'g' to give up on this game,", 12, FontStyle.REGULAR, Color.GRAY),
        new TextImage("and 'r' to restart with a new game)", 12, FontStyle.REGULAR, Color.GRAY));
    //Write the extra credit instructions:
    WorldImage extraInfo = new AboveImage(
        new TextImage("Press 'h' at any time to generate", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("a horizontally-bias game.", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("Press 'v' at any time to generate", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("a vertically-bias game.", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("Press 'x' to open a new canvas to see", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("our attemp at a hexagonal game.", 12, FontStyle.REGULAR, 
            Color.BLACK));
    //Align all the info into the background image:
    WorldImage placeTitle = new OverlayImage(titleImage.movePinholeTo(new Posn(0, 250)), bckgr);
    WorldImage placeTime = new OverlayImage(time.movePinholeTo(new Posn(0, 110)), placeTitle);
    WorldImage placeScore = new OverlayImage(score.movePinholeTo(new Posn(0, 50)), placeTime);
    WorldImage placeExtraCreditInfo = new OverlayImage(extraInfo.movePinholeTo(new Posn(0, -240)), 
        placeScore);
    WorldImage placeInstructions = new OverlayImage(instructions.movePinholeTo(new Posn(0, -300)), 
        placeExtraCreditInfo);
    //Handles the winning notification:
    int aTime = this.time;
    WorldImage winnerMsg = new AboveImage(
        new TextImage("YOU'RE A WINNER!", 20, FontStyle.BOLD, Color.GREEN),
        new TextImage("You beat the game in "
        + Integer.toString(aTime)
        + " seconds", 16, FontStyle.BOLD, Color.GREEN),
        new TextImage("Press the r key for a new challenege!", 
        15, FontStyle.REGULAR, Color.BLUE));
    if (this.gameWon) {
      return new OverlayImage(
          winnerMsg.movePinhole(0, -55), placeInstructions);
    }
    return placeInstructions;
  }
  
  ////////////////End Draw Controls///////////////
  
  // Keeps track of the user's "score" in terms of now many square on the
  // game grid are lit. This integer is also subtracted from the total number of
  // nodes in order to keep track of how many are remaining (the smaller the better).
  // EFFECT: If the score reflects a winning game, this method turns the gameWon boolean true.
  int keepScore() {
    int score = 0;
    for (int i = 0; i < this.nodes.size(); i++) {
      if (this.nodes.get(i).lit) {
        score = score + 1;
      }
    }
    if (score == this.width * this.height) {
      this.gameWon = true;
    }
    return score;
  }
  
  // Counts the number of seconds a user has been playing a game,
  // to be displayed in the game stats secion of the canvas.
  // When the game has been won, the timer pauses until the user restarts the game.
  // at which time the timer is reset:
  public void onTick() {
    if (!this.gameWon) {
      this.time = this.time + 1;
    }
  }
  
  ////////////////////MOUSE CONTROLS////////////////
  // Controls the clicking functionality of the game
  // when a GamePiece is clicked, the action of rotating the piece is delegated
  // to the GamePiece class, and the board is refreshed to reflect any new connections
  // or any disconnected pieces being un-lit:
  public void onMouseClicked(Posn pos) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (pos.x > bbWidth / width * j 
            && pos.x < bbWidth / width * (j + 1) 
            && pos.y > bbHeight / height * i 
            && pos.y < bbHeight / height * (i + 1)) {
          GamePiece curGP = this.board.get(i).get(j);
          curGP.rotateClockwise();
          this.refresh();
        }
      }
    }
  }
  /////////////////////////////////////////////////////
  
  ////////////////// OnKey Events/////////////////
  // Controls the key functionality of the game
  // When an arrow key is pressed, iterates through the entire board and finds the
  // power station. If the curGP is the power station, it loses the power station,
  // and passes it on to it's neighbor, depending on which key is pressed.
  // When the letter "g", which stands for "give up" is pressed, the game solves itself
  // by connecting all the wires. However, the power station is not moved, so the user
  // may move it into place to "win" the game.
  public void onKeyEvent(String key) {
    GamePiece powerStation = this.board.get(powerRow).get(powerCol);
    if (!this.gameWon) {
      if (key.equals("left")) {
        int jOfLeftn = powerCol - 1;
        // These conditionals prevent out of bounds errors:
        // So long as the j coordinate of the left neighbor is not out of bounds...
        // (if it is, ignore the user key press, and do nothing)
        if (jOfLeftn >= 0) {
          // finds the left neighbor of the power station:
          GamePiece leftn = this.board.get(powerRow).get(jOfLeftn);
          // makes that left neighbor the new designated power station,
          // only if they are also wire neighbors, in order to keep the
          // power station along the power lines:
          if (powerStation.wireNeighborsWith(leftn)) {
            powerStation.givePowerStationTo(leftn);
            // reset the power station
            this.powerCol = jOfLeftn;
          }
        }
      }
      // repeat for the rest:
      if (key.equals("right")) {
        int jOfRightn = powerCol + 1;
        if (jOfRightn < width) {
          GamePiece rightn = this.board.get(powerRow).get(jOfRightn);
          if (powerStation.wireNeighborsWith(rightn)) {
            powerStation.givePowerStationTo(rightn);
            this.powerCol = jOfRightn;
          }
        }
      }
      if (key.equals("up")) {
        int iOfTopn = powerRow - 1;
        if (iOfTopn >= 0) {
          GamePiece upn = this.board.get(iOfTopn).get(powerCol);
          if (powerStation.wireNeighborsWith(upn)) {
            powerStation.givePowerStationTo(upn);
            this.powerRow = iOfTopn;
          }
        }
      }
      if (key.equals("down")) {
        int iOfBotn = powerRow + 1;
        if (iOfBotn < height) {
          GamePiece downn = this.board.get(iOfBotn).get(powerCol);
          if (powerStation.wireNeighborsWith(downn)) {
            powerStation.givePowerStationTo(downn);
            this.powerRow = iOfBotn;
          }
        }
      }
    }
    // The "give-up" funcionality:
    if (key.equals("g")) {
      this.createMST(this.getEdges(this.nodes.size(), this.nodes.size()));
      connectTheWires();
    }
    // Press "r" to reset the game:
    if (key.equals("r")) {
      this.gameWon = false;
      this.powerCol = 0;
      this.powerRow = 0;
      this.board = makeBoard();
      populateNeighbors();
      this.nodes = getNodes();
      this.mst = this.createMST(this.getEdges(this.nodes.size(), this.nodes.size()));
      this.connectTheWires();
      this.refresh();
      this.radius = calculateRadius();
      this.time = 0;
      this.randomRotateAll();
    }
    // Press "v" for a vertical bias game, distinguished by the
    // vertical bias parameter being a much greater integer than the
    // horizontal bias parameter in the getEdges() method.
    // Or press "h" for a horizontal bias game, distinguished by the
    // horizontal bias parameter being a much greater integer than the
    // vertical bias parameter in the getEdges() method.
    if (key.equals("v") || key.equals("h")) {
      this.gameWon = false;
      this.powerCol = 0;
      this.powerRow = 0;
      this.board = makeBoard();
      populateNeighbors();
      this.nodes = getNodes();
      // The actual integers in the parameters (this.width, this.nodes.size(), etc)
      // are arbitrary; the most important factor is that one is significantly larger
      // than the other.
      if (key.equals("v")) {
        this.mst = this.createMST(this.getEdges(this.width, this.nodes.size() * 10));
      }
      if (key.equals("h")) {
        this.mst = this.createMST(this.getEdges(this.nodes.size() * 10, this.height));
      }
      this.connectTheWires();
      this.refresh();
      this.radius = calculateRadius();
      this.time = 0;
      this.randomRotateAll();
    }
    //Press "x" to open a new canvas, with our attempt at the hexagon game:
    if (key.equals("x")) {
      new LightEmAllHex(8, 10);
    }
    // and re-light the board:
    this.refresh();
  }
  //////////////////////////////////////////////////
 
  // EFFECT: Refreshes the lighting of the board by causing all GamePieces 
  // (except the power station) to momentarily lose power, then calls lightEmUp on 
  // the power station once again to refresh any newly connected power lines.
  // This method is called any time a change is made to the world by the user.
  public void refresh() {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        if (i != powerRow || j != powerCol) {
          curGP.loseLight();
        }
      }
    }
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        if (i == powerRow && j == powerCol) {
          curGP.lightEmUp(this.radius);
        }
      }
    }
  }
}

////////// REPRESENTS EDGE CLASS//////////
// Represents an Edge on a graph of GamePieces
class Edge<T> {
  T fromNode;
  T toNode;
  // Takes an integer representing the maximum weight that an Edge can be assigned
  int weight;

  // General constructor for random-weight Edges
  Edge(T from, T to, int maxWeight) {
    this.fromNode = from;
    this.toNode = to;
    Random r = new Random();
    this.weight = r.nextInt(maxWeight);
  }

  // Constructor to test Edges in the createMST() method:
  // If every edge has a weight of one, the sum of all the weights
  // of the min spanning tree.
  Edge(T from, T to) {
    this.fromNode = from;
    this.toNode = to;
    this.weight = 1;
  }
}

////////// ARRAY UTILS///////////
// An arrayUtils class that is used to help sort ArrayLists used throughout this program
class ArrayUtils {
  // Effect: Exchanges the values at the given two indices in the given array
  <T> void swap(ArrayList<T> arr, int index1, int index2) {
    T oldValueAtI1 = arr.get(index1);
    T oldValueAtI2 = arr.get(index2);

    arr.set(index1, oldValueAtI2);
    arr.set(index2, oldValueAtI1);
  }

  // EFFEECT: sorts the ArrayList in ascending order using the heapsort technique
  // by downheaping each item on the arrayList starting at the center of the
  // ArrayList
  // and ending at index 0 to create a valid heap.
  // Then, swap elements at index 0 of the valid heap with elements at index
  // heap.size() - 1,
  // and add the element previously at index 0 to a new ArrayList that is the
  // resulting sorted ArrayList, while downheaping the new element at index 0 with 
  // the rest of the heap:
  <T> ArrayList<T> heapsort(ArrayList<T> arr, IComparator<T> comp) {
    // Reorder arr into a heap:
    for (int i = (arr.size() - 1) / 2; i >= 0; i = i - 1) {
      downheap(arr, i, comp);
    }
    // put into non-decreasing order:
    ArrayList<T> sortedOrder = new ArrayList<T>();
    for (int i = arr.size() - 1; i >= 0; i = i - 1) {
      this.swap(arr, i, 0);
      T addThis = arr.remove(arr.size() - 1);
      this.downheap(arr, 0, comp);
      sortedOrder.add(0, addThis);
    }
    return sortedOrder;
  }

  // EFFECT: helps heap sort the given array by comparing the element at index i
  // to the elements at both child indices, determining if the element at index i is in place by
  // checking if it is less of a priority than either of its children elements. If so, it is
  // swapped with the child with the greatest priority,
  // and recurs until the element at the starting index is in place.
  <T> void downheap(ArrayList<T> arr, int startingIdx, IComparator<T> comp) {
    int leftChildIdx = 2 * startingIdx + 1;
    int rightChildIdx = 2 * startingIdx + 2;
    int biggerChildIdx = leftChildIdx;
    // IF BOTH CHILDREN EXIST:
    if (arr.size() - 1 >= leftChildIdx && arr.size() - 1 >= rightChildIdx) {
      // To determine which child has greater priority, we create a variable called
      // biggerChildIdx that is the left child by default, but once the two children's
      // elements are compared is reset to the right child if necessary.
      if (comp.compare(arr.get(rightChildIdx), arr.get(leftChildIdx)) > 0) {
        biggerChildIdx = rightChildIdx;
      }
      if (comp.compare(arr.get(startingIdx), arr.get(leftChildIdx)) < 0 || comp.compare(arr.get(
          startingIdx), arr.get(rightChildIdx)) < 0) {
        this.swap(arr, startingIdx, biggerChildIdx);
        this.downheap(arr, biggerChildIdx, comp);
      }
    }
    // IF ONLY THE RIGHT CHILD EXISTS
    if (arr.size() - 1 < leftChildIdx && arr.size() - 1 >= rightChildIdx) {
      if (comp.compare(arr.get(startingIdx), arr.get(rightChildIdx)) < 0) {
        this.swap(arr, startingIdx, rightChildIdx);
        this.downheap(arr, rightChildIdx, comp);
      }
    }
    // IF ONLY THE LEFT CHILD EXISTS
    if (arr.size() - 1 < rightChildIdx && arr.size() - 1 >= leftChildIdx) {
      if (comp.compare(arr.get(startingIdx), arr.get(leftChildIdx)) < 0) {
        this.swap(arr, startingIdx, leftChildIdx);
        this.downheap(arr, leftChildIdx, comp);
      }
    }
  }
}

// Represents a general IComparator to be used in the ArrayUtils class heap sort
interface IComparator<T> {
  int compare(T t1, T t2);
}

// Represents an IComparator<Edge> that can be used to determine which Edge is
// lighter
class CompareWeight implements IComparator<Edge<GamePiece>> {
  // If the weight of e1 is less than the weight of e2, returns a neg number
  // If the weight of e1 is greater than weight of e2, returns a pos number
  public int compare(Edge<GamePiece> e1, Edge<GamePiece> e2) {
    return e1.weight - e2.weight;
  }
}

// Represents an IComparator<Edge> that can be used to determine which Edge 
// that contains hexogonical GamePieces is lighter 
class CompareWeightHex implements IComparator<Edge<GamePieceHex>> {
  // If the weight of e1 is less than the weight of e2, returns a neg number
  // If the weight of e1 is greater than weight of e2, returns a pos number
  public int compare(Edge<GamePieceHex> e1, Edge<GamePieceHex> e2) {
    return e1.weight - e2.weight;
  }
}

// Represents an IComparator<Integer> that is used to easily test the heapsort
// and downheap methods in the examples class
class CompareIntegers implements IComparator<Integer> {
  public int compare(Integer i1, Integer i2) {
    return i1 - i2;
  }
}

class GamePieceHex {
  // location in the LightEmAllHex game-board 2D ArrayList: row -> i; col -> j;
  int col;
  int row;
  // whether this GamePiece is connected to the adjacent topLeft, topRight, 
  // left, right, bottomLeft, or bottomRight pieces
  boolean tl;
  boolean tr;
  boolean left;
  boolean right;
  boolean bl;
  boolean br;
  // A list of GamePieces of this GamePiece's neighbors
  ArrayList<GamePieceHex> neighbors;
  // whether the power station is on this piece
  boolean powerStation;
  // Whether the GamePiece is connected to power
  boolean lit;
  // The distance of the GamePiece from the power station.
  // Two GamePieces may have the same powerVals if they both span
  // out from the power station in different directions but are the same
  // distance from it
  int powerVal;
  // The distance field is used only once in the process of finding the diameter,
  // and can represent a more general sense of 'distance' between two arbitrary
  // GamePieces rather than specifically the distance from the power station.
  int distance;
  // A random number that represents the number of times a GamePiece is rotated when a
  // game is initialized
  int orientation;
  
  // Constructor for a general, non-power station GamePiece.
  // To begin, all fields are set to false, and given wires
  // in the connectTheWires() method in the LightEmAll class by delegating the
  // wire assignment to this class
  GamePieceHex(int col, int row) {
    this.col = col;
    this.row = row;
    this.tl = false;
    this.tr = false;
    this.left = false;
    this.right = false;
    this.bl = false;
    this.br = false;
    this.neighbors = new ArrayList<GamePieceHex>();
    this.powerStation = false;
    this.lit = false;
    this.powerVal = 0;
    this.distance = 0;
    Random r = new Random();
    this.orientation = r.nextInt(4);
  }
 
  // Constructor for the power station GamePiece, used to initialize the power
  // station in the makeBoard method in the LightEmAllHex class
  GamePieceHex(int col, int row, boolean powerStation) {
    this.col = col;
    this.row = row;
    this.tl = false;
    this.tr = false;
    this.left = false;
    this.right = false;
    this.bl = false;
    this.br = false;
    this.neighbors = new ArrayList<GamePieceHex>();
    this.powerStation = powerStation;
    this.lit = true;
    // The power station starts the count of distance at 0, its neighbors have a powerVal of 1
    this.powerVal = 0;
    Random r = new Random();
    this.orientation = r.nextInt(4);
  }

  // Constructor for testing different variations of orientation in examples class:
  GamePieceHex(int col, int row, boolean tl, boolean tr, boolean left, boolean right,
      boolean bl, boolean br) {
    this.col = col;
    this.row = row;
    this.tl = tl;
    this.tr = tr;
    this.left = left;
    this.right = right;
    this.bl = bl;
    this.br = br;
    this.neighbors = new ArrayList<GamePieceHex>();
    this.powerStation = false;
    this.lit = false;
    this.powerVal = 0;
    Random r = new Random();
    this.orientation = r.nextInt(4);
  }
  
  // EFFECT: Modifies this neighbors field to add a new neighbor, 
  // and adds this GamePiece to the neighbor's neighbors list,
  // only if the two GamePiece's are not already deemed neighbors
  void addNeighbor(GamePieceHex gp) {
    if (!this.neighbors.contains(gp)) {
      this.neighbors.add(gp);
      gp.neighbors.add(this);
    }
  }
  
  // After the min spanning tree has been generated, this method determines which neighbors 
  // each GamePiece should be wire-connected to.
  // EFFECT: Modifies the left/right/top/bottom boolean fields of this GamePiece and of its
  // neighbor GamePiece if they ought to be connected by wire
  void connectIfNeighborOf(GamePieceHex to) {
    //If the hex is on an even indexed row:
    if (this.row % 2 == 0) {
      // If this is the topLeft neighbor of to:
      if (this.row == to.row - 1 && this.col == to.col - 1) {
        this.br = true;
        to.tl = true;
      }
      // If this is the topRight neighbor of to:
      if (this.row == to.row - 1 && this.col == to.col) {
        this.bl = true;
        to.tr = true;
      }
      // If this is the bottomLeft neighbor of to:
      if (this.row == to.row + 1 && this.col == to.col - 1) {
        this.tr = true;
        to.bl = true;
      }
      // If this is the bottomRight neighbor of to:
      if (this.row == to.row + 1 && this.col == to.col) {
        this.tl = true;
        to.br = true;
      }
    }
    // If this is the LEFT neighbor of to:
    if (this.row == to.row && this.col == to.col - 1) {
      this.right = true;
      to.left = true;
    }
    // If this is the RIGHT neighbor to:
    if (this.row == to.row && this.col == to.col + 1) {
      this.left = true;
      to.right = true;
    }
    //If the hex is on an odd indexed row:
    if (this.row % 2 != 0) {
      // If this is the topLeft neighbor of to:
      if (this.row == to.row - 1 && this.col == to.col) {
        this.br = true;
        to.tl = true;
      }
      // If this is the topRight neighbor of to:
      if (this.row == to.row - 1 && this.col == to.col + 1) {
        this.bl = true;
        to.tr = true;
      }
      // If this is the bottomLeft neighbor of to:
      if (this.row == to.row + 1 && this.col == to.col) {
        this.tr = true;
        to.bl = true;
      }
      // If this is the bottomRight neighbor of to:
      if (this.row == to.row + 1 && this.col == to.col + 1) {
        this.tl = true;
        to.br = true;
      }
    }
  }
    
  // EFFECT: rotates the GamePiece a random number of times,
  // which is determined in the GamePiece constructor 
  // This method is used in the initiation of the game to randomize the rotation
  // of the board's GamePieces to start off
  void rotateRandom() {
    for (int i = this.orientation; i >= 0; i = i - 1) {
      this.rotateClockwise();
    }
  }
 
  // Determines if two GamePieces are connected by wires (not only by location)
  // First, checks which of the side neighbors the given piece is, in relation to this one
  // Then, returns whether the given GamePiece is also connected back to this one by wire
  boolean wireNeighborsWith(GamePieceHex that) {
    // If that is the LEFT neighbor:
    if (that.col == this.col - 1 && that.row == this.row) {
      return this.left && that.right;
    }
    // If that is the RIGHT neighbor:
    if (that.col == this.col + 1 && that.row == this.row) {
      return this.right && that.left;
    }
    // IF this hexagon is on an even indexed row:
    // If that is the TOPLEFT neighbor:
    if ((this.row % 2 == 0) && that.col == this.col && that.row == this.row - 1) {
      return this.tl && that.br;
    }
    // If that is the TOPRIGHT neighbor:
    if ((this.row % 2 == 0) && that.col == this.col + 1 && that.row == this.row - 1) {
      return this.tr && that.bl;
    }
    // If that is the BOTTOMLEFT neighbor:
    if ((this.row % 2 == 0) && that.col == this.col && that.row == this.row + 1) {
      return this.bl && that.tr;
    }
    // If that is the BOTTOMRIGHT neighbor:
    if ((this.row % 2 == 0) && that.col == this.col + 1 && that.row == this.row + 1) {
      return this.br && that.tl;
    }
    // IF this hexagon is on an odd indexed row:
    // If that is the TOPLEFT neighbor:
    if ((this.row % 2 != 0) && that.col == this.col - 1 && that.row == this.row - 1) {
      return this.tl && that.br;
    }
    // If that is the TOPRIGHT neighbor:
    if ((this.row % 2 != 0) && that.col == this.col && that.row == this.row - 1) {
      return this.tr && that.bl;
    }
    // If that is the BOTTOMLEFT neighbor:
    if ((this.row % 2 != 0) && that.col == this.col - 1 && that.row == this.row + 1) {
      return this.bl && that.tr;
    }
    // If that is the BOTTOMRIGHT neighbor:
    if ((this.row % 2 != 0) && that.col == this.col && that.row == this.row + 1) {
      return this.br && that.tl;
    }
    else {
      return false;
    }
  }
  
  // EFFECT: Uses breadth-first technique to modify the lit fields of the GamePieces
  // This method is only ever called on the PowerStation in the refresh method
  // in the LightEmAll class, and then spans out across the game board passing light across 
  // wire-connected pieces.
  // A convenient side effect of this is visiting specific GamePieces in order of distance
  // from the power station itself, so this method also has the effect of modifying GamePiece's
  // powerVal fields, which is a number representing its distance from the power station.
  // The GamePiece is only lit if it's powerVal is within the radius of light extension.
  void lightEmUp(int radius) {
    ArrayList<GamePieceHex> seen = new ArrayList<GamePieceHex>();
    ArrayList<GamePieceHex> workList = new ArrayList<GamePieceHex>();
    workList.add(this);
    this.powerVal = 0;
    while (workList.size() > 0) {
      GamePieceHex curGP = workList.remove(0); // removes and returns
      // only lights the GamePiece if it falls within the radius of the power station,
      // which is a parameter given by the LightEmAll class to this method
      if (curGP.powerVal <= radius) {
        curGP.lit = true;
      }
      for (int i = 0; i < curGP.neighbors.size(); i++) {
        GamePieceHex eachNeighbor = curGP.neighbors.get(i);
        if (curGP.wireNeighborsWith(eachNeighbor) && !seen.contains(eachNeighbor)) {
          workList.add(eachNeighbor);
          // Each GamePiece's powerVal is one more than that of its neighbor that is
          // closest
          // to the power station
          eachNeighbor.powerVal = curGP.powerVal + 1;
        }
      }
      seen.add(curGP);
    }
  }
  
  // EFFECT: Causes a GamePiece to lose it's light, which also 
  // resets its powerVal to zero by nature. 
  // This method is used in the refresh method in the LightEmAll class and applied to every
  // GP on the board (except the power station) in order to then re-light only the appropriate 
  // GPs in the lightEmUp method above
  void loseLight() {
    this.powerVal = 0;
    this.lit = false;
  }
  
  // Returns the GamePiece that is farthest from the given GamePiece,
  // using breadth-first search to process all GamePieces in breadth-first
  // order, then returns the last item on the seen list.
  // This method is a helper method to the following method: getDiameter();
  GamePieceHex farthestFrom() {
    ArrayList<GamePieceHex> seen = new ArrayList<GamePieceHex>();
    ArrayList<GamePieceHex> workList = new ArrayList<GamePieceHex>();
    workList.add(this);
    this.distance = 0;
    while (workList.size() > 0) {
      GamePieceHex curGP = workList.remove(0); // removes and returns
      for (int i = 0; i < curGP.neighbors.size(); i++) {
        GamePieceHex eachNeighbor = curGP.neighbors.get(i);
        if (curGP.wireNeighborsWith(eachNeighbor) && !seen.contains(eachNeighbor)) {
          workList.add(eachNeighbor);
          eachNeighbor.distance = curGP.distance + 1;
        }
      }
      seen.add(curGP);
    }
    GamePieceHex farthestFromThis = seen.remove(seen.size() - 1);
    return farthestFromThis;
  }
  
  // Returns an integer that represents the diameter of the extension of power
  // that the power station can give off. This is calculated by locating the farthest
  // GamePiece from the power station, then locating the farthest GamePiece from that 
  // GamePiece. All the while, the distance of movement across the board is kept track of
  // in the distance field of each GamePiece. Then, the diameter is the distance apart from
  // the two farthest GamePieces.
  // This method is called on the power station in the LightEmAll class.
  int getDiameter() {
    GamePieceHex farthestFromPS = this.farthestFrom();
    GamePieceHex farthestFromThat = farthestFromPS.farthestFrom();
    int diameter = farthestFromThat.distance;
    return diameter;
  }
 
  // Draws an individual GamePiece, depending on its properties:
  // if the GP is not lit, it is grey;
  // if it is and it is close to the power station, it is yellow;
  // the further away it gets from the power station, it turns orange;
  // the GamePieces the farthest away are pink;
  // GamePieces that fall outside the radius are not lit.
  // The length of an individual piece will depend on the properties of the full canvas,
  // therefore, the length of each piece will be calculated in the LightEmAll
  // class and given here
  WorldImage draw(int sideLen, int radius) {
    WorldImage gPiece = new OverlayImage(
        new RotateImage(new HexagonImage(sideLen - 2, OutlineMode.SOLID, Color.DARK_GRAY), 90),
        new RotateImage(new HexagonImage(sideLen, OutlineMode.SOLID, Color.BLACK), 90));
    // Each line's end point will always be a position on the outer border of the
    // gPiece image, and will land in the center of the length of the side of the gPiece image.
    // EFFECT: The variable clr contains the color for which the lines on the GamePiece should be
    // based on whether or not the GamePiece is connected to power 
    Color clr = Color.LIGHT_GRAY;
    if (this.lit) {
      if (this.powerVal <= radius) {
        clr = Color.YELLOW;
      }
      if (this.powerVal >= radius - 3) {
        clr = Color.ORANGE;
      }
      if (this.powerVal >= radius - 1) {
        clr = Color.PINK;
      }
    }
    // One horizontal and two diagonal lines to overlay at different positions 
    // over the plank gPiece image above
    int sideLenA = sideLen * 3 / 4;
    int sideLenA2 = sideLen / 3;
    WorldImage horizontal = new LineImage(new Posn(sideLen - 10, 0), clr);
    WorldImage diagFromL = new RotateImage(new LineImage(new Posn(0, sideLenA + 10), clr), -45);
    WorldImage diagFromR = new RotateImage(new LineImage(new Posn(0, sideLenA), clr), 45);
    if (this.tl) {
      gPiece = new OverlayOffsetImage(diagFromL, sideLenA2 - 5, sideLenA2 - 5, gPiece);
    }
    if (this.tr) {
      gPiece = new OverlayOffsetImage(diagFromR, sideLenA2 / 3 - 20, sideLenA2, gPiece);
    }
    if (this.left) {
      gPiece = new OverlayOffsetImage(horizontal, sideLenA2 + 5, 0, gPiece);
    }
    if (this.right) {
      gPiece = new OverlayOffsetImage(horizontal, -(sideLenA2 + 5), 0, gPiece);
    }
    if (this.bl) {
      gPiece = new OverlayOffsetImage(diagFromR, sideLenA2, -sideLenA2, gPiece);
    }
    if (this.br) {
      gPiece = new OverlayOffsetImage(diagFromL, -sideLenA2, -sideLenA2, gPiece);
    }
    if (this.powerStation) {
      WorldImage pwr = new OverlayImage(
          new CircleImage(sideLen / 4, OutlineMode.SOLID, Color.YELLOW),
          new StarImage(sideLen - 8, 7, OutlineMode.SOLID, Color.RED));
      gPiece = new OverlayImage(pwr, gPiece);
    }
    return gPiece;
  }
  
  // EFFECT: adjusts this GamePiece's properties to reflect a rotation in the clockwise direction.
  // When a GamePiece is clicked, the LightEmAll class delegates the rotation action to here
  void rotateClockwise() {
    boolean storetl = this.tl;
    boolean storetr = this.tr;
    boolean storeleft = this.left;
    boolean storeright = this.right;
    boolean storebl = this.bl;
    boolean storebr = this.br;
    this.tl = storeleft;
    this.tr = storetl;
    this.right = storetr;
    this.br = storeright;
    this.bl = storebr;
    this.left = storebl;
  }
  
  // EFFECT: modifies the power station field of two GamePieces,
  // causing one to lose its power station status and the other to
  // gain it, and also becoming lit by nature. This one method to
  // transfer the power station ensures that there can only ever
  // be one designated power station on the game grid, and it can
  // only ever pass it off the one other GamePiece
  void givePowerStationTo(GamePieceHex newPS) {
    this.powerStation = false;
    newPS.powerStation = true;
    newPS.lit = true;
    newPS.powerVal = 0;
  }
}

class LightEmAllHex extends World {
  // the width and height of the board
  // in terms of the number of GamePieces that go across and down the game board
  int width;
  int height;
  int powerCol;
  int powerRow;
  // a list of rows of GamePieces
  ArrayList<ArrayList<GamePieceHex>> board;
  // a list of all nodes in game
  ArrayList<GamePieceHex> nodes;
  // a list of edges of the minimum spanning tree 
  ArrayList<Edge<GamePieceHex>> mst;
  // the current location of the power station,
  // as well as its effective radius
  int radius;
  int time;
  boolean gameWon;
  int whitespace = 300;
  int bbWidth = 700;
  int bbHeight = 700;
  
  // Constructor takes the number of columns and rows respectively, and generates a playable game
  LightEmAllHex(int width, int height) {
    this.width = width;
    this.height = height;
    this.powerCol = 0;
    this.powerRow = 0;
    this.board = makeBoard();
    //After constructing the board, populates the neighbors of each GamePiece:
    populateNeighbors();
    this.nodes = getNodes();
    //The int parameters given to getEdges(int int) represent the option to define a 
    //bias in either horizontal or vertical direction. Here, they are equal, representing
    //no bias.
    //The createMST takes in an ArrayList<Edge> as a parameter in order to be abstract
    //enough to create a min spanning tree given any un ordered list of Edge.
    this.mst = this.createMST(this.getEdges());
    //Connects the edges in the min spanning tree by wires:
    this.connectTheWires();
    //Populates the GamePieces with power values to be used in calculateRadius:
    this.refresh();
    this.radius = calculateRadius();
    //Randomly rotates all GamePieces:
    randomRotateAll();
    //Prepares the board for game play:
    this.refresh();
    this.time = 0;
    this.gameWon = false;
    //The width of the canvas has 200 pixels of whitespace on the right side, where game stats
    //will be written to fulfill extra credit portions of the game:
    bigBang(bbWidth + whitespace, bbHeight, 1);
  }
  
  // Constructor for testing that does not open a canvas when called, and allows you to test
  // more properties than the previous constructor:
  LightEmAllHex(int width, int height, int powerCol, int powerRow) {
    this.width = width;
    this.height = height;
    if (powerCol >= width || powerCol < 0) {
      throw new IllegalArgumentException("Column does not exist on this board.");
    }
    else {
      this.powerCol = powerCol;
    }
    if (powerRow >= height || powerRow < 0) {
      throw new IllegalArgumentException("Row does not exist on this board.");
    }
    else {
      this.powerRow = powerRow;
    }
    this.board = makeBoard();
    this.nodes = getNodes();
    this.mst = this.createMST(this.getEdges());
    this.radius = calculateRadius();
    this.time = 0;
    this.gameWon = false;
  }
  
  ///////Constructs the game board and other initial properties://///////
  // Creates a 2D representation of the LightEmAll game board in row-major orientation
  // Allows coder to access a specific GamePiece, call this.board.get(i).get(j)
  ArrayList<ArrayList<GamePieceHex>> makeBoard() {
    ArrayList<ArrayList<GamePieceHex>> allRows = new ArrayList<ArrayList<GamePieceHex>>();
    for (int i = 0; i < height; i++) {
      ArrayList<GamePieceHex> aRow = new ArrayList<GamePieceHex>();
      for (int j = 0; j < width; j++) {
        //Add the power station:
        if (j == this.powerCol && i == this.powerRow) {
          aRow.add(new GamePieceHex(j, i, true));
        }
        //Add normal GamePieces, with all wire booleans set to false:
        else {
          aRow.add(new GamePieceHex(j, i));
        }
      }
      allRows.add(aRow);
    }
    return allRows;
  }
  
  // EFFECT: After constructing the board, this method populates the neighbors of each GamePiece
  // by modifying the curGP's neighbor list 
  // If the curGP is along the side of the grid, it will have three neighbors
  // If the curGP is a corner piece, it will have two neighbors
  public void populateNeighbors() {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePieceHex curGP = this.board.get(i).get(j);
        // LEFT
        int jOfLeftn = j - 1; // the j index of the left neighbor
        if (jOfLeftn >= 0) { // so long as curGP's left neighbor exists...
          GamePieceHex leftN = this.board.get(i).get(jOfLeftn);
          curGP.addNeighbor(leftN); // add the neighbor
        }
        // RIGHT
        int jOfRightn = j + 1; // j of right...
        if (jOfRightn <= width - 1) {
          GamePieceHex rightN = this.board.get(i).get(jOfRightn);
          curGP.addNeighbor(rightN);
        }
        // Even indexed rows
        if (i % 2 == 0) {
          // TOP NEIGHBORS
          if (i - 1 >= 0 && j + 1 <= width - 1) {
            GamePieceHex trn = this.board.get(i - 1).get(j + 1);
            curGP.addNeighbor(trn);
          }
          if (i - 1 >= 0 && j >= 0) {
            GamePieceHex tln = this.board.get(i - 1).get(j);
            curGP.addNeighbor(tln);
          }
          // BOTTOM NEIGHBORS
          if (i + 1 < this.height && j + 1 < this.width) {
            GamePieceHex brn = this.board.get(i + 1).get(j + 1);
            curGP.addNeighbor(brn);
          }
          if (i + 1 < this.height && j >= 0) {
            GamePieceHex bln = this.board.get(i + 1).get(j);
            curGP.addNeighbor(bln);
          }
        }
        if (i % 2 != 0) {
          // TOP NEIGHBORS
          if (i - 1 >= 0 && j <= width - 1) {
            GamePieceHex trn = this.board.get(i - 1).get(j);
            curGP.addNeighbor(trn);
          }
          if (i - 1 >= 0 && j - 1 >= 0) {
            GamePieceHex tln = this.board.get(i - 1).get(j - 1);
            curGP.addNeighbor(tln);
          }
          // BOTTOM NEIGHBORS
          if (i + 1 <= this.height - 1 && j <= this.width - 1) {
            GamePieceHex brn = this.board.get(i + 1).get(j);
            curGP.addNeighbor(brn);
          }
          if (i + 1 <= this.height - 1 && j - 1 >= 0) {
            GamePieceHex bln = this.board.get(i + 1).get(j - 1);
            curGP.addNeighbor(bln);
          }
        }
      }
    }
  }
  
  // Generates an ArrayList of all nodes in the game
  // by iterating through the board, taking one GamePiece at a time, and 
  // adding it to a new flattened list
  ArrayList<GamePieceHex> getNodes() {
    ArrayList<GamePieceHex> nodeList = new ArrayList<GamePieceHex>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePieceHex curGP = this.board.get(i).get(j);
        nodeList.add(curGP);
      }
    }
    return nodeList;
  }
  
  ///////////////KRUSKALS ALGORITHM//////////////////
  // A series of helper methods followed by a method called createMST
  // that brings together all of the helper methods:
  
  // Generates an ArrayList of all weighted edges possible to connect the board
  // by locating the curGP's bottom and right neighbors only (because if we generated edges with
  // all neighbors, there would be repeat edges) then for each neighbor, if it exists, add a new 
  // Edge to the list of Edges with a randomized weight, which is randomized in the constructor of 
  // the Edge class. 
  // --When the game board has no bias, he maxWeight variables are equal, arbitrarily calculated,
  // and is just a simple way to generate a max for the random.nextInt() used in the Edge class.
  // --When the game board has a vertical bias, the maximum weight used in the generation of 
  // horizontal is much, much higher than that of the max weight a vertical edge may have. 
  // Therefore, the vertical edges will inevitably come first in the ordering of edges from 
  // smallest to largest, and therefore appear more frequently on the final game, because they 
  // are processed first and have less likelihood to create a cycle then.
  // (Press "v" at any time to start a vertically bias game)
  // --The same but opposite logic applies to a horizontally bias game.
  // (Press "h" at any time to start a horizontally bias game)
  ArrayList<Edge<GamePieceHex>> getEdges() {
    ArrayList<Edge<GamePieceHex>> edgeList = new ArrayList<Edge<GamePieceHex>>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePieceHex curGP = this.board.get(i).get(j);
        //GET RIGHT NEIGHBOR EDGE
        int jOfRightn = j + 1; // the j index of the right neighbor
        if (jOfRightn <= width - 1) { // so long as curGP's right neighbor exists...
          GamePieceHex rightN = this.board.get(i).get(jOfRightn);
          edgeList.add(new Edge<GamePieceHex>(curGP, rightN, this.nodes.size() * 4));
        }
        int jOfBotLeftn = j; // i of bottom left...
        int jOfBotRightn = j + 1; // i of bottom right...
        if (i % 2 != 0) {
          jOfBotLeftn = j - 1; // i of bottom...
          jOfBotRightn = j; // i of bottom...
        }
        //GET BOTTOM LEFT NEIGHBOR EDGE
        if (i + 1 <= height - 1 && jOfBotLeftn >= 0) {
          GamePieceHex botLeftN = this.board.get(i + 1).get(jOfBotLeftn);
          edgeList.add(new Edge<GamePieceHex>(curGP, botLeftN, this.nodes.size() * 4));
        }
        //GET BOTTOM RIGHT NEIGHBOR EDGE
        if (i + 1 <= height - 1 && jOfBotRightn >= 0) {
          GamePieceHex botRightN = this.board.get(i + 1).get(j);
          edgeList.add(new Edge<GamePieceHex>(curGP, botRightN, this.nodes.size() * 4));
        }
      }
    }
    return edgeList;
  }
 
  // Creates a hashmap of representatives to be used in Krushkal's algorithm
  // Uses unique integers pulled from the GamePiece's unique index in the ArrayList
  // of total game board nodes to be represented in the hashmap. This way, when we are
  // comparing representatives in the createMST method, it is better to compare equality 
  // of integers, rather than compare equality of GamePieces, etc.
  HashMap<Integer, Integer> makeHashMapOfReps() {
    HashMap<Integer, Integer> reps = new HashMap<Integer, Integer>();
    for (int i = 0; i < this.nodes.size(); i++) {
      reps.put(i, i);
    }
    return reps;
  }

  // Searches through the reps HashMap and finds the Key that has the given GamePiece
  // representative Integer as a Value. Returns the Key:
  int find(HashMap<Integer, Integer> reps, GamePieceHex gp) {
    int find = 0;
    for (int i = 0; i < this.nodes.size(); i++) {
      GamePieceHex node = this.nodes.get(i);
      if (node.row == gp.row && node.col == gp.col) {
        find = i;
      }
    }
    while (find != reps.get(find)) {
      find = reps.get(find);
    }
    return find;
  }
  
  // Performs Kruskal's algorithm to determine the minimum spanning tree of the solved game:
  // The edges parameter is useful in case you want to generate a board with a vertical or 
  // horizontal bias, you can call getEdges(int verticalBias, int horizontalBias) with varying 
  // values.
  ArrayList<Edge<GamePieceHex>> createMST(ArrayList<Edge<GamePieceHex>> edges) {
    //Initialize the hashmap:
    HashMap<Integer, Integer> reps = this.makeHashMapOfReps();
    //Initialize the workList, which is edges sorted from smallest weight to largest weight:
    ArrayUtils au = new ArrayUtils();
    ArrayList<Edge<GamePieceHex>> edgeListSmToLg = au.heapsort(edges, new CompareWeightHex());
    //Initialize the min spanning tree:
    ArrayList<Edge<GamePieceHex>> mst = new ArrayList<Edge<GamePieceHex>>();
    while (edgeListSmToLg.size() > 0) {
      Edge<GamePieceHex> curEdge = edgeListSmToLg.remove(0); //removes and returns
      int x = this.find(reps, curEdge.fromNode);
      int y = this.find(reps, curEdge.toNode);
      //If this edge does not create a cycle in the mst:
      if (x != y) {
        //add it to the tree:
        mst.add(curEdge);
        //and union the trees:
        reps.put(x, y); //union
      }
    }
    return mst;
  }
  ////////////End Kruskal's Algorithm////////////////////
  
  // Methods that finish up the initiation of the game board:
  
  // Calculates the finite radius of effectiveness that is given off by 
  // the power station by locating the power station, using breadth-first search
  // to calculate the longest distance from the power station to an end node when
  // the puzzle is solved, then calculating the distance between that GamePiece and its
  // furthest GamePiece (diameter).
  // then calculates the radius using that diameter.
  // This method is called once at the initiation of the game, but before the pieces
  // are randomly rotated (so, as a solved puzzle)
  int calculateRadius() {
    GamePieceHex powerstation = this.board.get(this.powerRow).get(this.powerCol);
    int diameter = powerstation.getDiameter();
    int rad = (diameter / 2) + 1;
    return rad;
  }
  
  // Traverses through the min spanning tree list of edges that represent the solved
  // game and 'gives wires' to each GamePiece according to their connected neighbors.
  // EFFECT: delegates to the GamePiece class to modify the left/right/top/bottom
  // boolean fields of the GamePieces that need to be connected
  void connectTheWires() {
    for (int i = 0; i < this.mst.size(); i++) {
      Edge<GamePieceHex> e = this.mst.get(i);
      GamePieceHex from = e.fromNode;
      GamePieceHex to = e.toNode;
      to.connectIfNeighborOf(from);
    }
  }
  
  // EFFECT: traverses through the total list of nodes of GamePieces and rotates
  // them a random number of times to finally prepare the board for game play:
  void randomRotateAll() {
    for (int i = 0; i < this.nodes.size(); i++) {
      GamePieceHex curGP = this.nodes.get(i);
      curGP.rotateRandom();
    }
  }
  //////////////////////////////////////////////////
  
  //////////////////Draw controls/////////////////////
  // Draws the LightEmAll game as a grid of GamePieces 
  // the gpWidth is the width of big bang (minus the whitespace where game stats are kept; 
  // this is done in bigbang above) divided by the number of columns
  // the gpHeight is the height of big bang divided by the number of rows
  public WorldScene makeScene() {
    int sideLen = (bbWidth / this.width) / 2;
    int sideLenx2 = sideLen * 2;
    int sideLenh = sideLen / 6 - 2;
    int distanceBtwTwo = (int) (Math.sqrt(3.0) * sideLen);
    WorldScene w = new WorldScene(bbWidth, bbHeight);
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int height = (i * (sideLen + 4 * sideLenh)) + sideLenx2;
        GamePieceHex curGP = this.board.get(i).get(j);
        if (i % 2 == 0) {

          w.placeImageXY(curGP.draw(sideLen, this.radius), (j * distanceBtwTwo) + sideLenx2 - 8,
              height - 40);
        }
        if (i % 2 != 0) {
          w.placeImageXY(curGP.draw(sideLen, this.radius), (j * distanceBtwTwo) + sideLen
              + sideLenh - 8, height - 40);
        }
      }
    }
    w.placeImageXY(this.writeGameStats(), bbWidth + (whitespace / 2), bbHeight / 2);
    return w;
  }
  
  // Produces a rectangular world image to be placed on the right side of the canvas and 
  // will contain text that keeps track of the game status throughout the game
  WorldImage writeGameStats() {
    //Initialize the rectangle where the info will be placed:
    WorldImage bckgr = new RectangleImage(whitespace, bbHeight, OutlineMode.OUTLINE,
        Color.BLACK);
    //Creates the Title & Instructions:
    WorldImage pwr = new OverlayImage(
        new CircleImage(8, OutlineMode.SOLID, Color.YELLOW),
        new StarImage(15, 7, OutlineMode.SOLID, Color.RED));
    WorldImage title = new TextImage("LIGHT 'EM ALL", 20, FontStyle.ITALIC, Color.BLACK);
    WorldImage titleImage = new AboveImage(
        new BesideImage(pwr, title, pwr),
        new TextImage("To Win: Connect all the wires and move", 14, FontStyle.ITALIC, Color.BLACK),
        new TextImage("the power station to light the board.", 14, FontStyle.ITALIC, Color.BLACK),
        new TextImage("(Use the a, w, e, d, x, & z keys", 14, FontStyle.ITALIC, Color.BLACK),
        new TextImage("to move the power station)", 14, FontStyle.ITALIC, Color.BLACK));
    //Keeps track of time:
    WorldImage time = new TextImage("TIME: " + Integer.toString(this.time), 14, FontStyle.REGULAR,
        Color.BLACK);
    //Keeps track of score:
    WorldImage score = new AboveImage(
        new TextImage("You have connected ", 16, FontStyle.BOLD, Color.BLACK),
        new TextImage(Integer.toString(this.keepScore()) + " / "
        + Integer.toString(this.width * this.height) + " hexagons", 16, FontStyle.BOLD, 
        Color.BLACK),
        new TextImage("Connect " 
            + Integer.toString(this.width * this.height - this.keepScore())
            + " more to win!", 16, FontStyle.REGULAR, 
            Color.MAGENTA));
    WorldImage instructions = new AboveImage(
        new TextImage("(press 'g' to give up on this game,", 12, FontStyle.REGULAR, Color.GRAY),
        new TextImage("and 'r' to restart with a new game)", 12, FontStyle.REGULAR, Color.GRAY));
    //Write the extra credit instructions:
    WorldImage extraInfo = new AboveImage(
        new TextImage("Close this canvas to return to", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("a square game.", 12, FontStyle.REGULAR, 
            Color.BLACK));
    //Align all the info into the background image:
    WorldImage placeTitle = new OverlayImage(titleImage.movePinholeTo(new Posn(0, 250)), bckgr);
    WorldImage placeTime = new OverlayImage(time.movePinholeTo(new Posn(0, 110)), placeTitle);
    WorldImage placeScore = new OverlayImage(score.movePinholeTo(new Posn(0, 50)), placeTime);
    WorldImage placeExtraCreditInfo = new OverlayImage(extraInfo.movePinholeTo(new Posn(0, -300)), 
        placeScore);
    WorldImage placeInstructions = new OverlayImage(instructions.movePinholeTo(new Posn(0, -250)), 
        placeExtraCreditInfo);
    //Handles the winning notification:
    int aTime = this.time;
    WorldImage winnerMsg = new AboveImage(
        new TextImage(":D!!!YOU'RE A WINNER!!!:D", 20, FontStyle.BOLD, Color.GREEN),
        new TextImage("You beat the game in "
        + Integer.toString(aTime)
        + " seconds", 16, FontStyle.BOLD, Color.GREEN),
        new TextImage("Press the r key for a new challenege!", 
        15, FontStyle.REGULAR, Color.BLUE));
    if (this.gameWon) {
      return new OverlayImage(
          winnerMsg.movePinhole(0, -55), placeInstructions);
    }
    return placeInstructions;
  }
  
  ////////////////End Draw Controls///////////////
  
  // Keeps track of the user's "score" in terms of now many square on the
  // game grid are lit. This integer is also subtracted from the total number of
  // nodes in order to keep track of how many are remaining (the smaller the better).
  // EFFECT: If the score reflects a winning game, this method turns the gameWon boolean true.
  int keepScore() {
    int score = 0;
    for (int i = 0; i < this.nodes.size(); i++) {
      if (this.nodes.get(i).lit) {
        score = score + 1;
      }
    }
    if (score == this.width * this.height) {
      this.gameWon = true;
    }
    return score;
  }
  
  // Counts the number of seconds a user has been playing a game,
  // to be displayed in the game stats secion of the canvas.
  // When the game has been won, the timer pauses until the user restarts the game.
  // at which time the timer is reset:
  public void onTick() {
    if (!this.gameWon) {
      this.time = this.time + 1;
    }
  }
  
  ////////////////////MOUSE CONTROLS////////////////
  // Controls the clicking functionality of the game
  // when a GamePiece is clicked, the action of rotating the piece is delegated
  // to the GamePiece class, and the board is refreshed to reflect any new connections
  // or any disconnected pieces being un-lit:
  public void onMouseClicked(Posn pos) {
    System.out.println("MOUSE POSX: " + Integer.toString(pos.x));
    System.out.println("MOUSE POSY: " + Integer.toString(pos.y));
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePieceHex curGP = this.board.get(i).get(j);
        if (pos.y >= i * 79 && pos.y <= (i * 85) + 79) {
          if (i % 2 == 0) {
            if (pos.x >= j * 79 + 40
                && pos.x <= (j * 79) + 119) {
              curGP.rotateClockwise();
            }
          }
          else {
            if (pos.x >= j * 79
                && pos.x <= (j * 79) + 79) {
              curGP.rotateClockwise();
            }
          }
        }
      }
    }
    this.refresh();
  }
  /////////////////////////////////////////////////////
  
  ////////////////// OnKey Events/////////////////
  // Controls the key functionality of the game
  // When an arrow key is pressed, iterates through the entire board and finds the
  // power station. If the curGP is the power station, it loses the power station,
  // and passes it on to it's neighbor, depending on which key is pressed.
  // When the letter "g", which stands for "give up" is pressed, the game solves itself
  // by connecting all the wires. However, the power station is not moved, so the user
  // may move it into place to "win" the game.
  public void onKeyEvent(String key) {
    GamePieceHex powerStation = this.board.get(powerRow).get(powerCol);
    if (!this.gameWon) {
      // LEFT
      if (key.equals("a")) {
        int jOfLeftn = powerCol - 1;
        // These conditionals prevent out of bounds errors:
        // So long as the j coordinate of the left neighbor is not out of bounds...
        // (if it is, ignore the user key press, and do nothing)
        if (jOfLeftn >= 0) {
          // finds the left neighbor of the power station:
          GamePieceHex leftn = this.board.get(powerRow).get(jOfLeftn);
          // makes that left neighbor the new designated power station,
          // only if they are also wire neighbors, in order to keep the
          // power station along the power lines:
          if (powerStation.wireNeighborsWith(leftn)) {
            powerStation.givePowerStationTo(leftn);
            // reset the power station
            this.powerCol = jOfLeftn;
          }
        }
      }
      // repeat for the rest:
      // RIGHT
      if (key.equals("d")) {
        int jOfRightn = powerCol + 1;
        if (jOfRightn < this.width) {
          GamePieceHex rightn = this.board.get(powerRow).get(jOfRightn);
          if (powerStation.wireNeighborsWith(rightn)) {
            powerStation.givePowerStationTo(rightn);
            this.powerCol = jOfRightn;
          }
        }
      }
      int jOftn = powerCol;
      int iOftn = powerRow - 1;
      // TOP LEFT
      if (key.equals("w")) {
        // Rows of even indices line up differently than off index rows.
        // Therefore, we need to check if it is an odd or even indexed row
        // in order to compute neighbors to pass the power station off to.
        if (powerRow % 2 != 0) {
          if (powerCol - 1 >= 0 && iOftn >= 0) {
            GamePieceHex topleftn = this.board.get(iOftn).get(powerCol - 1);
            if (powerStation.wireNeighborsWith(topleftn)) {
              powerStation.givePowerStationTo(topleftn);
              this.powerCol = powerCol - 1;
              this.powerRow = iOftn;
            }
          }
        }
        else {
          if (jOftn >= 0 && iOftn >= 0) {
            GamePieceHex topleftn = this.board.get(iOftn).get(jOftn);
            if (powerStation.wireNeighborsWith(topleftn)) {
              powerStation.givePowerStationTo(topleftn);
              this.powerCol = jOftn;
              this.powerRow = iOftn;
            }
          }
        }
      }
      // TOP RIGHT
      if (key.equals("e")) {
        if (powerRow % 2 == 0) {
          if (powerCol + 1 < this.width && iOftn >= 0) {
            GamePieceHex toprightn = this.board.get(iOftn).get(powerCol + 1);
            if (powerStation.wireNeighborsWith(toprightn)) {
              powerStation.givePowerStationTo(toprightn);
              this.powerCol = powerCol + 1;
              this.powerRow = iOftn;
            }
          }
        }
        else {
          if (jOftn < this.width && iOftn >= 0) {
            GamePieceHex toprightn = this.board.get(iOftn).get(jOftn);
            if (powerStation.wireNeighborsWith(toprightn)) {
              powerStation.givePowerStationTo(toprightn);
              this.powerCol = jOftn;
              this.powerRow = iOftn;
            }
          }
        }
      }
      int jOfbn = powerCol;
      int iOfbn = powerRow + 1;
      // BOTTOM LEFT
      if (key.equals("z")) {
        if (powerRow % 2 != 0) {
          if (powerCol - 1 >= 0 && iOfbn < this.height) {
            GamePieceHex bottomleftn = this.board.get(iOfbn).get(powerCol - 1);
            if (powerStation.wireNeighborsWith(bottomleftn)) {
              powerStation.givePowerStationTo(bottomleftn);
              this.powerCol = powerCol - 1;
              this.powerRow = iOfbn;
            }
          }
        }
        else {
          if (jOfbn >= 0 && iOfbn < this.height) {
            GamePieceHex bottomleftn = this.board.get(iOfbn).get(jOfbn);
            if (powerStation.wireNeighborsWith(bottomleftn)) {
              powerStation.givePowerStationTo(bottomleftn);
              this.powerCol = jOfbn;
              this.powerRow = iOfbn;
            }
          }
        }
      }
      // BOTTOM RIGHT
      if (key.equals("x")) {
        if (powerRow % 2 == 0) {
          if (powerCol + 1 < this.width && iOfbn < this.height) {
            GamePieceHex bottomrightn = this.board.get(iOfbn).get(powerCol + 1);
            if (powerStation.wireNeighborsWith(bottomrightn)) {
              powerStation.givePowerStationTo(bottomrightn);
              this.powerCol = powerCol + 1;
              this.powerRow = iOfbn;
            }
          }
        }
        else {
          if (jOfbn < this.width && iOfbn < this.height) {
            GamePieceHex bottomrightn = this.board.get(iOfbn).get(jOfbn);
            if (powerStation.wireNeighborsWith(bottomrightn)) {
              powerStation.givePowerStationTo(bottomrightn);
              this.powerCol = jOfbn;
              this.powerRow = iOfbn;
            }
          }
        }
      }
      // The "give-up" funcionality:
      if (key.equals("g")) {
        this.createMST(this.getEdges());
        connectTheWires();
      }
    }
    // Press "r" to reset the game:
    if (key.equals("r")) {
      this.gameWon = false;
      this.powerCol = 0;
      this.powerRow = 0;
      this.board = makeBoard();
      populateNeighbors();
      this.nodes = getNodes();
      this.mst = this.createMST(this.getEdges());
      this.connectTheWires();
      this.refresh();
      this.radius = calculateRadius();
      this.time = 0;
      this.randomRotateAll();
    }
    // and re-light the board:
    this.refresh();
  }
  //////////////////////////////////////////////////
 
  // EFFECT: Refreshes the lighting of the board by causing all GamePieces 
  // (except the power station) to momentarily lose power, then calls lightEmUp on 
  // the power station once again to refresh any newly connected power lines.
  // This method is called any time a change is made to the world by the user.
  public void refresh() {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePieceHex curGP = this.board.get(i).get(j);
        if (i != powerRow || (j != powerCol)) {
          curGP.loseLight();
        }
      }
    }
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePieceHex curGP = this.board.get(i).get(j);
        if (i == powerRow && j == powerCol) {
          curGP.lightEmUp(this.radius);
        }
      }
    }
  }
}

///////////////EXAMPLES CLASS//////////////
class ExamplesLightEmAll {
  //Examples of GamePieces:
  GamePiece gp1;
  GamePiece gp2;
  GamePiece gp3;
  GamePiece gp4;
  GamePiece gp5;
  GamePiece gp6;
  GamePiece gp7;
  GamePiece gp8;
  GamePiece gp9;
  
  //Examples of a manually-generated, 3x3 board
  ArrayList<ArrayList<GamePiece>> manualBoard;
  ArrayList<GamePiece> row1;
  ArrayList<GamePiece> row2;
  ArrayList<GamePiece> row3;
  
  //GAME EXAMPLE:
  LightEmAll lea = new LightEmAll(8, 8);
  
  //TESTER GAME EXAMPLE:
  //Does not open a canvas when called, but allows you to test properties of the board:
  LightEmAll leaTest;
  
  //Example of ArrayUtils
  ArrayUtils au = new ArrayUtils();
  
  // Initial game Conditions of the manually generated game:
  // Some game pieces are left without any wires in order to easily modify and
  // test different variations of connectivity
  void initConditions() {
    this.gp1 = new GamePiece(0, 0, false, true, false, true);
    this.gp2 = new GamePiece(1, 0, true, true, false, true);
    this.gp3 = new GamePiece(2, 0, false, false, true, true);
    this.gp4 = new GamePiece(0, 1, false, false, true, true);
    this.gp5 = new GamePiece(1, 1, false, false, false, false);
    this.gp6 = new GamePiece(2, 1, false, false, false, false);
    this.gp7 = new GamePiece(0, 2, false, true, true, false);
    this.gp8 = new GamePiece(1, 2, true, true, false, false);
    this.gp9 = new GamePiece(2, 2, true, false, false, false);
    
    this.gp1.addNeighbor(this.gp2);
    this.gp1.addNeighbor(this.gp4);
    this.gp2.addNeighbor(this.gp5);
    this.gp2.addNeighbor(this.gp3);
    this.gp3.addNeighbor(this.gp6);
    this.gp4.addNeighbor(this.gp5);
    this.gp4.addNeighbor(this.gp7);
    this.gp5.addNeighbor(this.gp6);
    this.gp5.addNeighbor(this.gp8);
    this.gp6.addNeighbor(this.gp9);
    this.gp7.addNeighbor(this.gp8);
    this.gp8.addNeighbor(this.gp9);
    
    this.manualBoard = new ArrayList<ArrayList<GamePiece>>();
    this.row1 = new ArrayList<GamePiece>();
    this.row1.add(this.gp1);
    this.row1.add(this.gp2);
    this.row1.add(this.gp3);
    this.row2 = new ArrayList<GamePiece>();
    this.row2.add(this.gp4);
    this.row2.add(this.gp5);
    this.row2.add(this.gp6);
    this.row3 = new ArrayList<GamePiece>();
    this.row3.add(this.gp7);
    this.row3.add(this.gp8);
    this.row3.add(this.gp9);
    this.manualBoard.add(row1);
    this.manualBoard.add(row2);
    this.manualBoard.add(row3);
    
    leaTest = new LightEmAll(3, 3, 0, 0);
  }
  
  //Tests for addNeighbor in GamePiece class:
  void testAddNeighbor(Tester t) {
    this.initConditions();
    GamePiece gpExample = new GamePiece(3, 3);
    t.checkExpect(gpExample.neighbors, new ArrayList<GamePiece>());
    gpExample.addNeighbor(this.gp1);
    t.checkExpect(gpExample.neighbors, new ArrayList<GamePiece>(
        Arrays.asList(this.gp1)));
    t.checkExpect(this.gp1.neighbors, new ArrayList<GamePiece>(
        Arrays.asList(this.gp2, this.gp4, gpExample)));
  }
  
  //Tests for connectIfNEighbor in GamePiece class:
  void testConnectIfNeighbor(Tester t) {
    this.initConditions();
    //Test if not neighbors at all:
    this.gp6.connectIfNeighborOf(this.gp7);
    t.checkExpect(this.gp6.wireNeighborsWith(this.gp7), false);
    //Test functionality if they are neighbors:
    this.gp6.connectIfNeighborOf(this.gp9);
    t.checkExpect(this.gp6.wireNeighborsWith(this.gp9), true);
    
  }
  
  //Tests for rotateRandom (the orientation field is random, here it is given)
  void testRotateRandom(Tester t) {
    this.initConditions();
    this.gp1.orientation = 1;
    t.checkExpect(this.gp1.left, false);
    this.gp1.rotateRandom();
    t.checkExpect(this.gp1.left, true);
    //four rotations bring the piece back to the same place:
    this.gp2.orientation = 4;
    t.checkExpect(this.gp2.left, true);
    this.gp2.rotateRandom();
    t.checkExpect(this.gp1.left, true);
  }
  
  //Tests for wireNeighborsWith in GamePiece class
  void testWireNeighborsWith(Tester t) {
    this.initConditions();
    //If called on itself:
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp1), false);
    //If not neighbors at all:
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp6), false);
    //And testing that the game works as expected:
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp2), true);
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp3), false);
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp4), true);
    t.checkExpect(this.gp2.wireNeighborsWith(this.gp1), true);
    t.checkExpect(this.gp2.wireNeighborsWith(this.gp3), false);
    t.checkExpect(this.gp3.wireNeighborsWith(this.gp2), false);
    t.checkExpect(this.gp4.wireNeighborsWith(this.gp1), true);
    t.checkExpect(this.gp4.wireNeighborsWith(this.gp7), true);
    t.checkExpect(this.gp7.wireNeighborsWith(this.gp8), true);

  }
  
  //Test for lightEmUp and loseLight in GamePiece class
  void testLightEmUp(Tester t) {
    this.initConditions();
    this.gp1.powerStation = true;
    this.gp1.lit = true;
    this.gp1.lightEmUp(4);
    t.checkExpect(this.gp1.lit, true);
    t.checkExpect(this.gp2.lit, true);
    t.checkExpect(this.gp3.lit, false);
    t.checkExpect(this.gp4.lit, true);
    this.gp1.rotateClockwise();
    //Mimick the "refresh" method:
    this.gp2.loseLight();
    this.gp3.loseLight();
    this.gp4.loseLight();
    //and recall the lightOrNot method:
    this.gp1.lightEmUp(4);
    t.checkExpect(this.gp1.lit, true);
    t.checkExpect(this.gp2.lit, false);
    t.checkExpect(this.gp3.lit, false);
    t.checkExpect(this.gp4.lit, true);
  }
  
  //Tests for getDiameter in GamePiece class
  void testGetDiameter(Tester t) {
    this.initConditions();
    //get diameter from a piece that is not connected to anything:
    t.checkExpect(this.gp3.getDiameter(), 0);
    //get diameter from a piece that is connected to some pieces:
    this.gp1.powerStation = true;
    t.checkExpect(this.gp1.getDiameter(), 5);
  }
  
  //Tests for farthestFrom in GamePiece class
  void testFarthestFrom(Tester t) {
    this.initConditions();
    //Create a link of connections that span around the gameboard in a U shape:
    this.gp3.left = true;
    this.gp7.top = true;
    this.gp7.right = true;
    this.gp8.left = true;
    this.gp8.right = true;
    this.gp9.left = true;
    t.checkExpect(this.gp9.farthestFrom(), this.gp3);
    //gp6 is not connected to anything on the manual board:
    t.checkExpect(this.gp6.farthestFrom(), this.gp6);
  }
  
  //Tests for draw in GamePiece class
  //***Tests extra credit for changing colors as distance from power station increases
  void testDraw(Tester t) {
    this.initConditions();
    GamePiece exampleLeft = new GamePiece(0, 0, true, false, false, false);
    GamePiece exampleTopAndRight = new GamePiece(0, 0, false, true, true, false);
    WorldImage backgroundPiece = new OverlayImage(
        new RectangleImage(18, 18, OutlineMode.SOLID, Color.DARK_GRAY), 
        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLACK));
    WorldImage horizontalG = new LineImage(new Posn(10, 0), Color.LIGHT_GRAY);
    WorldImage verticalG = new LineImage(new Posn(0, 10), Color.LIGHT_GRAY);
    WorldImage horizontalY = new LineImage(new Posn(10, 0), Color.YELLOW);
    WorldImage verticalY = new LineImage(new Posn(0, 10), Color.YELLOW);
    WorldImage horizontalO = new LineImage(new Posn(10, 0), Color.ORANGE);
    WorldImage verticalO = new LineImage(new Posn(0, 10), Color.ORANGE);
    WorldImage horizontalP = new LineImage(new Posn(10, 0), Color.PINK);
    WorldImage verticalP = new LineImage(new Posn(0, 10), Color.PINK);
    //GRAY LINES
    t.checkExpect(exampleLeft.draw(20, 20, 5),
        new OverlayOffsetImage(horizontalG, 5.0, 0.0, backgroundPiece));
    WorldImage bckgTop = new OverlayOffsetImage(horizontalG, -5.0, 0.0, backgroundPiece);
    t.checkExpect(exampleTopAndRight.draw(20, 20, 5),
        new OverlayOffsetImage(verticalG, 0.0, 5.0, bckgTop));
    //YELLOW LINES
    exampleLeft.lit = true;
    exampleLeft.powerVal = 1;
    exampleTopAndRight.lit = true;
    exampleTopAndRight.powerVal = 1;
    t.checkExpect(exampleLeft.draw(20, 20, 15),
        new OverlayOffsetImage(horizontalY, 5.0, 0.0, backgroundPiece));
    WorldImage bckgTopY = new OverlayOffsetImage(horizontalY, -5.0, 0.0, backgroundPiece);
    t.checkExpect(exampleTopAndRight.draw(20, 20, 15),
        new OverlayOffsetImage(verticalY, 0.0, 5.0, bckgTopY));
    //ORANGE LINES - powerVal is greater than radius - 4 (here: 15 - 4)
    exampleLeft.powerVal = 13;
    exampleTopAndRight.powerVal = 13;
    t.checkExpect(exampleLeft.draw(20, 20, 15),
        new OverlayOffsetImage(horizontalO, 5.0, 0.0, backgroundPiece));
    WorldImage bckgTopO = new OverlayOffsetImage(horizontalO, -5.0, 0.0, backgroundPiece);
    t.checkExpect(exampleTopAndRight.draw(20, 20, 15),
        new OverlayOffsetImage(verticalO, 0.0, 5.0, bckgTopO));
    //PINK LINES - powerVal is greater than radius - 1 (here: 15 - 1)
    exampleLeft.powerVal = 14;
    exampleTopAndRight.powerVal = 14;
    t.checkExpect(exampleLeft.draw(20, 20, 15),
        new OverlayOffsetImage(horizontalP, 5.0, 0.0, backgroundPiece));
    WorldImage bckgTopP = new OverlayOffsetImage(horizontalP, -5.0, 0.0, backgroundPiece);
    t.checkExpect(exampleTopAndRight.draw(20, 20, 15),
        new OverlayOffsetImage(verticalP, 0.0, 5.0, bckgTopP));
  }
  
  //Tests for rotateClockwise in GamePiece class
  void testRotateClockwise(Tester t) {
    this.initConditions();
    t.checkExpect(this.gp1.left, false);
    t.checkExpect(this.gp1.right, true);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.bottom, true);
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp2), true);
    this.gp1.rotateClockwise();
    t.checkExpect(this.gp1.left, true);
    t.checkExpect(this.gp1.right, false);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.bottom, true);
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp2), false);
  }
  
  //Tests for givePowerTo in GamePiece class
  void testGivePowerTo(Tester t) {
    this.initConditions();
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp2.powerStation, false);
    this.gp1.powerStation = true;
    t.checkExpect(this.gp1.powerStation, true);
    t.checkExpect(this.gp2.powerStation, false);
    this.gp1.givePowerStationTo(this.gp2);
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp2.powerStation, true);
    //If the user tries to move the power station out of bounds of the game board,
    //it doesn't actually move:
    this.gp2.givePowerStationTo(this.gp2);
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp2.powerStation, true);
  }
  
  //TESTS FOR LIGHTEMALL CLASS//////////
  //Tests for constructor Exception in LightEmAll class
  void testPowerStationOutOfBounds(Tester t) {
    this.initConditions();
    t.checkConstructorException(new IllegalArgumentException(
        "Column does not exist on this board."),
        "LightEmAll", 3, 3, 4, 2);
    t.checkConstructorException(new IllegalArgumentException(
        "Row does not exist on this board."),
        "LightEmAll", 3, 3, 2, 4);
  }
  
  //The tester constructor in the LightEmAll class does NOT call
  // - populateNeighbors(), connectTheWires(), or randomRotateAll()
  // so that we can test the properties BEFORE AND after the calling
  // of these methods. This explains why in some tests populateNeighbors()
  // is called, even though it is not relevant to that particular test.
  // It is just setting up the board as needed to test further properties.
  
  //Test for makeBoard in LightEmAll class
  void testMakeBoard(Tester t) {
    this.initConditions();
    //Test random properties of the board to make sure the .get(index).get(index) technique works
    t.checkExpect(this.leaTest.board.get(0).get(0).powerStation, true);
    t.checkExpect(this.leaTest.board.get(0).get(1).powerStation, false);
    t.checkExpect(this.leaTest.board.get(2).get(2).lit, false);
  }
  
  //Test for populate neighbors in LightEmAll class
  void testPopulateNeighbors(Tester t) {
    this.initConditions();
    ArrayList<ArrayList<GamePiece>> board = this.leaTest.board;
    t.checkExpect(board.get(0).get(0).neighbors.contains(board.get(0).get(1)),
        false);
    this.leaTest.populateNeighbors();
    t.checkExpect(board.get(0).get(0).neighbors.contains(board.get(0).get(1)),
        true);
    t.checkExpect(board.get(2).get(1).neighbors.contains(board.get(0).get(1)),
        false);
  }
  
  //Test for getNodes in LightEmAll class
  void testGetNodes(Tester t) {
    this.initConditions();
    ArrayList<ArrayList<GamePiece>> board = this.leaTest.board;
    t.checkExpect(this.leaTest.nodes.get(0), board.get(0).get(0));
    t.checkExpect(this.leaTest.nodes.get(2), board.get(0).get(2));
    t.checkExpect(this.leaTest.nodes.get(8), board.get(2).get(2));
  }
  
  //Test for getEdges in LightEmAll class
  void testGetEdges(Tester t) {
    this.initConditions();
    this.leaTest.populateNeighbors();
    ArrayList<GamePiece> board = this.leaTest.nodes;
    ArrayList<Edge<GamePiece>> testEdges = this.leaTest.getEdges(
        this.leaTest.nodes.size(), this.leaTest.nodes.size());
    t.checkExpect(testEdges.get(0).fromNode, board.get(0));
    t.checkExpect(testEdges.get(0).toNode, board.get(1));
    t.checkExpect(testEdges.get(1).fromNode, board.get(0));
    t.checkExpect(testEdges.get(1).toNode, board.get(3));
  }
  
  //Test for makeHashMapOfReps in LightEmAll class
  void testMakeHMofReps(Tester t) {
    this.initConditions();
    this.leaTest.populateNeighbors();
    HashMap<Integer, Integer> reps = this.leaTest.makeHashMapOfReps();
    //When the hashmap is made, all of its keys refer to their own values:
    t.checkExpect(reps.get(0), 0);
    t.checkExpect(reps.get(1), 1);
    //does not exist on this HashMap:
    t.checkExpect(reps.get(10), null);
  }
  
  //Test for find in LightEmAll class
  void testFind(Tester t) {
    this.initConditions();
    this.leaTest.populateNeighbors();
    HashMap<Integer, Integer> reps = this.leaTest.makeHashMapOfReps();
    t.checkExpect(this.leaTest.find(reps, this.gp1), 0);
    t.checkExpect(this.leaTest.find(reps, this.gp3), 2);
    reps.put(2, 0);
    t.checkExpect(this.leaTest.find(reps, this.gp3), 0);
  }
  
  //Test for createMST in LightEmAllclass
  void testCreateMST(Tester t) {
    this.initConditions();
    //Creates a simple example of a 2x2 board
    ArrayList<Edge<GamePiece>> nonRandomEdges = new ArrayList<Edge<GamePiece>>();
    Edge<GamePiece> e1 = new Edge<GamePiece>(this.gp1, this.gp2);
    Edge<GamePiece> e2 = new Edge<GamePiece>(this.gp1, this.gp4);
    Edge<GamePiece> e3 = new Edge<GamePiece>(this.gp2, this.gp3);
    Edge<GamePiece> e4 = new Edge<GamePiece>(this.gp2, this.gp5);
    Edge<GamePiece> e5 = new Edge<GamePiece>(this.gp3, this.gp6);
    Edge<GamePiece> e6 = new Edge<GamePiece>(this.gp4, this.gp7);
    Edge<GamePiece> e7 = new Edge<GamePiece>(this.gp4, this.gp5);
    Edge<GamePiece> e8 = new Edge<GamePiece>(this.gp5, this.gp8);
    Edge<GamePiece> e9 = new Edge<GamePiece>(this.gp5, this.gp6);
    Edge<GamePiece> e10 = new Edge<GamePiece>(this.gp6, this.gp9);
    Edge<GamePiece> e11 = new Edge<GamePiece>(this.gp7, this.gp8);
    Edge<GamePiece> e12 = new Edge<GamePiece>(this.gp8, this.gp9);
    nonRandomEdges.add(e1);
    nonRandomEdges.add(e2);
    nonRandomEdges.add(e3);
    nonRandomEdges.add(e4);
    nonRandomEdges.add(e5);
    nonRandomEdges.add(e6);
    nonRandomEdges.add(e7);
    nonRandomEdges.add(e8);
    nonRandomEdges.add(e9);
    nonRandomEdges.add(e10);
    nonRandomEdges.add(e11);
    nonRandomEdges.add(e12);
    this.leaTest.mst = this.leaTest.createMST(nonRandomEdges);
    int sum = 0;
    for (int i = 0; i < this.leaTest.mst.size(); i++) {
      sum = sum + this.leaTest.mst.get(i).weight;
    }
    //In the tester constructor of Edges, each weight is assigned 1,
    //therefore, the sum of all weight will always equal the minimum
    //number of edges needed to connect all pieces: total number of nodes - 1
    t.checkExpect(sum, 8);
  }
  
  //Test for calculateRadius in LightEmAll class
  void testCalcRad(Tester t) {
    this.initConditions();
    this.gp1.powerStation = true;
    this.leaTest.board = this.manualBoard;
    t.checkExpect(this.leaTest.calculateRadius(), 3);
  }
  
  //Test for connectTheWires in LightEmAll class
  void testConnectTheWires(Tester t) {
    this.initConditions();
    this.leaTest.mst = new ArrayList<Edge<GamePiece>>();
    this.leaTest.mst.add(new Edge<GamePiece>(this.gp5, this.gp6, 10));
    t.checkExpect(this.gp5.right, false);
    this.leaTest.connectTheWires();
    t.checkExpect(this.gp5.right, true);
  }
  
  //Test for randomRotateAll in LightEmAll class
  void testRandomRotateAll(Tester t) {
    this.initConditions();
    this.leaTest.nodes = new ArrayList<GamePiece>();
    this.gp1.orientation = 1;
    this.leaTest.nodes.add(this.gp1);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.left, false);
    this.leaTest.randomRotateAll();
    t.checkExpect(this.gp1.top, true);
    t.checkExpect(this.gp1.left, true);
  }
  
  //Test for write game stats in LightEmAll class
  void testWriteGameStats(Tester t) {
    this.initConditions();
    this.leaTest.time = 5;
    this.leaTest.board = this.manualBoard;
    this.leaTest.nodes = this.leaTest.getNodes();
    this.gp1.powerStation = true;
    this.gp1.lit = true;
    this.leaTest.refresh();
    //Initialize the rectangle where the info will be placed:
    WorldImage bckgr = new RectangleImage(300, 700, OutlineMode.OUTLINE,
        Color.BLACK);
    //Creates the Title & Instructions:
    WorldImage pwr = new OverlayImage(
        new CircleImage(8, OutlineMode.SOLID, Color.YELLOW),
        new StarImage(15, 7, OutlineMode.SOLID, Color.RED));
    WorldImage title = new TextImage("LIGHT 'EM ALL", 20, FontStyle.ITALIC, Color.BLACK);
    WorldImage titleImage = new AboveImage(
        new BesideImage(pwr, title, pwr),
        new TextImage("To Win: Connect all the wires and move", 14, FontStyle.ITALIC, Color.BLACK),
        new TextImage("the power station to light the board", 14, FontStyle.ITALIC, Color.BLACK));
    //Keeps track of time:
    WorldImage time = new TextImage("TIME: " + Integer.toString(5), 14, FontStyle.REGULAR,
        Color.BLACK);
    //Keeps track of score:
    WorldImage score = new AboveImage(
        new TextImage("You have connected ", 16, FontStyle.BOLD, Color.BLACK),
        new TextImage(Integer.toString(3) + " / "
        + Integer.toString(9) + " squares", 16, FontStyle.BOLD, 
        Color.BLACK),
        new TextImage("Connect " 
            + Integer.toString(6)
            + " more to win!", 16, FontStyle.REGULAR, 
            Color.BLACK));
    WorldImage instructions = new AboveImage(
        new TextImage("(or press 'g' to give up on this game,", 12, FontStyle.REGULAR, Color.GRAY),
        new TextImage("and 'r' to restart with a new game)", 12, FontStyle.REGULAR, Color.GRAY));
    //Write the extra credit instructions:
    WorldImage extraInfo = new AboveImage(
        new TextImage("Press 'h' at any time to generate", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("a horizontally-bias game.", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("Press 'v' at any time to generate", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("a vertically-bias game.", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("Press 'x' to open a new canvas to see", 12, FontStyle.REGULAR, 
            Color.BLACK),
        new TextImage("our attemp at a hexagonal game.", 12, FontStyle.REGULAR, 
            Color.BLACK));
    //Align all the info into the background image:
    WorldImage placeTitle = new OverlayImage(titleImage.movePinholeTo(new Posn(0, 250)), bckgr);
    WorldImage placeTime = new OverlayImage(time.movePinholeTo(new Posn(0, 110)), placeTitle);
    WorldImage placeScore = new OverlayImage(score.movePinholeTo(new Posn(0, 50)), placeTime);
    WorldImage placeExtraCreditInfo = new OverlayImage(extraInfo.movePinholeTo(new Posn(0, -240)), 
        placeScore);
    WorldImage placeInstructions = new OverlayImage(instructions.movePinholeTo(new Posn(0, -300)), 
        placeExtraCreditInfo);
    t.checkExpect(this.leaTest.writeGameStats(), placeInstructions);
  }
  
  //Test for keepScore in LightEmAll class
  void testKeepScore(Tester t) {
    this.initConditions();
    //When the power station is the only lit piece at start of game:
    t.checkExpect(this.leaTest.keepScore(), 1);
    //When more pieces become lit:
    this.leaTest.board.get(1).get(0).lit = true;
    t.checkExpect(this.leaTest.keepScore(), 2);
    this.leaTest.board.get(2).get(0).lit = true;
    t.checkExpect(this.leaTest.keepScore(), 3);
  }
  
  //Test for time keeper:
  void testOnTick(Tester t) {
    //test the time ticking:
    this.leaTest.time = 0;
    this.leaTest.onTick();
    this.leaTest.time = 1;
    this.leaTest.onTick();
    this.leaTest.time = 2;
    //test that time tick stops in between games
    this.leaTest.gameWon = true;
    this.leaTest.onTick();
    this.leaTest.time = 2;
    //and that time is reset when the r button is pressed
    this.leaTest.onKeyEvent("r");
    this.leaTest.time = 0;
  }
  
  //Tests for On-Mouse Events:
  void testOnMouse(Tester t) {
    this.initConditions();
    this.leaTest.board = this.manualBoard;
    this.leaTest.refresh();
    t.checkExpect(this.gp3.wireNeighborsWith(this.gp2), false);
    this.leaTest.onMouseClicked(new Posn(550, 100));
    t.checkExpect(this.gp3.wireNeighborsWith(this.gp2), true);
  }
  
  //Tests for On-Key Events
  void testOnKey(Tester t) {
    this.initConditions();
    this.leaTest.board = this.manualBoard;
    this.gp1.powerStation = true;
    //Doesnt move the power station because it is at 0,0:
    this.leaTest.onKeyEvent("up");
    t.checkExpect(this.leaTest.powerRow, 0);
    //Moves the power station:
    this.leaTest.onKeyEvent("down");
    t.checkExpect(this.leaTest.powerRow, 1);
    //Test the "give-up" functionality
    this.gp2.rotateClockwise();
    t.checkExpect(this.gp2.wireNeighborsWith(this.gp2), false);
    this.leaTest.onKeyEvent("g");
    t.checkExpect(this.gp1.wireNeighborsWith(this.gp2), true);
    //Test the press r to reset functionality
    t.checkExpect(this.gp4.powerStation, true);
    this.leaTest.onKeyEvent("r");
    t.checkExpect(this.leaTest.powerCol, 0);
    t.checkExpect(this.leaTest.powerRow, 0);
    //Test the press h to get a horizontally bias board
    this.leaTest.onKeyEvent("h");
    //Because this feature is a visual addition to the board,
    //and dependent on randoms, the testing of createMST() method
    //above suffices to test the functionality of creating an MST,
    //while the visual testing of pressing v or h and then pressing
    //"g" (which connects all the wires) shows the bias in one direction 
    //or another. What we can test, is that the game has been reset:
    t.checkExpect(this.leaTest.powerCol, 0);
    t.checkExpect(this.leaTest.powerRow, 0);
    //Test the press v to get a vertically bias board
    this.leaTest.onKeyEvent("v");
    t.checkExpect(this.leaTest.powerCol, 0);
    t.checkExpect(this.leaTest.powerRow, 0);
  }
  
  //Tests for refresh
  void testRefresh(Tester t) {
    this.initConditions();
    this.leaTest.board = this.manualBoard;
    this.gp1.powerStation = true;
    this.gp1.lit = true;
    t.checkExpect(this.gp2.lit, false);
    this.leaTest.refresh();
    t.checkExpect(this.gp2.lit, true);
  }
  
  //TESTS FOR ARRAYUTILS CLASS//////////
  //Tests for Swap in ArrayUtils
  void testSwap(Tester t) {
    String example = "dog";
    String example2 = "cat";
    ArrayList<String> before = new ArrayList<String>(Arrays.asList(example, example2));
    ArrayList<String> after = new ArrayList<String>(Arrays.asList(example2, example));
    this.au.swap(before, 0, 1);
    t.checkExpect(before, after);
  }
  
  //Tests for downheap in ArrayUtils
  void testDownHeap(Tester t) {
    ArrayList<Integer> unsorted = new ArrayList<Integer>(Arrays.asList(3, 5, 10, 7, 6, 5));
    //Downheaping 10 at index 2 does nothing:
    this.au.downheap(unsorted, 2, new CompareIntegers());
    t.checkExpect(unsorted, new ArrayList<Integer>(Arrays.asList(3, 5, 10, 7, 6, 5)));
    //Downheaping 5 at index 1 returns...
    this.au.downheap(unsorted, 1, new CompareIntegers());
    t.checkExpect(unsorted, new ArrayList<Integer>(Arrays.asList(3, 7, 10, 5, 6, 5)));
    //Downheaping 3 at index 0 returns...
    this.au.downheap(unsorted, 0, new CompareIntegers());
    t.checkExpect(unsorted, new ArrayList<Integer>(Arrays.asList(10, 7, 5, 5, 6, 3)));
  }
  
  //Tests for heapsort in ArrayUtils
  void testHeap(Tester t) {
    ArrayList<Integer> unsorted = new ArrayList<Integer>(Arrays.asList(3, 5, 10, 7, 6, 5));
    ArrayList<Integer> sorted = new ArrayList<Integer>(Arrays.asList(3, 5, 5, 6, 7, 10));
    t.checkExpect(this.au.heapsort(unsorted, new CompareIntegers()), sorted);
  }
  
  /////////////// EXAMPLES CLASS ONLY FOR HEXAGONS//////////////
  // Contains only tests for method that have been changed in order to
  // implement the hexagon game grid
  // Ones that have been reused have already been tested in the regular
  // examples class.

  // Example of a hexagonal game that opens a canvas:
  // LightEmAllHex hexample = new LightEmAllHex(8, 8);

  // Examples of GamePieces:
  GamePieceHex hex1;
  GamePieceHex hex2;
  GamePieceHex hex3;
  GamePieceHex hex4;
  GamePieceHex hex5;
  GamePieceHex hex6;
  GamePieceHex hex7;
  GamePieceHex hex8;
  GamePieceHex hex9;

  // Examples of a manually-generated, 3x3 board
  ArrayList<ArrayList<GamePieceHex>> manualHexBoard;
  ArrayList<GamePieceHex> hexrow1;
  ArrayList<GamePieceHex> hexrow2;
  ArrayList<GamePieceHex> hexrow3;

  // TESTER GAME EXAMPLE:
  // Does not open a canvas when called, but allows you to test properties of the
  // board:
  LightEmAllHex leaTestHex;

  // Initial game Conditions of the manually generated game:
  // Some game pieces are left without any wires in order to easily modify and
  // test different variations of connectivity
  void initConditionsHEX() {
    this.hex1 = new GamePieceHex(0, 0, false, true, false, true, false, true);
    this.hex2 = new GamePieceHex(1, 0, true, true, true, true, false, true);
    this.hex3 = new GamePieceHex(2, 0, true, false, false, true, false, false);
    this.hex4 = new GamePieceHex(0, 1, false, true, false, false, false, true);
    this.hex5 = new GamePieceHex(1, 1, false, false, false, false, false, false);
    this.hex6 = new GamePieceHex(2, 1, false, false, false, false, false, false);
    this.hex7 = new GamePieceHex(0, 2, true, true, true, false, false, false);
    this.hex8 = new GamePieceHex(1, 2, true, true, false, false, false, false);
    this.hex9 = new GamePieceHex(2, 2, true, false, false, false, false, false);

    // Add more neighbors with the hexagon grid:
    this.hex1.addNeighbor(this.hex2);
    this.hex1.addNeighbor(this.hex4);
    this.hex1.addNeighbor(this.hex5);
    this.hex2.addNeighbor(this.hex5);
    this.hex2.addNeighbor(this.hex6);
    this.hex2.addNeighbor(this.hex3);
    this.hex3.addNeighbor(this.hex6);
    this.hex4.addNeighbor(this.hex5);
    this.hex4.addNeighbor(this.hex7);
    this.hex5.addNeighbor(this.hex6);
    this.hex5.addNeighbor(this.hex7);
    this.hex5.addNeighbor(this.hex8);
    this.hex6.addNeighbor(this.hex8);
    this.hex6.addNeighbor(this.hex9);
    this.hex7.addNeighbor(this.hex8);
    this.hex8.addNeighbor(this.hex9);

    this.manualHexBoard = new ArrayList<ArrayList<GamePieceHex>>();
    this.hexrow1 = new ArrayList<GamePieceHex>();
    this.hexrow1.add(this.hex1);
    this.hexrow1.add(this.hex2);
    this.hexrow1.add(this.hex3);
    this.hexrow2 = new ArrayList<GamePieceHex>();
    this.hexrow2.add(this.hex4);
    this.hexrow2.add(this.hex5);
    this.hexrow2.add(this.hex6);
    this.hexrow3 = new ArrayList<GamePieceHex>();
    this.hexrow3.add(this.hex7);
    this.hexrow3.add(this.hex8);
    this.hexrow3.add(this.hex9);
    this.manualHexBoard.add(hexrow1);
    this.manualHexBoard.add(hexrow2);
    this.manualHexBoard.add(hexrow3);

    this.leaTestHex = new LightEmAllHex(3, 3, 0, 0);
  }
  
  //Tests for connectIfNEighbor in GamePiece class:
  void testConnectIfNeighborHEX(Tester t) {
    this.initConditionsHEX();
    //Test if not neighbors at all:
    this.hex6.connectIfNeighborOf(this.hex7);
    t.checkExpect(this.hex6.wireNeighborsWith(this.hex7), false);
    //Test functionality if they are neighbors:
    this.hex6.connectIfNeighborOf(this.hex9);
    t.checkExpect(this.hex6.wireNeighborsWith(this.hex9), true);
    //Test a bottom-right neighbor to test new hexagon abilities
    this.hex5.connectIfNeighborOf(this.hex7);
    t.checkExpect(this.hex5.wireNeighborsWith(this.hex7), true);
  }
  
  //Tests for rotateRandom (the orientation field is random, here it is given)
  void testRotateRandomHEX(Tester t) {
    this.initConditionsHEX();
    this.hex1.orientation = 1;
    t.checkExpect(this.hex1.tr, true);
    this.hex1.rotateRandom();
    t.checkExpect(this.hex1.right, false);
    //Before, 4 rotations brough it back to the same place. Now, it does not:
    this.hex9.orientation = 4;
    t.checkExpect(this.hex9.tl, true);
    this.hex2.rotateRandom();
    t.checkExpect(this.hex1.tl, false);
  }
  
  //Tests for wireNeighborsWith in GamePiece class
  void testWireNeighborsWithHEX(Tester t) {
    this.initConditionsHEX();
    //If called on itself:
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex1), false);
    //If not neighbors at all:
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex6), false);
    //And testing that the game works as expected:
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex2), true);
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex3), false);
    t.checkExpect(this.hex2.wireNeighborsWith(this.hex1), true);
    t.checkExpect(this.hex2.wireNeighborsWith(this.hex3), false);
  }
  
  //Tests for rotateClockwise in GamePiece class
  void testRotateClockwiseHEX(Tester t) {
    this.initConditionsHEX();
    t.checkExpect(this.hex1.left, false);
    t.checkExpect(this.hex1.right, true);
    t.checkExpect(this.hex1.tl, false);
    t.checkExpect(this.hex1.tr, true);
    t.checkExpect(this.hex1.bl, false);
    t.checkExpect(this.hex1.br, true);
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex4), false);
    this.hex1.rotateClockwise();
    t.checkExpect(this.hex1.left, false);
    t.checkExpect(this.hex1.right, true);
    t.checkExpect(this.hex1.tl, false);
    t.checkExpect(this.hex1.tr, false);
    t.checkExpect(this.hex1.bl, true);
    t.checkExpect(this.hex1.br, true);
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex2), true);
  }
  
  //TESTS FOR LIGHTEMALLHEX CLASS//////////
  //Test for makeBoard in LightEmAll class
  void testMakeBoardHEX(Tester t) {
    this.initConditionsHEX();
    //Test random properties of the board to make sure the .get(index).get(index) technique works
    this.hex1.powerStation = true;
    t.checkExpect(this.leaTestHex.board.get(0).get(0).powerStation, true);
    t.checkExpect(this.leaTestHex.board.get(0).get(1).powerStation, false);
    t.checkExpect(this.leaTestHex.board.get(2).get(2).lit, false);
  }
  
  //Test for populate neighbors in LightEmAll class
  void testPopulateNeighborsHEX(Tester t) {
    this.initConditionsHEX();
    this.leaTestHex.populateNeighbors();
    ArrayList<ArrayList<GamePieceHex>> board = this.leaTestHex.board;
    t.checkExpect(board.get(0).get(0).neighbors.contains(board.get(0).get(1)),
        true);
    t.checkExpect(board.get(0).get(0).neighbors.contains(board.get(2).get(1)),
        false);
    //The center piece on a 3x3 board hassix neighbors
    t.checkExpect(board.get(1).get(1).neighbors.size(), 6);
  }
  
  //Test for getEdges in LightEmAll class
  void testGetEdgesHEX(Tester t) {
    this.initConditionsHEX();
    this.leaTestHex.populateNeighbors();
    ArrayList<GamePieceHex> nodes = this.leaTestHex.getNodes();
    ArrayList<Edge<GamePieceHex>> testEdges = this.leaTestHex.getEdges();
    t.checkExpect(testEdges.get(0).fromNode, nodes.get(0));
    t.checkExpect(testEdges.get(0).toNode, nodes.get(1));
    t.checkExpect(testEdges.get(1).fromNode, nodes.get(0));
    t.checkExpect(testEdges.get(1).toNode, nodes.get(3));
  }
  
  //Test for createMST in LightEmAllclass
  void testCreateMSTHEX(Tester t) {
    this.initConditionsHEX();
    //Creates a simple example of a 2x2 board
    ArrayList<Edge<GamePieceHex>> nonRandomEdges = new ArrayList<Edge<GamePieceHex>>();
    Edge<GamePieceHex> e1 = new Edge<GamePieceHex>(this.hex1, this.hex2);
    Edge<GamePieceHex> e2 = new Edge<GamePieceHex>(this.hex1, this.hex4);
    Edge<GamePieceHex> e3 = new Edge<GamePieceHex>(this.hex2, this.hex3);
    Edge<GamePieceHex> e4 = new Edge<GamePieceHex>(this.hex2, this.hex5);
    Edge<GamePieceHex> e5 = new Edge<GamePieceHex>(this.hex3, this.hex6);
    Edge<GamePieceHex> e6 = new Edge<GamePieceHex>(this.hex4, this.hex7);
    Edge<GamePieceHex> e7 = new Edge<GamePieceHex>(this.hex4, this.hex5);
    Edge<GamePieceHex> e8 = new Edge<GamePieceHex>(this.hex5, this.hex8);
    Edge<GamePieceHex> e9 = new Edge<GamePieceHex>(this.hex5, this.hex6);
    Edge<GamePieceHex> e10 = new Edge<GamePieceHex>(this.hex6, this.hex9);
    Edge<GamePieceHex> e11 = new Edge<GamePieceHex>(this.hex7, this.hex8);
    Edge<GamePieceHex> e12 = new Edge<GamePieceHex>(this.hex8, this.hex9);
    nonRandomEdges.add(e1);
    nonRandomEdges.add(e2);
    nonRandomEdges.add(e3);
    nonRandomEdges.add(e4);
    nonRandomEdges.add(e5);
    nonRandomEdges.add(e6);
    nonRandomEdges.add(e7);
    nonRandomEdges.add(e8);
    nonRandomEdges.add(e9);
    nonRandomEdges.add(e10);
    nonRandomEdges.add(e11);
    nonRandomEdges.add(e12);
    this.leaTestHex.mst = this.leaTestHex.createMST(nonRandomEdges);
    int sum = 0;
    for (int i = 0; i < this.leaTestHex.mst.size(); i++) {
      sum = sum + this.leaTestHex.mst.get(i).weight;
    }
    //In the tester constructor of Edges, each weight is assigned 1,
    //therefore, the sum of all weight will always equal the minimum
    //number of edges needed to connect all pieces: total number of nodes - 1
    t.checkExpect(sum, 8);
  }
  
  //Test for randomRotateAll in LightEmAll class
  void testRandomRotateAllHEX(Tester t) {
    this.initConditionsHEX();
    this.leaTestHex.nodes = new ArrayList<GamePieceHex>();
    this.hex1.orientation = 1;
    this.leaTestHex.nodes.add(this.hex1);
    t.checkExpect(this.hex1.tr, true);
    t.checkExpect(this.hex1.tl, false);
    this.leaTestHex.randomRotateAll();
    t.checkExpect(this.hex1.tr, false);
  }
  
  //Tests for On-Key Events
  void testOnKeyHEX(Tester t) {
    this.initConditionsHEX();
    this.leaTestHex.board = this.manualHexBoard;
    this.hex1.powerStation = true;
    //Doesnt move the power station because it is at 0,0:
    this.leaTestHex.onKeyEvent("a");
    t.checkExpect(this.leaTestHex.powerRow, 0);
    //Moves the power station:
    this.leaTestHex.onKeyEvent("d");
    t.checkExpect(this.leaTestHex.powerCol, 1);
    this.leaTestHex.onKeyEvent("x");
    t.checkExpect(this.leaTestHex.powerCol, 1);
    //Test the "give-up" functionality
    //this.gp2.rotateClockwise();
    //t.checkExpect(this.gp2.wireNeighborsWith(this.gp1), false);
    //this.leaTestHex.onKeyEvent("g");
    t.checkExpect(this.hex1.wireNeighborsWith(this.hex2), true);
    //Test the press r to reset functionality
    this.leaTestHex.onKeyEvent("r");
    t.checkExpect(this.leaTestHex.powerCol, 0);
    t.checkExpect(this.leaTestHex.powerRow, 0);
  }
}

