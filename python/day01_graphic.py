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

AOC_DAY=1
AOC_YEAR=2022
INPUTDIR="../input"
inputfile = os.path.join(INPUTDIR, f"day{AOC_DAY:02d}_input")
FRAME_WAIT=0.02
# for recording
#FRAME_WAIT=0.3
DOWN_FRAMES=7

logfile = '/tmp/aoc.log'
logger = logging.Logger(f'AOC{AOC_YEAR}d{AOC_DAY}')
logger.addHandler(logging.FileHandler(logfile))
logger.setLevel(logging.INFO)

elf_head_faceright_template = '''\|||/ __      \|||/
 | | /_ \__    | | 
 | |o  \  _)   | | 
 | |   (_/ >   | | 
  \ \   \__I  / /  '''
elf_head_faceleft_template = '''\|||/      __ \|||/
 | |    __/ _\ | | 
 | |   (_  /  o| | 
 | |   < \_)   | | 
  \ \  I__/   / /  '''
elf_body_template = '''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
        | |        
       / _ \       
      / / \ \      
   o_/ /   \ \_o   
   \__/     \__/   '''
elf_body_jump_templates = ['''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
        | |        
      _/ _ \       
  o  / _/ | |      
  |\/ /   | \_o    
  \__/     \__/    ''',
'''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
        | |        
  o   _/ _ \       
  |\_/ _/ | |      
  \___/   | \_o    
           \__/    ''',
'''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
        | |        
   o  _/ _ \       
   |\/ _/ | |      
   |__/  / /_o     
         \__/      ''',
'''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
   o    | |        
   |\__/ _ \       
   |____/ | |      
         / /_o     
         \__/      ''',
'''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
       _| |        
   o  / __ \       
   |\/ /  | |      
    \_/  / /_o     
         \__/      ''',
'''   \ \__/ \__/ /   
    \__/   \__/    
   .__/_____\__.   
   | Hello, my |   
   |  name is  |   
   |{:^11}|   
   |___________|   
      __| |        
     /  __ \       
   o | / / /       
   |\/ |/ /_o      
    \_/ \__/       ''',
]
elf_template_width = elf_body_template.find("\n")
elf_template_height = len(elf_head_faceright_template.split("\n")) + len(elf_body_template.split("\n"))


class Elf:
    def __init__(self, maxheight = None):
        self.sum = 0
        self.foodslist = []
        self.drawing = None
        self.pad = None
        self.erasepad = None
        self.positionx = 0
        self.positiony = 0
        self.dropping = False
        self.maxheight = maxheight

    @classmethod
    def get_max_jump_step(cls):
        return len(elf_body_jump_templates) * 2 - 1

    def add_food(self, food):
        self.foodslist.append(food)
        self.sum += food

    FACELEFT = True
    FACERIGHT = False
    def create_drawing(self, faceleft = True, jump_step = None):
        def make_width(line, width):
            return line + " " * (width - len(line))
        # make the drawing
        width = max(len(self.foodslist), elf_template_width)
        margin = ""
        if(len(self.foodslist) < width):
            margin = " " * int((width - len(self.foodslist))/2)
        # draw the foods
        self.drawing = []
        foodheight = int(max(self.foodslist)/1000)
        if self.maxheight != None:
            foodheight = min(self.maxheight - elf_template_height - 1, foodheight)
        for lineno in range(foodheight, 1, -1):
            line = margin
            for food in self.foodslist:
                line += "_" if food > (lineno * 1000) else " "
            self.drawing.append(make_width(line, width))

        # draw the plate
        self.drawing.append("\\" + "_" * (width - 2) + "/")
        # draw the elf
        head_template = elf_head_faceleft_template if faceleft else elf_head_faceright_template
        self.drawing += head_template.split('\n')
        body_template = elf_body_template
        if jump_step != None:
            if jump_step >= len(elf_body_jump_templates):
                jump_step = min(0, len(elf_body_jump_templates) - jump_step)
            body_template = elf_body_jump_templates[jump_step]
        for line in body_template.format(self.sum).split('\n'):
          self.drawing.append(line.center(width))
        self.create_pads()

        return self.drawing
    # internal init routine
    def create_pads(self):
        if self.drawing == None:
            self.create_drawing()
        self.pad = curses.newpad(len(self.drawing), len(self.drawing[0]))
        self.erasepad = curses.newpad(len(self.drawing), len(self.drawing[0]))

        # fill in self.pad
        #  note that our coordinate system, with y == 0 at the top, is different
        # than the curses coordinate system, with y == 0 at the bottom.
        food_color_pair = curses.color_pair(0)
        elf_color_pair = curses.color_pair(2) if self.dropping else curses.color_pair(1)
        for y in range(0, len(self.drawing)):
            for x in range(0, len(self.drawing[y])):
                color_pair = food_color_pair if y < (self.height() - elf_template_height) else elf_color_pair
                self.pad.insch(y, x, ord(self.drawing[y][x]), color_pair)
    def height(self):
        if self.drawing == None:
            self.create_drawing()
        return len(self.drawing)
    def width(self):
        if self.drawing == None:
            self.create_drawing()
        return len(self.drawing[0])

# functionized ugly stream of code, so we can run it inside the curses wrapper
def main(screen):
    # init some curses stuff
    curses.curs_set(0)
    curses.init_pair(1, curses.COLOR_GREEN, curses.COLOR_BLACK)
    curses.init_pair(2, curses.COLOR_RED, curses.COLOR_BLACK)
    time.sleep(2)

    elves = []
    maxy, maxx = screen.getmaxyx()
    elf = Elf(maxheight = maxy)
    with open(inputfile) as fh:
        for line in fh.readlines():
            line = line.strip()
            if(line == ""):
                elves.append(elf)
                elf = Elf(maxheight = maxy)
            else:
                elf.add_food(int(line))
    # don't forget the last elf
    if elf.sum != 0:
        elves.append(elf)

    # discern elf width/height
    elf_height = 0;
    elf_width = 0;
    for elf in elves:
        drawing = elf.create_drawing(Elf.FACELEFT)
        elf_height = max(elf.height(), elf_height)
        elf_width = max(elf.width() + 2, elf_width)  # 2 is the margin between elves

    # debug code: print elves
    #for idx, elf in enumerate(elves):
    #    for line in elf.drawing:
    #        logger.debug(line)

    elf0 = None
    elf1 = None

    # calculate elf positions
    if maxx < (elf_width * 3 + 4):
        raise Exception("Please make your terminal window bigger! We need at" \
                f" least {elf_width * 3 + 4} columns for our display.")
    leftmargin = (maxy - (elf_width * 3 + 4)) // 2
    down_frame_magnitude = maxy//DOWN_FRAMES

    screen.clear()

    # walk elves in from the right until our screen is full of elves
    visible_elves = [elves.pop(0)]
    visible_elves[0].positionx = maxx - 1
    visible_elves[0].positiony = maxy - 1 - visible_elves[0].height()

    # this loop should process all elves.
    #  by default, each elf moves one step to the left. Any elf in the leftmost
    #    position (margin_left) doesn't move.
    #  if the first elf arrives at the leftmost position + DOWN_FRAMES
    #     (leftmargin + DOWN_FRAMES), make it face right
    #  if the second elf arrives at leftmargin + elf_width + DOWN_FRAMES, compare
    #     the left two elves (visible_elves[0] and visible_elves[1]). Mark the
    #     smaller of the two elves for moving down.
    #  if any elf is moving down, move it down down_frame_magnitude
    #  move any of the rest of the visible elves
    #  if the rightmost elf (visible_elves[-1]) is at maxx - elf_width, add an elf
    #     to the visible_elves array at position maxx. It should start to become
    #     visible on the next iteration.
    # note that elf positions indicate the top left of the elf, in curses style.
    while(len(visible_elves) > 1 or (len(visible_elves) == 1 and len(elves) > 0)):
        # move all elves one step to the left. Drop those which are dropping.
        # also, undisplay the elves in their last position
        for elf in visible_elves:
            elf.erasepad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                    maxy - 1, min(elf.positionx + elf_width, maxx - 1))
            elf.positionx = max(leftmargin, elf.positionx - 1)
            if elf.dropping:
                elf.positiony += down_frame_magnitude

        # if the first elf has arrived at the leftmost position + DOWN_FRAMES,
        # make it face right
        if visible_elves[0].positionx == (leftmargin + DOWN_FRAMES):
            visible_elves[0].create_drawing(Elf.FACERIGHT)

        #  if the second elf arrives at leftmargin + elf_width + DOWN_FRAMES, compare
        #     the left two elves (visible_elves[0] and visible_elves[1]). Mark the
        #     smaller of the two elves for moving down.
        if len(visible_elves) > 1 and visible_elves[1].positionx == (leftmargin + elf_width + DOWN_FRAMES):
            smallestelf = visible_elves[0]
            if visible_elves[1].sum < visible_elves[0].sum:
                smallestelf = visible_elves[1]
            smallestelf.dropping = True
            logger.info(f"dropping elf {elf.sum}")
            smallestelf.create_drawing()
            # looks a bit odd if it's in exactly the same position and just
            # changes color, so we drop it one row.
            smallestelf.positiony += 1

        # if there's room for another visible elf, add one to the array
        if len(elves) > 0 and visible_elves[-1].positionx < maxx - elf_width:
            nextelf = elves.pop(0)
            nextelf.positionx = visible_elves[-1].positionx + elf_width
            nextelf.positiony = maxy - 1 - nextelf.height()
            visible_elves.append(nextelf)

        # display the elves. Also, remove any that have dropped off the screen.
        toremove = []
        for elfidx, elf in enumerate(visible_elves):
            if(elf.positiony >= maxy):
                toremove.append(elfidx)
            else:
                elf.pad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                        maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        toremove.reverse()
        for idx in toremove:
            visible_elves.pop(idx)
        screen.refresh()
        time.sleep(FRAME_WAIT)

    # make the winning elf jump
    #  move elf to the center (x)
    time.sleep(3*FRAME_WAIT)
    elf = visible_elves[0]
    elf_centerx = maxx//2 - elf.width()//2
    while elf.positionx < elf_centerx:
        # redraw elf one step to the right, facing right
        elf.create_drawing(faceleft = Elf.FACERIGHT)
        elf.erasepad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        elf.positionx += 1
        elf.pad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        time.sleep(FRAME_WAIT)
        screen.refresh()
    #  jump up two steps
    for plusy in range(2):
        elf.create_drawing(faceleft = Elf.FACELEFT)
        # this isn't quite right, for tall elves we might have to cut a row off
        # the top. This will crash in short windows.
        elf.erasepad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        elf.positiony -= 1
        elf.pad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        screen.refresh()
        time.sleep(FRAME_WAIT)
    #  kick legs
    for jump_step in range(1, elf.get_max_jump_step()):
        elf.create_drawing(jump_step = jump_step)
        elf.pad.refresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        time.sleep(FRAME_WAIT*6)
    elf.create_drawing()
    elf.pad.refresh(0, 0, elf.positiony, elf.positionx,
            maxy - 1, min(elf.positionx + elf_width, maxx - 1))
    time.sleep(FRAME_WAIT*2)
    #  jump back down
    for plusy in range(2):
        elf.erasepad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        elf.positiony += 1
        elf.pad.noutrefresh(0, 0, elf.positiony, elf.positionx,
                maxy - 1, min(elf.positionx + elf_width, maxx - 1))
        time.sleep(FRAME_WAIT*2)
        screen.refresh()
    #  pause for a moment
    time.sleep(20)


# run the main function inside the curses wrapper
curses.wrapper(main)
