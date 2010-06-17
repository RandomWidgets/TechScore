(setq cwd "/home/dayan/ts/techscore-dev/")
;; (setq cwd (file-name-directory (buffer-file-name)))
(setq src (concat cwd "src"))
(jde-set-variables
 '(jde-make-working-directory cwd)
 '(jde-log-max 5000)
 '(jde-enable-abbrev-mode t)
 '(jde-make-program "ant")
 '(jde-run-application-class "edu.mit.techscore.tscore.TScoreGUI")

 '(jde-gen-buffer-boilerplate
   '("/**"
     " * This file is part of TechScore.<p>"
     " * "
     " * TechScore is free software: you can redistribute it and/or modify"
     " * it under the terms of the GNU General Public License as published by"
     " * the Free Software Foundation, either version 3 of the License, or"
     " * (at your option) any later version.<p>"
     " * "
     " * TechScore is distributed in the hope that it will be useful,"
     " * but WITHOUT ANY WARRANTY; without even the implied warranty of"
     " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
     " * GNU General Public License for more details.<p>"
     " * "
     " * You should have received a copy of the GNU General Public License"
     " * along with TechScore.  If not, see <http://www.gnu.org/licenses/>."
     " *"
     " */"
     ))

 '(jde-sourcepath '(src)))
(jde-set-global-classpath (concat cwd "bin"))
(jde-set-compile-options  (concat "-d " cwd "bin"))
