#TechScore: Java Regatta Scoring Program

###This is the Random Widgets Fork of TechScore. The original version is available at: [Sourceforge](http://techscore.sourceforge.net), created by Dayan Paez.
---
#### What is TechScore?
TechScore is FREE software licensed under the GPL license. TechScore is a cross-platform, sailing regatta scoring application. TechScore is Java-based software. Currently, TechScore uses ICSA sailing rules, and can be extended to include other sailing rules. The program handles rotations, penalties, breakdowns, team penalties, RP information, combined division scoring with up to 26 different divisions, and more!


#### What's Different About This Fork?
As a scorer in the ISSA SAISA district, we wish to improve TechScore. As nothing much has happened to the TechScore SoureForge Repository, we have "forked" it and will be working on new features and bug fixes here 

#### Other Less Important Stuff:

##### License:
TechScore is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your
option) any later version.

TechScore is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with TechScore.  If not, see <http://www.gnu.org/licenses/>.

##### Structure of Source:
The program consists of four packages:

| Dir.    | Description                                  |
|---------|----------------------------------------------|
| dpxml   | XML Library                                  |
| regatta | Library of sailing regatta tools             |
| tscore  | The MAIN package with all the GUI tools      |
| nscore  | A set of compatibility tools for NavyScoring |

The main executable is TScoreGUI.java, found in the folder "tscore".

