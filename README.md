# FundiesII_LightEmAll
Fundies II (CS2510) Final Project, a game that taught us graph theory

Click on the tiles to rotate them until the entire wire grid is connected<br>
Use the arrow keys to move the weak power station over the wires until the entire grid is connected and lit<br>


Project highlights:
<ul>
<li>Identified the minimum spanning tree of the game's underlying graph using Kruskal’s algorithm to generate the continuous wire grid before randomly rotating tiles for gameplay</li>
<li>Calculated radius of power by finding the furthest distance between any two nodes and cutting that in half</li>
<li>Used breadth first search to traverse tiles stemming from power station and light only the tiles within the radius of power; Implemented gradient color to make closer nodes brighter than further nodes</li>
<li>Implemented the option to generate a biased graph toward more horizontal or vertical wires by increasing the weight of horizontal or vertical edges respectively</li>
<li>Implemented hexagonal version of the game where tiles are 6-sided</li>
</ul>

![GUIScreenshot](https://user-images.githubusercontent.com/46666676/129463625-50e55f9c-6cda-4285-810e-fa3dda380830.png)

