# Advent of Code, Day 1: The Great Elf Flapjack Competition
# or, How I Learned to Curse
#  (https://docs.python.org/3/howto/curses.html)
#
# This is not a production-level script
# To run: put the AoC day 1 input in ../input/day01_input
# Note that this may create a logfile in /tmp/aoc.log
import os;
import time;
import curses;
import logging;

AOC_DAY=10
AOC_YEAR=2022
INPUTDIR="../input"
INPUTFILE = os.path.join(INPUTDIR, f"day{AOC_DAY:02d}_input")
FRAMEWAIT=80   # in ms
# for recording
#FRAME_WAIT=0.3
DOWN_FRAMES=7

logfile = '/tmp/aoc.log'
logger = logging.Logger(f'AOC{AOC_YEAR}d{AOC_DAY}')
logger.addHandler(logging.FileHandler(logfile))
logger.setLevel(logging.INFO)


# this will store the permanent screen characters
SCREENWIDTH = 40
display = [
        [' '] * SCREENWIDTH,
        [' '] * SCREENWIDTH,
        [' '] * SCREENWIDTH,
        [' '] * SCREENWIDTH,
        [' '] * SCREENWIDTH,
        [' '] * SCREENWIDTH,
]

SCREENHEIGHT = len(display)
lastsprite=1


def drawCRT(cycle, x, pad):
    global lastsprite
    global display

    # first draw the permanent display
    newchcolumn = (cycle - 1) % SCREENWIDTH
    newchrow = (cycle - 1) // SCREENWIDTH

    if(newchrow >= SCREENHEIGHT):
        # no more characters to compute. Turn all sprites off, ignore next
        # instructions.
        nextsprite = SCREENWIDTH + 3
        newchcolumn = nextsprite
    else:
        newch = '#' if abs(x - newchcolumn) < 2 else '.'
        display[newchrow][newchcolumn] = newch

        nextsprite = x

    # now undraw the last sprite
    if nextsprite != lastsprite:
        # clear last sprite from pad
        for column in range(lastsprite - 1, lastsprite + 2):
            if column < 0 or column >= SCREENWIDTH:
                continue
            for row in range(SCREENHEIGHT):
                ch = display[row][column]
                color_pair = curses.color_pair(1)
                if row == (SCREENHEIGHT - 1) and column == (SCREENWIDTH - 1):
                    pad.insch(row, column, ord(ch), curses.color_pair(1))
                else:
                    pad.addch(row, column, ord(ch), curses.color_pair(1))
        # draw new sprite onto pad
        for column in range(nextsprite - 1, nextsprite + 2):
            if column < 0 or column >= SCREENWIDTH:
                continue
            for row in range(SCREENHEIGHT):
                ch = display[row][column]
                color_pair = curses.color_pair(1)
                if row == (SCREENHEIGHT - 1) and column == (SCREENWIDTH - 1):
                    pad.insch(row, column, ord(ch), curses.color_pair(2))
                else:
                    pad.addch(row, column, ord(ch), curses.color_pair(2))
        lastsprite = nextsprite

    # draw the new character, if we haven't already
    if abs(newchcolumn - nextsprite) >= 2:
        if newchrow == (SCREENHEIGHT - 1) and newchcolumn == (SCREENWIDTH - 1):
            pad.insch(newchrow, newchcolumn, ord(newch), curses.color_pair(1))
        else:
            pad.addch(newchrow, newchcolumn, newch, curses.color_pair(1))


# functionized ugly stream of code, so we can run it inside the curses wrapper
def main(screen):
    # init some curses stuff
    curses.curs_set(0)
    curses.init_pair(1, curses.COLOR_GREEN, curses.COLOR_BLACK)
    curses.init_pair(2, curses.COLOR_BLACK, curses.COLOR_RED)
    screen.clear()

    # init curses pad to show display on screen
    crt = curses.newpad(SCREENHEIGHT,SCREENWIDTH)
    for column in range(SCREENWIDTH - 1):
        for row in range(SCREENHEIGHT - 1):
            crt.addch(row, column, ord(' '), curses.color_pair(1))

    x = 1
    cycle = 1
    addend = 0

    # place the CRT on the screen
    maxy, maxx = screen.getmaxyx()

    displayminx = (maxx - SCREENWIDTH)//2
    displayminy = (maxy - SCREENHEIGHT)//2
    displaymaxx = displayminx + SCREENWIDTH
    displaymaxy = displayminy + SCREENHEIGHT

    # run program
    with open(INPUTFILE, "r") as fh:
        for line in fh.readlines():
            line = line.strip()
            if line == "":
                continue
            # parse instruction
            instruction = line.split()
            if instruction[0] == "addx":
                drawCRT(cycle, x, crt)
                addend = int(instruction[1])
                cycle += 1
            else:
                addend = 0

            drawCRT(cycle, x, crt)
            cycle += 1
            x += addend

            # draw displayn
            crt.noutrefresh(0, 0,
                    displayminy, displayminx, displaymaxy, displaymaxx)
            curses.doupdate()
            curses.napms(FRAMEWAIT)

    cycle = SCREENWIDTH * SCREENHEIGHT + 1
    drawCRT(cycle, x, crt)
    crt.noutrefresh(0, 0,
            displayminy, displayminx, displaymaxy, displaymaxx)
    curses.doupdate()

    curses.napms(FRAMEWAIT * 100)

    #for row in range(len(display)):
    #    logger.info("".join(display[row]))


# run the main function inside the curses wrapper
curses.wrapper(main)
