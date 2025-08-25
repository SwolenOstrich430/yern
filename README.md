# Overview 
This is the repo for a knitting and crochet tool -- the name is in progress. 

## Local Setup 

### Install Java
brew install java 

### Install Postgres 
brew install postgres 


## Definitions 

* Panel: a section of a design. See also: piece(s), section, squares (granny square blankets).
* Piece(s): either a garment of clothing in its entirety or a section of a design. 
* Project: one or more pieces that a user is working on. See also: set. 
* Section: synonmy for panel.
* Set: A project with mulitple pieces.
* Working in "The Round": creating a continuous piece that isn't sewn together from separate panels. 

## Features 

### User Accounts 

#### Registration 

#### Login

### Stitch Counter 

#### Design 
I have this many stitches for a project. 

### Row Counter 
#### Purpose 
Keep track of which line in a piece or section of a piece that you're working on. 

#### Design 
##### Overview 
* You press '-' or '+' and that increases the current row that you're on.
* Counts which row that you're on in the current section of your design.
* Would be nice to have it for each panel OR the entire garment. Most apps offer this as a premium feature. 

***** Design 
* Is a table connected to garments/garment_panels.
* 
### Timer 

## DB Objects
### Projects 
A project is one or more patterns that a user is working on. A project has: 

* name: custom identifier a user 
* patterns: a list of garments/knittables a user is working on.
* type: the kind of project that you're going to be working on. E.g., crochet, knitting, machine, loom. For now, we'll just do crochet. 
* start date: when the user began working on the project. 
* finish date: when the user wants to finish the project by. 

### Pattern 
Something you buy from another individual or create on your own. It is an external file/set of files that users can import. Patterns provide directions for how to create the piece(s) in a project. 

Users can upload the file from personal or external sources. For now, we'll just focus on from your device and Google Drive. Dropbox after if possible. 

Files are validated when a user attempts to upload them. 

Users can edit the pdf/write on it, highlight sections, add notes, etc.. 

Patterns have: 
* name
* files
* global counter
* one or more sections

### Sections 
* name
* Types of panels can be introduced later. E.g., sleeve, torso, cuff, etc. 

