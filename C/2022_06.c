#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/mman.h>

#define AOC_YEAR 2022
#define AOC_DAY 6

#ifdef DEBUG
#define DBGPRINT(...) fprintf(stderr, __VA_ARGS__)
#else
#define DBGPRINT(...)
#endif

#ifdef PART1
#define NUNIQUE 4
#define MARKERNAME "Start-of-packet"
#endif
#ifdef PART2
#define NUNIQUE 14
#define MARKERNAME "Start-of-message"
#endif


int main(int argc, char ** argv)
{
  size_t headidx, tailidx, tmptailidx;
  char * messagemap;
  size_t filesize;
  int fd;

  if(argc != 2) {
    printf("Usage: %s [filename]\n", argv[0]);
    printf("  solves Advent of Code %d day %d\n", AOC_YEAR, AOC_DAY);
    return -1;
  }

  fd = open(argv[1], O_RDONLY, 0);
  if(fd < 0) {
    printf("ERROR opening file %s: %s\n", argv[1], strerror(errno));
    return -2;
  }
  filesize = (size_t) lseek(fd, 0, SEEK_END);
  if(filesize < 0) {
    close(fd);
    printf("ERROR seeking to end of file %s: %s\n", argv[1], strerror(errno));
    return -3;
  }
  lseek(fd, 0, SEEK_SET);
  messagemap = (char *) mmap(NULL, filesize, PROT_READ, MAP_SHARED, fd, 0);
  if(messagemap == MAP_FAILED) {
    close(fd);
    printf("ERROR mapping file %s: %s\n", argv[1], strerror(errno));
    return -4;
  }

  tailidx = 0;
  for(headidx = 1; headidx < filesize; headidx++) {
    if(headidx - tailidx >= NUNIQUE)
      break;

    for(tmptailidx = tailidx; tmptailidx < headidx; tmptailidx++) {
      if(messagemap[tmptailidx] == messagemap[headidx])
        tailidx = tmptailidx + 1;
    }
  }

  munmap((void *) messagemap, filesize);
  close(fd);
  printf(MARKERNAME " start: %ld\n", (long) (tailidx + NUNIQUE));

  return 0;
}

