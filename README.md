# Untangle


An android app written in native Java. The goal is to turn all the red lines blue by dragging the white nodes around the screen until none of the lines cross. I am particularly proud of the puzzle generation algorithm: as this style of game was, as far as I know, unique at the time of development, I had to design my own algorithm to create complex but fair puzzles. It essentially works in five steps:

Place the nodes randomly. The option is given to input a seed before the game begins: using the same settings and seed guarantees the same result.

At the midpoint average between all of the generated nodes, run through each node, ordering them in a list sorted in a clockwise fashion from 0 degrees horizontal.

For each node on the list, add a line to the next node on the list. Add a line between the first and last nodes. This forms an irregular polygon.

Add lines across the polygon until no more uncrossed lines can be generated.

Scramble the positions of each node.



Builds

------

prealpha	core gameplay, bad puzzle gen

prealpha2	core gameplay, improved puzzle gen (sometimes generates unsolveable puzzles)

prealpha3	core gameplay, new puzzle gen algorithm generates consistently solvable puzzles.

prealpha4	core gameplay, improvements to new algorithm to increase visual appeal. Most core gameplay complete.

prealpha5	rudimentary menu.

prealpha6	timer, victory test, node scaling based on connection size

prealpha6b	bugfixes, disable touch input on victory screen.

prealpha7	graphic improvements, new background, glow effects.

prealpha7b	glow effects cause lag; disabled.

prealpha8	Added game control bar, snips.

Last Modified: 28/9/2016